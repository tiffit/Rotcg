package net.tiffit.rotcg.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    /**
     * @author Tiffit
     * @reason Saving is useless
     */
    @Overwrite()
    public boolean saveAllChunks(boolean p_129886_, boolean p_129887_, boolean p_129888_) {
        return true;
    }

}
