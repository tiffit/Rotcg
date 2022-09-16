package net.tiffit.rotcg.registry.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.map.object.StatType;
import net.tiffit.realmnetapi.util.LangLoader;

import javax.annotation.Nullable;

public class PortalEntity extends RotcgEntity{

    public static RegistryObject<EntityType<PortalEntity>> TYPE;

    public PortalEntity(EntityType<? extends RotcgEntity> type, Level level) {
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
        RObject obj = getReference();
        if(obj != null) {
            GameObjectState state = getReference().getState();
            String name;
            if(!state.getGameObject().goClass.equals("GuildHallPortal")){
                name = state.getStat(StatType.NAME);
            }else{
                name = state.getGameObject().dungeonName;
            }
            if (name == null) name = state.getGameObject().id;
            if(name.startsWith("NexusPortal."))name = name.substring(12);
            return Component.literal(ChatFormatting.WHITE + LangLoader.format(name));
        }else{
            return Component.empty();
        }
    }

}
