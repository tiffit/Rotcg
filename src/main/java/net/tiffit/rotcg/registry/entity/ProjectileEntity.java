package net.tiffit.rotcg.registry.entity;

import net.minecraft.client.particle.ReversePortalParticle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.RegistryObject;
import net.tiffit.realmnetapi.api.IObjectListener;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.map.object.StatType;
import net.tiffit.realmnetapi.map.projectile.RProjectile;
import net.tiffit.realmnetapi.net.RealmNetworker;
import net.tiffit.rotcg.Rotcg;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ProjectileEntity extends LivingEntity{

    public static RegistryObject<EntityType<ProjectileEntity>> TYPE;

    private RProjectile reference;
    private static final EntityDataAccessor<Integer> PROJECTILE_ID = SynchedEntityData.defineId(ProjectileEntity.class, EntityDataSerializers.INT);
    private static List<ItemStack> armorSlots = new ArrayList<>(0);

    public ProjectileEntity(EntityType<? extends ProjectileEntity> type, Level level) {
        super(type, level);
    }

    public void initialize(RProjectile projectile) {
        reference = projectile;
        entityData.set(PROJECTILE_ID, projectile.getProjectileId());
    }

    public RProjectile getReference() {
        if(reference == null){
            RealmNetworker net = Rotcg.ACTIVE_CONNECTION;
            int id = getProjectileId();
            if(id > -1){
                reference = net.map.getProjectileList().get(id);
            }
        }
        return reference;
    }

    public int getProjectileId(){
        return entityData.get(PROJECTILE_ID);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(PROJECTILE_ID, -1);
    }


    @Override
    public void tick() {
        setNoGravity(true);
        setInvulnerable(true);
        noPhysics = true;
        RProjectile ref = getReference();
        if(ref == null){
            if(Rotcg.ACTIVE_CONNECTION.map.getProjectileList().isRemoved(getProjectileId())){
                remove(RemovalReason.DISCARDED);
            }
            return;
        }
        if(ref.isDead()){
            remove(RemovalReason.DISCARDED);
            return;
        }
        super.tick();
    }

    @Override
    public boolean attackable() {
        return false;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public void push(Entity p_21294_) {
    }

    @Override
    protected void doPush(Entity p_20971_) {
    }

    @Override
    protected void pushEntities() {
    }


    @Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return armorSlots;
    }

    @Override
    public @NotNull ItemStack getItemBySlot(@NotNull EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {}

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

}
