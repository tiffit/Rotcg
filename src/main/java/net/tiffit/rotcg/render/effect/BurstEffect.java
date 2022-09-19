package net.tiffit.rotcg.render.effect;

import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.tiffit.realmnetapi.util.math.Vec2f;

import java.awt.*;

public class BurstEffect extends RotMGEffect {

    private ParticleOptions data;

    public BurstEffect(int targetObjectId, Vec2f start, Vec2f end, int color, double duration) {
        super(targetObjectId, start, end, color, duration);
        Color c = new Color(this.color);
        data = new DustParticleOptions(new Vector3f(c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f), 1);
    }

    @Override
    public void onCreate() {
        Minecraft mc = Minecraft.getInstance();
        double x = start.x();
        double y = 65.1;
        double z = start.y();

        Vec2f vec = end.sub(start).absolute();

        double radius = Math.sqrt(vec.distanceSqr(Vec2f.ZERO));

        double totalAngle = Math.PI*2;

        for(double angle = 0; angle < totalAngle; angle += totalAngle/180){
            mc.level.addParticle(data, x + Math.cos(angle) * radius, y + 0.2, z + Math.sin(angle) * radius, 0, 5, 0);
        }

        int points = 10;

        for(double ox = -radius; ox < radius; ox += radius/points){
            for(double oy = -radius; oy < radius; oy += radius/points){
                double dist = Math.sqrt(Math.pow(ox, 2) + Math.pow(oy, 2));
                if(dist < radius){
                    double offX = (Math.random() * 0.1) - 0.05;
                    double offY = (Math.random() * 0.1) - 0.05;
                    mc.level.addParticle(data, x + ox + offX, y, z + oy + offY, 0, 0, 0);
                }
            }
        }

    }

    @Override
    public void onTick() {}

    @Override
    public void onDestroy() {}
}
