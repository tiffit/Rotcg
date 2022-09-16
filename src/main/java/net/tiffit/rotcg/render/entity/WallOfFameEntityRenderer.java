package net.tiffit.rotcg.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tiffit.realmnetapi.assets.OBJModel;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.rotcg.registry.entity.WallOfFameEntity;

public class WallOfFameEntityRenderer extends RotMGEntityRenderer<WallOfFameEntity> {
    public WallOfFameEntityRenderer(EntityRendererProvider.Context rendererManager) {
        super(rendererManager);
        isWallOfFame = true;
    }

    @Override
    protected void setupModel(WallOfFameEntity entity, GameObject go, GameObjectState state, OBJModel model, PoseStack stack) {
        stack.mulPose(Vector3f.YP.rotationDegrees(90));
        stack.scale(2, 2, 2);
    }
}
