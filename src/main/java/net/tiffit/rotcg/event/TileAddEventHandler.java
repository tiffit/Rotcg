package net.tiffit.rotcg.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.tiffit.realmnetapi.api.event.TileAddEvent;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.registry.ModRegistry;
import net.tiffit.rotcg.render.hud.map.Minimap;

public class TileAddEventHandler {

    public static void handle(TileAddEvent tileAddEvent, Level level) {
        if(Rotcg.MAP == null){
            Rotcg.MAP = new Minimap(Rotcg.ACTIVE_CONNECTION.map);
        }
        Rotcg.MAP.setTiles(tileAddEvent.newTiles());
        tileAddEvent.newTiles().forEach((vec2i, ground) ->
                level.setBlock(new BlockPos(vec2i.x(), 64, vec2i.y()), ModRegistry.R_GROUNDS.get(ground.type).get().defaultBlockState(),11));
    }
}
