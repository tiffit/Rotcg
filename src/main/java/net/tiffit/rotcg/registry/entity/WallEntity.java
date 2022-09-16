package net.tiffit.rotcg.registry.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.rotcg.Rotcg;

public class WallEntity extends RotcgEntity {

    public static RegistryObject<EntityType<WallEntity>> TYPE;

    public WallEntity(EntityType<? extends RotcgEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void onAddedToWorld() {
        if(!getLevel().isClientSide()) {
            GameObjectState state = getReference().getState();
            BlockPos pos = new BlockPos(getX(), getY(), getZ());
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(Rotcg.MODID, "wall_" + state.type));
            assert block != null;
            level.setBlockAndUpdate(pos, block.defaultBlockState());
        }
        super.onAddedToWorld();
    }

    @Override
    public void onRemovedFromWorld() {
        BlockPos pos = new BlockPos(getX(), getY(), getZ());
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        super.onRemovedFromWorld();
    }

}
