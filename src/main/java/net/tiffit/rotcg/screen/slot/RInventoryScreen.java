package net.tiffit.rotcg.screen.slot;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.network.chat.Component;
import net.tiffit.realmnetapi.map.RMap;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.map.object.StatType;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.Rotcg;

import java.util.List;

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
            addSlot(scaledWidth / 2 + xPos, scaledHeight/2 - 70, i, playerState);
        }
        for(int i = StatType.INVENTORY_4.id; i <= StatType.INVENTORY_11.id; i++) {
            int offset_i = i - StatType.INVENTORY_4.id;
            int xPos = offset_i * slotPadding - slotPadding*4;
            addSlot(scaledWidth / 2 + xPos, scaledHeight/2 - 50, i, playerState);
        }
        if(playerState.<Integer>getStat(StatType.HASBACKPACK) == 1){
            for(int i = StatType.BACKPACK_0.id; i <= StatType.BACKPACK_7.id; i++) {
                int offset_i = i - StatType.BACKPACK_0.id;
                int xPos = offset_i * slotPadding - slotPadding*4;
                addSlot(scaledWidth / 2 + xPos, scaledHeight/2 - 30, i, playerState);
            }
        }
        RMap map = Rotcg.ACTIVE_CONNECTION.map;
        List<RObject> containers = map.getEntityList().getAll(object -> {
            if(!object.getGameObject().goClass.equals("Container"))return false;
            Vec2f playerPos = map.getPlayerPos().getPos();
            if(playerPos.distanceSqr(object.getCurrentPos()) < 9){
                return true;
            }
            return false;
        });

        for (int i = 0; i < containers.size(); i++) {
            RObject containerObj = containers.get(i);
            for(int j = StatType.INVENTORY_0.id; j <= StatType.INVENTORY_7.id; j++) {
                if(containerObj.getState().hasStat(StatType.byID(j))){
                    int offset_j = j - StatType.INVENTORY_0.id;
                    int xPos = offset_j * slotPadding - slotPadding*4;
                    addSlot(scaledWidth / 2 + xPos, scaledHeight/2 - 10 + i*20, j, containerObj.getState());
                }
            }
        }
    }
}
