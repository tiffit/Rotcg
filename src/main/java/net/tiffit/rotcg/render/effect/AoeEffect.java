package net.tiffit.rotcg.render.effect;

import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.tiffit.realmnetapi.util.math.Vec2f;

import java.awt.*;

public class AoeEffect extends RotMGEffect {

    private ParticleOptions data;

    public AoeEffect(int targetObjectId, Vec2f start, Vec2f end, int color, double duration) {
        super(targetObjectId, start, end, color, duration);
        Color c = new Color(this.color);
        data = new DustParticleOptions(new Vector3f(c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f), 1);

    }

    @Override
    public void onCreate() {
        Minecraft mc = Minecraft.getInstance();
        double radius = end.x();
        double totalAngle = Math.PI*2;
        for(double angle = 0; angle < totalAngle; angle += totalAngle/18){
            for(double i = 0; i <= radius; i += radius/10) {
                mc.level.addParticle(data, start.x() + Math.cos(angle) * i, 65, start.y() + Math.sin(angle) * i, 0, 5, 0);
            }
        }
    }

    @Override
    public void onTick() {}

    @Override
    public void onDestroy() {}
}
