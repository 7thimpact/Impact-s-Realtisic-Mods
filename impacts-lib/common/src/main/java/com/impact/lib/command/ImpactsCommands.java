package com.impact.lib.command;

import com.impact.lib.config.LibCommonConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/** The {@code /impacts} command. Each subcommand lives in its own class; Impact mods add more through Lib. */
public final class ImpactsCommands {
    public static final String ROOT_LITERAL = "impacts";

    private ImpactsCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(ROOT_LITERAL)
                .requires(ImpactsCommands::hasPermission)
                .then(DebugCommand.build())
                .then(ConfigCommand.build())
                .then(DumpCommand.build())
                .then(RecipeCheckCommands.audit())
                .then(RecipeCheckCommands.conflicts()));
    }

    private static boolean hasPermission(CommandSourceStack source) {
        return source.hasPermission(LibCommonConfig.get().commands.permissionLevel.get());
    }
}
