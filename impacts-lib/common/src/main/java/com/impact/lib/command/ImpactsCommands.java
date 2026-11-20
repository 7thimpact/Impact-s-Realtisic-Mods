package com.impact.lib.command;

import com.impact.lib.config.LibCommonConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/** The {@code /impacts} command. Each subcommand lives in its own class; Impact mods add more through Lib. */
public final class ImpactsCommands {
    public static final String ROOT_LITERAL = "impacts";
    private static final List<Supplier<LiteralArgumentBuilder<CommandSourceStack>>> MOD_SUBCOMMANDS = new CopyOnWriteArrayList<>();

    private ImpactsCommands() {
    }

    /** Adds a mod's own subcommand under {@code /impacts}, for example {@code /impacts vitality}. Call during mod construction. */
    public static void addSubcommand(Supplier<LiteralArgumentBuilder<CommandSourceStack>> subcommand) {
        MOD_SUBCOMMANDS.add(subcommand);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(ROOT_LITERAL)
                .requires(ImpactsCommands::hasPermission)
                .then(DebugCommand.build())
                .then(ConfigCommand.build())
                .then(DumpCommand.build())
                .then(RecipeCheckCommands.audit())
                .then(RecipeCheckCommands.conflicts());
        MOD_SUBCOMMANDS.forEach(subcommand -> root.then(subcommand.get()));
        dispatcher.register(root);
    }

    private static boolean hasPermission(CommandSourceStack source) {
        return source.hasPermission(LibCommonConfig.get().commands.permissionLevel.get());
    }
}
