package net.tiffit.rotcg.mixin;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {

    /**
     * @author Tiffit
     * @reason Saving is useless
     */
    @Redirect(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;save(Z)V"))
    public void saveRedirect(ServerChunkCache instance, boolean pFlush) {}

    /**
     * @author Tiffit
     * @reason Saving is useless
     */
    @Redirect(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;close()V"))
    public void chunkMapCloseRedirect(ChunkMap instance) {}
}
