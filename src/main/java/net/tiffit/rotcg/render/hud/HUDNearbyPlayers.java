package net.tiffit.rotcg.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.map.object.StatType;
import net.tiffit.realmnetapi.util.Tuple;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.pack.RotCGPack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HUDNearbyPlayers {

    private static List<Tuple<RObject, Float>> tuples = new ArrayList<>();
    private static long lastUpdate = 0;

    static void render(RenderGuiOverlayEvent e, GameObjectState selfState, int scaledWidth, int scaledHeight, Minecraft mc, Font font){
        List<RObject> players = Rotcg.ACTIVE_CONNECTION.map.getEntityList().getAll(object -> object.getGameObject().player);
        PoseStack stack = e.getPoseStack();
        int index = 0;
        if(System.currentTimeMillis() - lastUpdate > 1000){
            Vec2f playerPos = new Vec2f((float) mc.player.getX(), (float) mc.player.getZ());
            tuples = players.stream()
                    .map(object -> new Tuple<>(object, (float)Math.sqrt(object.getCurrentPos().distanceSqr(playerPos))))
                    .sorted(Comparator.comparingDouble(Tuple::b))
                    .limit(12)
                    .toList();
            lastUpdate = System.currentTimeMillis();
        }
        for (Tuple<RObject, Float> tuple : tuples) {
            if(index > 12)break;
            RObject player = tuple.a();
            GameObjectState state = player.getState();
            String name = state.hasStat(StatType.NAME) ? state.<String>getStat(StatType.NAME).split(",")[0] : "None";
            int distance = tuple.b().intValue();
            stack.pushPose();
            stack.translate(scaledWidth - 100, 110, 0);
            stack.scale(0.75f, 0.75f, 1);
            stack.translate(0, index*10, 0);
            font.drawShadow(stack, name + ChatFormatting.GRAY + " (" + distance + "m)", 10, 0, 0xffffffff);
            RenderSystem.setShaderTexture(0, RotCGPack.animRl(state.getGameObject().texture.get(0).toSpriteLocation(), 0, 0));
            Screen.blit(stack, 0, 0, 8, 8, 0, 0, 8, 8, 8, 8);
            stack.popPose();
            index++;
        }
    }

}
