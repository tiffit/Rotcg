package net.tiffit.rotcg.util;

import net.minecraft.resources.ResourceLocation;
import net.tiffit.rotcg.Rotcg;

public class RotCGResourceLocation extends ResourceLocation {

    public RotCGResourceLocation(String resourceName) {
        super(Rotcg.MODID, resourceName);
    }
}
