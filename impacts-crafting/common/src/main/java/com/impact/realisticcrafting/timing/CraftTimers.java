package com.impact.realisticcrafting.timing;

import com.impact.lib.crafting.CraftingProgress;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.advancement.CraftingAchievements;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.menu.CraftingStation;
import com.impact.realisticcrafting.registry.CraftingSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Optional;

/**
 * Timed crafting in every grid. When a grid's result changes, its timer starts over for the new recipe; the output
 * stays locked (and shows faded) until the timer finishes. Shift-clicking the output queues repeated crafts, each taken
 * when its timer finishes, until the inputs run out, the output changes, or the player clicks anything else.
 */
public final class CraftTimers {
    private static final int RESULT_SLOT = 0;
    private static final float FINISH_VOLUME = 0.3F;
    private static final float FINISH_PITCH = 1.4F;
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);

    private CraftTimers() {
    }

    public static Optional<CraftTimer> timerOf(AbstractContainerMenu menu) {
        return menu instanceof CraftTimerHolder holder ? Optional.of(holder.impactscrafting$craftTimer()) : Optional.empty();
    }

    /** Server, whenever a grid's contents change: the craft in progress is lost. */
    public static void onGridChanged(AbstractContainerMenu menu) {
        timerOf(menu).ifPresent(CraftTimer::clear);
    }

    /** Server, when a recipe fills the output: its timer starts, unless this grid isn't timed. */
    public static void onRecipeAccepted(AbstractContainerMenu menu, Recipe<?> recipe, Player player) {
        Optional<CraftTimer> timer = timerOf(menu);
        CraftingStation station = CraftingStation.of(menu);
        if (timer.isEmpty() || !CraftTimes.isTimed(station)) {
            return;
        }
        int ticks = CraftTimes.ticksFor(recipe);
        timer.get().start(recipe.getId(), ticks);
        LOG.debug(CraftingLogCategories.TIMER, "craft started", "player", player.getScoreboardName(), "grid", station.serializedName,
                "recipe", recipe.getId(), "depth", CraftDepthTable.recipeDepth(recipe).stream().boxed().findFirst().map(String::valueOf).orElse("fallback"),
                "ticks", ticks);
    }

    /** Server, every tick: the open grid's craft runs; a queued repeat takes each finished craft. */
    public static void tick(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        Optional<CraftTimer> timer = timerOf(menu);
        if (timer.isEmpty()) {
            return;
        }
        if (!hasOutput(menu)) {
            timer.get().clear();
            timer.get().setRepeatQueued(false);
            return;
        }
        boolean hasFinished = timer.get().advance();
        if (!hasFinished) {
            return;
        }
        LOG.debug(CraftingLogCategories.TIMER, "craft finished", "player", player.getScoreboardName(), "recipe", timer.get().recipeId(),
                "ticks", timer.get().totalTicks());
        CraftingAchievements.onCraftFinished(player, timer.get().totalTicks());
        player.playNotifySound(CraftingSounds.craftFinish(), SoundSource.PLAYERS, FINISH_VOLUME, FINISH_PITCH);
        if (timer.get().isRepeatQueued()) {
            takeQueuedCraft(player, menu, timer.get());
        }
    }

    /** Whether the output may be taken: not while its craft runs. Both sides read the synced timer. */
    public static boolean isOutputLocked(Player player, Slot slot) {
        AbstractContainerMenu menu = player.containerMenu;
        return menu.slots.size() > RESULT_SLOT && menu.getSlot(RESULT_SLOT) == slot && timerOf(menu).map(CraftTimer::isRunning).orElse(false);
    }

    public static boolean isOutputLocked(AbstractContainerMenu menu) {
        return timerOf(menu).map(CraftTimer::isRunning).orElse(false);
    }

    /** Before a click is handled: shift-clicking the output queues repeats; any other slot click cancels them. */
    public static void onClicked(AbstractContainerMenu menu, int slotIndex, ClickType clickType) {
        Optional<CraftTimer> timer = timerOf(menu);
        if (timer.isEmpty() || slotIndex < 0) {
            return;
        }
        timer.get().setRepeatQueued(slotIndex == RESULT_SLOT && clickType == ClickType.QUICK_MOVE);
    }

    /** Lib's crafting progress, read by every screen's arrow: the craft that runs (or just finished) in the menu. */
    public static Optional<CraftingProgress.Progress> progressOf(AbstractContainerMenu menu) {
        return timerOf(menu)
                .filter(CraftTimer::hasCraft)
                .filter(timer -> hasOutput(menu))
                .map(timer -> new CraftingProgress.Progress(timer.elapsedTicks(), timer.totalTicks()));
    }

    private static void takeQueuedCraft(ServerPlayer player, AbstractContainerMenu menu, CraftTimer timer) {
        ItemStack moved = menu.quickMoveStack(player, RESULT_SLOT);
        if (moved.isEmpty()) {
            timer.setRepeatQueued(false);
            LOG.debug(CraftingLogCategories.TIMER, "repeat stopped", "player", player.getScoreboardName(), "reason", "no room or no output");
        }
    }

    private static boolean hasOutput(AbstractContainerMenu menu) {
        return menu.slots.size() > RESULT_SLOT && menu.getSlot(RESULT_SLOT).hasItem();
    }
}
