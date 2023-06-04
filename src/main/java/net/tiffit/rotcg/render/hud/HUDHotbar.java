package net.tiffit.rotcg.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.tiffit.realmnetapi.assets.ConditionEffect;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.StatType;
import net.tiffit.rotcg.registry.EquipmentItem;
import net.tiffit.rotcg.render.RenderUtils;
import net.tiffit.rotcg.util.RotCGResourceLocation;

import java.util.List;

public class HUDHotbar {

    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    
    static void render(RenderGuiOverlayEvent e, GameObjectState state, int scaledWidth, int scaledHeight, Minecraft mc, Font font){
        int hotbarSlotSize = 20;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        int drawY = scaledHeight - hotbarSlotSize - 1;
        int texHeight = hotbarSlotSize + 1;
        RenderSystem.setShaderColor(230/255f, 190/255f, 14/255f, 1);
        PoseStack ps = e.getPoseStack();
        Gui.blit(ps, scaledWidth/2 - (int)(hotbarSlotSize*12*0.5 - 1), drawY, 0, 0, hotbarSlotSize*4 + 1, texHeight, 256, 256);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        Gui.blit(ps, scaledWidth/2 - (int)(hotbarSlotSize*((12)*0.5-4) - 1) + 1, drawY, 1 + hotbarSlotSize, 0, hotbarSlotSize*8 + 1, texHeight, 256, 256);

        int currentItem = mc.player.getInventory().selected;
        if(currentItem >= 0 && currentItem < 9){
            int slotIndex = currentItem;
            if(currentItem > 0)slotIndex += 3;
            int select_left = scaledWidth/2 - (int)(hotbarSlotSize*(12*0.5-slotIndex));
            Gui.blit(ps, select_left, drawY -1, 0, 22, 24, 24, 256, 256);
        }
        ps.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        for(int i = 0; i < 4 + 8; i++) {
            int id = i + 8;
            if (state.hasStat(StatType.byID(id))) {
                Item itm = ForgeRegistries.ITEMS.getValue(new RotCGResourceLocation("item_" + state.<Integer>getStat(StatType.byID(id))));
                ItemStack stack = new ItemStack(itm);
                if(stack.isEmpty())continue;
                if(itm instanceof EquipmentItem item){
                    int itemDrawX = scaledWidth / 2 - hotbarSlotSize * 6 + 4 + hotbarSlotSize * i;
                    int itemDrawY = scaledHeight - hotbarSlotSize + 2;
                    itemRenderer.renderGuiItem(stack, itemDrawX, itemDrawY);
                    stack.setCount(item.go.quantity);
                    itemRenderer.renderGuiItemDecorations(font, stack, itemDrawX, itemDrawY, null);
                }
            }
        }
        ps.popPose();
        int statsLeft = scaledWidth/2 - (int)(hotbarSlotSize*12*0.5 - 1) - 17;
        int statsRight = scaledWidth/2 + (int)(hotbarSlotSize*12*0.5 - 1) + 17;
        int statsHeight = 13;
        RenderUtils.drawStringRight(font, ps, getStatString("ATT ", state, StatType.ATTACK, StatType.ATTACK_BOOST), statsLeft, scaledHeight - statsHeight - 20, 0xffffffff);
        RenderUtils.drawStringRight(font, ps, getStatString("SPD ", state, StatType.SPEED, StatType.SPEED_BOOST), statsLeft, scaledHeight - statsHeight - 10, 0xffffffff);
        RenderUtils.drawStringRight(font, ps,getStatString("VIT ", state, StatType.VITALITY, StatType.VITALITY_BOOST), statsLeft, scaledHeight - statsHeight, 0xffffffff);

        font.drawShadow(ps, getStatString("DEF ", state, StatType.DEFENSE, StatType.DEFENSE_BOOST), statsRight, scaledHeight - statsHeight - 20, 0xffffffff);
        font.drawShadow(ps, getStatString("DEX ", state, StatType.DEXTERITY, StatType.DEXTERITY_BOOST), statsRight, scaledHeight - statsHeight - 10, 0xffffffff);
        font.drawShadow(ps, getStatString("WIS ", state, StatType.WISDOM, StatType.WISDOM_BOOST), statsRight, scaledHeight - statsHeight, 0xffffffff);

        List<ConditionEffect> effectList = state.getAllEffects();
        if(!mc.options.renderDebug) {
            for (int i = 0; i < effectList.size(); i++) {
                ConditionEffect effect = effectList.get(i);
                if (effect == ConditionEffect.GROUND_DAMAGE || effect == ConditionEffect.NOTHING)
                    continue;
                String name = effect.getDisplayName();
                int color = effect.buff ? 0xff_00_ff_00 : 0xff_ff_00_00;
                font.drawShadow(ps, name, scaledWidth - font.width(name) - 5, scaledHeight - 25 - i * 15, color);
            }
        }
    }

    public static String getStatString(String prefix, GameObjectState state, StatType stat, StatType boostType){
        int statVal = state.<Integer>getStat(stat);
        int boostVal = state.<Integer>getStat(boostType);
        String str = prefix + statVal;

        int toMax = state.getGameObject().maxStats.get(stat) - (statVal - boostVal);
        if(toMax == 0){
            str = ChatFormatting.YELLOW + str;
        }
        if(boostVal != 0){
            str = ChatFormatting.GREEN + str + " (" + (boostVal > 0 ? "+" : "") + boostVal + ")";
        }
        if(toMax > 0){
            if(stat == StatType.MAX_HP || stat == StatType.MAX_MP){
                toMax = (int)Math.ceil(toMax/5f);
            }
            str += " | " + toMax;
        }

        return str;
    }

}
