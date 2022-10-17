package net.tiffit.rotcg.render.model;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiffit.realmnetapi.assets.xml.Ground;
import net.tiffit.realmnetapi.assets.xml.TileAnimate;
import net.tiffit.realmnetapi.net.RealmNetworker;
import net.tiffit.rotcg.pack.RotCGPack;
import net.tiffit.rotcg.registry.block.AnimateGroundBlock;

public class AnimateGroundBER implements BlockEntityRenderer<AnimateGroundBlock.AnimateGroundBlockEntity> {

    @Override
    public void render(AnimateGroundBlock.AnimateGroundBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        AnimateGroundBlock block = (AnimateGroundBlock)pBlockEntity.getBlockState().getBlock();
        Ground ground = block.ground;
        ResourceLocation texLoc = RotCGPack.textToRlFull(ground.textures.get(0));
        VertexConsumer consumer = pBufferSource.getBuffer(RenderType.entityTranslucent(texLoc));
        pPoseStack.pushPose();
        PoseStack.Pose pose = pPoseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f normal = pose.normal();
        float u0 = 0, u1 = 1, v0 = 0, v1 = 1;
        int time = RealmNetworker.getTimeReal();
        TileAnimate animate = ground.animate;
        float uOffset = 0, vOffset = 0;
        switch (animate.type) {
            case "Wave" -> {
                uOffset = (float)Math.sin(animate.dx * time / 1000);
                vOffset = (float)Math.sin(animate.dy * time / 1000);
            }
            case "Flow" -> {
                uOffset = animate.dx * time / 1000;
                vOffset = animate.dy * time / 1000;
            }
        }
        u0 += uOffset;
        u1 += uOffset;
        v0 += vOffset;
        v1 += vOffset;
        pPackedLight = LightTexture.FULL_SKY;
        RenderSystem.enableBlend();
        consumer.vertex(matrix4f,0, 1, 0).color(1f, 1, 1, 1f).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(normal, 0, 1, 0).endVertex();
        consumer.vertex(matrix4f,0, 1, 1).color(1f, 1, 1, 1f).uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(normal, 0, 1, 0).endVertex();
        consumer.vertex(matrix4f,1, 1, 1).color(1f, 1, 1, 1f).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(normal, 0, 1, 0).endVertex();
        consumer.vertex(matrix4f,1, 1, 0).color(1f, 1, 1, 1f).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(normal, 0, 1, 0).endVertex();
        pPoseStack.popPose();
    }

}
