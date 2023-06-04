package net.tiffit.rotcg.screen.slot;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.assets.xml.XMLLoader;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.StatType;
import net.tiffit.realmnetapi.net.RealmNetworker;
import net.tiffit.realmnetapi.net.SlotObjectData;
import net.tiffit.realmnetapi.net.packet.out.InvDropPacketOut;
import net.tiffit.realmnetapi.net.packet.out.InvSwapPacketOut;
import net.tiffit.realmnetapi.net.packet.out.UseItemPacketOut;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.render.RenderUtils;
import net.tiffit.rotcg.util.RotCGResourceLocation;

public class MoveableSlotScreen extends Screen {

    protected RotMGSlot dragSlot;

    protected MoveableSlotScreen(Component title) {
        super(title);
    }


    @Override
    public void render(PoseStack ps, int mouseX, int mouseY, float partialTick) {
        renderBackground(ps);
        super.render(ps, mouseX, mouseY, partialTick);
        for(GuiEventListener guiEventListener : children()){
            if(guiEventListener instanceof RotMGSlot slot){
                slot.renderToolTip(ps, mouseX, mouseY);
            }
        }
        assert minecraft != null;
        Window window = minecraft.getWindow();
        int scaledWidth = window.getGuiScaledWidth();
        int scaledHeight = window.getGuiScaledHeight();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        drawCenteredString(ps, font, title, scaledWidth/2, scaledHeight/2 - 80, 0xffffffff);
    }

    @Override
    public boolean mouseClicked(double x, double y, int mouse) {
        RealmNetworker net = Rotcg.ACTIVE_CONNECTION;
        for(GuiEventListener guiEventListener : children()){
            if(guiEventListener instanceof RotMGSlot slot){
                if(slot.isMouseOver(x, y)){
                    ItemStack stack = slot.getStack();
                    if(dragSlot == null) {
                        if (!stack.isEmpty()) {
                            if(Screen.hasShiftDown()){
                                GameObject itemObj = slot.getItemObject();
                                if(itemObj.consumable){
                                    if(itemObj.potion){
                                        assert minecraft != null;
                                        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(new SoundEvent(new ResourceLocation("rotcg:use_potion")), 1f));
                                    }
                                    SlotObjectData data = new SlotObjectData(slot.state.objectId, slot.getPacketSlot(), slot.getItemId());
                                    net.send(new UseItemPacketOut(RealmNetworker.getTime(), data, Vec2f.ZERO,0));
                                }
                                return true;
                            }
                            dragSlot = slot;
                            return true;
                        }
                    }else{
                        if(dragSlot == slot){
                            dragSlot = null;
                            return true;
                        }
                        SlotObjectData origData = new SlotObjectData(dragSlot.state.objectId, dragSlot.getPacketSlot(), dragSlot.getItemId());
                        SlotObjectData newData = new SlotObjectData(slot.state.objectId, slot.getPacketSlot(), slot.getItemId());
                        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(new SoundEvent(new ResourceLocation("rotcg:inventory_move_item")), 1f));
                        Rotcg.ACTIVE_CONNECTION.send(new InvSwapPacketOut(RealmNetworker.getTime(), net.map.getPlayerPos().getPos(), origData, newData));
                        dragSlot = null;
                        return true;
                    }
                }
            }
        }
        if(dragSlot != null && dragSlot.state == net.map.getSelfState()){
            SlotObjectData data = new SlotObjectData(dragSlot.state.objectId, dragSlot.getPacketSlot(), dragSlot.getItemId());
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(new SoundEvent(new ResourceLocation("rotcg:loot_appears")), 1f));
            net.send(new InvDropPacketOut(data));
        }
        dragSlot = null;
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected void addSlot(int x, int y, int slot, GameObjectState state){
        addRenderableWidget(new RotMGSlot(x, y, slot, state));
    }

    public class RotMGSlot extends Button {

        public int slotId;
        public GameObjectState state;

        public RotMGSlot(int x, int y, int slotId, GameObjectState state) {
            super(x, y, 16, 16, Component.empty(), pButton -> {});
            this.slotId = slotId;
            this.state = state;
        }

        @Override
        public void renderButton(PoseStack ps, int mousex, int mousey, float partialTick) {
            ItemStack stack = getStack();
            int offsetX = x;
            int offsetY = y;
            if(!stack.isEmpty() && dragSlot == this){
                offsetX = mousex - 8;
                offsetY = mousey - 8;
            }
            RenderUtils.renderSlotAndItemWithOffset(ps, x, y, offsetX, offsetY, stack);
        }

        @Override
        public void renderToolTip(PoseStack ps, int x, int y) {
            if(dragSlot != null)return;
            ItemStack stack = getStack();
            if(!stack.isEmpty()){
                if(isHoveredOrFocused()){
                    renderTooltip(ps, stack, x, y);
                }
            }
        }

        private ItemStack getStack(){
            int itemid = getItemId();
            if(itemid == -1)return ItemStack.EMPTY;
            ResourceLocation itemRL = new RotCGResourceLocation("item_" + itemid);
            return new ItemStack(ForgeRegistries.ITEMS.getValue(itemRL));
        }

        private GameObject getItemObject(){
            return XMLLoader.OBJECTS.get(getItemId());
        }

        private int getItemId(){
            return state.<Integer>getStat(StatType.byID(slotId));
        }

        private int getPacketSlot(){
            if(slotId >= StatType.BACKPACK_0.id){
                return slotId - StatType.BACKPACK_0.id + 12;
            }
            return slotId - StatType.INVENTORY_0.id;
        }
    }
}
