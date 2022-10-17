package net.tiffit.rotcg.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.client.DimensionSpecialEffectsManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;forType(Lnet/minecraft/world/level/dimension/DimensionType;)Lnet/minecraft/client/renderer/DimensionSpecialEffects;"))
    public DimensionSpecialEffects forType(DimensionType pDimensionType) {
        return DimensionSpecialEffectsManager.getForType(BuiltinDimensionTypes.END_EFFECTS);
    }

}
