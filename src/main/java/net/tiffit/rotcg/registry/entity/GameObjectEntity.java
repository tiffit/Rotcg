package net.tiffit.rotcg.registry.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

public class GameObjectEntity extends RotcgEntity{

    public static RegistryObject<EntityType<GameObjectEntity>> TYPE;

    public GameObjectEntity(EntityType<? extends RotcgEntity> type, Level level) {
        super(type, level);
    }

}
