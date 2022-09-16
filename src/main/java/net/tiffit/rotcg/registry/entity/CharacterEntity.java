package net.tiffit.rotcg.registry.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

public class CharacterEntity extends RotcgEntity{

    public static RegistryObject<EntityType<CharacterEntity>> TYPE;

    public CharacterEntity(EntityType<? extends RotcgEntity> type, Level level) {
        super(type, level);
    }

}
