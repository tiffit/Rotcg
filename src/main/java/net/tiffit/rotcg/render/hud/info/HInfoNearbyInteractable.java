package net.tiffit.rotcg.render.hud.info;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.tiffit.realmnetapi.map.object.RObject;
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
            stack.pushPose();
            stack.translate(5, data.getPosY(), 0);
            for (int i = 0; i < nearbyContainers.size(); i++) {
                RotcgEntity containerEntity = nearbyContainers.get(i);
                RenderSystem.setShaderTexture(0, RotCGPack.textToRlFull(containerEntity.animationManager.getTexture()));
                int colorDiff = nearbyContainers.size() > 3 ? 0 : 3;
                float color = (i + colorDiff) / (float)(nearbyContainers.size() + colorDiff);
                RenderSystem.setShaderColor(color, color, color, 1f);
                GuiComponent.blit(stack, i*8, -5, 32, 32, 0, 0, 16, 16, 16, 16);
            }
            stack.popPose();
            data.increasePosY(35);
        }
        stack.popPose();
    }

}
