package net.tiffit.rotcg.rna.event;

import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.*;
import net.tiffit.realmnetapi.api.event.ChatEvent;
import net.tiffit.realmnetapi.api.event.NotificationEvent;
import net.tiffit.realmnetapi.net.packet.in.NotificationPacketIn;
import net.tiffit.realmnetapi.net.packet.in.TextPacketIn;
import net.tiffit.realmnetapi.util.LangLoader;
import net.tiffit.rotcg.Rotcg;

public class ChatEventHandler {
    public static void handle(ChatEvent chatEvent){
        String sendText = "";
        TextPacketIn packet = chatEvent.packet();
        int numStars = packet.numStars;
        String name = packet.name;
        if(!name.startsWith("#")){
            name = name.split(",")[0];
        }
        String text = packet.text;
        if (numStars >= 0) {
            ChatFormatting starColor = numStars <= 14 ? ChatFormatting.BLUE :
                    numStars <= 29 ? ChatFormatting.DARK_BLUE :
                            numStars <= 44 ? ChatFormatting.DARK_RED :
                                    numStars <= 59 ? ChatFormatting.GOLD :
                                            numStars <= 74 ? ChatFormatting.YELLOW : ChatFormatting.WHITE;
            sendText = starColor + "\u2b50 ";
        }
        if(packet.recipient.equals("*Guild*")){
            sendText += ChatFormatting.GREEN + "<" + name + "> " + text;
        }else if(name.equals("*Error*")) {
            sendText += ChatFormatting.DARK_RED + text;
        }else if(!packet.recipient.isEmpty() && !name.isEmpty()){
            String suffix = text;
            if(packet.objectId == Rotcg.ACTIVE_CONNECTION.map.getObjectId()){
                suffix = "To: <" + packet.recipient + "> " + suffix;
            }else{
                suffix = "<" + packet.name + "> " + suffix;
            }
            sendText += ChatFormatting.AQUA + suffix;
        } else {
            ChatFormatting nameColor = packet.isSupporter ? ChatFormatting.DARK_PURPLE : ChatFormatting.DARK_GREEN;
            if (name.startsWith("#")) nameColor = ChatFormatting.GOLD;
            String nameText = name.isEmpty() ? "" : nameColor + "<" + (name.startsWith("#") ? name.substring(1) : name) + "> ";
            if (numStars < 0)text = LangLoader.format(text);
            sendText += nameText + ChatFormatting.RESET + text;

            if (name.trim().isEmpty())
                sendText = ChatFormatting.YELLOW + ChatFormatting.stripFormatting(sendText);
        }
        MutableComponent comp = Component.literal(sendText);
        if(!name.isEmpty()){
            comp.setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ":/ignore " + name)));
        }
        Rotcg.SERVER_PLAYER.sendSystemMessage(comp);
    }

    private static final MessageSignature QUEUE_SIGNATURE = new MessageSignature("QUEUE".getBytes());

    public static void handleNotification(NotificationEvent notificationEvent){
        NotificationPacketIn packet = notificationEvent.packet();
        Minecraft mc = Minecraft.getInstance();
        switch (packet.type) {
            case StatIncrease -> {
            }
            case ServerMessage -> {
            }
            case ErrorMessage -> {
            }
            case KeepMessage -> {
            }
            case Queue -> {
                MutableComponent comp = packet.queuePos == -1 ? Component.literal("You have left the queue!") :
                        Component.literal("Queue Position: " + packet.queuePos);
                ChatComponent chat = mc.gui.getChat();
                chat.deleteMessage(QUEUE_SIGNATURE);
                chat.addMessage(comp.withStyle(ChatFormatting.YELLOW), QUEUE_SIGNATURE, GuiMessageTag.system());
            }
            case Death -> {
            }
            case DungeonOpened -> {
            }
            case TeleportationError -> mc.gui.setOverlayMessage(Component.literal(LangLoader.format(packet.message)).withStyle(ChatFormatting.RED), false);
            case DungeonCall -> {
                String[] messageSplit = packet.message.split(";", 2);
                MutableComponent msg = Component.literal(ChatFormatting.DARK_AQUA + "<" + messageSplit[0] + "> " + LangLoader.format(messageSplit[1]));
                msg.setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ":/tp " + messageSplit[0])));
                assert mc.player != null;
                mc.player.sendSystemMessage(msg);
            }
        }
    }

}
