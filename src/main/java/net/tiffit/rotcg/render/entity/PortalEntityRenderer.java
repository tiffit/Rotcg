package net.tiffit.rotcg.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tiffit.rotcg.registry.entity.PortalEntity;

public class PortalEntityRenderer extends RotMGEntityRenderer<PortalEntity> {
    public PortalEntityRenderer(EntityRendererProvider.Context rendererManager) {
        super(rendererManager);
    }
}
