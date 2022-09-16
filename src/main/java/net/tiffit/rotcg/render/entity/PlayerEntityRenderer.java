package net.tiffit.rotcg.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tiffit.rotcg.registry.entity.PlayerEntity;

public class PlayerEntityRenderer extends RotMGEntityRenderer<PlayerEntity> {
    public PlayerEntityRenderer(EntityRendererProvider.Context rendererManager) {
        super(rendererManager);
    }
}
