package net.tiffit.rotcg.render.effect;

import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.tiffit.realmnetapi.util.math.Vec2f;

import java.awt.*;

public class ThrownEffect extends RotMGEffect {

    private ParticleOptions data;

    public ThrownEffect(int targetObjectId, Vec2f start, Vec2f end, int color, double duration) {
        super(targetObjectId, start, end, color, duration);
        Color c = new Color(this.color);
        data = new DustParticleOptions(new Vector3f(c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f), 1);
    }

    @Override
    public void onCreate() {}

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        Vec2f xPos = getCurrentPos(start.x(), end.x());
        Vec2f zPos = getCurrentPos(start.y(), end.y());
        mc.level.addParticle(data, xPos.x(), 65 + xPos.y(), zPos.x(), 0, 0, 0);
    }

    private Vec2f getCurrentPos(float start, float end){
        float timePass = System.currentTimeMillis() - createTime;
        float diff = start - end;
        float x = (float) (end + diff * timePass/(duration*1000));
        float midway = end + diff/2;
        float height = -(midway - end) * (midway - start);

        float part1 = x - end;
        float part2 = x - start;

        float newHeight = 2.5f;

        return new Vec2f(x, (-1 * part1 * part2) * (newHeight/height));
    }

    @Override
    public void onDestroy() {}
}
