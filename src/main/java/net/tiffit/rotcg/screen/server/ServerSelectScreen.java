package net.tiffit.rotcg.screen.server;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.tiffit.realmnetapi.auth.RealmAuth;
import net.tiffit.realmnetapi.auth.RotmgEnv;
import net.tiffit.realmnetapi.auth.data.ServerInfo;
import net.tiffit.rotcg.Rotcg;

import java.util.List;

public class ServerSelectScreen extends Screen {

    private ServerSelectList list;
    public final List<ServerInfo> servers;

    public ServerSelectScreen(RotmgEnv env) {
        super(Component.literal("Select Server"));
        servers = RealmAuth.getServers(env, Rotcg.TOKEN);
    }

    @Override
    protected void init() {
        this.list = new ServerSelectList(this.minecraft, this.width, this.height, 48, this.height - 64, 15, this);
        addRenderableWidget(list);
        super.init();
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 8, 16777215);
    }
}
