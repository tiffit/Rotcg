package net.tiffit.rotcg.event;

import net.tiffit.realmnetapi.api.event.AoeEvent;
import net.tiffit.realmnetapi.net.packet.in.AoePacketIn;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.render.effect.AoeEffect;
import net.tiffit.rotcg.util.TickExecutor;

public class AoeEventHandler {
    public static void handle(AoeEvent aoeEvent){
        AoePacketIn packet = aoeEvent.packet();
        AoeEffect effect = new AoeEffect(0, packet.pos, new Vec2f(packet.radius, 0), packet.color, packet.duration);
        TickExecutor.trackEffect(effect);
    }

}
