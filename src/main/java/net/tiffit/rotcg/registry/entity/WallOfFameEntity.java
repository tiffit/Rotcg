package net.tiffit.rotcg.registry.entity;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

public class WallOfFameEntity extends RotcgEntity{

    public static RegistryObject<EntityType<WallOfFameEntity>> TYPE;

    public WallOfFameEntity(EntityType<? extends RotcgEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if(level.isClientSide()){
            ClientLevel world = (ClientLevel) level;
            for (int i = 0; i < 5; i++) {
                world.addParticle(random.nextBoolean() ? ParticleTypes.WAX_OFF : ParticleTypes.WAX_ON,
                        getX() + random.nextFloat()*4 -2,
                        getY() + random.nextFloat()*5.5f,
                        getZ() + random.nextFloat()*4 -2,
                        0, -2D - random.nextFloat(), 0);
            }
        }
    }
}
