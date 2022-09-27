package net.tiffit.rotcg.render.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.tiffit.realmnetapi.assets.ConditionEffect;
import net.tiffit.realmnetapi.assets.xml.HealthBarBoss;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.render.RenderUtils;

import java.util.ArrayList;
import java.util.List;

public class HUDBossBars {

    private static List<RObject> bossBars = new ArrayList<>();
    private static long lastUpdate = 0;

    static void render(RenderGuiOverlayEvent e, GameObjectState state, int scaledWidth, int scaledHeight, Minecraft mc, Font font){
        if(System.currentTimeMillis() - lastUpdate > 1000) {
            Vec2f playerPos = new Vec2f((float) mc.player.getX(), (float) mc.player.getZ());
            bossBars = Rotcg.ACTIVE_CONNECTION.map.getEntityList().getAll(object -> {
                HealthBarBoss hbb = object.getGameObject().healthBar;
                if (hbb.radius() == 0) return false;
                Vec2f objPos = object.getCurrentPos().add(new Vec2f(hbb.xOffset(), hbb.yOffset()));
                return playerPos.distanceSqr(objPos) <= hbb.radius() * hbb.radius();
            });
            lastUpdate = System.currentTimeMillis();
        }
        int eachWidth = 150;
        int eachHeight = 15;
        for (int i = 0; i < bossBars.size(); i++) {
            PoseStack stack = e.getPoseStack();
            stack.pushPose();
            RObject robj = bossBars.get(i);
            GameObjectState enemyState = robj.getState();
            int hp = enemyState.getHP();
            int maxHp = enemyState.getHPMax();
            float hpPercent = (float)hp / maxHp;
            stack.translate(5, 20, 0);
            int left = 0;
            int top = i*(eachHeight+20);
            int barWidth = (int)(hpPercent * eachWidth);
            int color = 0xff_ff_00_00;
            if(enemyState.hasEffect(ConditionEffect.INVULNERABLE) || enemyState.hasEffect(ConditionEffect.INVULNERABLE)){
                color = 0xff_1e30ca;
            }
            GuiComponent.fill(stack, left, top, left + eachWidth, top + eachHeight, 0x55000000);
            GuiComponent.fill(stack, left, top, left + barWidth, top + eachHeight, color);
            String enemyName = Strings.isNullOrEmpty(robj.getGameObject().displayid) ? robj.getGameObject().id : robj.getGameObject().displayid;
            RenderUtils.drawStringOutline(font, stack, enemyName, left + 1, top - 10, 0xffffffff, 0);
            String text = hp + "/" + maxHp + " (" + (int)(hpPercent * 100) + "%)";
            font.drawShadow(stack, text, left + eachWidth/2f - font.width(text)/2f, top + 4, 0xffffffff);
            RenderUtils.hLine(stack, left, left + eachWidth - 1, top, 0xa0_00_00_00);
            RenderUtils.hLine(stack, left, left + eachWidth - 1, top + eachHeight - 1, 0xa0_00_00_00);
            RenderUtils.vLine(stack, left, top, top + eachHeight, 0xa0_00_00_00);
            RenderUtils.vLine(stack, left + eachWidth - 1, top, top + eachHeight, 0xa0_00_00_00);
            stack.popPose();
        }
    }

}
