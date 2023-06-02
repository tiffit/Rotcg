package net.tiffit.rotcg.registry.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidType;
import net.tiffit.realmnetapi.assets.ConditionEffect;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.map.object.StatType;
import net.tiffit.realmnetapi.net.RealmNetworker;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.Rotcg;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class RotcgEntity extends LivingEntity{

    private RObject reference;
    public RAnimationManager animationManager;
    private static final EntityDataAccessor<Integer> OBJECT_ID = SynchedEntityData.defineId(RotcgEntity.class, EntityDataSerializers.INT);
    private static List<ItemStack> armorSlots = new ArrayList<>(0);

    public RotcgEntity(EntityType<? extends RotcgEntity> type, Level level) {
        super(type, level);
    }

    public void initialize(RObject object) {
        reference = object;
        entityData.set(OBJECT_ID, object.getState().objectId);
    }

    public RObject getReference() {
        if(reference == null){
            RealmNetworker net = Rotcg.ACTIVE_CONNECTION;
            int id = getObjectId();
            reference = net.map.getEntityList().get(id);
            if(reference != null){
                animationManager = new RAnimationManager(reference);
            }
        }
        return reference;
    }

    public int getObjectId(){
        return entityData.get(OBJECT_ID);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OBJECT_ID, 0);
    }

    @Override
    public void tick() {
        setNoGravity(true);
        setInvulnerable(true);
        noPhysics = true;
        RObject ref = getReference();
        if(ref == null){
            if(Rotcg.ACTIVE_CONNECTION.map.getEntityList().isRemoved(getObjectId())){
                remove(RemovalReason.DISCARDED);
            }
            return;
        }
        if(ref.isDead()){
            remove(RemovalReason.DISCARDED);
            return;
        }
        teleportTo(ref.getCorrectedX(), getY(), ref.getCorrectedY());
        if(level.isClientSide() && animationManager != null){
            animationManager.update();
        }
        super.tick();
    }

    public void move(Vec2f pos){
        Vec2f diff = new Vec2f((float) getX(), (float) getZ()).sub(pos);
        if(diff.distanceSqr(Vec2f.ZERO) < 0.1)return;
        setYHeadRot((float) Math.toDegrees(Math.atan2(diff.x(), -diff.y())));
        setYBodyRot(yHeadRot);
        setYRot(yHeadRot);
        moveTo(pos.x(), 65, pos.y());
    }

    @Override
    public boolean attackable() {
        return false;
    }

    @Override
    public boolean hasCustomName() {
        return renderHealth();
    }

    @Nullable
    @Override
    public Component getCustomName() {
        if(!renderHealth())return Component.literal("no name");
        GameObjectState state = getReference().getState();
        return Component.literal(ChatFormatting.RED.toString() + state.getStat(StatType.HP));
    }

    public boolean renderHealth(){
        if(getReference() == null)return false;
        GameObjectState state = getReference().getState();
        return state.getGameObject() != null && !state.getGameObject().invincible && state.getGameObject().enemy && state.hasStat(StatType.HP) && !state.hasEffect(ConditionEffect.INVINCIBLE);
    }

    @Override
    public Component getDisplayName() {
        return getCustomName();
    }

    @Override
    public boolean isCustomNameVisible() {
        return hasCustomName();
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
        if(getReference() == null)return false;
        GameObjectState state = getReference().getState();
        GameObject go = state.getGameObject();
        return go != null && go.occupySquare;
    }

    @Override
    protected AABB makeBoundingBox() {
        double width = 0.5;
        return new AABB(this.getX() - width, this.getY(), this.getZ() - width, this.getX() + width, this.getY() + 1, this.getZ() + width);
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
