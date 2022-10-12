package net.tiffit.rotcg.registry;

import net.minecraft.world.entity.LivingEntity;
import net.tiffit.realmnetapi.api.IObjectListener;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.Rotcg;

public abstract class McEntityContainer<R, E extends LivingEntity> implements IObjectListener<R> {

    private static final int APPEAR_DISTANCE_SQR = 15 * 15;
    private static final int DISAPPEAR_DISTANCE_SQR = 17 * 17;

    protected final R obj;
    protected E entity;

    public McEntityContainer(R obj) {
        this.obj = obj;
    }

    @Override
    public void updateLoop() {
        Vec2f currentPos = getPosition();
        float distanceSqr = currentPos.distanceSqr(new Vec2f((float) Rotcg.SERVER_PLAYER.getX(), (float)Rotcg.SERVER_PLAYER.getZ()));
        if(entity == null){
            if(distanceSqr < APPEAR_DISTANCE_SQR){
                spawnEntity();
            }
        }else{
            if(distanceSqr > DISAPPEAR_DISTANCE_SQR){
                removeEntity();
            }else{
                teleport(currentPos);
            }
        }
    }

    protected abstract void spawnEntity();

    protected abstract void removeEntity();

    protected abstract Vec2f getPosition();

    protected abstract void teleport(Vec2f pos);

    @Override
    public void objectKill() {
        if(entity != null){
            removeEntity();
        }
    }

    public E getEntity(){
        return entity;
    }
}
