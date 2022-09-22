package net.tiffit.rotcg.screen.server;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.tiffit.realmnetapi.auth.data.ServerInfo;

public class ServerSelectList extends ObjectSelectionList<ServerSelectListEntry> {
    private final ServerSelectScreen parent;

    public ServerSelectList(Minecraft pMinecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight, ServerSelectScreen screen) {
        super(pMinecraft, pWidth, pHeight, pY0, pY1, pItemHeight);
        this.parent = screen;
        for (ServerInfo si : screen.servers) {
            addEntry(new ServerSelectListEntry(si));
        }
    }
}
