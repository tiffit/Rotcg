package net.tiffit.rotcg.cmd;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.tiffit.realmnetapi.map.object.GameObjectState;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.map.object.RotMGEntityList;
import net.tiffit.realmnetapi.map.object.StatType;
import net.tiffit.rotcg.Rotcg;

public class RObjectStatsCommand {

    private static Component FAILURE = Component.literal("No object found with ID!");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("robjectstats")
                        .executes(context -> execute(context.getSource(), null))
                .then(Commands.argument("object_id", StringArgumentType.word())
                        .executes(context -> execute(context.getSource(), StringArgumentType.getString(context, "object_id")))));
    }

    private static int execute(CommandSourceStack src, String strId) throws CommandSyntaxException {
        GameObjectState resultState;
        if(strId != null) {
            RotMGEntityList list = Rotcg.ACTIVE_CONNECTION.map.getEntityList();
            int intId = -1;
            try {
                intId = Integer.parseInt(strId);
            } catch (NumberFormatException ignored) {
            }
            int finalIntId = intId;
            RObject obj = list.get(object -> {
                GameObjectState state = object.getState();
                if (state.hasStat(StatType.NAME) && state.getStat(StatType.NAME).equals(strId)) {
                    return true;
                }
                return state.objectId == finalIntId;
            });
            resultState = obj == null ? null : obj.getState();
        }else{
            resultState = Rotcg.ACTIVE_CONNECTION.map.getSelfState();
        }

        if(resultState == null){
            src.sendFailure(FAILURE);
            return 0;
        }

        src.sendSystemMessage(Component.literal(ChatFormatting.GOLD + "Stats:"));
        for(StatType type : StatType.values()){
            if(resultState.hasStat(type)){
                String val = type.stringType ? resultState.getStat(type) : (resultState.<Integer>getStat(type) + "");
                src.sendSystemMessage(Component.literal(ChatFormatting.YELLOW.toString() + type + ": " + ChatFormatting.GRAY + val));
            }
        }
        src.sendSuccess(Component.literal(ChatFormatting.GOLD + "-------"), false);
        return Command.SINGLE_SUCCESS;
    }
}
