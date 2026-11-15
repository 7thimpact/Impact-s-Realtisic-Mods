package com.impact.lib.command;

import com.impact.lib.ImpactsLib;
import com.impact.lib.lang.LibTranslations;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/** The {@code <mod>} argument: a mod ID such as {@code impactsinventory}, or its short name such as {@code inventory}. */
public final class ModArgument {
    public static final String ALL_MODS = "all";
    static final String ARGUMENT_NAME = "mod";
    private static final String NO_MODS = "(none)";
    private static final String LIST_SEPARATOR = ", ";

    private ModArgument() {
    }

    static RequiredArgumentBuilder<CommandSourceStack, String> argument(Supplier<Collection<String>> knownModIds, boolean isAllAllowed) {
        return Commands.argument(ARGUMENT_NAME, StringArgumentType.word())
                .suggests((context, suggestions) -> SharedSuggestionProvider.suggest(
                        suggestionsFor(knownModIds.get(), isAllAllowed), suggestions));
    }

    static String typedName(CommandContext<CommandSourceStack> context) {
        return StringArgumentType.getString(context, ARGUMENT_NAME);
    }

    /** The mod ID that was meant: the full ID, or {@code impacts} + the short name. */
    public static Optional<String> resolve(String typedName, Collection<String> knownModIds) {
        if (knownModIds.contains(typedName)) {
            return Optional.of(typedName);
        }
        String fullModId = ImpactsLib.MOD_ID_PREFIX + typedName;
        return knownModIds.contains(fullModId) ? Optional.of(fullModId) : Optional.empty();
    }

    static void sendUnknownMod(CommandSourceStack source, String typedName, Collection<String> knownModIds) {
        String knownModList = knownModIds.isEmpty() ? NO_MODS : String.join(LIST_SEPARATOR, knownModIds);
        source.sendFailure(Component.translatable(LibTranslations.UNKNOWN_MOD, typedName, knownModList));
    }

    private static List<String> suggestionsFor(Collection<String> knownModIds, boolean isAllAllowed) {
        List<String> suggestions = new ArrayList<>(knownModIds);
        if (isAllAllowed) {
            suggestions.add(ALL_MODS);
        }
        return suggestions;
    }
}
