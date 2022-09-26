package net.tiffit.rotcg.render.effect;

import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.net.RealmNetworker;
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
        RealmNetworker net = Rotcg.ACTIVE_CONNECTION;
        Vec2f end = null;
        if(targetObjectId == net.map.getObjectId()){
            end = new Vec2f((float) mc.player.getX(), (float) mc.player.getZ());
        }else if(net.map.getEntityList().has(targetObjectId)){
            RObject entity = net.map.getEntityList().get(targetObjectId);
            end = entity.getCurrentPos();
        }else{
            return;
        }
        Vec2f subVec = end.sub(start);
        double distance = Math.sqrt(subVec.distanceSqr(Vec2f.ZERO));
        int numberPer = 3;
        double amount = numberPer * distance;
        double dx = (end.x() - start.x())/amount;
        double dy = (end.y() - start.y())/amount;
        for(int i = 0; i < amount; i++){
            double x = start.x() + dx*i;
            double y = start.y() + dy*i;
            mc.level.addParticle(data, x, 66, y, 0, 0, 0);
        }
    }

    @Override
    public void onTick() {
    }

    @Override
    public void onDestroy() {
    }
}
