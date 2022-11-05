package net.tiffit.rotcg.screen.character;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.tiffit.realmnetapi.auth.RealmAuth;
import net.tiffit.realmnetapi.auth.RotmgEnv;
import net.tiffit.realmnetapi.auth.data.PlayerChar;
import net.tiffit.rotcg.Rotcg;

import java.util.Comparator;
import java.util.List;

public class CharSelectScreen extends Screen {

    private CharSelectList list;
    public final RotmgEnv env;
    public final List<PlayerChar> chars;

    public CharSelectScreen(RotmgEnv env) {
        super(Component.literal("Select Character"));
        this.env = env;
        chars = RealmAuth.charList(env, Rotcg.TOKEN);
        chars.sort(Comparator.comparingInt(value -> -value.id()));
    }

    @Override
    protected void init() {
        this.list = new CharSelectList(this.minecraft, this.width, this.height, 48, this.height - 64, 36, this);
        addRenderableWidget(list);
        super.init();
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 8, 16777215);
    }
}
