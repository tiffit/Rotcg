package net.tiffit.rotcg.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tiffit.rotcg.registry.entity.GameObjectEntity;

public class GameObjectEntityRenderer extends RotMGEntityRenderer<GameObjectEntity> {
    public GameObjectEntityRenderer(EntityRendererProvider.Context rendererManager) {
        super(rendererManager);
    }
}
