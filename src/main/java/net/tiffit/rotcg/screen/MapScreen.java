package net.tiffit.rotcg.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.map.RMap;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.map.object.StatType;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.render.hud.HUDMinimap;
import net.tiffit.rotcg.util.TickExecutor;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class MapScreen extends Screen {

    private int viewRange = 100;
    private double xOffset = 0, yOffset = 0;
    private List<RObject> hovered = new LinkedList<>();
    private Vec2f mouseWorldPos = Vec2f.ZERO;

    public MapScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
    }

    @Override
    public void render(PoseStack ms, int mouseX, int mouseY, float partialTick) {
        renderBackground(ms);
        super.render(ms, mouseX, mouseY, partialTick);

        int mapSize = 300;
        ms.pushPose();
        ms.translate(width/2f + xOffset, height/2f + yOffset, 0);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        ms.pushPose();
        RMap map = Rotcg.ACTIVE_CONNECTION.map;

        int drawX = -mapSize/2;
        int drawY = -mapSize/2;

        HUDMinimap.renderMap(minecraft, ms, drawX, drawY, mapSize, viewRange,false);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1F);
        ms.popPose();
        ms.popPose();

        Player player = minecraft.player;
        float scale = (float) mapSize / (viewRange*2);
        float mousePosX = (mouseX - drawX - (width/2f + (float) xOffset))/scale - viewRange + (float) player.getX();
        float mousePosY = (mouseY - drawY - (height/2f + (float) yOffset))/scale - viewRange + (float) player.getZ();
        mouseWorldPos = new Vec2f(mousePosX, mousePosY);
        List<Component> tooltips = new LinkedList<>();
        hovered.clear();
        for(RObject entity : map.getEntityList().getEntities()){
            GameObjectState entityState = entity.getState();
            GameObject go = entityState.getGameObject();
            if(go != null && go.player){
                if(entityState.hasStat(StatType.NAME) && entity.getCurrentPos().distanceSqr(mouseWorldPos) < 9){
                    tooltips.add(Component.literal(entityState.getStat(StatType.NAME)).withStyle(ChatFormatting.GRAY));
                    hovered.add(entity);
                }
            }
        }
        if(tooltips.size() > 0){
            if(map.isAllowTeleport()){
                tooltips.add(0, Component.literal("Click to Teleport").withStyle(ChatFormatting.GOLD));
            }
            renderComponentTooltip(ms, tooltips, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        viewRange = Mth.clamp(viewRange - (int)pDelta*10, 10, 300);
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if(pButton == 1){
            xOffset += pDragX;
            yOffset += pDragY;
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if(Rotcg.ACTIVE_CONNECTION.map.isAllowTeleport() && hovered.size() > 0){
            onClose();
            TickExecutor.addClient(() -> {
                hovered.sort(Comparator.comparingDouble(value -> value.getCurrentPos().distanceSqr(mouseWorldPos)));
                Rotcg.ACTIVE_CONNECTION.controller.teleport(hovered.get(0));
            });
            return true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
