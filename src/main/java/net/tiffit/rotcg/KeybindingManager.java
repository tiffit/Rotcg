package net.tiffit.rotcg;

import com.ibm.icu.impl.coll.BOCSU;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.net.RealmNetworker;
import net.tiffit.realmnetapi.net.packet.out.ChangeAllyShootPacketOut;
import net.tiffit.realmnetapi.net.packet.out.UsePortalPacketOut;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeybindingManager {

    public static KeyMapping INTERACT;
    public static KeyMapping TOGGLE_ALLY_SHOOT;

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
    }

}
