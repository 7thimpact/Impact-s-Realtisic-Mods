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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
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
    private static final ToolCraftingOverrides NO_OVERRIDES = new ToolCraftingOverrides() {
    };
    private static final Map<String, ToolCraftingOverrides> OVERRIDES_BY_NAMESPACE = new ConcurrentHashMap<>();
    private static final List<Consumer<CompletedToolCraft>> CRAFTED_LISTENERS = new CopyOnWriteArrayList<>();
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

    /** Config-driven yields and byproduct chances for every tool-crafting recipe whose ID is in the namespace. */
    public static void registerOverrides(String recipeNamespace, ToolCraftingOverrides overrides) {
        OVERRIDES_BY_NAMESPACE.put(recipeNamespace, overrides);
    }

    /** The overrides registered for the recipe's namespace, or ones that keep the recipe's own values. */
    public static ToolCraftingOverrides overridesFor(ToolCraftingRecipe recipe) {
        return OVERRIDES_BY_NAMESPACE.getOrDefault(recipe.getId().getNamespace(), NO_OVERRIDES);
    }

    /** Runs the listener on the server after every tool craft, once its byproducts are given. */
    public static void onCrafted(Consumer<CompletedToolCraft> listener) {
        CRAFTED_LISTENERS.add(listener);
    }

    /** Whether the stack wears as a tool: it has vanilla durability or Lib's uses. Any other tool is used up instead. */
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

    /**
     * One tool after one craft: worn by the damage, or empty when it broke. A tool without wear (flint as a trimming
     * tool) is used up. Only one item of a stacked tool is used; the rest stays in the grid.
     */
    static ItemStack wearTool(ItemStack tool, int damage, Player player) {
        ItemStack worn = tool.copyWithCount(1);
        if (!canBeTool(worn)) {
            return ItemStack.EMPTY;
        }
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

    /** Server side, once per craft: byproducts through Give or drop, the {@code impactslib:tool_crafted} trigger, the log line, the listeners. */
    static void finishCraft(Player player, ToolCraftingRecipe recipe, CraftingContainer grid, ItemStack result, List<ItemStack> toolsBefore,
                            List<ItemStack> toolsAfter) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ToolCraftingOverrides overrides = overridesFor(recipe);
        List<ItemStack> givenByproducts = new ArrayList<>();
        for (Byproduct byproduct : recipe.byproducts()) {
            Byproduct configured = byproduct.withChance(overrides.byproductChance(recipe, byproduct));
            int count = overrides.byproductCount(recipe, byproduct, grid);
            if (count > 0 && configured.rolls(serverPlayer.getRandom())) {
                ItemStack given = configured.stack().copyWithCount(count);
                givenByproducts.add(given.copy());
                ImpactsItems.giveOrDrop(serverPlayer, given);
            }
        }
        LibTriggers.TOOL_CRAFTED.trigger(serverPlayer, result, toolsBefore, givenByproducts);
        if (LOG.isEnabled(LibLogCategories.TOOL_CRAFTING)) {
            LOG.debug(LibLogCategories.TOOL_CRAFTING, "crafted", "player", serverPlayer.getGameProfile().getName(), "recipe", recipe.getId(),
                    "result", ResourceIds.itemId(result.getItem()), "tools", toolsBefore.stream().map(tool -> ResourceIds.itemId(tool.getItem()).toString()).toList(),
                    "byproducts", givenByproducts.stream().map(given -> ResourceIds.itemId(given.getItem()).toString()).toList());
        }
        CompletedToolCraft craft = new CompletedToolCraft(serverPlayer, recipe, result, List.copyOf(toolsBefore), List.copyOf(toolsAfter), List.copyOf(givenByproducts));
        CRAFTED_LISTENERS.forEach(listener -> listener.accept(craft));
    }

    private static Optional<Integer> firstFreeToolSlot(CraftingContainer grid, Ingredient tool, Set<Integer> claimed) {
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            ItemStack stack = grid.getItem(slot);
            if (!claimed.contains(slot) && !stack.isEmpty() && tool.test(stack)) {
                return Optional.of(slot);
            }
        }
        return Optional.empty();
    }
}
