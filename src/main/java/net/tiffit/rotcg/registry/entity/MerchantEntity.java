package net.tiffit.rotcg.registry.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.map.object.StatType;
import net.tiffit.rotcg.Rotcg;

public class MerchantEntity extends RotcgEntity {

    public static RegistryObject<EntityType<MerchantEntity>> TYPE;

    public MerchantEntity(EntityType<? extends RotcgEntity> type, Level level) {
        super(type, level);
    }

    public ItemStack getMerchandise(){
        RObject obj = getReference();
        if(obj != null) {
            GameObjectState state = obj.getState();
            if (state.getStat(StatType.MERCHANDISE_TYPE) != null) {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Rotcg.MODID, "item_" + state.getStat(StatType.MERCHANDISE_TYPE)));
                if (item != null) return new ItemStack(item);
            }
        }
        return ItemStack.EMPTY;
    }

}
