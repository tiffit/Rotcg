package net.tiffit.rotcg.render.model;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.NamedRenderTypeManager;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.tiffit.realmnetapi.assets.xml.Ground;

import java.util.*;
import java.util.function.Function;

public class GroundBlockModel implements IUnbakedGeometry<GroundBlockModel> {

    public static final ResourceLocation TRANSLUCENT = new ResourceLocation("translucent");
    public static final ResourceLocation SOLID = new ResourceLocation("solid");
    private static final Matrix4f IDENTITY = new Matrix4f();

    static {
        IDENTITY.setIdentity();
    }

    private final Ground ground;

    public GroundBlockModel(Ground ground){
        this.ground = ground;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
        RenderTypeGroup renderType = NamedRenderTypeManager.get(ground.alpha ? TRANSLUCENT : SOLID);
        List<BakedModel> models = new ArrayList<>();

        for (int i = 0; i < ground.textures.size(); i++) {
            SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(false, false, false, ItemTransforms.NO_TRANSFORMS, ItemOverrides.EMPTY);
            TextureAtlasSprite sprite = spriteGetter.apply(context.getMaterial(i + ""));
            builder.particle(sprite);
            if(ground.animate == null){
                fillQuads(IDENTITY, builder, sprite);
            }
            models.add(builder.build(renderType));
        }
        Map<SameTypeEdgeModeMaterials, Map<Integer, BakedModel>> connectedModels = null;
        if(ground.animate == null && ground.sameTypeEdgeMode){
            connectedModels = new HashMap<>();
            for (SameTypeEdgeModeMaterials value : SameTypeEdgeModeMaterials.values()) {
                Map<Integer, BakedModel> rotMap = new HashMap<>();
                for(int rot = 0; rot < 4; rot++){
                    SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(false, false, false, ItemTransforms.NO_TRANSFORMS, ItemOverrides.EMPTY);
                    TextureAtlasSprite sprite = spriteGetter.apply(context.getMaterial(value.materialName));
                    builder.particle(sprite);
                    Matrix4f matrix4f = IDENTITY.copy();
                    matrix4f.multiply(Vector3f.YP.rotationDegrees(rot * 90));
                    switch (rot){
                        case 1 -> matrix4f.translate(new Vector3f(0, 0, 1));
                        case 2 -> matrix4f.translate(new Vector3f(1, 0, 1));
                        case 3 -> matrix4f.translate(new Vector3f(1, 0, 0));
                    }
                    fillQuads(matrix4f, builder, sprite);
                    rotMap.put(rot, builder.build(renderType));
                }
                connectedModels.put(value, rotMap);
            }
        }

        return new RBakedModel(models, connectedModels);
    }

    private static void fillQuads(Matrix4f matrix, SimpleBakedModel.Builder builder, TextureAtlasSprite sprite){
        QuadBakingVertexConsumer quadBuilder = new QuadBakingVertexConsumer(bakedQuad -> builder.addCulledFace(bakedQuad.getDirection(), bakedQuad));
        quadBuilder.setSprite(sprite);
        quadBuilder.setDirection(Direction.UP);
        quadBuilder.setShade(true);
        quadBuilder.setTintIndex(-1);

        quadBuilder.vertex(matrix, 0, 1, 0).uv(sprite.getU0(), sprite.getV0()).color(1f, 1f, 1f, 1f).normal(0, 1, 0).endVertex();
        quadBuilder.vertex(matrix, 0, 1, 1).uv(sprite.getU0(), sprite.getV1()).color(1f, 1f, 1f, 1f).normal(0, 1, 0).endVertex();
        quadBuilder.vertex(matrix, 1, 1, 1).uv(sprite.getU1(), sprite.getV1()).color(1f, 1f, 1f, 1f).normal(0, 1, 0).endVertex();
        quadBuilder.vertex(matrix, 1, 1, 0).uv(sprite.getU1(), sprite.getV0()).color(1f, 1f, 1f, 1f).normal(0, 1, 0).endVertex();
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext context, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        Set<Material> materials = Sets.newHashSet();
        for (int i = 0; i < ground.textures.size(); i++) {
            materials.add(context.getMaterial(i + ""));
        }
        if(ground.sameTypeEdgeMode){
            for (SameTypeEdgeModeMaterials value : SameTypeEdgeModeMaterials.values()) {
                materials.add(context.getMaterial(value.materialName));
            }
        }
        return materials;
    }

    public enum SameTypeEdgeModeMaterials {
        Edge("edge"),
        InnerCorner("innerCorner"),
        Corner("corner");

        final String materialName;

        SameTypeEdgeModeMaterials(String materialName){
            this.materialName = materialName;
        }
    }
}
