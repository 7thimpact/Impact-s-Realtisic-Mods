package com.impact.realisticcrafting.recipe;

import com.impact.lib.ResourceIds;
import com.impact.lib.hold.HoldToUse;
import com.impact.lib.item.ImpactsItems;
import com.impact.lib.item.UsesDurability;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.CraftingFeature;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.registry.CraftingSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.Optional;

/**
 * Finishing loose items in hand: the main hand holds the loose item, the off hand a hammer, and holding right-click
 * runs Lib's hold-to-use action for the recipe's time. When it finishes, the loose item becomes its result and the
 * hammer wears. Letting go or switching slots loses all progress.
 */
public final class Hammering {
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);
    private static final HammeringAction ACTION = new HammeringAction();
    private static final float FINISH_VOLUME = 0.4F;
    private static final float FINISH_PITCH = 0.6F;
    private static final ResourceLocation IRON_HAMMER_ID = ResourceIds.of(CraftingMod.MOD_ID, "iron_hammer");

    private Hammering() {
    }

    public static HammeringAction action() {
        return ACTION;
    }

    /** The recipe for the loose item in the main hand and the hammer in the off hand, when hammering is on. */
    public static Optional<HammeringRecipe> recipeFor(Level level, ItemStack loose, ItemStack hammer) {
        if (!CraftingFeature.HAMMERING.isOn() || loose.isEmpty() || hammer.isEmpty()) {
            return Optional.empty();
        }
        return level.getRecipeManager().getAllRecipesFor(CraftingRecipeTypes.hammering()).stream()
                .filter(recipe -> recipe.matches(loose, hammer))
                .findFirst();
    }

    /** The hold's length: the recipe's own time, or the hammer's from the config (the Iron Hammer is quicker). */
    public static int ticksFor(HammeringRecipe recipe, ItemStack hammer) {
        CraftingServerConfig.HammeringSettings settings = CraftingServerConfig.get().hammering;
        int baseTicks = recipe.useTicks() == HammeringRecipe.HAMMER_DECIDES_TICKS ? settings.hammerUseTicks.get() : recipe.useTicks();
        if (!ResourceIds.itemId(hammer.getItem()).equals(IRON_HAMMER_ID)) {
            return baseTicks;
        }
        return Math.max(1, Math.round((float) baseTicks * settings.ironHammerUseTicks.get() / settings.hammerUseTicks.get()));
    }

    /** Server, when the hold finishes: the loose item becomes the result and the hammer wears. */
    public static void finish(ServerPlayer player, ItemStack loose) {
        ItemStack hammer = player.getOffhandItem();
        Optional<HammeringRecipe> recipe = recipeFor(player.level(), loose, hammer);
        if (recipe.isEmpty()) {
            return;
        }
        ItemStack result = recipe.get().assemble(null, player.level().registryAccess());
        player.setItemInHand(InteractionHand.MAIN_HAND, result);
        String hammerId = ResourceIds.itemId(hammer.getItem()).toString();
        int hammerUsesLeft = wearHammer(player, recipe.get().hammerDamage());
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), CraftingSounds.hammerFinish(), SoundSource.PLAYERS,
                FINISH_VOLUME, FINISH_PITCH);
        LOG.debug(CraftingLogCategories.HAMMER, "finished", "player", player.getScoreboardName(), "item", ResourceIds.itemId(loose.getItem()),
                "result", ResourceIds.itemId(result.getItem()), "hammer", hammerId, "usesLeft", hammerUsesLeft,
                "ticks", ticksFor(recipe.get(), hammer), "pos", player.blockPosition().toShortString());
    }

    /** Lets other mods' loose items use the hammering action: every recipe input gets it, once recipes load (both sides). */
    public static void registerHoldActions(RecipeManager recipes) {
        recipes.getAllRecipesFor(CraftingRecipeTypes.hammering()).stream()
                .flatMap(recipe -> Arrays.stream(recipe.input().getItems()))
                .map(ItemStack::getItem)
                .distinct()
                .filter(item -> HoldToUse.actionFor(new ItemStack(item)).isEmpty())
                .forEach(Hammering::registerHoldAction);
    }

    public static void registerHoldAction(Item looseItem) {
        HoldToUse.register(looseItem, ACTION);
    }

    // One hammer of a stack wears, like a tool in a grid; the rest stays in the off hand.
    private static int wearHammer(Player player, int damage) {
        ItemStack hammer = player.getOffhandItem();
        if (UsesDurability.isTracked(hammer)) {
            ItemStack used = hammer.getCount() > 1 ? hammer.split(1) : hammer;
            boolean isWornOut = UsesDurability.use(used, damage);
            if (isWornOut) {
                used.shrink(1);
                player.playSound(SoundEvents.ITEM_BREAK);
            } else if (used != hammer) {
                ImpactsItems.giveOrDrop(player, used);
            }
            return isWornOut ? 0 : UsesDurability.get(used);
        }
        if (hammer.isDamageableItem()) {
            hammer.hurtAndBreak(damage, player, brokenBy -> brokenBy.broadcastBreakEvent(InteractionHand.OFF_HAND));
            return hammer.isEmpty() ? 0 : hammer.getMaxDamage() - hammer.getDamageValue();
        }
        return 0;
    }
}
