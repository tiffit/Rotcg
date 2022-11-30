package net.tiffit.rotcg.cmd;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandRegistry {

    @SubscribeEvent
    public static void registerCommandEvent(RegisterCommandsEvent e) {
        RObjectStatsCommand.register(e.getDispatcher());
    }
}
