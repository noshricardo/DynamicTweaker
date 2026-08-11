package com.noshricardo.dynamictweaker.command;

import com.mojang.brigadier.CommandDispatcher;
import com.noshricardo.dynamictweaker.network.EditorPayload;
import com.noshricardo.dynamictweaker.network.RecipePayload;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class RecipeEditorCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("recipeeditor")
                .requires(source -> source.hasPermission(4))
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerPlayer player = source.getPlayerOrException();



                    player.connection.send(EditorPayload.INSTANCE);

                    return 1;
                })
        );
    }


}
