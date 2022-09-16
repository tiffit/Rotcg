package net.tiffit.rotcg.registry;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.tiffit.realmnetapi.assets.ItemType;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.assets.xml.Projectile;
import net.tiffit.realmnetapi.util.Tuple;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EquipmentItem extends Item {
    private static final Properties PROPERTIES = new Item.Properties().tab(CreativeModeTab.TAB_MISC);

    public final GameObject go;

    public EquipmentItem(GameObject obj) {
        super(PROPERTIES);
        this.go = obj;
    }

    @Override
    public String getDescriptionId() {
        String name = go.displayid.isEmpty() ? go.id : go.displayid;
        if(go.item) {
            if(ItemType.byID(go.slotType).canBeEquipped()) {
                int tier = go.tier;
                ChatFormatting color;
                String text;
                if(!go.setName.isEmpty()){
                    color = ChatFormatting.GOLD;
                    text = "ST";
                }else if(tier >= 0){
                    color = ChatFormatting.DARK_AQUA;
                    text = "T" + tier;
                }else{
                    color = ChatFormatting.DARK_PURPLE;
                    text = "UT";
                }
                name += " " + color + text;
            }
        }
        return name;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if(!go.description.isEmpty()){
            String[] descriptions = go.description.split("\\\\n");
            for(String str : descriptions){
                tooltip.add(Component.literal(ChatFormatting.GRAY + str));
            }
        }

        List<Tuple<String, String>> extraInfo = new ArrayList<>();
        if(go.projectiles.size() > 0){
            Projectile proj = go.projectiles.get(0);
            if(proj.damage > 0){
                extraInfo.add(new Tuple<>("Damage", "" + proj.damage));
            }else{
                extraInfo.add(new Tuple<>("Damage", proj.minDamage + " - " + proj.maxDamage));
            }
        }
        if(extraInfo.size() > 0){
            if(tooltip.size() > 0)tooltip.add(Component.empty());
            for(Tuple<String, String> info : extraInfo){
                tooltip.add(Component.literal(ChatFormatting.GRAY + info.a() + ": " + ChatFormatting.YELLOW + info.b()));
            }
        }
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}
