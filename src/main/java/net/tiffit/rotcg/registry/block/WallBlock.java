package net.tiffit.rotcg.registry.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.tiffit.realmnetapi.assets.xml.GameObject;

public class WallBlock extends Block {

    public final GameObject go;


    public WallBlock(GameObject go) {
        super(Properties.of(Material.STONE));
        this.go = go;
    }

    @Override
    public String getDescriptionId() {
        return go.displayid.isEmpty() ? go.id : go.displayid;
    }
}
