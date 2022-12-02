package net.tiffit.rotcg.rna.event;

import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.tiffit.realmnetapi.api.event.QueueInformationEvent;
import net.tiffit.realmnetapi.api.event.ReconnectEvent;
import net.tiffit.realmnetapi.net.ConnectionAddress;
import net.tiffit.realmnetapi.net.RealmNetworker;
import net.tiffit.realmnetapi.net.packet.in.ReconnectPacketIn;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.screen.MenuScreen;
import net.tiffit.rotcg.util.TickExecutor;

public class ReconnectEventHandler {

    private static long NextReconnectTime = -1;
    private static MessageSignature QueueSignature = new MessageSignature("rotcgqueue".getBytes());

    public static void handle(ReconnectEvent reconnectEvent){
        Rotcg.ACTIVE_CONNECTION.disconnect();
        ReconnectPacketIn packet = reconnectEvent.packet();
        String address = packet.host.isEmpty() ? Rotcg.ADDRESS.address() : packet.host;
        Rotcg.ADDRESS = new ConnectionAddress(address, 2050, packet.key, packet.keyTime, packet.gameId);
        TickExecutor.addClient(() -> {
            Minecraft mc = Minecraft.getInstance();
            mc.level.disconnect();
            mc.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
            MenuScreen.connect(mc);
        });
    }

    public static void handleQueue(QueueInformationEvent queueInformationEvent){
        TickExecutor.addClient(() -> {
            Minecraft mc = Minecraft.getInstance();
            if(mc.player != null){
                String queuePosition = queueInformationEvent.position() + "/" + queueInformationEvent.outOf();
                ChatComponent chat = mc.gui.getChat();
                chat.deleteMessage(QueueSignature);
                chat.addMessage(Component.literal(ChatFormatting.RED + "[RotCG] " + ChatFormatting.RESET + "In Queue: " + queuePosition), QueueSignature, GuiMessageTag.system());
                NextReconnectTime = System.currentTimeMillis() + 5000;
                AttemptQueueReconnect();
            }else{
                handleQueue(queueInformationEvent);
            }
        });
    }

    private static void AttemptQueueReconnect(){
        if(NextReconnectTime == -1)return;
        TickExecutor.addClient(() -> {
            if(System.currentTimeMillis() > NextReconnectTime){
                RealmNetworker networker = new RealmNetworker(Rotcg.ADDRESS);
                networker.connect(Rotcg.TOKEN);
                Rotcg.ACTIVE_CONNECTION = networker;
                NextReconnectTime = -1;
            }else{
                AttemptQueueReconnect();
            }
        });
    }

}
