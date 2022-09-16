package net.tiffit.rotcg.render.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.StatType;
import net.tiffit.rotcg.render.RenderUtils;

public class HUDBars {

    static void render(RenderGuiOverlayEvent e, String overlayType, GameObjectState state, int scaledWidth, int scaledHeight, Minecraft mc, Font font){
        int eachWidth = 85;

        int type = overlayType.equals(RenderHUD.HEALTH) ? 0 : overlayType.equals(RenderHUD.FOOD) ? 1 : 2;
        int valMin, valMax, color;
        String typeName;
        switch (type){
            case 0:
                valMin = state.getHP();
                valMax = state.getHPMax();
                color = 0xffff0000;
                typeName = "HP";
                break;
            case 1:
                valMin = state.getMP();
                valMax = state.getMPMax();
                color = 0xff5555ff;
                typeName = "MP";
                break;
            default:
                int level = state.<Integer>getStat(StatType.LEVEL);
                boolean maxLevel = level == 20;
                if(!maxLevel){
                    valMin = state.<Integer>getStat(StatType.EXP);
                    valMax = state.<Integer>getStat(StatType.NEXT_LEVEL_EXP);
                    color = 0xff00ff00;
                    typeName = "Level " + level;
                }else {
                    valMin = state.<Integer>getStat(StatType.CURR_FAME);
                    valMax = state.<Integer>getStat(StatType.NEXT_CLASS_QUEST_FAME);
                    color = 0xffe67600;
                    typeName = "Fame";
                }
        }
        PoseStack stack = e.getPoseStack();
        stack.pushPose();
        int left = scaledWidth / 2 - ((eachWidth+2)*3/2) + (eachWidth+2)*type;
        int top = scaledHeight - 34;
        int barWidth = valMax == -1 ? eachWidth : (int)(eachWidth * valMin/(float)valMax);
        GuiComponent.fill(stack, left, top, left + eachWidth, top + 12, 0x55000000);
        GuiComponent.fill(stack, left, top, left + barWidth, top + 12, color);
        RenderUtils.drawStringOutline(font, stack, typeName,left + eachWidth/2f - font.width(typeName)/2f, top - 10, 0xffffffff, 0);
        String text = valMax == -1 ? valMin + "" : valMin + "/" + valMax;
        font.drawShadow(stack, text, left + eachWidth/2f - font.width(text)/2f, top + 2, 0xffffffff);
        stack.popPose();
    }

}
