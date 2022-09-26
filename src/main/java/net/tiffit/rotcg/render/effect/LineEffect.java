package net.tiffit.rotcg.render.effect;

import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.tiffit.realmnetapi.map.RMap;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.Rotcg;

import java.awt.*;

public class LineEffect extends RotMGEffect {

    private ParticleOptions data;
    public LineEffect(int targetObjectId, Vec2f start, Vec2f end, int color, double duration) {
        super(targetObjectId, start, end, color, duration);
        Color c = new Color(this.color);
        data = new DustParticleOptions(new Vector3f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f), 1);
    }

    @Override
    public void onCreate() {
        Minecraft mc = Minecraft.getInstance();
        Vec2f pos;
        RMap map = Rotcg.ACTIVE_CONNECTION.map;
        if (targetObjectId == map.getObjectId()) {
            pos = new Vec2f((float) mc.player.getX(), (float) mc.player.getZ());
        } else if (map.getEntityList().has(targetObjectId)) {
            RObject entity = map.getEntityList().get(targetObjectId);
            pos = new Vec2f(entity.getCorrectedX(), entity.getCorrectedY());
        } else {
            return;
        }
        for (int i = 0; i < 40; i++) {
            double offX = (Math.random() * 0.5) - 0.25;
            double offY = Math.random();
            double offZ = (Math.random() * 0.5) - 0.25;
            mc.level.addParticle(data, pos.x() + offX, 65 + offY, pos.y() + offZ, 0, 100000, 0);
        }
    }

    @Override
    public void onTick() {
    }

    @Override
    public void onDestroy() {
    }
}
