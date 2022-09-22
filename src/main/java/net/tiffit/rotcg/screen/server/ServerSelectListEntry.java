package net.tiffit.rotcg.screen.server;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.tiffit.realmnetapi.auth.data.ServerInfo;
import net.tiffit.realmnetapi.net.ConnectionAddress;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.screen.MenuScreen;

public class ServerSelectListEntry extends ObjectSelectionList.Entry<ServerSelectListEntry>{

    private final ServerInfo info;

    public ServerSelectListEntry(ServerInfo info) {
        this.info = info;
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
        Font font = Minecraft.getInstance().font;
        font.draw(ps, info.name(), (float)(pLeft + 32 + 3), (float)(pTop + 1), 16777215);
        String usageText;
        if(info.usage() == 0){
            usageText = ChatFormatting.DARK_GREEN + "Normal";
        }else if(info.usage() == 1){
            usageText = ChatFormatting.DARK_RED + "Full";
        }else{
            usageText = ChatFormatting.GOLD + "Crowded";
        }
        font.draw(ps, usageText, pLeft + pWidth - font.width(usageText), (float)(pTop + 1), 16777215);
        ps.popPose();
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        Rotcg.SERVER = info;
        Rotcg.ADDRESS = ConnectionAddress.getNexusAddress(info.dns());
        MenuScreen.connect(Minecraft.getInstance());
        return true;
    }
}
