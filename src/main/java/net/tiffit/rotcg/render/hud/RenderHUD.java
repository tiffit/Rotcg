package net.tiffit.rotcg.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.net.RealmNetworker;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.render.hud.info.HInfoBossBars;
import net.tiffit.rotcg.render.hud.info.HInfoHeroesRemaining;
import net.tiffit.rotcg.render.hud.info.HInfoNearbyInteractable;
import net.tiffit.rotcg.render.hud.info.HUDInfoData;

@Mod.EventBusSubscriber
public class RenderHUD {

    public static final String HEALTH = "player_health";
    public static final String FOOD = "food_level";
    public static final String EXPERIENCE = "experience_bar";
    public static final String HOTBAR = "hotbar";
    public static final String ARMOR = "armor_level";

    @SubscribeEvent
    public static void renderHUD(RenderGuiOverlayEvent e){
        try {
            //System.out.println(e.getOverlay());
            renderUnsafe(e);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }


    public static void renderUnsafe(RenderGuiOverlayEvent e){
        RealmNetworker net = Rotcg.ACTIVE_CONNECTION;
        boolean inGame = net != null && net.map != null && net.map.getSelfState() != null;
        String overlayType = e.getOverlay().id().getPath();
        if(e instanceof RenderGuiOverlayEvent.Pre){
            switch (overlayType) {
                case HEALTH, FOOD, EXPERIENCE, HOTBAR, ARMOR -> e.setCanceled(true);
            }
        }
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int scaledWidth = e.getWindow().getGuiScaledWidth();
        int scaledHeight = e.getWindow().getGuiScaledHeight();
//        if(elementType == RenderGameOverlayEvent.ElementType.ALL && RotCG.NETWORKER != null && !RotCG.NETWORKER.connected){
//            String text = "Not Connected";
//            font.drawStringWithShadow(text, scaledWidth/2 - font.getStringWidth(text)/2, 5, 0xffff0000);
//        }
        if(!inGame)return;
        GameObjectState state = net.map.getSelfState();
        if(e instanceof RenderGuiOverlayEvent.Pre pre){
            HUDInfoData data = new HUDInfoData();
            data.increasePosY(20);
            switch (overlayType) {
                case HEALTH, FOOD, EXPERIENCE -> HUDBars.render(e, overlayType, state, scaledWidth, scaledHeight, mc, font);
                case HOTBAR -> HUDHotbar.render(e, state, scaledWidth, scaledHeight, mc, font);
                case ARMOR -> {
                    HInfoHeroesRemaining.render(e, data, mc, font);
                    HInfoBossBars.render(e, data, mc, font);
                    HInfoNearbyInteractable.render(e, data, mc, font);

                    HUDMinimap.render(e, pre.getPoseStack(), state, scaledWidth, scaledHeight, mc, font);
                    HUDNearbyPlayers.render(e, state, scaledWidth, scaledHeight, mc, font);
                }
            }
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }
}
