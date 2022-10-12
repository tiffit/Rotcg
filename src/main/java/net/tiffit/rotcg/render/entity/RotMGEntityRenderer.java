package net.tiffit.rotcg.render.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.tiffit.realmnetapi.assets.OBJModel;
import net.tiffit.realmnetapi.assets.spritesheet.AnimMap;
import net.tiffit.realmnetapi.assets.spritesheet.AnimSpriteDefinition;
import net.tiffit.realmnetapi.assets.spritesheet.SheetReference;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.assets.xml.Ground;
import net.tiffit.realmnetapi.assets.xml.Texture;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.StatType;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.realmnetapi.util.math.Vec3f;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.pack.RotCGPack;
import net.tiffit.rotcg.registry.GroundBlock;
import net.tiffit.rotcg.registry.entity.RotcgEntity;
import net.tiffit.rotcg.render.RenderUtils;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.List;

public abstract class RotMGEntityRenderer<T extends RotcgEntity> extends EntityRenderer<T> {

    protected boolean isWallOfFame = false;

    public RotMGEntityRenderer(EntityRendererProvider.Context rendererManager) {
        super(rendererManager);
    }

    @Override
    public void render(T entity, float yaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int packedLight) {
        if(entity.getReference() == null)return;
        GameObjectState state = entity.getReference().getState();
        GameObject go = state.getGameObject();
        if(go == null)return;
        stack.pushPose();
        double scale = 0;
        if(!go.model.isEmpty()){
            OBJModel model = OBJModel.getModel(go.model);
            if(model == null){
                //System.out.println("Unknown model " + go.model);
            }else{
                doRenderModel(entity, yaw, partialTicks, go, state, model, stack, buffer, packedLight);
            }
        }else {
            try {
                scale = doRenderNormal(entity, yaw, partialTicks, go, state, stack, buffer, packedLight);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        stack.popPose();
        double yOffset = scale + 0.2;
        if(go.model.isEmpty() && scale > 0 && (shouldShowName(entity) || entity.renderHealth())) {
            String name = shouldShowName(entity) ? entity.getDisplayName().getString() : "";
            renderLivingLabel(entity, name, scale, entity.renderHealth() ? state.getHP() / (double) state.getHPMax() : -1, stack, buffer, packedLight);
        }
    }

    protected void renderLivingLabel(T entityIn, String str, double scale, double hp, PoseStack stack, MultiBufferSource buffer, int packedLight) {
        RenderUtils.drawNameplate(this.getFont(), str, scale, hp, stack, buffer, packedLight, entityRenderDispatcher.cameraOrientation());
    }

    protected void doRenderModel(T entity, float entityYaw, float partialTicks, GameObject go, GameObjectState state, OBJModel model, PoseStack stack, MultiBufferSource buffer, int packedLight){
        setupModel(entity, go, state, model, stack);
        stack.pushPose();
        RenderType type = isWallOfFame ? RenderUtils.ENTITY_TRANSLUCENT_TRIANGLES.apply(new ResourceLocation(Rotcg.MODID, "textures/walloffame.png"), true) :
                RenderUtils.ENTITY_TRANSLUCENT_TRIANGLES.apply(RotCGPack.textToRlFull(entity.animationManager.getTexture()), true);
        VertexConsumer builder = buffer.getBuffer(type);
        PoseStack.Pose entry = stack.last();
        Matrix4f matrix4f = entry.pose();
        Matrix3f matrix3f = entry.normal();

        for (int i = 0; i < model.faces.size(); i++) {
            OBJModel.OBJVertexDefinition[] vertexDefinitions = model.faces.get(i);
            if(vertexDefinitions.length == 3){
                for (OBJModel.OBJVertexDefinition definition : vertexDefinitions) {
                    Vec3f pos = model.vertices.get(definition.vertex - 1);
                    Vec2f uv = model.vertexTextures.get(definition.vertex - 1);
                    if(isWallOfFame){
                        uv = new Vec2f(uv.x(), -uv.y());
                    }
                    //Vec3f normal = definition.hasNormal ? model.vertexNormals.get(definition.vertex - 1) : new Vec3f(0, 1f, 0);
                    Vec3f normal = new Vec3f(0, 1f, 0);
                    builder.vertex(matrix4f, pos.x(), pos.y(), pos.z());
                    builder.color(255, 255, 255, 255);
                    builder.uv(uv.x(), uv.y());
                    builder.overlayCoords(OverlayTexture.NO_OVERLAY);
                    builder.uv2(packedLight);
                    builder.normal(matrix3f, normal.x(), normal.y(), normal.z()).endVertex();
                }
            }
        }
        stack.popPose();
    }

    protected double doRenderNormal(T entity, float entityYaw, float partialTicks, GameObject go, GameObjectState state, PoseStack stack, MultiBufferSource buffer, int packedLight){
        stack.pushPose();
        if(go.texture.size() == 0){
            stack.popPose();
            return 0;
        }
        Texture texture = entity.animationManager.getTexture();
        if(texture.file.equals("invisible")){
            stack.popPose();
            return 0;
        }
        double scale = setupNormal(entity, go, state, texture, stack);
        if(scale <= 0){
            stack.popPose();
            return scale;
        }

        RenderType type = null;

        if(texture.animated){
            Vec2f playerPos = Rotcg.ACTIVE_CONNECTION.map.getPlayerPos().getPos();
            Vec2f diff = playerPos.sub(state.position);
            float angle = (float) Math.toDegrees(Math.atan2(diff.x(), diff.y()));
            angle -= entity.getYHeadRot();
            angle += 360*2;
            angle += 45;
            angle %= 360;
            int directionAmount = (int)(angle / 90);
            int direction = 0;
            boolean flip = false;
            switch (directionAmount){
                case 0 -> direction = AnimMap.DIRECTION_DOWN;
                case 1 -> {
                    direction = AnimMap.DIRECTION_SIDE;
                    flip = true;
                }
                case 2 -> direction = AnimMap.DIRECTION_UP;
                case 3 -> direction = AnimMap.DIRECTION_SIDE;
            }
            List<AnimSpriteDefinition> list = SheetReference.getAnimatedSprites(texture.toSpriteLocation()).getDefinition(AnimMap.ACTION_IDLE, direction);
            if(!list.isEmpty()){
                AnimSpriteDefinition asd = list.get(0);
                type = RenderType.entityTranslucent(RotCGPack.animRl(texture.toSpriteLocation(), asd.action, asd.direction));
            }
            if(flip){
                stack.scale(-1, 1, 1);
            }
        }
        if(type == null){
            type = RenderType.entityTranslucent(RotCGPack.textToRlFull(texture));
        }
        
        VertexConsumer builder = buffer.getBuffer(type);
        PoseStack.Pose entry = stack.last();
        Matrix4f matrix4f = entry.pose();
        Matrix3f matrix3f = entry.normal();
        vertex(builder, matrix4f, matrix3f, -0.5f, 0, 0, 1, packedLight);
        vertex(builder, matrix4f, matrix3f, 0.5f, 0, 1, 1, packedLight);
        vertex(builder, matrix4f, matrix3f, 0.5f, 1, 1, 0, packedLight);
        vertex(builder, matrix4f, matrix3f, -0.5f, 1, 0, 0, packedLight);


        vertex(builder, matrix4f, matrix3f, -0.5f, 0, 0, 1, packedLight);
        vertex(builder, matrix4f, matrix3f, -0.5f, 1, 0, 0, packedLight);
        vertex(builder, matrix4f, matrix3f, 0.5f, 1, 1, 0, packedLight);
        vertex(builder, matrix4f, matrix3f, 0.5f, 0, 1, 1, packedLight);
        stack.popPose();
        return scale;
    }

    private static void vertex(VertexConsumer bufferIn, Matrix4f matrixIn, Matrix3f matrixNormalIn, float x, float y, float texU, float texV, int packedLight) {
        bufferIn.vertex(matrixIn, x, y, 0).color(255, 255, 255, 255).uv(texU, texV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(matrixNormalIn, 0.0F, 1.0F, 0.0F).endVertex();
    }

    protected void setupModel(T entity, GameObject go, GameObjectState state, OBJModel model, PoseStack stack){
        stack.mulPose(Vector3f.XP.rotationDegrees(-90));
        stack.mulPose(Vector3f.ZP.rotationDegrees(-go.rotation));
        stack.mulPose(Vector3f.ZP.rotationDegrees(180));
    }

    protected double setupNormal(T entity, GameObject go, GameObjectState state, Texture texture, PoseStack stack){
        Block belowBlock = entity.level.getBlockState(entity.blockPosition().below()).getBlock();
        if(belowBlock instanceof GroundBlock groundBlock){
            Ground ground = groundBlock.ground;
            if(ground.sink && !go.drawOnGround)stack.translate(0, -4/16f, 0);
        }
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        if(!go.drawOnGround) {
            //Entity player = Minecraft.getInstance().player;
            //stack.mulPose(Vector3f.YP.rotation((float)Math.atan2(player.getX() - entity.getX(), player.getZ() - entity.getZ())));
            Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            stack.mulPose(Vector3f.YP.rotation((float)Math.atan2(cameraPos.x() - entity.getX(), cameraPos.z() - entity.getZ())));
        }else{
            if(entity.getX() == (int)entity.getX() && entity.getZ() == (int)entity.getZ()){
                stack.translate(0.5f, 0, 0.5f);
            }
        }
        int size = go.size;
        if(state.hasStat(StatType.SIZE))size = state.getStat(StatType.SIZE);
        float scaleVal = size / 100F;
        if(size > 0){
            BufferedImage img = texture.animated ? SheetReference.getAnimatedSprite(texture.toSpriteLocation()) : SheetReference.getSprite(texture.toSpriteLocation());
            if(img != null)scaleVal *= Math.max(img.getWidth(), img.getHeight()) / 8.0f;
            scaleVal = getScaleValue(scaleVal);
            if(go.drawOnGround){
                stack.translate(0, 0.0001, 0.5f * img.getWidth() / 8.0f);
                stack.mulPose(Vector3f.XP.rotationDegrees(-90));
            }
            stack.scale(scaleVal, scaleVal, scaleVal);
        }

        return scaleVal;
    }

    protected float getScaleValue(float current){
        return current;
    }

    @Nullable
    @Override
    public ResourceLocation getTextureLocation(T merchantEntity) {
        return null;
    }

    @Override
    public boolean shouldRender(T p_114491_, Frustum p_114492_, double p_114493_, double p_114494_, double p_114495_) {
        return true;
    }
}
