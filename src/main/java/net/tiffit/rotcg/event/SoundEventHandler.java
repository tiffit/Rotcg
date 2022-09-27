package net.tiffit.rotcg.event;

import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.tiffit.realmnetapi.api.event.DamageEvent;
import net.tiffit.realmnetapi.api.event.EnemyHitEvent;
import net.tiffit.realmnetapi.api.event.PlayerShootEvent;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.map.RMap;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.map.projectile.ProjectileState;
import net.tiffit.realmnetapi.net.packet.in.DamagePacketIn;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.util.RotCGResourceLocation;
import net.tiffit.rotcg.util.TickExecutor;

public class SoundEventHandler {

    public static final RandomSource src = RandomSource.create();

    public static void handleEnemyHit(EnemyHitEvent enemyHitEvent){
        if(enemyHitEvent.proj().getProjectileState().team == ProjectileState.ProjectileTeam.ALLY)return;
        if(enemyHitEvent.damage() <= 3)return;
        GameObject go = enemyHitEvent.enemyState().getGameObject();
        String sound = enemyHitEvent.kill() ? go.deathSound : go.hitSound;
        if(!Strings.isNullOrEmpty(sound)){
            ResourceLocation rl = new RotCGResourceLocation(sound.toLowerCase().replaceAll("/", "."));
            Vec2f position = enemyHitEvent.enemyState().position;
            TickExecutor.addClient(() -> {
                Minecraft mc = Minecraft.getInstance();
                SoundSource cat = go.enemy ? SoundSource.HOSTILE : SoundSource.NEUTRAL;
                mc.getSoundManager().play(new SimpleSoundInstance(rl, cat, 1, 1, src,
                        false, 0, SoundInstance.Attenuation.LINEAR, position.x(), 65, position.y(), false));
            });
        }
    }

    public static void handlePlayerShoot(PlayerShootEvent playerShootEvent){
        GameObject go = playerShootEvent.weapon();
        String sound = switch (playerShootEvent.type()) {
            case DAGGER_TYPE, KATANA_TYPE, SWORD_TYPE -> "bladeSwing";
            case WAND_TYPE, STAFF_TYPE -> "magicShoot";
            case BOW_TYPE -> "arrowShoot";
            default -> go.sound;
        };
        if(!Strings.isNullOrEmpty(sound)){
            ResourceLocation rl = new RotCGResourceLocation(sound.toLowerCase().replaceAll("/", "."));
            TickExecutor.addClient(() -> {
                Minecraft mc = Minecraft.getInstance();
                mc.getSoundManager().play(new SimpleSoundInstance(rl, SoundSource.PLAYERS, 0.2f, 1, src,
                        false, 0, SoundInstance.Attenuation.LINEAR, mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
            });
        }
    }

    public static void handleDamage(DamageEvent damageEvent){
        DamagePacketIn packet = damageEvent.packet();
        RMap map = Rotcg.ACTIVE_CONNECTION.map;
        if(packet.objectId == map.getObjectId() && map.getEntityList().has(packet.targetId)){
            if(packet.damageAmount > 0){
                RObject obj = map.getEntityList().get(packet.targetId);
                GameObject go = obj.getGameObject();
                String sound = packet.kill ? go.deathSound : go.hitSound;
                if(!Strings.isNullOrEmpty(sound)){
                    Vec2f position = obj.getState().position;
                    ResourceLocation rl = new RotCGResourceLocation(sound.toLowerCase().replaceAll("/", "."));
                    TickExecutor.addClient(() -> {
                        Minecraft mc = Minecraft.getInstance();
                        mc.getSoundManager().play(new SimpleSoundInstance(rl, SoundSource.HOSTILE, 1, 1, src,
                                false, 0, SoundInstance.Attenuation.LINEAR, position.x(), 65, position.y(), false));
                    });
                }
            }
        }
    }

}
