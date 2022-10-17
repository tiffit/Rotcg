package net.tiffit.rotcg.registry.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tiffit.realmnetapi.assets.xml.Ground;
import net.tiffit.rotcg.util.MoveSpeedUtil;

public class GroundBlock extends Block{

    public static IntegerProperty TEXTUREUSED = IntegerProperty.create("textureused", 0, 16);

    protected static final VoxelShape SINK_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 9.0D, 16.0D);
    protected static final VoxelShape WALL_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D*3, 16.0D);
    protected static final VoxelShape DEFAULT_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.95D, 16.0D);

    public final Ground ground;

    public GroundBlock(Ground ground) {
        super(BlockBehaviour.Properties.of(Material.STONE));
        this.ground = ground;
        registerDefaultState(defaultBlockState().setValue(TEXTUREUSED, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TEXTUREUSED);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return ground.nowalk ? WALL_SHAPE : ground.sink ? SINK_SHAPE : DEFAULT_SHAPE;
    }

    @Override
    public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
        if(!(pEntity instanceof Player))return;
        Player player = (Player) pEntity;
        if(ground.push && ground.animate != null){
            player.push(-MoveSpeedUtil.bpsToMoveSpeed(ground.animate.dx) * 1.75f, 0, -MoveSpeedUtil.bpsToMoveSpeed(ground.animate.dy) * 1.75f);
        }
    }

    @Override
    public MutableComponent getName() {
        return Component.literal(ground.id);
    }

    @Override
    public String getDescriptionId() {
        return ground.id;
    }
}
