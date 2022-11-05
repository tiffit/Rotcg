package net.tiffit.rotcg.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.tiffit.realmnetapi.RealmNetApi;
import net.tiffit.realmnetapi.auth.RotmgEnv;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.util.WorldUtils;

import java.io.File;

public class MenuScreen extends Screen {

    private CycleButton<RotmgEnv> envButton;

    public MenuScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        addRenderableWidget(new Button(width / 2 - 100, 100, 200, 20, Component.literal("Connect to RotMG").withStyle(ChatFormatting.GOLD),p_93751_ -> {
            assert minecraft != null;
            try{
                RealmNetApi.ENV = Rotcg.CONFIG.env = envButton.getValue();
                Rotcg.CONFIG.save();
                this.minecraft.setScreen(new LoginScreen());
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }));

        envButton = new CycleButton.Builder<RotmgEnv>(env -> Component.literal(env.toString())).withValues(RotmgEnv.values()).withInitialValue(Rotcg.CONFIG.env)
                .create(width / 2 - 100, 150, 200, 20, Component.literal("RotMG Environment"));
        addRenderableWidget(envButton);
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
