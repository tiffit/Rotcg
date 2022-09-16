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

    @Nullable
    @Override
    public Component getCustomName() {
        GameObjectState state = getReference().getState();
        String name = state.getStat(StatType.NAME);
        boolean nameChosen = state.<Integer>getStat(StatType.NAME_CHOSEN) != null && state.<Integer>getStat(StatType.NAME_CHOSEN) > 0;
        boolean supporter = state.<Integer>getStat(StatType.SUPPORTER) != null && state.<Integer>getStat(StatType.SUPPORTER) > 0;
        if(nameChosen){
            if(supporter)name = ChatFormatting.LIGHT_PURPLE + name;
            else name = ChatFormatting.YELLOW + name;
        }
        return Component.literal(ChatFormatting.WHITE + name);
    }

    @Override
    public boolean renderHealth() {
        return true;
    }
}
