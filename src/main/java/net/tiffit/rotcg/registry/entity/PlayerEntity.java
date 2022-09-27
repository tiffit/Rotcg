package net.tiffit.rotcg.registry.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.StatType;

import javax.annotation.Nullable;

public class PlayerEntity extends RotcgEntity {

    public static RegistryObject<EntityType<PlayerEntity>> TYPE;

    public PlayerEntity(EntityType<? extends RotcgEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public Component getDisplayName() {
        return getCustomName();
    }

    @Override
    public boolean isCustomNameVisible() {
        return true;
    }

    private Component cachedName;

    @Nullable
    @Override
    public Component getCustomName() {
        if(cachedName != null)return cachedName;
        GameObjectState state = getReference().getState();
        String name = state.<String>getStat(StatType.NAME).split(",")[0];
        boolean nameChosen = state.hasStat(StatType.NAME_CHOSEN) && state.<Integer>getStat(StatType.NAME_CHOSEN) > 0;
        boolean supporter = state.hasStat(StatType.SUPPORTER) && state.<Integer>getStat(StatType.SUPPORTER) > 0;
        if(nameChosen){
            if(supporter)name = ChatFormatting.LIGHT_PURPLE + name;
            else name = ChatFormatting.YELLOW + name;
        }
        cachedName = Component.literal(ChatFormatting.WHITE + name);
        return cachedName;
    }

    @Override
    public boolean renderHealth() {
        return true;
    }
}
