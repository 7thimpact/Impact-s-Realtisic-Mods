package com.impact.lib.command;

import com.impact.lib.dump.DumpSection;
import com.impact.lib.dump.ImpactsDump;
import com.impact.lib.lang.LibTranslations;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** {@code /impacts dump player <name>}: every Impact mod's state for one player, for bug reports. */
final class DumpCommand {
    private static final String LITERAL = "dump";
    private static final String PLAYER_LITERAL = "player";
    private static final String TARGET_ARGUMENT = "target";

    private DumpCommand() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(LITERAL)
                .then(Commands.literal(PLAYER_LITERAL)
                        .then(Commands.argument(TARGET_ARGUMENT, EntityArgument.player())
                                .executes(DumpCommand::dumpPlayer)));
    }

    private static int dumpPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, TARGET_ARGUMENT);
        CommandSourceStack source = context.getSource();
        String playerName = player.getGameProfile().getName();
        source.sendSuccess(() -> Component.translatable(LibTranslations.DUMP_HEADER, playerName), false);
        ImpactsDump.describePlayer(player).forEach(section -> sendSection(source, section));
        return 1;
    }

    private static void sendSection(CommandSourceStack source, DumpSection section) {
        source.sendSuccess(() -> Component.translatable(LibTranslations.DUMP_SECTION, section.name()), false);
        section.entries().forEach((key, value) ->
                source.sendSuccess(() -> Component.translatable(LibTranslations.DUMP_ENTRY, key, String.valueOf(value)), false));
    }
}
