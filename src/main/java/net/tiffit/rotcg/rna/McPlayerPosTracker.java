package net.tiffit.rotcg.rna;

import net.minecraft.world.entity.player.Player;
import net.tiffit.realmnetapi.api.IPlayerPosTracker;
import net.tiffit.realmnetapi.util.math.Vec2f;

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
        System.out.println("Setting player position");
        player.teleportToWithTicket(vec2f.x(), 65.1f, vec2f.y());
    }
}
