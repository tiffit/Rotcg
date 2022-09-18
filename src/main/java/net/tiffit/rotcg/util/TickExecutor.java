package net.tiffit.rotcg.util;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tiffit.realmnetapi.net.RealmNetworker;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.render.effect.RotMGEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

@Mod.EventBusSubscriber
public class TickExecutor {

    private static ArrayList<RotMGEffect> TRACKED_EFFECTS = new ArrayList<>();

    private static LinkedBlockingQueue<Runnable> TASKS = new LinkedBlockingQueue<>();
    private static LinkedBlockingQueue<Runnable> TASKS_UPDATE = new LinkedBlockingQueue<>();
    private static LinkedBlockingQueue<Runnable> TASKS_RENDER = new LinkedBlockingQueue<>();
    private static LinkedBlockingQueue<Runnable> TASKS_CLIENT = new LinkedBlockingQueue<>();

    public static void add(Runnable runnable){
        synchronized (TASKS){
            TASKS.add(runnable);
        }
    }

    public static void addRender(Runnable runnable){
        synchronized (TASKS_RENDER){
            TASKS_RENDER.add(runnable);
        }
    }

    public static void addUpdate(Runnable runnable){
        synchronized (TASKS_UPDATE){
            TASKS_UPDATE.add(runnable);
        }
    }

    public static void addClient(Runnable runnable){
        synchronized (TASKS_UPDATE){
            TASKS_UPDATE.add(runnable);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(final TickEvent.LevelTickEvent.ServerTickEvent e) {
        RealmNetworker net = Rotcg.ACTIVE_CONNECTION;
        if(net == null || !net.connected){
            return;
        }
        if(e.phase == TickEvent.Phase.START){
            ArrayList<Runnable> tasks = new ArrayList<>();
            TASKS.drainTo(tasks);
            for(Runnable run : tasks)run.run();
        }
    }

    @SubscribeEvent
    public static void onRenderTick(final TickEvent.RenderTickEvent e) {
        if(e.phase == TickEvent.Phase.START){
//            if(net.updater != null){
//                net.updater.run();
//            }

            ArrayList<Runnable> tasks = new ArrayList<>();
            TASKS_RENDER.drainTo(tasks);
            for(Runnable run : tasks)run.run();

            List<RotMGEffect> removals = new ArrayList<>();
            for(RotMGEffect effect : TRACKED_EFFECTS){
                effect.onTick();
                if(effect.duration * 1000 + effect.createTime <= System.currentTimeMillis()){
                    effect.onDestroy();
                    removals.add(effect);
                }
            }
            TRACKED_EFFECTS.removeAll(removals);
        }
    }

    @SubscribeEvent
    public static void onClientTick(final TickEvent.ClientTickEvent e) {
        RealmNetworker net = Rotcg.ACTIVE_CONNECTION;
        if(net == null)return;
        if(e.phase == TickEvent.Phase.START){
//            if(net.updater != null){
//                net.updater.run();
//            }

            ArrayList<Runnable> tasks = new ArrayList<>();
            TASKS_CLIENT.drainTo(tasks);
            for(Runnable run : tasks)run.run();
        }
    }

    public static void trackEffect(RotMGEffect effect) {
        addRender(() -> {
            if(effect.duration > 0)TRACKED_EFFECTS.add(effect);
            effect.onCreate();
        });
    }

    public static void onUpdateLoop() {
        ArrayList<Runnable> tasks = new ArrayList<>();
        TASKS_UPDATE.drainTo(tasks);
        for(Runnable run : tasks)run.run();
    }

    public static void clear() {
        TRACKED_EFFECTS.clear();
        TASKS_RENDER.clear();
        TASKS.clear();
        TASKS_UPDATE.clear();
    }

}
