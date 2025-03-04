package net.tiffit.rotcg.rna;

import net.minecraft.world.entity.player.Player;
import net.tiffit.realmnetapi.api.IPlayerPosTracker;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.Rotcg;

public class McPlayerPosTracker implements IPlayerPosTracker {

    private final Player player;

    public McPlayerPosTracker(Player player){
        this.player = player;
    }

    @Override
    public Vec2f getPos() {
        return new Vec2f((float)player.getX(), (float)player.getZ());
    }

    @Override
    public void setPos(Vec2f vec2f) {
        Rotcg.LOGGER.info("Setting player position: " + vec2f);
        player.teleportToWithTicket(vec2f.x(), 65.1f, vec2f.y());
    }
}
