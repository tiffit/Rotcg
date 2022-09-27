package net.tiffit.rotcg.event;

import net.minecraft.client.Minecraft;
import net.tiffit.realmnetapi.api.event.DeathEvent;
import net.tiffit.realmnetapi.net.packet.in.DeathPacketIn;
import net.tiffit.rotcg.screen.RDeathScreen;
import net.tiffit.rotcg.util.TickExecutor;

public class DeathEventHandler {
    public static void handle(DeathEvent deathEvent){
        DeathPacketIn packet = deathEvent.packet();
        TickExecutor.addClient(() -> Minecraft.getInstance().setScreen(new RDeathScreen(packet.killedBy, packet.gravestoneType, packet.totalFame)));
    }

}
