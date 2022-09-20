package net.tiffit.rotcg.registry.entity;

import net.minecraft.world.entity.Entity;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.registry.McEntityContainer;
import net.tiffit.rotcg.util.ObjectEntityTypeMapping;
import net.tiffit.rotcg.util.TickExecutor;

public class RotcgEntityContainer extends McEntityContainer<RObject, RotcgEntity> {

    public RotcgEntityContainer(RObject obj) {
        super(obj);
    }

    protected void spawnEntity(){
        if(entity != null)return;
        entity = ObjectEntityTypeMapping.MAP.get(obj.getGameObject().goClass).get().create(Rotcg.SERVER_PLAYER.getLevel());
        GameObjectState state = obj.getState();
        entity.initialize(obj);
        entity.teleportTo(state.position.x(), 65, state.position.y());
        TickExecutor.add(() -> {
            if(entity != null){
                Rotcg.SERVER_PLAYER.getLevel().addFreshEntity(entity);
            }
        });
    }

    protected void removeEntity(){
        RotcgEntity instance = entity;
        TickExecutor.add(() -> {
            if(instance != null){
                instance.remove(Entity.RemovalReason.DISCARDED);
            }
        });
        entity = null;
    }

    @Override
    protected Vec2f getPosition() {
        return obj.getCurrentPos();
    }

    @Override
    protected void teleport(Vec2f pos) {
        entity.move(pos);
    }
}
