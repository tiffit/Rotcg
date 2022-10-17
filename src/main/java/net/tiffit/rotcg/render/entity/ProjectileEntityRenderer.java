package net.tiffit.rotcg.render.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiffit.realmnetapi.map.projectile.ProjectileState;
import net.tiffit.realmnetapi.net.RealmNetworker;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.pack.RotCGPack;
import net.tiffit.rotcg.registry.entity.ProjectileEntity;

public class ProjectileEntityRenderer extends EntityRenderer<ProjectileEntity> {

    public ProjectileEntityRenderer(EntityRendererProvider.Context rendererManager) {
        super(rendererManager);
    }

    @Override
    public void render(ProjectileEntity entity, float yaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int packedLight) {
        if(entity.getReference() == null)return;
        ProjectileState state = entity.getReference().getProjectileState();
        if (state == null || state.obj == null) return;
        float angleCorrection = state.obj.angleCorrection;
        Vec2f vec = entity.getReference().getPositionAt(state);
        vec = vec.sub((float) entity.getX(), (float) entity.getZ());
        stack.pushPose();
        stack.translate(vec.x(), 0, vec.y());
        float angle = (float)-Math.toDegrees(state.angle) - 45 * angleCorrection + 180;
        if(state.obj.rotation != 0){
            int msPassed = RealmNetworker.getTimeReal() - entity.getReference().startTime;
            angle += state.obj.rotation / 250f * msPassed;
        }
        stack.mulPose(Vector3f.YP.rotationDegrees(angle));
        stack.scale(0.7f, 0.7f, 0.7f);
        RenderType type = RenderType.entityTranslucent(RotCGPack.textToRlFull(state.obj.texture.get(0)));
        stack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        VertexConsumer ivertexbuilder = buffer.getBuffer(type);
        PoseStack.Pose matrixstack$entry = stack.last();
        Matrix4f matrix4f = matrixstack$entry.pose();
        Matrix3f matrix3f = matrixstack$entry.normal();
        float height = 0.5f;
        vertex(ivertexbuilder, matrix4f, matrix3f, -0.5f, height,-0.5f, 255, 255, 255, 255,0, 0, packedLight);
        vertex(ivertexbuilder, matrix4f, matrix3f, -0.5F, height,0.5f, 255, 255, 255, 255,0, 1, packedLight);
        vertex(ivertexbuilder, matrix4f, matrix3f, 0.5F, height,0.5F, 255, 255, 255, 255,1, 1, packedLight);
        vertex(ivertexbuilder, matrix4f, matrix3f, 0.5F, height,-0.5F, 255, 255, 255, 255,1, 0, packedLight);

//        vertex(ivertexbuilder, matrix4f, matrix3f, -0.5f, 0.1f,-0.5f, 0, 0, 0, 50,0, 0, packedLight);
//        vertex(ivertexbuilder, matrix4f, matrix3f, -0.5F, 0.1f,0.5f, 0, 0, 0, 50,0, 1, packedLight);
//        vertex(ivertexbuilder, matrix4f, matrix3f, 0.5F, 0.1f,0.5F, 0, 0, 0, 50,1, 1, packedLight);
//        vertex(ivertexbuilder, matrix4f, matrix3f, 0.5F, 0.1f,-0.5F, 0, 0, 0, 50,1, 0, packedLight);
        stack.popPose();
        RenderSystem.disableBlend();
        super.render( entity,  yaw,  partialTicks,  stack,  buffer,  packedLight);
    }

    private static void vertex(VertexConsumer bufferIn, Matrix4f matrixIn, Matrix3f matrixNormalIn, float x, float y, float z, int red, int green, int blue, int alpha, float texU, float texV, int packedLight) {
        bufferIn.vertex(matrixIn, x, y, z).color(red, green, blue, alpha).uv(texU, texV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(matrixNormalIn, 0.0F, 1.0F, 0.0F).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(ProjectileEntity p_114482_) {
        return null;
    }

    @Override
    public boolean shouldRender(ProjectileEntity p_114491_, Frustum p_114492_, double p_114493_, double p_114494_, double p_114495_) {
        return true;
    }
}
