package com.impact.realisticvitality.command;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.player.ThirstState;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.temperature.BodyState;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * {@code /impacts vitality}: shows a player's thirst, Hydration and temperatures, and sets them for testing:
 * {@code show <player>} and {@code set <players> thirst|hydration|body <value>}.
 */
public final class VitalityCommand {
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);
    private static final String TARGETS = "targets";
    private static final String VALUE = "value";

    private VitalityCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("vitality")
                .then(Commands.literal("show").then(Commands.argument(TARGETS, EntityArgument.player()).executes(VitalityCommand::show)))
                .then(Commands.literal("set").then(Commands.argument(TARGETS, EntityArgument.players())
                        .then(setter("thirst", 0, ThirstState.FULL, VitalityLogCategories.THIRST, (state, value) -> state.thirst.setLevel(Math.round(value))))
                        .then(setter("hydration", 0, ThirstState.FULL, VitalityLogCategories.THIRST, (state, value) -> state.thirst.setHydration(value)))
                        .then(setter("body", -100, 100, VitalityLogCategories.TEMPERATURE, (state, value) -> state.temperature.setBody(value)))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setter(String name, float minimum, float maximum, String logCategory,
                                                                     BiConsumer<VitalityState, Float> apply) {
        return Commands.literal(name).then(Commands.argument(VALUE, FloatArgumentType.floatArg(minimum, maximum))
                .executes(context -> set(context, name, logCategory, apply)));
    }

    private static int set(CommandContext<CommandSourceStack> context, String name, String logCategory, BiConsumer<VitalityState, Float> apply)
            throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, TARGETS);
        float value = FloatArgumentType.getFloat(context, VALUE);
        for (ServerPlayer player : players) {
            apply.accept(VitalityState.of(player), value);
            LOG.debug(logCategory, "value set by command", "player", player.getScoreboardName(), "value", name, "to", value);
        }
        context.getSource().sendSuccess(() -> Component.literal("Set " + name + " to " + value + " for " + players.size() + " player(s)"), true);
        return players.size();
    }

    private static int show(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, TARGETS);
        VitalityState state = VitalityState.of(player);
        String report = String.format("%s: thirst %d, hydration %.1f, body %.1f (%s), ambient %.1f°F, wet %d ticks",
                player.getScoreboardName(), state.thirst.level(), state.thirst.hydration(), state.temperature.body(),
                BodyState.of(state.temperature.body()).serializedName, state.temperature.ambient(), state.temperature.wetTicks());
        context.getSource().sendSuccess(() -> Component.literal(report), false);
        return 1;
    }
}
