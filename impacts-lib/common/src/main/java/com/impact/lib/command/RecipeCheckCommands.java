package com.impact.lib.command;

import com.impact.lib.audit.RecipeAudit;
import com.impact.lib.audit.RecipeChecks;
import com.impact.lib.audit.RecipeConflict;
import com.impact.lib.lang.LibTranslations;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.util.List;

/**
 * {@code /impacts audit} (which Impact mod replaced or turned off each vanilla recipe right now) and
 * {@code /impacts conflicts} (the conflict checker on the live recipe set).
 */
final class RecipeCheckCommands {
    private static final String AUDIT_LITERAL = "audit";
    private static final String CONFLICTS_LITERAL = "conflicts";
    private static final int LISTED_ENTRIES = 20;

    private RecipeCheckCommands() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> audit() {
        return Commands.literal(AUDIT_LITERAL).executes(RecipeCheckCommands::runAudit);
    }

    static LiteralArgumentBuilder<CommandSourceStack> conflicts() {
        return Commands.literal(CONFLICTS_LITERAL).executes(RecipeCheckCommands::runConflicts);
    }

    private static int runAudit(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        List<RecipeAudit.Entry> entries = RecipeAudit.audit(server.getResourceManager(), server.getRecipeManager());
        source.sendSuccess(() -> Component.translatable(LibTranslations.AUDIT_SUMMARY, entries.size()), false);
        entries.stream().limit(LISTED_ENTRIES).forEach(entry -> source.sendSuccess(() -> Component.translatable(
                entry.isTurnedOff() ? LibTranslations.AUDIT_TURNED_OFF : LibTranslations.AUDIT_REPLACED,
                entry.recipeId().toString(), String.join(", ", entry.overridingMods())), false));
        return entries.size();
    }

    private static int runConflicts(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        List<RecipeConflict> conflicts = RecipeChecks.runAndLog(source.getServer());
        source.sendSuccess(() -> Component.translatable(LibTranslations.CONFLICTS_SUMMARY, conflicts.size()), false);
        conflicts.stream().limit(LISTED_ENTRIES).forEach(conflict -> source.sendSuccess(() -> Component.translatable(LibTranslations.CONFLICT_ENTRY,
                conflict.first().toString(), conflict.second().toString(), conflict.reason()), false));
        return conflicts.size();
    }
}
