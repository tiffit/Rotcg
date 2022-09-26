package net.tiffit.rotcg.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.network.chat.Component;
import net.tiffit.realmnetapi.api.event.ReconnectEvent;
import net.tiffit.realmnetapi.net.ConnectionAddress;
import net.tiffit.realmnetapi.net.packet.in.ReconnectPacketIn;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.screen.MenuScreen;
import net.tiffit.rotcg.util.TickExecutor;

public class ReconnectEventHandler {

    public static void handle(ReconnectEvent reconnectEvent){
        Rotcg.ACTIVE_CONNECTION.disconnect();
        ReconnectPacketIn packet = reconnectEvent.packet();
        String address = packet.host.isEmpty() ? Rotcg.ADDRESS.address() : packet.host;
        Rotcg.ADDRESS = new ConnectionAddress(address, 2050, packet.key, packet.keyTime, packet.gameId);
        TickExecutor.addRender(() -> {
            Minecraft mc = Minecraft.getInstance();
            mc.level.disconnect();
            mc.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
            MenuScreen.connect(mc);
        });
    }

}
