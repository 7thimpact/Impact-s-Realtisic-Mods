package com.impact.lib.audit;

import com.impact.lib.ImpactsLib;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.log.ImpactsLog;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs the recipe audit and the conflict checker on the live recipe set: at every server start and data pack reload,
 * and from {@code /impacts audit} and {@code /impacts conflicts}. Each conflict is a warning, so any conflict fails the
 * family test setups.
 */
public final class RecipeChecks {
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);

    private RecipeChecks() {
    }

    public static List<RecipeConflict> runAndLog(MinecraftServer server) {
        List<RecipeAudit.Entry> audit = RecipeAudit.audit(server.getResourceManager(), server.getRecipeManager());
        List<RecipeConflict> conflicts = new ArrayList<>(RecipeAudit.overriddenByTwoMods(audit));
        conflicts.addAll(RecipeConflictChecker.findGridConflicts(server.getRecipeManager()));
        long turnedOff = audit.stream().filter(RecipeAudit.Entry::isTurnedOff).count();
        LOG.debug(LibLogCategories.AUDIT, "vanilla recipes checked", "replaced", audit.size() - turnedOff, "turnedOff", turnedOff);
        audit.forEach(entry -> LOG.verbose(LibLogCategories.AUDIT, entry.isTurnedOff() ? "turned off" : "replaced",
                "recipe", entry.recipeId(), "by", entry.overridingMods()));
        conflicts.forEach(conflict -> LOG.warn(LibLogCategories.CONFLICTS, "recipe conflict", "first", conflict.first(),
                "second", conflict.second(), "reason", conflict.reason()));
        LOG.debug(LibLogCategories.CONFLICTS, "conflict check finished", "conflicts", conflicts.size());
        return conflicts;
    }
}
