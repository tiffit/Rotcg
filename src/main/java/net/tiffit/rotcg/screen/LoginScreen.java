package net.tiffit.rotcg.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.tiffit.realmnetapi.RealmNetApi;
import net.tiffit.realmnetapi.auth.AccessToken;
import net.tiffit.realmnetapi.auth.RealmAuth;
import net.tiffit.realmnetapi.auth.RotmgEnv;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.screen.character.CharSelectScreen;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;

public class LoginScreen extends Screen {

    private static final HashSet<RotmgEnv> alreadyLoggedIn = new HashSet<>();
    private boolean failedLogin;
    private boolean successfulLogin;
    private long failedLoginTime = 0;
    private final RotmgEnv env;

    private boolean oldTokenSuccess = false;

    private Button login;

    public LoginScreen() {
        super(Component.literal("Login Screen"));
        env = RealmNetApi.ENV;
        try {
            AccessToken oldToken = Rotcg.CONFIG.getToken(env);
            if(oldToken != null){
                if(!alreadyLoggedIn.contains(env)){
                    Rotcg.TOKEN = RealmAuth.authenticate(env, oldToken);
                    Rotcg.CONFIG.tokens.put(env, Rotcg.TOKEN);
                    Rotcg.CONFIG.save();
                    alreadyLoggedIn.add(env);
                }else{
                    Rotcg.TOKEN = Rotcg.CONFIG.tokens.get(env);
                }
                oldTokenSuccess = true;
                successfulLogin = true;
            }
        }catch (Exception ignored){}

    }

    @Override
    protected void init() {
        super.init();

        EditBox username = new EditBox(font, this.width / 2 - 100, this.height/2 - 15, 200, 20, Component.literal("Username"));
        username.setCanLoseFocus(true);
        username.setFocus(false);
        username.setMaxLength(100);

        EditBox password = new EditBox(font, this.width / 2 - 100, this.height/2 +20, 200, 20, Component.literal("Password"));
        password.setFormatter((s, integer) -> FormattedCharSequence.forward(StringUtils.leftPad("", s.length(), '*'), Style.EMPTY));
        password.setCanLoseFocus(true);
        password.setFocus(false);
        password.setMaxLength(100);

        addRenderableWidget(username);
        addRenderableWidget(password);

        login = new Button(width /2 - 100, height/2 + 70, 200, 20, Component.literal("Login"), pButton -> {
            pButton.active = false;
            try {
                Rotcg.TOKEN = RealmAuth.authenticate(env, username.getValue(), password.getValue());
                Rotcg.TOKEN = RealmAuth.authenticate(env, Rotcg.TOKEN);
                Rotcg.CONFIG.tokens.put(env, Rotcg.TOKEN);
                Rotcg.CONFIG.save();
                alreadyLoggedIn.add(env);
                failedLogin = false;
                successfulLogin = true;
            }catch (Exception ex){
                failedLogin = true;
                failedLoginTime = System.currentTimeMillis();
                pButton.active = true;
            }
        });
        addRenderableWidget(login);

        if(oldTokenSuccess){
            username.active = false;
            password.active = false;
            login.active = false;
        }
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);
        drawCenteredString(pPoseStack, font, "Login to RotMG (" + RealmNetApi.ENV + ")", width/2, 20, 0xff_ff_ff_ff);
        font.drawShadow(pPoseStack, "Username", this.width/2f - 100, this.height/2f - 25, 0xff_ff_ff_ff);
        font.drawShadow(pPoseStack, "Password", this.width/2f - 100, this.height/2f + 10, 0xff_ff_ff_ff);

        if(failedLogin){
            int time = (int)(System.currentTimeMillis() - failedLoginTime);
            if(time < 2500){
                int alpha = (int)((1 - time/3000f) * 255);
                font.drawShadow(pPoseStack, "Invalid login!", this.width/2f - 100, this.height/2f + 45, 0x00_ff_00_00 + (alpha << 24));
            }
        }else if(successfulLogin){
            font.drawShadow(pPoseStack, "Login success...", this.width/2f - 100, this.height/2f + 45, 0xff_00_ff_00);
        }

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void tick() {
        super.tick();
        if(successfulLogin){
            assert minecraft != null;
            int tries = 0;
            while(tries < 5){
                try{
                    this.minecraft.setScreen(new CharSelectScreen(env));
                    break;
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                tries++;
            }
            if(tries == 5){
                login.active = true;
                successfulLogin = false;
            }
        }
    }
}
