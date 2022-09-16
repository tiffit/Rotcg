package net.tiffit.rotcg.render;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;
import java.util.function.BiFunction;

public class RenderUtils {

    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_TRANSLUCENT_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentShader);
    protected static final RenderStateShard.CullStateShard NO_CULL = new RenderStateShard.CullStateShard(false);
    protected static final RenderStateShard.LightmapStateShard LIGHTMAP = new RenderStateShard.LightmapStateShard(true);
    protected static final RenderStateShard.OverlayStateShard OVERLAY = new RenderStateShard.OverlayStateShard(true);
    protected static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });


    public static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_TRANSLUCENT_TRIANGLES = Util.memoize((p_173227_, p_173228_) -> {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(p_173227_, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(p_173228_);
        return RenderType.create("entity_translucent_tri", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256, true, true, rendertype$compositestate);
    });

    public static RenderType.CompositeState translucentState = RenderType.CompositeState.builder()
            .setLightmapState(new RenderStateShard.LightmapStateShard(true))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(true);

    private static RenderType NameplateType = RenderType.create("rotcgnameplate", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS, 262144, true, true, translucentState);

    public static void drawNameplate(Font fontRendererIn, String str, double scale, double hp, PoseStack stack, MultiBufferSource buffer, int packedLight, Quaternion cameraOrientation) {
        float verticalShift = 0;
        boolean drawHp = hp != -1 && hp != -2;
        stack.pushPose();
        stack.translate(0.0D, scale + 0.2f, 0.0D);
        stack.mulPose(cameraOrientation);
        stack.scale(-0.025F, -0.025F, 0.025F);
        PoseStack.Pose matrixstack$entry = stack.last();
        //if(!drawHp) {
            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
       //}
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        float backgroundSize = drawHp ? (float)Math.max(scale*16 / 0.75, fontRendererIn.width(str) / 2f): fontRendererIn.width(str) / 2f;
        RenderSystem.disableTexture();
        float allScale = 0.75f;
        stack.scale(allScale, allScale, allScale);
        //VertexConsumer builder = buffer.getBuffer(RenderType.textIntensity());
        Matrix4f matrix4f = matrixstack$entry.pose();
        Matrix3f matrix3f = matrixstack$entry.normal();
//        int alpha = (int)((drawHp ? 0.75f : 0.25f) * 255);
//        if(drawHp){
//            float startX = -backgroundSize - 1;
//            float endX = backgroundSize + 1;
//            float barWidth = endX * ((float)hp*2-1);
//            int greenVal = (int)(255 * 0.5f);
//            vertex(builder, matrix4f, matrix3f, startX, (-1 + verticalShift), greenVal, alpha);
//            vertex(builder, matrix4f, matrix3f, startX, (8 + verticalShift), greenVal, alpha);
//            vertex(builder, matrix4f, matrix3f, barWidth, (8 + verticalShift), greenVal, alpha);
//            vertex(builder, matrix4f, matrix3f, barWidth, (-1 + verticalShift), greenVal, alpha);
//
//            vertex(builder, matrix4f, matrix3f, barWidth, (-1 + verticalShift), 0, alpha);
//            vertex(builder, matrix4f, matrix3f, barWidth, (8 + verticalShift), 0, alpha);
//            vertex(builder, matrix4f, matrix3f, endX, (8 + verticalShift), 0, alpha);
//            vertex(builder, matrix4f, matrix3f, endX, (-1 + verticalShift), 0, alpha);
//        }else if(hp != -2){
//            vertex(builder, matrix4f, matrix3f, (-backgroundSize - 1), (-1 + verticalShift), 0, alpha);
//            vertex(builder, matrix4f, matrix3f, (-backgroundSize - 1), (8 + verticalShift), 0, alpha);
//            vertex(builder, matrix4f, matrix3f, (backgroundSize + 1), (8 + verticalShift), 0, alpha);
//            vertex(builder, matrix4f, matrix3f, (backgroundSize + 1), (-1 + verticalShift), 0, alpha);
//        }
        float textScale = hp == -2 ? 1 : 0.75f;
//
        stack.pushPose();
        stack.scale(textScale, textScale, textScale);
        RenderSystem.enableBlend();
        MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        RenderSystem.depthMask(true);
        fontRendererIn.drawInBatch(str, (float)(-fontRendererIn.width(str) / 2), verticalShift, 0, false, matrix4f, multibuffersource$buffersource, false, 0, packedLight);
        multibuffersource$buffersource.endBatch();
        multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        RenderSystem.depthMask(false);
        RenderSystem.enableDepthTest();
        fontRendererIn.drawInBatch(str, (float)(-fontRendererIn.width(str) / 2), verticalShift, 0, false, matrix4f, multibuffersource$buffersource, false, 0xa0000000, packedLight);
        multibuffersource$buffersource.endBatch();
        RenderSystem.depthMask(true);
        stack.popPose();
        stack.popPose();
    }

    private static void vertex(VertexConsumer bufferIn, Matrix4f matrixIn, Matrix3f matrixNormalIn, float x, float y, int green, int alpha) {
        bufferIn.vertex(matrixIn, x, y, 0.5f).color(0, green, 0, alpha).uv2(15728640).endVertex();
    }

    public static void drawStringOutline(Font font, PoseStack stack, String string, float x, float y, int color, int outlineColor){
        font.draw(stack, string, x + 1, y, outlineColor);
        font.draw(stack, string, x - 1, y, outlineColor);
        font.draw(stack, string, x, y + 1, outlineColor);
        font.draw(stack, string, x, y - 1, outlineColor);
        font.draw(stack, string, x, y, color);
    }

    public static void drawStringRight(Font font, PoseStack ps, String string, float x, float y, int color){
        font.drawShadow(ps, string, x - font.width(string), y, color);
    }

}
