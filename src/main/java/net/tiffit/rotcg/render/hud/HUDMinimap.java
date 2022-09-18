package net.tiffit.rotcg.render.hud;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.map.RMap;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.rotcg.Rotcg;
import org.lwjgl.opengl.GL11;

public class HUDMinimap {

    public static int VIEW_RANGE = 30;

    static void render(RenderGuiOverlayEvent e, PoseStack ps, GameObjectState state, int scaledWidth, int scaledHeight, Minecraft mc, Font font){
        if(mc.player == null || Rotcg.MAP == null || Rotcg.MAP.mapRL == null)return;
        RMap map = Rotcg.ACTIVE_CONNECTION.map;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, Rotcg.MAP.mapRL);
        int mapSize = 100;
        int viewRange = VIEW_RANGE;

        double texU = mc.player.getX() - viewRange;
        double texV = mc.player.getZ() - viewRange;

        if(texU < 0 || texU + viewRange >= map.getWidth() || texV < 0 || texV + viewRange >= map.getHeight()){
            texU = 0;
            texV = 0;
            viewRange = map.getWidth() / 2 - 1;
        }

        MinimapBuilder minimap = new MinimapBuilder(mc.player,scaledWidth - mapSize - 5, 5, texU, texV, viewRange, mapSize, map);
        minimap.beginRender(ps);
        minimap.render(ps);
        for(RObject entity : map.getEntityList().getEntities()){
            int color = 0x0;
            GameObjectState entityState = entity.getState();
            GameObject go = entityState.getGameObject();
            if(go == null)continue;
            if(go.enemy)color = 0xffff0000;
            else if(go.player)color = 0xffebe134;
            if(color != 0x0)minimap.renderDot(ps, entity.getCorrectedX(), entity.getCorrectedY(), color);
        }
        minimap.renderDot(ps, mc.player.getX(), mc.player.getZ(), 0xff0000ff);
        minimap.endRender(ps);
    }

    private static class MinimapBuilder{

        LocalPlayer player;
        int drawX, drawY, mapSize;
        double texU, texV;
        RMap map;
        int viewRange;

        MinimapBuilder(LocalPlayer player, int drawX, int drawY, double texU, double texV, int viewRange, int mapSize, RMap map){
            this.player = player;
            this.drawX = drawX;
            this.drawY = drawY;
            this.texU = texU;
            this.texV = texV;
            this.viewRange = viewRange;
            this.mapSize = mapSize;
            this.map = map;
        }

        void beginRender(PoseStack ps){
            ps.pushPose();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            Window window = Minecraft.getInstance().getWindow();

            double xFactor = window.getWidth() / (double)window.getGuiScaledWidth();
            double yFactor = window.getHeight() / (double)window.getGuiScaledHeight();

            int leftBound = (int) (drawX * xFactor);
            int topBound = window.getHeight() - (int) ((drawY+mapSize) * yFactor);

            GL11.glScissor(leftBound, topBound, (int)(mapSize * xFactor), (int)(mapSize * xFactor));
            ps.translate(drawX, drawY, 0);
        }

        void render(PoseStack ps){
            ps.pushPose();
            float scale = (float) mapSize/(viewRange*2);
            ps.scale(scale, scale, 1);
            ps.translate(-texU, -texV, 0);
            Screen.blit(ps, 0, 0, 0, 0, map.getWidth(), map.getHeight(), map.getWidth(), map.getHeight());
            ps.popPose();
        }

        void renderDot(PoseStack ps, double xPos, double zPos, int color){
            ps.pushPose();
            double scale = (double)mapSize/(viewRange*2);
            double dx = xPos - player.getX();
            double dz = zPos - player.getZ();
            int drawX = (int)((viewRange + dx)* scale);
            int drawY = (int)((viewRange + dz)* scale);
            Screen.fill(ps, drawX - 1, drawY - 1, drawX + 1, drawY + 1, color);
            ps.popPose();
        }

        void endRender(PoseStack ps){
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            ps.popPose();
        }

    }

}