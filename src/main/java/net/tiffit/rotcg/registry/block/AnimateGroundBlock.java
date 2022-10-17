package net.tiffit.rotcg.registry.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.tiffit.realmnetapi.assets.xml.Ground;
import net.tiffit.rotcg.registry.ModRegistry;
import org.jetbrains.annotations.Nullable;

public class AnimateGroundBlock extends GroundBlock implements EntityBlock{

    public AnimateGroundBlock(Ground ground) {
        super(ground);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AnimateGroundBlockEntity(pPos, pState);
    }

    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return 1.0F;
    }

    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return true;
    }

    public static class AnimateGroundBlockEntity extends BlockEntity {

        public AnimateGroundBlockEntity(BlockPos pPos, BlockState pBlockState) {
            super(ModRegistry.animateGroundBlockEntity, pPos, pBlockState);
        }

        @Override
        public AABB getRenderBoundingBox() {
            return super.getRenderBoundingBox().inflate(1f);
        }
    }
}
