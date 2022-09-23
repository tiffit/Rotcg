package net.tiffit.rotcg.render.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.pack.RotCGPack;
import net.tiffit.rotcg.render.effect.RotMGEffect;
import net.tiffit.rotcg.render.effect.ThrownEffect;
import net.tiffit.rotcg.util.RotCGResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ProjectileThrownParticle extends SingleQuadParticle {
    private static final AABB INITIAL_AABB = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

    private final RotMGEffect effect;
    private final ProjectileThrownParticleRenderType renderType;
    private final Vec2f start, end;
    private final float rollIncrease;

    public ProjectileThrownParticle(ClientLevel pLevel, RotMGEffect effect, GameObject go, Vec2f start, Vec2f end) {
        super(pLevel, end.x(), 65, end.y());
        hasPhysics = false;
        this.effect = effect;
        this.start = start;
        this.end = end;
        this.lifetime = (int)Math.ceil(effect.duration * 20);
        this.renderType = new ProjectileThrownParticleRenderType(RotCGPack.textToRlFull(go.texture.get(0)));
        rollIncrease = (float) ((Math.PI * 2)/lifetime) * (float) Math.ceil(Math.sqrt(start.distanceSqr(end)) / 3);
    }

    @Override
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        Vec3 vec3 = pRenderInfo.getPosition();
        float f = (float)(Mth.lerp((double)pPartialTicks, this.xo, this.x) - vec3.x());
        float f1 = (float)(Mth.lerp((double)pPartialTicks, this.yo, this.y) - vec3.y());
        float f2 = (float)(Mth.lerp((double)pPartialTicks, this.zo, this.z) - vec3.z());
        Quaternion quaternion;
        if (this.roll == 0.0F) {
            quaternion = pRenderInfo.rotation();
        } else {
            quaternion = new Quaternion(pRenderInfo.rotation());
            float f3 = Mth.lerp(pPartialTicks, this.oRoll, this.roll);
            quaternion.mul(Vector3f.ZP.rotation(f3));
        }
        quaternion.mul(2);

        Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
        vector3f1.transform(quaternion);
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f4 = this.getQuadSize(pPartialTicks);

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.transform(quaternion);
            vector3f.mul(f4);
            vector3f.add(f, f1, f2);
        }

        float f7 = this.getU0();
        float f8 = this.getU1();
        float f5 = this.getV0();
        float f6 = this.getV1();
        int j = this.getLightColor(pPartialTicks);
        pBuffer.vertex((double)avector3f[0].x(), (double)avector3f[0].y(), (double)avector3f[0].z()).uv(f8, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        pBuffer.vertex((double)avector3f[1].x(), (double)avector3f[1].y(), (double)avector3f[1].z()).uv(f8, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        pBuffer.vertex((double)avector3f[2].x(), (double)avector3f[2].y(), (double)avector3f[2].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        pBuffer.vertex((double)avector3f[3].x(), (double)avector3f[3].y(), (double)avector3f[3].z()).uv(f7, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
    }

    @Override
    protected float getU0() {
        return 0;
    }

    @Override
    protected float getU1() {
        return 1;
    }

    @Override
    protected float getV0() {
        return 0;
    }

    @Override
    protected float getV1() {
        return 1;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return renderType;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        oRoll = roll;
        roll += rollIncrease;

        Vec2f xPos = ThrownEffect.interpolate(effect, start.x(), end.x());
        Vec2f zPos = ThrownEffect.interpolate(effect, start.y(), end.y());
        this.setBoundingBox(INITIAL_AABB.move(xPos.x(), 65.7f + xPos.y(), zPos.x()));
        this.setLocationFromBoundingbox();
    }

    private record ProjectileThrownParticleRenderType(RotCGResourceLocation rl) implements ParticleRenderType {

        @Override
            public void begin(BufferBuilder pBuilder, @NotNull TextureManager pTextureManager) {
                RenderSystem.disableBlend();
                RenderSystem.depthMask(true);
                RenderSystem.setShaderTexture(0, rl);
                pBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            }

            @Override
            public void end(@NotNull Tesselator pTesselator) {
                pTesselator.end();
            }
        }
}
