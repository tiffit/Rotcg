package net.tiffit.rotcg.screen.character;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.assets.xml.XMLLoader;
import net.tiffit.realmnetapi.auth.RotmgEnv;
import net.tiffit.realmnetapi.auth.data.PlayerChar;
import net.tiffit.rotcg.pack.RotCGPack;
import net.tiffit.rotcg.screen.server.ServerSelectScreen;
import net.tiffit.rotcg.util.RotCGResourceLocation;

public class CharSelectListEntry  extends ObjectSelectionList.Entry<CharSelectListEntry>{

    private final PlayerChar pc;
    private final GameObject go;
    private final RotmgEnv env;
    private final RotCGResourceLocation classIcon;
    private final RotCGResourceLocation classIconHover;
    private final RotCGResourceLocation[] equipIcons;

    public static int SelectedCharacterId;

    public CharSelectListEntry(PlayerChar pc, RotmgEnv env) {
        this.pc = pc;
        this.env = env;
        go = XMLLoader.OBJECTS.get(pc.objectType());
        classIcon = RotCGPack.textToRlFull(go.texture.get(0));
        classIconHover = RotCGPack.animRl(go.texture.get(0).toSpriteLocation() , 0, 3);

        equipIcons = new RotCGResourceLocation[4];
        for(int i = 0; i < 4; i++){
            int id = pc.equipment()[i];
            if(id == -1)continue;
            GameObject itemGo = XMLLoader.OBJECTS.getOrDefault(id, null);
            if(itemGo == null)continue;
            equipIcons[i] = RotCGPack.textToRlFull(itemGo.texture.get(0));
        }
    }

    @Override
    public Component getNarration() {
        return Component.literal("");
    }

    @Override
    public void render(PoseStack ps, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
        ps.pushPose();
        if(pIsMouseOver){
            Screen.fill(ps, pLeft, pTop, pLeft + pWidth, pTop + pHeight, 0x50_ff_ff_ff);
        }
        Minecraft.getInstance().font.draw(ps, go.id + ChatFormatting.GRAY + " (Lvl. " + pc.level() + ")", (float)(pLeft + 32 + 3), (float)(pTop + 1), 16777215);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, pIsMouseOver ? classIconHover : classIcon);
        GuiComponent.blit(ps, pLeft, pTop, 0.0F, 0.0F, 32, 32, 32, 32);

        //Equipment
        for (int i = 0; i < equipIcons.length; i++) {
            RotCGResourceLocation rl = equipIcons[i];
            if(rl == null)continue;
            RenderSystem.setShaderTexture(0, rl);
            ps.pushPose();
            ps.translate(pLeft + 35 + i*18, pTop + 13, 0);
            ps.scale(0.5f, 0.5f, 1);
            GuiComponent.blit(ps, 0, 0, 0.0F, 0.0F, 32, 32, 32, 32);
            ps.popPose();
        }
        ps.popPose();
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        SelectedCharacterId = pc.id();
        Minecraft.getInstance().setScreen(new ServerSelectScreen(env));
        return true;
    }
}
