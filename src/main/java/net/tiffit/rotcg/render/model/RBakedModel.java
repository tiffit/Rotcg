package net.tiffit.rotcg.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.NamedRenderTypeManager;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.tiffit.realmnetapi.assets.xml.Ground;
import net.tiffit.realmnetapi.util.math.Rectf;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.registry.block.GroundBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static net.tiffit.rotcg.render.model.GroundBlockModel.TRANSLUCENT;

public class RBakedModel implements BakedModel {

    private static Rectf[] COMPOSITE_RECTS = new Rectf[]{
            new Rectf(0, 0, .5f, .5f),
            new Rectf(.5f, 0, .5f, .5f),
            new Rectf(0, .5f, .5f, .5f),
            new Rectf(.5f, .5f, .5f, .5f),
    };

    private static ModelProperty<BlockPos> BLOCK_POS = new ModelProperty<>();
    private static ModelProperty<BlockState[]> COMPOSITE = new ModelProperty<>();
    private static ModelProperty<GroundBlockModel.SameTypeEdgeModeMaterials> EDGE_TYPE = new ModelProperty<>();
    private static ModelProperty<Integer> EDGE_ROTATION = new ModelProperty<>();

    private final BakedModel internal;
    private final List<BakedModel> models;
    private final Map<GroundBlockModel.SameTypeEdgeModeMaterials, Map<Integer, BakedModel>> connectedModels;

    public RBakedModel(List<BakedModel> models, Map<GroundBlockModel.SameTypeEdgeModeMaterials, Map<Integer, BakedModel>> connectedModels){
        this.models = models;
        internal = models.get(0);
        this.connectedModels = connectedModels;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pDirection, @NotNull RandomSource pRandom) {
        return internal.getQuads(pState, pDirection, pRandom);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        return getCorrectModel(state, side, rand, data, renderType).getQuads(state, side, rand, data, renderType);
    }

    private BakedModel getCorrectModel(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType){
        if(data.has(COMPOSITE)){
            BlockState[] surrounding = data.get(COMPOSITE);
            ModelManager manager = Minecraft.getInstance().getModelManager();
            SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(false, false, false, ItemTransforms.NO_TRANSFORMS, ItemOverrides.EMPTY);
            builder.particle(internal.getParticleIcon(data));
            for (int i = 0; i < surrounding.length; i++) {
                if(surrounding[i] == null)continue;
                BakedModel model = manager.getModel(BlockModelShaper.stateToModelLocation(surrounding[i]));
                if(model instanceof RBakedModel rmodel){
                    TextureAtlasSprite sprite = rmodel.internal.getParticleIcon(data);
                    Rectf rect = COMPOSITE_RECTS[i];
                    createCompositeQuads(builder, sprite, rect.getStart(), rect.getEnd());
                }
            }
            return builder.build(NamedRenderTypeManager.get(TRANSLUCENT));
        }
        if(connectedModels != null){
            GroundBlockModel.SameTypeEdgeModeMaterials edgeType = data.get(EDGE_TYPE);
            if(edgeType != null){
                return connectedModels.get(edgeType).get(data.get(EDGE_ROTATION));
            }
        }
        if(models.size() > 1){
            return models.get(rand.forkPositional().at(data.get(BLOCK_POS)).nextInt(models.size()));
        }

        return internal;
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        ModelData.Builder builder = modelData.derive();
        builder.with(BLOCK_POS, pos);
        if(state.getBlock() instanceof GroundBlock groundBlock){
            Ground ground = groundBlock.ground;
            if(ground.type == 253){ //Is Composite
                BlockState[] arr = new BlockState[4];
                Direction[] directions = new Direction[]{Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
                for (int i = 0; i < 4; i++) {
                    arr[i] = getCompositeState(level, pos.relative(directions[i]));
                    if(arr[i] == null){
                        arr[i] = getCompositeState(level, pos.relative(directions[i + 1]));
                    }
                }
                builder.with(COMPOSITE, arr);
            }
        }
        connectedModels: {
            if(connectedModels != null){
                builder.with(EDGE_TYPE, null);
                builder.with(EDGE_ROTATION, 0);

                boolean west = differentBlock(level, pos.west(), state);
                boolean east = differentBlock(level, pos.east(), state);
                boolean south = differentBlock(level, pos.south(), state);
                boolean north = differentBlock(level, pos.north(), state);
                if(!north && !west && differentBlock(level, pos.west().north(), state)){
                    builder.with(EDGE_TYPE, GroundBlockModel.SameTypeEdgeModeMaterials.Corner);
                    builder.with(EDGE_ROTATION, 0);
                    break connectedModels;
                }
                if(!north && !east && differentBlock(level, pos.east().north(), state)){
                    builder.with(EDGE_TYPE, GroundBlockModel.SameTypeEdgeModeMaterials.Corner);
                    builder.with(EDGE_ROTATION, 3);
                    break connectedModels;
                }
                if(!south && !west && differentBlock(level, pos.west().south(), state)){
                    builder.with(EDGE_TYPE, GroundBlockModel.SameTypeEdgeModeMaterials.Corner);
                    builder.with(EDGE_ROTATION, 1);
                    break connectedModels;
                }
                if(!south && !east && differentBlock(level, pos.east().south(), state)){
                    builder.with(EDGE_TYPE, GroundBlockModel.SameTypeEdgeModeMaterials.Corner);
                    builder.with(EDGE_ROTATION, 2);
                    break connectedModels;
                }
                if(west){
                    if(north){
                        builder.with(EDGE_TYPE, GroundBlockModel.SameTypeEdgeModeMaterials.InnerCorner);
                        builder.with(EDGE_ROTATION, 0);
                        break connectedModels;
                    }
                    if(south){
                        builder.with(EDGE_TYPE, GroundBlockModel.SameTypeEdgeModeMaterials.InnerCorner);
                        builder.with(EDGE_ROTATION, 1);
                        break connectedModels;
                    }
                    builder.with(EDGE_TYPE, GroundBlockModel.SameTypeEdgeModeMaterials.Edge);
                    builder.with(EDGE_ROTATION, 0);
                    break connectedModels;
                }
                if(east){
                    if(north){
                        builder.with(EDGE_TYPE, GroundBlockModel.SameTypeEdgeModeMaterials.InnerCorner);
                        builder.with(EDGE_ROTATION, 3);
                        break connectedModels;
                    }
                    if(south){
                        builder.with(EDGE_TYPE, GroundBlockModel.SameTypeEdgeModeMaterials.InnerCorner);
                        builder.with(EDGE_ROTATION, 2);
                        break connectedModels;
                    }
                    builder.with(EDGE_TYPE, GroundBlockModel.SameTypeEdgeModeMaterials.Edge);
                    builder.with(EDGE_ROTATION, 2);
                    break connectedModels;
                }
                if(south){
                    builder.with(EDGE_TYPE, GroundBlockModel.SameTypeEdgeModeMaterials.Edge);
                    builder.with(EDGE_ROTATION, 1);
                    break connectedModels;
                }
                if(north){
                    builder.with(EDGE_TYPE, GroundBlockModel.SameTypeEdgeModeMaterials.Edge);
                    builder.with(EDGE_ROTATION, 3);
                    break connectedModels;
                }
            }
        }
        return builder.build();
    }

    private boolean differentBlock(BlockAndTintGetter level, BlockPos pos, BlockState thisState){
        return !level.getBlockState(pos).getBlock().equals(thisState.getBlock());
    }

    private BlockState getCompositeState(BlockAndTintGetter level, BlockPos pos){
        BlockState otherState = level.getBlockState(pos);
        if(otherState.getBlock() instanceof GroundBlock otherBlock){
            if(otherBlock.ground.type != 253){
                return otherState;
            }
        }
        return null;
    }

    private static void createCompositeQuads(SimpleBakedModel.Builder builder, TextureAtlasSprite sprite, Vec2f start, Vec2f end){
        QuadBakingVertexConsumer quadBuilder = new QuadBakingVertexConsumer(bakedQuad -> builder.addCulledFace(bakedQuad.getDirection(), bakedQuad));
        quadBuilder.setSprite(sprite);
        quadBuilder.setDirection(Direction.UP);
        quadBuilder.setShade(true);
        quadBuilder.setTintIndex(-1);

        float u0 = sprite.getU(start.x() * 16);
        float u1 = sprite.getU(end.x() * 16);
        float v0 = sprite.getV(start.y() * 16);
        float v1 = sprite.getV(end.y() * 16);

        quadBuilder.vertex(start.x(), 1, start.y()).uv(u0, v0).color(1f, 1f, 1f, 1f).normal(0, 1, 0).endVertex();
        quadBuilder.vertex(start.x(), 1, end.y()).uv(u0, v1).color(1f, 1f, 1f, 1f).normal(0, 1, 0).endVertex();
        quadBuilder.vertex(end.x(), 1, end.y()).uv(u1, v1).color(1f, 1f, 1f, 1f).normal(0, 1, 0).endVertex();
        quadBuilder.vertex(end.x(), 1, start.y()).uv(u1, v0).color(1f, 1f, 1f, 1f).normal(0, 1, 0).endVertex();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return internal.useAmbientOcclusion();
    }

    @Override
    public boolean useAmbientOcclusion(@NotNull BlockState state) {
        return internal.useAmbientOcclusion(state);
    }

    @Override
    public boolean useAmbientOcclusion(@NotNull BlockState state, @NotNull RenderType renderType) {
        return internal.useAmbientOcclusion(state, renderType);
    }

    @Override
    public boolean isGui3d() {
        return internal.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return internal.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return internal.isCustomRenderer();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return internal.getParticleIcon();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return internal.getParticleIcon(data);
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return internal.getOverrides();
    }

    @Override
    public @NotNull BakedModel applyTransform(ItemTransforms.@NotNull TransformType transformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform) {
        return internal.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }

    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return internal.getRenderTypes(state, rand, data);
    }

    @Override
    public @NotNull List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
        return internal.getRenderPasses(itemStack, fabulous);
    }

    @Override
    public @NotNull List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        return internal.getRenderTypes(itemStack, fabulous);
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return internal.getTransforms();
    }
}
