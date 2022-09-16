package net.tiffit.rotcg.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.tiffit.realmnetapi.RealmNetApi;
import net.tiffit.realmnetapi.auth.RotmgEnv;
import net.tiffit.realmnetapi.net.ConnectionAddress;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.util.WorldUtils;

import java.io.File;

public class MenuScreen extends Screen {

    public MenuScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        Button connect;
        boolean canConnect = true;
        addRenderableWidget(connect = new Button(width / 2 - 100, 150, 200, 20, Component.literal("Connect to RotMG"), p_93751_ -> {
            assert minecraft != null;
            Rotcg.ADDRESS = ConnectionAddress.getNexusAddress(RealmNetApi.ENV.defaultIp);
            connect(minecraft);
        }));
        if (!canConnect) {
            connect.active = false;
        }
    }

    @Override
    public void render(PoseStack ms, int mouseX, int mouseY, float partialTick) {
        renderBackground(ms);
        super.render(ms, mouseX, mouseY, partialTick);
    }

    public static void connect(Minecraft minecraft) {
        try {
            minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.literal("Copying template files...")));
            File created = WorldUtils.createEmptyCopy();
            minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("selectWorld.data_read")));
            minecraft.createWorldOpenFlows().loadLevel(null, created.getName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
