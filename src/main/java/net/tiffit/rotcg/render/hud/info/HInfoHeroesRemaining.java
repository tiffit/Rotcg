package net.tiffit.rotcg.render.hud.info;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.map.RMap;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.net.RealmNetworker;
import net.tiffit.rotcg.Rotcg;

public class HInfoHeroesRemaining {

    public static void render(RenderGuiOverlayEvent e, HUDInfoData data, Minecraft mc, Font font){
        RealmNetworker net = Rotcg.ACTIVE_CONNECTION;
        RMap map = net.map;
        int remaining = map.getHeroesRemaining();
        if(remaining >= 0 || map.getQuestObjectId() > 0){
            PoseStack stack = e.getPoseStack();
            stack.pushPose();
            stack.translate(5, data.getPosY(), 0);
            String worldName =  map.getRealmName();
            if(worldName.startsWith("NexusPortal."))worldName = worldName.substring(12);
            font.drawShadow(stack, ChatFormatting.BOLD + Rotcg.SERVER.name() + " " + worldName, 5, -10, 0xff_ff_ff_ff);
            int yPos = 0;
            font.drawShadow(stack, "• Explore " + ChatFormatting.GOLD + map.getName(), 5, yPos, 0xff_e8af5a);
            yPos += 10;

            RObject questObj = map.getEntityList().get(map.getQuestObjectId());
            if(questObj != null){
                GameObject go = questObj.getGameObject();
                String name = go.displayid.isEmpty() ? go.id : go.displayid;
                font.drawShadow(stack, "• Kill " + ChatFormatting.GOLD + name, 5, yPos, 0xff_e8af5a);
                yPos+= 10;
            }
            if(remaining >= 0){
                font.drawShadow(stack, "• Vanquish " + ChatFormatting.GOLD + remaining + ChatFormatting.RESET + " heroes", 5, yPos, 0xff_e8af5a);
                yPos+= 10;
            }

            data.increasePosY(15 + yPos);
            stack.popPose();
        }
    }

}
