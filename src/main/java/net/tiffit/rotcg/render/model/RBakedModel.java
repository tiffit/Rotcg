package net.tiffit.rotcg.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class RBakedModel implements BakedModel {

    private static ModelProperty<BlockPos> BLOCK_POS = new ModelProperty<>();
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
