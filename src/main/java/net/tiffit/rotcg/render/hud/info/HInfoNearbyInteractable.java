package net.tiffit.rotcg.render.hud.info;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.assets.xml.XMLLoader;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.map.object.StatType;
import net.tiffit.realmnetapi.net.RealmNetworker;
import net.tiffit.rotcg.Constants;
import net.tiffit.rotcg.KeybindingManager;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.pack.RotCGPack;
import net.tiffit.rotcg.registry.entity.PortalEntity;
import net.tiffit.rotcg.registry.entity.RAnimationManager;
import net.tiffit.rotcg.registry.entity.RotcgEntity;
import net.tiffit.rotcg.registry.entity.RotcgEntityContainer;
import net.tiffit.rotcg.screen.slot.RInventoryScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HInfoNearbyInteractable {

    private static PortalEntity portalEntity = null;
    private static List<RotcgEntity> nearbyContainers = new ArrayList<>();
    private static long lastUpdate = 0;

    public static void render(RenderGuiOverlayEvent e, HUDInfoData data, Minecraft mc, Font font){
        RealmNetworker net = Rotcg.ACTIVE_CONNECTION;
        if(System.currentTimeMillis() - lastUpdate > 150) {
            nearbyContainers = RInventoryScreen.getNearbyEntities(net.map).stream().map(object -> {
                RotcgEntityContainer container = (RotcgEntityContainer) object.getListener();
                RotcgEntity entity = container.getEntity();
                if(entity == null)return null;
                return (RotcgEntity) mc.level.getEntity(entity.getId());
            }).filter(Objects::nonNull).collect(Collectors.toList());

            RObject nearbyPortal = net.map.getClosestGameObject(KeybindingManager.PORTAL_RANGE, Constants.CLASSES_PORTAL);
            lastUpdate = System.currentTimeMillis();
            if(nearbyPortal == null){
                portalEntity = null;
            }else{
                RotcgEntityContainer container = (RotcgEntityContainer) nearbyPortal.getListener();
                portalEntity = (PortalEntity)container.getEntity();
                portalEntity = portalEntity == null ? null : (PortalEntity) mc.level.getEntity(portalEntity.getId());
            }
        }
        PoseStack stack = e.getPoseStack();
        stack.pushPose();
        if(portalEntity != null && portalEntity.animationManager != null){
            RAnimationManager animationManager = portalEntity.animationManager;
            stack.pushPose();
            stack.translate(5, data.getPosY(), 0);
            RenderSystem.setShaderTexture(0, RotCGPack.textToRlFull(animationManager.getTexture()));
            GuiComponent.blit(stack, 0, -5, 32, 32, 0, 0, 16, 16, 16, 16);

            String portalName = portalEntity.getDisplayName().getString();

            stack.pushPose();
            stack.translate(32 + 4, 2, 0);
            font.drawShadow(stack, portalName, 0, 0, 0xffffffff);
            stack.translate(0, 10, 0);
            stack.scale(0.5f, 0.5f, 1);
            font.drawShadow(stack, "Press " + ChatFormatting.GOLD + KeybindingManager.INTERACT.getKey().getDisplayName().getString() +
                    ChatFormatting.RESET + " to Enter", 0, 0, 0xffcecece);
            stack.popPose();
            stack.popPose();

            data.increasePosY(35);
        }
        if(nearbyContainers.size() > 0){
            List<GameObject> items = new ArrayList<>();

            stack.pushPose();
            stack.translate(5, data.getPosY(), 0);
            int colorDiff = 3;
            int containerCount = nearbyContainers.size();
            for (int i = 0; i < containerCount; i++) {
                RotcgEntity containerEntity = nearbyContainers.get(i);
                RenderSystem.setShaderTexture(0, RotCGPack.textToRlFull(containerEntity.animationManager.getTexture()));
                float color = (i + colorDiff) / (float)(containerCount + colorDiff);
                RenderSystem.setShaderColor(color, color, color, 1f);
                GuiComponent.blit(stack, i*8, -5, 32, 32, 0, 0, 16, 16, 16, 16);
                for(int j = StatType.INVENTORY_0.id; j <= StatType.INVENTORY_7.id; j++) {
                    GameObjectState state = containerEntity.getReference().getState();
                    int itemid = state.<Integer>getStat(StatType.byID(j));
                    if(itemid == -1)continue;
                    items.add(XMLLoader.OBJECTS.get(itemid));
                }
            }
            stack.pushPose();
            stack.translate(32 + containerCount*8 + 5, -5, 0);
            int numItems = Math.min(items.size(), 8*4);
            int numRows = numItems <= 4 ? 1 : numItems <= 8 ? 2 : 4;
            int drawSize = numRows == 1 ? 32 : numRows == 2 ? 16 : 8;
            for (int i = 0; i < numItems; i++) {
                GameObject go = items.get(i);
                RenderSystem.setShaderTexture(0, RotCGPack.textToRlFull(go.texture.get(0)));
                int y = i % numRows;
                int x = i / numRows;
                GuiComponent.blit(stack, x*drawSize, y * drawSize, drawSize, drawSize, 0, 0, 16, 16, 16, 16);
            }
            stack.translate(-13, 3, 0);
            stack.scale(3, 3, 1);
            font.drawShadow(stack, "{", 0, 0, 0xff_ff_ff_ff);
            stack.translate(Math.ceil(numItems / (float)numRows)*drawSize/3f + 6, 0, 0);
            font.drawShadow(stack, "}", 0, 0, 0xff_ff_ff_ff);
            stack.popPose();

            stack.popPose();
            data.increasePosY(35);
        }
        stack.popPose();
    }

}
