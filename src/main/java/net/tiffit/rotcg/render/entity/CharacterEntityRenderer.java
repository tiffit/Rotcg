package net.tiffit.rotcg.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tiffit.rotcg.registry.entity.CharacterEntity;

public class CharacterEntityRenderer extends RotMGEntityRenderer<CharacterEntity> {
    public CharacterEntityRenderer(EntityRendererProvider.Context rendererManager) {
        super(rendererManager);
    }
}
