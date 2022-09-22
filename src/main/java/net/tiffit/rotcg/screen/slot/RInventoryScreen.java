package net.tiffit.rotcg.screen.slot;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.network.chat.Component;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.StatType;
import net.tiffit.rotcg.Rotcg;

public class RInventoryScreen extends MoveableSlotScreen {

    private GameObjectState playerState;

    public RInventoryScreen() {
        super(Component.literal("Inventory"));
        this.playerState = Rotcg.ACTIVE_CONNECTION.map.getSelfState();
    }

    @Override
    protected void init() {
        super.init();
        Window window = minecraft.getWindow();
        int scaledWidth = window.getGuiScaledWidth();
        int scaledHeight = window.getGuiScaledHeight();

        int slotSize = 16;
        int slotPadding = slotSize + 4;
        for(int i = StatType.INVENTORY_0.id; i < StatType.INVENTORY_4.id; i++) {
            int offset_i = i - StatType.INVENTORY_0.id;
            int xPos = offset_i * slotPadding - slotPadding*2;
            addSlot(scaledWidth / 2 + xPos, scaledHeight/2, i, playerState);
        }
        for(int i = StatType.INVENTORY_4.id; i <= StatType.INVENTORY_11.id; i++) {
            int offset_i = i - StatType.INVENTORY_4.id;
            int xPos = offset_i * slotPadding - slotPadding*4;
            addSlot(scaledWidth / 2 + xPos, scaledHeight/2 + 20, i, playerState);
        }
        if(playerState.<Integer>getStat(StatType.HASBACKPACK) == 1){
            for(int i = StatType.BACKPACK_0.id; i <= StatType.BACKPACK_7.id; i++) {
                int offset_i = i - StatType.BACKPACK_0.id;
                int xPos = offset_i * slotPadding - slotPadding*4;
                addSlot(scaledWidth / 2 + xPos, scaledHeight/2 + 40, i, playerState);
            }
        }
    }
}
