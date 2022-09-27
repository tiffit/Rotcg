package net.tiffit.rotcg.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.tiffit.realmnetapi.assets.xml.XMLLoader;
import net.tiffit.rotcg.pack.RotCGPack;

public class RDeathScreen extends Screen {

    private final String killer;
    private final ResourceLocation graveRl;
    private int delayTicker = -1;
    private int fame;

    public RDeathScreen(String killer, int graveType, int fame) {
        super(Component.literal("You died!").withStyle(ChatFormatting.RED));
        this.killer = killer;
        this.fame = fame;
        graveRl = RotCGPack.textToRlFull(XMLLoader.OBJECTS.get(graveType).texture.get(0));
    }

    protected void init() {
        if(delayTicker == -1){
            this.delayTicker = 0;
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(new SoundEvent(new ResourceLocation("rotcg:death_screen")), 1f));
        }
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 60, 200, 20,
                Component.literal("Return to Main Menu"), (p_95930_) -> exitToTitleScreen()));
    }

    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        float progress = Math.min(delayTicker + pPartialTick, 20) / 20f;
        RenderSystem.setShaderColor(1, 1, 1, progress);
        this.fillGradient(pPoseStack, 0, 0, this.width, this.height, 0x60500000, 0xA0803030);
        drawCenteredString(pPoseStack, this.font, "Killed by " + ChatFormatting.BOLD + killer, this.width / 2, 30 + (int)(progress * 20), 0xFFFFFF);

        float fameProgess = progress < 1 ? 0 : Math.min(delayTicker + pPartialTick - 20, 40) / 40f;
        if(fameProgess > 0){
            drawCenteredString(pPoseStack, this.font,
                    ChatFormatting.GOLD + "Total Fame Earned: " + ChatFormatting.BOLD + (int)(fame * fameProgess),
                    this.width / 2, 70, 0xFFFFFF);
        }
        pPoseStack.pushPose();
        pPoseStack.translate(this.width / 2f, progress * 20, 0);
        pPoseStack.scale(2.0F, 2.0F, 2.0F);
        drawCenteredString(pPoseStack, this.font, this.title, 0, 0, 0xFFFFFF);
        pPoseStack.popPose();
        RenderSystem.setShaderColor(1, 1, 1, 1f);

        pPoseStack.pushPose();
        RenderSystem.setShaderTexture(0, graveRl);
        pPoseStack.translate(width/2f, height/2f - 20, 0);
        pPoseStack.scale(15, 15, 1);
        pPoseStack.translate(-4, -4, 0);
        RenderSystem.setShaderColor(1, 1, 1, progress);
        blit(pPoseStack, 0, 0, 0, 0, 8, 8, 8, 8);
        pPoseStack.popPose();

        float xPos = width / 2f;
        float yPos = height / 2f + 25;
        int scale = 30;
        InventoryScreen.renderEntityInInventory((int)xPos, (int)yPos, scale,
                width - pMouseX - xPos, height - pMouseY - yPos - scale * 1.3f,
                minecraft.player);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    private void exitToTitleScreen() {
        if (this.minecraft.level != null) {
            this.minecraft.level.disconnect();
        }

        this.minecraft.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
        this.minecraft.setScreen(new MenuScreen());
    }

    public void tick() {
        super.tick();
        ++this.delayTicker;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
