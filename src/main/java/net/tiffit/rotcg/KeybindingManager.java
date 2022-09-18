package net.tiffit.rotcg;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tiffit.realmnetapi.api.PlayerController;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.net.RealmNetworker;
import net.tiffit.realmnetapi.net.packet.out.ChangeAllyShootPacketOut;
import net.tiffit.realmnetapi.net.packet.out.UsePortalPacketOut;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.render.hud.HUDMinimap;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeybindingManager {

    public static KeyMapping INTERACT;
    public static KeyMapping TOGGLE_ALLY_SHOOT;
    public static KeyMapping USE_ABILITY;

    public static KeyMapping MINIMAP_ZOOM_OUT;
    public static KeyMapping MINIMAP_ZOOM_IN;

    @SubscribeEvent
    public static void onKeyboardPress(InputEvent.Key e){
        RealmNetworker net = Rotcg.ACTIVE_CONNECTION;
        Minecraft mc = Minecraft.getInstance();
        if(INTERACT.isDown()){
            if(net != null && net.connected){
                RObject nearestPortal = net.map.getClosestGameObject(5, Constants.CLASSES_PORTAL);
                if(nearestPortal == null) {
                    mc.gui.setOverlayMessage(Component.literal(ChatFormatting.RED + "Nothing to interact with!"), true);
                }else{
                    net.send(new UsePortalPacketOut(nearestPortal.getState().objectId));
                }
            }
        }
        if(TOGGLE_ALLY_SHOOT.isDown()){
            if(net != null && net.connected){
                net.showAllyShots = !net.showAllyShots;
                net.send(new ChangeAllyShootPacketOut(net.showAllyShots));
                mc.gui.setOverlayMessage(Component.literal(ChatFormatting.GOLD + (net.showAllyShots ? "Enabled ally shots" : "Disabled ally shots")), true);
            }
        }
        if(USE_ABILITY.isDown()){
            if(net == null || !net.connected)return;

            double maxDistance = 10;
            int steps = 30;
            double mult = maxDistance/steps;
            Vec3 start = mc.player.position();
            Vec3 end = new Vec3(start.x, start.y + mc.player.getEyeHeight(), start.z);
            Vec3 look = mc.player.getLookAngle();
            for(int i = 0; i <= steps; i++){
                double multAmount = mult * i;
                end = end.add(look.multiply(multAmount, multAmount, multAmount));
                if(end.y <= 65)break;
            }
            PlayerController.AbilityUseResult result = net.controller.useAbility(new Vec2f((float)end.x, (float)end.z));
            switch (result){
                case ON_COOLDOWN -> mc.gui.setOverlayMessage(Component.literal(ChatFormatting.RED + "On Cooldown"), true);
                case NO_ABILITY -> mc.gui.setOverlayMessage(Component.literal(ChatFormatting.DARK_RED + "No Ability Equipped"), true);
            }
        }
        if(net != null && Rotcg.MAP != null) {
            int zoomOffset = 0;
            if (MINIMAP_ZOOM_OUT.isDown()) {
                zoomOffset += 5;
            } else if (MINIMAP_ZOOM_IN.isDown()) {
                zoomOffset -= 5;
            }
            HUDMinimap.VIEW_RANGE += zoomOffset;
            if(HUDMinimap.VIEW_RANGE < 5)HUDMinimap.VIEW_RANGE = 5;
            else if(HUDMinimap.VIEW_RANGE*2 >= net.map.getWidth())HUDMinimap.VIEW_RANGE = net.map.getWidth()/2;
        }
    }

}
