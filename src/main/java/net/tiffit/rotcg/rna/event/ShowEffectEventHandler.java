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
                RotMGEffect reffect = effect.effectClass.getConstructor(int.class, Vec2f.class, Vec2f.class, int.class, double.class)
                        .newInstance(packet.targetObjectId, packet.start, packet.end, packet.color, packet.duration);
                TickExecutor.trackEffect(reffect);
            } catch (ReflectiveOperationException ex) {
                ex.printStackTrace();
            }
        }else{
            NetworkLogger logger = Rotcg.ACTIVE_CONNECTION.logger;
            logger.writeFormat("Unknown effect %d (start: %s, end: %s, target: %d, duration: %f)", packet.effectType, packet.start.toString(), packet.end.toString(), packet.targetObjectId, packet.duration);
        }
    }

}
