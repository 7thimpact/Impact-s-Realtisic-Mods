package com.impact.lib.crafting;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.advancement.LibTriggers;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.item.ImpactsItems;
import com.impact.lib.item.UsesDurability;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.platform.LoaderPlatform;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Tool crafting: Lib's {@code impactslib:tool_crafting} recipe type, and the same tool behavior for other mods' special
 * recipes ({@link #damageTools}). A tool is an item with durability (vanilla damage or Lib's uses); it stays in the
 * grid and comes back worn, or breaks with vanilla's break sound.
 */
public final class ToolCrafting {
    private static final float BREAK_VOLUME = 0.8F;
    private static final float BREAK_PITCH_BASE = 0.8F;
    private static final float BREAK_PITCH_SPREAD = 0.4F;
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);
    private static Supplier<ToolCraftingRecipe.Serializer> serializer;

    private ToolCrafting() {
    }

    /** Called once, during Lib's construction. */
    public static void register() {
        serializer = LoaderPlatform.current().registrar(ImpactsLib.MOD_ID)
                .register(Registries.RECIPE_SERIALIZER, "tool_crafting", ToolCraftingRecipe.Serializer::new);
    }

    public static ToolCraftingRecipe.Serializer serializer() {
        return serializer.get();
    }

    /** Whether the stack can act as a tool: it has vanilla durability or Lib's uses. */
    public static boolean canBeTool(ItemStack stack) {
        return stack.isDamageableItem() || UsesDurability.isTracked(stack);
    }

    /** The grid slot each tool sits in, one per tool ingredient, or nothing when one is missing. */
    public static Optional<Set<Integer>> findToolSlots(CraftingContainer grid, List<Ingredient> tools) {
        Set<Integer> claimed = new HashSet<>();
        for (Ingredient tool : tools) {
            Optional<Integer> slot = firstFreeToolSlot(grid, tool, claimed);
            if (slot.isEmpty()) {
                return Optional.empty();
            }
            claimed.add(slot.get());
        }
        return Optional.of(claimed);
    }

    /**
     * For other mods' special recipes: the remaining items after a craft, with every tool the predicate picks damaged
     * (or broken) and everything else left to vanilla's remainders.
     */
    public static NonNullList<ItemStack> damageTools(CraftingContainer grid, Predicate<ItemStack> isTool, int damage) {
        Player player = craftingPlayer().orElse(null);
        NonNullList<ItemStack> remaining = NonNullList.withSize(grid.getContainerSize(), ItemStack.EMPTY);
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            ItemStack stack = grid.getItem(slot);
            boolean isWorn = isTool.test(stack) && canBeTool(stack);
            remaining.set(slot, isWorn ? wearTool(stack, damage, player) : LoaderPlatform.current().craftingRemainder(stack));
        }
        return remaining;
    }

    /** The player taking a craft's result right now, when the loader knows it (Forge sets it around the take). */
    public static Optional<Player> craftingPlayer() {
        return LoaderPlatform.current().craftingPlayer();
    }

    /** The tool after one craft: worn by the damage, or empty when it broke. */
    static ItemStack wearTool(ItemStack tool, int damage, Player player) {
        ItemStack worn = tool.copy();
        boolean hasBroken = UsesDurability.isTracked(worn)
                ? UsesDurability.use(worn, damage)
                : worn.hurt(damage, RandomSource.create(), player instanceof ServerPlayer serverPlayer ? serverPlayer : null);
        if (!hasBroken) {
            return worn;
        }
        if (player != null) {
            float pitch = BREAK_PITCH_BASE + player.level().random.nextFloat() * BREAK_PITCH_SPREAD;
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, BREAK_VOLUME, pitch);
            LOG.debug(LibLogCategories.TOOL_CRAFTING, "tool broke", "player", player.getGameProfile().getName(), "tool", ResourceIds.itemId(tool.getItem()));
        }
        return ItemStack.EMPTY;
    }

    /** Server side, once per craft: byproducts through Give or drop, the {@code impactslib:tool_crafted} trigger, the log line. */
    static void finishCraft(Player player, ToolCraftingRecipe recipe, ItemStack result, List<ItemStack> usedTools) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        List<ItemStack> givenByproducts = new ArrayList<>();
        for (Byproduct byproduct : recipe.byproducts()) {
            if (byproduct.rolls(serverPlayer.getRandom())) {
                ItemStack given = byproduct.stack().copy();
                givenByproducts.add(given.copy());
                ImpactsItems.giveOrDrop(serverPlayer, given);
            }
        }
        LibTriggers.TOOL_CRAFTED.trigger(serverPlayer, result, usedTools, givenByproducts);
        if (LOG.isEnabled(LibLogCategories.TOOL_CRAFTING)) {
            LOG.debug(LibLogCategories.TOOL_CRAFTING, "crafted", "player", serverPlayer.getGameProfile().getName(), "recipe", recipe.getId(),
                    "result", ResourceIds.itemId(result.getItem()), "tools", usedTools.stream().map(tool -> ResourceIds.itemId(tool.getItem()).toString()).toList(),
                    "byproducts", givenByproducts.stream().map(given -> ResourceIds.itemId(given.getItem()).toString()).toList());
        }
    }

    private static Optional<Integer> firstFreeToolSlot(CraftingContainer grid, Ingredient tool, Set<Integer> claimed) {
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            ItemStack stack = grid.getItem(slot);
            if (!claimed.contains(slot) && canBeTool(stack) && tool.test(stack)) {
                return Optional.of(slot);
            }
        }
        return Optional.empty();
    }
}
