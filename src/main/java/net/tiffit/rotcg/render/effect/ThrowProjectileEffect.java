package net.tiffit.rotcg.render.effect;

import net.minecraft.client.Minecraft;
import net.tiffit.realmnetapi.assets.xml.XMLLoader;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.render.particle.ProjectileThrownParticle;

public class ThrowProjectileEffect extends RotMGEffect {

    private ProjectileThrownParticle particle;

    public ThrowProjectileEffect(int targetObjectId, Vec2f start, Vec2f end, int color, double duration) {
        super(targetObjectId, start, end, color, duration);
    }

    @Override
    public void onCreate() {
        Minecraft mc = Minecraft.getInstance();
        particle = new ProjectileThrownParticle(mc.level, this, XMLLoader.OBJECTS.get(color), start, end);
        mc.particleEngine.add(particle);
    }

    @Override
    public void onTick() {
    }

    @Override
    public void onDestroy() {
        particle.remove();
    }
}
