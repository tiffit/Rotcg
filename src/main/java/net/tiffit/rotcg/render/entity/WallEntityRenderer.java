package net.tiffit.rotcg.render.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tiffit.rotcg.registry.entity.PlayerEntity;
import net.tiffit.rotcg.registry.entity.WallEntity;

public class WallEntityRenderer extends EntityRenderer<WallEntity> {
    public WallEntityRenderer(EntityRendererProvider.Context rendererManager) {
        super(rendererManager);
    }

    @Override
    public ResourceLocation getTextureLocation(WallEntity p_114482_) {
        return null;
    }
}
