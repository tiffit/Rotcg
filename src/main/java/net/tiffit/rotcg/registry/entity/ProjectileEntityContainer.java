package net.tiffit.rotcg.registry.entity;

import net.minecraft.world.entity.Entity;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.map.projectile.ProjectileState;
import net.tiffit.realmnetapi.map.projectile.RProjectile;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.registry.McEntityContainer;
import net.tiffit.rotcg.util.ObjectEntityTypeMapping;
import net.tiffit.rotcg.util.TickExecutor;

public class ProjectileEntityContainer extends McEntityContainer<RProjectile, ProjectileEntity> {

    public ProjectileEntityContainer(RProjectile obj) {
        super(obj);
    }

    protected void spawnEntity(){
        if(entity != null)return;
        ProjectileState state = obj.getProjectileState();
        entity = ProjectileEntity.TYPE.get().create(Rotcg.SERVER_PLAYER.getLevel());
        entity.initialize(obj);
        entity.teleportTo(state.startX, 65, state.startY);
        TickExecutor.add(() -> {
            if(entity != null){
                Rotcg.SERVER_PLAYER.getLevel().addFreshEntity(entity);
            }
        });
    }

    protected void removeEntity(){
        ProjectileEntity instance = entity;
        TickExecutor.add(() -> {
            if(instance != null){
                instance.remove(Entity.RemovalReason.DISCARDED);
            }
        });
        entity = null;
    }

    @Override
    protected Vec2f getPosition() {
        return obj.getPositionAt(obj.getProjectileState());
    }

    @Override
    protected void teleport(Vec2f pos) {
        //entity.teleportTo(pos.x(), 65, pos.y());
    }
}
