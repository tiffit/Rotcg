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
        if(remaining >= 0){
            PoseStack stack = e.getPoseStack();
            stack.pushPose();
            stack.translate(5, data.getPosY(), 0);
            font.drawShadow(stack, ChatFormatting.BOLD + Rotcg.SERVER.name() + " " + map.getRealmName().substring(12), 5, -10, 0xff_ff_ff_ff);
            font.drawShadow(stack, "• " + ChatFormatting.GOLD + remaining + ChatFormatting.RESET + " remaining heroes", 5, 0, 0xff_e8af5a);
            data.increasePosY(25);

            RObject questObj = map.getEntityList().get(map.getQuestObjectId());
            if(questObj != null){
                GameObject go = questObj.getGameObject();
                String name = go.displayid.isEmpty() ? go.id : go.displayid;
                font.drawShadow(stack, "• Kill " + ChatFormatting.GOLD + name, 5, 10, 0xff_e8af5a);
                data.increasePosY(10);
            }
            stack.popPose();
        }
    }

}
