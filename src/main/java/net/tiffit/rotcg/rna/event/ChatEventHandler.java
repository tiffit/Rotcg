package net.tiffit.rotcg.rna.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.tiffit.realmnetapi.api.event.ChatEvent;
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
            if (numStars < 0) text = LangLoader.format(text);
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

}
