package net.tiffit.rotcg.render.effect;

import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.map.object.RotMGEntityList;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.Rotcg;

import java.awt.*;

public class NovaEffect extends RotMGEffect {

    private ParticleOptions data;
    private Vec2f center;

    public NovaEffect(int targetObjectId, Vec2f start, Vec2f end, int color, double duration) {
        super(targetObjectId, start, end, color, duration);
        Color c = new Color(this.color);
        data = new DustParticleOptions(new Vector3f(c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f), 1);
        RotMGEntityList list = Rotcg.ACTIVE_CONNECTION.map.getEntityList();
        if(list.has(targetObjectId)){
            RObject entity = list.get(targetObjectId);
            center = entity.getCurrentPos();
        }
    }

    @Override
    public void onCreate() {
        Minecraft mc = Minecraft.getInstance();
        if(center != null) {
            double radius = start.x();
            double totalAngle = Math.PI*2;
            for(double angle = 0; angle < totalAngle; angle += totalAngle/8){
                for(double i = 0; i <= 1; i += 0.1) {
                    double newr = radius * i;
                    mc.level.addParticle(data, center.x() + Math.cos(angle) * newr, 65, center.y() + Math.sin(angle) * newr, 0, 5, 0);
                }
            }
        }
    }

    @Override
    public void onTick() {}

    @Override
    public void onDestroy() {}
}
