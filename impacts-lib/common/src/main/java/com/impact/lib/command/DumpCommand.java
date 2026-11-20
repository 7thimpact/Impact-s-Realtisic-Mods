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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** {@code /impacts dump player <name>}, {@code dump held} and {@code dump block}: every Impact mod's state, for bug reports. */
final class DumpCommand {
    private static final String LITERAL = "dump";
    private static final String PLAYER_LITERAL = "player";
    private static final String TARGET_ARGUMENT = "target";
    private static final String HELD_LITERAL = "held";
    private static final String BLOCK_LITERAL = "block";
    private static final double REACH_BLOCKS = 6.0;

    private DumpCommand() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(LITERAL)
                .then(Commands.literal(PLAYER_LITERAL)
                        .then(Commands.argument(TARGET_ARGUMENT, EntityArgument.player())
                                .executes(DumpCommand::dumpPlayer)))
                .then(Commands.literal(HELD_LITERAL).executes(DumpCommand::dumpHeld))
                .then(Commands.literal(BLOCK_LITERAL).executes(DumpCommand::dumpBlock));
    }

    private static int dumpHeld(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ItemStack held = player.getMainHandItem();
        source.sendSuccess(() -> Component.translatable(LibTranslations.DUMP_HELD_HEADER, player.getGameProfile().getName()), false);
        ImpactsDump.describeItem(held, player).forEach(section -> sendSection(source, section));
        return 1;
    }

    private static int dumpBlock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        HitResult target = player.pick(REACH_BLOCKS, 1.0F, false);
        if (!(target instanceof BlockHitResult blockTarget) || target.getType() != HitResult.Type.BLOCK) {
            source.sendFailure(Component.translatable(LibTranslations.DUMP_NO_BLOCK));
            return 0;
        }
        BlockPos position = blockTarget.getBlockPos();
        source.sendSuccess(() -> Component.translatable(LibTranslations.DUMP_BLOCK_HEADER, position.toShortString()), false);
        ImpactsDump.describeBlock(player.serverLevel(), position).forEach(section -> sendSection(source, section));
        return 1;
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
