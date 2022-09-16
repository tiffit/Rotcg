package net.tiffit.rotcg.render.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.net.RealmNetworker;
import net.tiffit.rotcg.Rotcg;

import java.util.Arrays;

@Mod.EventBusSubscriber
public class RenderHUD {

    public static final String HEALTH = "player_health";
    public static final String FOOD = "food_level";
    public static final String EXPERIENCE = "experience_bar";
    public static final String HOTBAR = "hotbar";

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
        boolean inGame = net != null && net.map.getSelfState() != null;
        String overlayType = e.getOverlay().id().getPath();
        if(e instanceof RenderGuiOverlayEvent.Pre){
            switch (overlayType) {
                case HEALTH, FOOD, EXPERIENCE, HOTBAR -> e.setCanceled(true);
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
//        if(RotCG.NETWORKER != null && RotCG.NETWORKER.playerState != null){
//            String text = Arrays.toString(RotCG.NETWORKER.playerState.getAllEffects().toArray());
//            font.drawStringWithShadow(text, scaledWidth/2 - font.getStringWidth(text)/2, 15, 0xffffffff);
//        }
        if(!inGame)return;
        GameObjectState state = net.map.getSelfState();
        if(e instanceof RenderGuiOverlayEvent.Pre){
            switch (overlayType) {
                case HEALTH, FOOD, EXPERIENCE -> HUDBars.render(e, overlayType, state, scaledWidth, scaledHeight, mc, font);
                case HOTBAR -> HUDHotbar.render(e, state, scaledWidth, scaledHeight, mc, font);
            }
        }
//        if(elementType == RenderGameOverlayEvent.ElementType.HOTBAR){
//            HUDHotbar.render(e, state, scaledWidth, scaledHeight, mc, font);
//        }
//        if(elementType == RenderGameOverlayEvent.ElementType.AIR){
//            HUDMinimap.render(e, state, scaledWidth, scaledHeight, mc, font);
//            RotMGEntity nearestEntity = WorldUtils.getClosestInteractableEntity(mc.player, ContainerEntity.class, PlayerEntity.class);
//            if(nearestEntity != null){
//                nearestEntity.renderInfo(e, state, scaledWidth, scaledHeight, mc, font);
//            }
//        }
//        RenderSystem.color4f(1, 1, 1, 1);
    }
}
