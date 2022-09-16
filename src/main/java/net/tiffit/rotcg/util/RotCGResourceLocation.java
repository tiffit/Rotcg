package net.tiffit.rotcg.util;

import net.minecraft.resources.ResourceLocation;
import net.tiffit.rotcg.Rotcg;

import java.io.Serializable;

public class RotCGResourceLocation extends ResourceLocation {

    public RotCGResourceLocation(String resourceName) {
        this(Rotcg.MODID, resourceName);
    }

    public RotCGResourceLocation(String namespaceIn, String pathIn) {
        super(namespaceIn, pathIn);
    }
}
