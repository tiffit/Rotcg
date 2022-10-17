package net.tiffit.rotcg.render.model;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.NamedRenderTypeManager;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.tiffit.realmnetapi.assets.xml.Ground;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class GroundBlockModel implements IUnbakedGeometry<GroundBlockModel> {

    private static final ResourceLocation TRANSLUCENT = new ResourceLocation("translucent");
    private static final ResourceLocation SOLID = new ResourceLocation("solid");

    private final Ground ground;

    public GroundBlockModel(Ground ground){
        this.ground = ground;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
        TextureAtlasSprite sprite = spriteGetter.apply(context.getMaterial("all"));
        SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(false, false, false, ItemTransforms.NO_TRANSFORMS, ItemOverrides.EMPTY);
        builder.particle(sprite);
        QuadBakingVertexConsumer quadBuilder = new QuadBakingVertexConsumer(bakedQuad -> builder.addCulledFace(bakedQuad.getDirection(), bakedQuad));
        quadBuilder.setSprite(sprite);
        quadBuilder.setDirection(Direction.UP);
        quadBuilder.setShade(true);
        quadBuilder.setTintIndex(-1);
        if(ground.animate == null){
            quadBuilder.vertex(0, 1, 0).uv(sprite.getU0(), sprite.getV0()).color(1f, 1f, 1f, 1f).normal(0, 1, 0).endVertex();
            quadBuilder.vertex(0, 1, 1).uv(sprite.getU0(), sprite.getV1()).color(1f, 1f, 1f, 1f).normal(0, 1, 0).endVertex();
            quadBuilder.vertex(1, 1, 1).uv(sprite.getU1(), sprite.getV1()).color(1f, 1f, 1f, 1f).normal(0, 1, 0).endVertex();
            quadBuilder.vertex(1, 1, 0).uv(sprite.getU1(), sprite.getV0()).color(1f, 1f, 1f, 1f).normal(0, 1, 0).endVertex();
        }
        return builder.build(NamedRenderTypeManager.get(ground.alpha ? TRANSLUCENT : SOLID));
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext context, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        Set<Material> materials = Sets.newHashSet();
        materials.add(context.getMaterial("all"));
        return materials;
    }
}
