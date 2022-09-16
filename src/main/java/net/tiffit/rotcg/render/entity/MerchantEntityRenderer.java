package net.tiffit.rotcg.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.tiffit.rotcg.registry.entity.MerchantEntity;
import net.tiffit.rotcg.registry.entity.WallEntity;

public class MerchantEntityRenderer extends EntityRenderer<MerchantEntity> {

    private ItemRenderer itemRenderer;

    public MerchantEntityRenderer(EntityRendererProvider.Context rendererManager) {
        super(rendererManager);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(MerchantEntity entity, float yaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource buffer, int packedLight) {
        matrixStackIn.pushPose();
        ItemStack itemstack = entity.getMerchandise();
        BakedModel bakedModel = this.itemRenderer.getModel(itemstack, entity.level, null, 0);
        boolean flag = bakedModel.isGui3d();
        float f2 = bakedModel.getTransforms().getTransform(ItemTransforms.TransformType.GROUND).scale.y();
        matrixStackIn.translate(0.0D, 0.25F * f2, 0.0D);
        float bob = System.currentTimeMillis() % 2000;
        if(bob > 1000)bob = 2000 - bob;
        bob /= 2000f;
        Player player = Minecraft.getInstance().player;
        matrixStackIn.translate(0, bob, 0);
        matrixStackIn.mulPose(Vector3f.YP.rotation((float)Math.atan2(player.getX() - entity.getX(), player.getZ() - entity.getZ())));
        matrixStackIn.pushPose();
        this.itemRenderer.render(itemstack, ItemTransforms.TransformType.GROUND, false, matrixStackIn, buffer, packedLight, OverlayTexture.NO_OVERLAY, bakedModel);
        matrixStackIn.popPose();
        if (!flag) {
            matrixStackIn.translate(0.0, 0.0, 0.09375F);
        }
        matrixStackIn.popPose();
        super.render(entity, yaw, partialTicks, matrixStackIn, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MerchantEntity p_114482_) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public boolean shouldRender(MerchantEntity p_114491_, Frustum p_114492_, double p_114493_, double p_114494_, double p_114495_) {
        return true;
    }
}
