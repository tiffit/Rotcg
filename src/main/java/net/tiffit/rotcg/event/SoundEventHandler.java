package net.tiffit.rotcg.event;

import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.tiffit.realmnetapi.api.event.EnemyHitEvent;
import net.tiffit.realmnetapi.api.event.PlayerShootEvent;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.map.projectile.ProjectileState;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.util.RotCGResourceLocation;
import net.tiffit.rotcg.util.TickExecutor;

public class SoundEventHandler {

    public static final RandomSource src = RandomSource.create();

    public static void handleEnemyHit(EnemyHitEvent enemyHitEvent){
        if(enemyHitEvent.proj().getProjectileState().team == ProjectileState.ProjectileTeam.ALLY)return;
        GameObject go = enemyHitEvent.enemyState().getGameObject();
        String sound = enemyHitEvent.kill() ? go.deathSound : go.hitSound;
        if(!Strings.isNullOrEmpty(sound)){
            ResourceLocation rl = new RotCGResourceLocation(sound.replaceAll("/", "."));
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
        String sound = go.sound;
        if(!Strings.isNullOrEmpty(sound)){
            ResourceLocation rl = new RotCGResourceLocation(sound.replaceAll("/", "."));
            TickExecutor.addClient(() -> {
                Minecraft mc = Minecraft.getInstance();
                mc.getSoundManager().play(new SimpleSoundInstance(rl, SoundSource.PLAYERS, 1, 1, src,
                        false, 0, SoundInstance.Attenuation.LINEAR, mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
            });
        }
    }

}
