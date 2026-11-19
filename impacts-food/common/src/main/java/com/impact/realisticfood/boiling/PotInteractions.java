package com.impact.realisticfood.boiling;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodFeature;
import com.impact.realisticfood.debug.FoodLogCategories;
import com.impact.realisticfood.registry.FoodSounds;
import com.impact.realisticfood.registry.FoodTags;
import com.impact.realisticfood.soup.SoupRecipes;
import com.impact.realisticfood.soup.SoupServings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Clicks every kind of pot shares: ladling a ready soup into a bowl, eating it with an empty hand, and putting the held
 * item in. Anything else passes, so the next handler (vanilla, another mod) gets the click.
 */
public final class PotInteractions {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    private static final float SPLASH_VOLUME = 0.4F;

    private PotInteractions() {
    }

    /**
     * Serves a ready soup, or puts the held item into the pot. {@code existingPot} is the pot as it is now;
     * {@code potToAddTo} gives the pot to put an item into, starting one when needed.
     */
    public static InteractionResult serveOrAdd(ServerPlayer player, InteractionHand hand, PotHost host, Optional<BoilingPot> existingPot,
                                               Supplier<BoilingPot> potToAddTo) {
        ItemStack held = player.getItemInHand(hand);
        boolean isSoupReady = existingPot.map(BoilingPot::isSoupReady).orElse(false);
        if (isSoupReady && held.is(FoodTags.BOWLS)) {
            return SoupServings.ladle(player, hand, host, existingPot.get());
        }
        if (isSoupReady && held.isEmpty() && hand == InteractionHand.MAIN_HAND && FoodFeature.EATING_FROM_POT.isOn()) {
            return SoupServings.eatFromPot(player, host, existingPot.get());
        }
        return addFromHand(player, held, host, existingPot, potToAddTo);
    }

    /** The client's guess at the server's answer, from the heat below and the server's last update about the pot. */
    public static InteractionResult predictOnClient(Level level, BlockPos position, ItemStack held, InteractionHand hand, PotKind kind,
                                                    boolean isHeated) {
        SoupState soupState = ClientBoilingPots.soupState(level, position);
        boolean isServing = soupState == SoupState.READY && (held.is(FoodTags.BOWLS) || (held.isEmpty() && hand == InteractionHand.MAIN_HAND));
        boolean isHot = isHeated || ClientBoilingPots.isBoiling(level, position);
        boolean takesItem = isHot && (BoilingRecipes.find(level, held, kind).isPresent() || SoupRecipes.isIngredient(level, held, kind));
        return isServing || takesItem ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private static InteractionResult addFromHand(ServerPlayer player, ItemStack held, PotHost host, Optional<BoilingPot> existingPot,
                                                 Supplier<BoilingPot> potToAddTo) {
        if (held.isEmpty()) {
            return InteractionResult.PASS;
        }
        boolean isHot = host.isHeated() || existingPot.map(BoilingPot::isBoiling).orElse(false);
        if (!isHot) {
            logPassedOn(held, host, "cold");
            return InteractionResult.PASS;
        }
        if (!potToAddTo.get().tryAdd(host, held)) {
            logPassedOn(held, host, "no recipe or no room");
            return InteractionResult.PASS;
        }
        if (!player.getAbilities().instabuild) {
            ItemStack remainder = LoaderPlatform.current().craftingRemainder(held);
            held.shrink(1);
            if (!remainder.isEmpty()) {
                player.getInventory().placeItemBackInInventory(remainder);
            }
        }
        host.level().playSound(null, host.position(), FoodSounds.potSplash(), SoundSource.BLOCKS, SPLASH_VOLUME, 1.0F);
        return InteractionResult.CONSUME;
    }

    private static void logPassedOn(ItemStack held, PotHost host, String reason) {
        if (LOG.isVerbose(FoodLogCategories.BOILING)) {
            LOG.debug(FoodLogCategories.BOILING, "passed on", "item", ResourceIds.itemId(held.getItem()), "pot", host.kind().logName,
                    "pos", host.position(), "reason", reason);
        }
    }
}
