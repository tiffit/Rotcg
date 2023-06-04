package net.tiffit.rotcg.rna.event;

import net.tiffit.realmnetapi.api.event.ShowEffectEvent;
import net.tiffit.realmnetapi.net.NetworkLogger;
import net.tiffit.realmnetapi.net.packet.in.ShowEffectPacketIn;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.render.effect.RotMGEffect;
import net.tiffit.rotcg.util.TickExecutor;

public class ShowEffectEventHandler {

    public static void handle(ShowEffectEvent showEffectEvent){
        ShowEffectPacketIn packet = showEffectEvent.packet();
        RotMGEffect.VisualEffect effect = RotMGEffect.VisualEffect.byId(packet.effectType);
        if (effect.effectClass != null) {
            try {
                RotMGEffect r_effect = effect.effectClass.getConstructor(int.class, Vec2f.class, Vec2f.class, int.class, double.class)
                        .newInstance(packet.targetObjectId, packet.start, packet.end, packet.color, packet.duration);
                TickExecutor.trackEffect(r_effect);
            } catch (ReflectiveOperationException ex) {
                ex.printStackTrace();
            }
        }else{
            NetworkLogger logger = Rotcg.ACTIVE_CONNECTION.logger;
            logger.writeFormat("Unknown effect %d (%s) (start: %s, end: %s, target: %d, duration: %f, color: %d, byte: %d)",
                    packet.effectType, effect,
                    packet.start, packet.end, packet.targetObjectId, packet.duration, packet.color, packet.unknownByte);
        }
    }

}
