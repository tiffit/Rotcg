package net.tiffit.rotcg.render.effect;

import net.tiffit.realmnetapi.util.math.Vec2f;

public abstract class RotMGEffect {

    protected int targetObjectId, color;
    protected Vec2f start, end;
    public double duration;
    public long createTime;

    public RotMGEffect(int targetObjectId, Vec2f start, Vec2f end, int color, double duration){
        this.targetObjectId = targetObjectId;
        this.start = start;
        this.end = end;
        this.color = color;
        this.duration = duration;
        createTime = System.currentTimeMillis();
    }

    public abstract void onCreate();

    public abstract void onTick();

    public abstract void onDestroy();

    public enum VisualEffect {
        UNKNOWN_EFFECT_TYPE(0),
        HEAL_EFFECT_TYPE(1),
        TELEPORT_EFFECT_TYPE(2),
        STREAM_EFFECT_TYPE(3),
        THROW_EFFECT_TYPE(4, ThrownEffect.class),
        NOVA_EFFECT_TYPE(5),
        POISON_EFFECT_TYPE(6),
        LINE_EFFECT_TYPE(7),
        BURST_EFFECT_TYPE(8),
        FLOW_EFFECT_TYPE(9),
        RING_EFFECT_TYPE(10),
        LIGHTNING_EFFECT_TYPE(11),
        COLLAPSE_EFFECT_TYPE(12),
        CONEBLAST_EFFECT_TYPE(13),
        JITTER_EFFECT_TYPE(14),
        FLASH_EFFECT_TYPE(15),
        THROW_PROJECTILE_EFFECT_TYPE(16),
        SHOCKER_EFFECT_TYPE(17),
        SHOCKEE_EFFECT_TYPE(18),
        RISING_FURY_EFFECT_TYPE(19),
        NOVA_NO_AOE_EFFECT_TYPE(20);

        public final int id;
        public final Class<? extends RotMGEffect> effectClass;

        VisualEffect(int id){
            this(id, null);
        }

        VisualEffect(int id, Class<? extends RotMGEffect> effectClass){
            this.id = id;
            this.effectClass = effectClass;
        }

        public static VisualEffect byId(int id){
            for(VisualEffect effect : values()){
                if(effect.id == id)return effect;
            }
            return UNKNOWN_EFFECT_TYPE;
        }
    }
}
