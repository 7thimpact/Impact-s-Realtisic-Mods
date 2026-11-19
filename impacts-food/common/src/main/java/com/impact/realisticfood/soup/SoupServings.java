package com.impact.realisticfood.soup;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.boiling.BoilingPot;
import com.impact.realisticfood.boiling.PotHost;
import com.impact.realisticfood.debug.FoodLogCategories;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;

import java.util.Optional;

/**
 * Serving a ready soup: into any bowl, or eaten straight from the pot with an empty hand. A serving eaten from the pot
 * counts exactly like a bowl: the soup's own finishing (food, effects, stats and vanilla's consume trigger), then the
 * loader's finished-using event, so other mods (Vitality's thirst and warmth) see it.
 */
public final class SoupServings {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    private static final float LADLE_VOLUME = 0.6F;

    private SoupServings() {
    }

    /** Fills the bowl in the player's hand with a serving. */
    public static InteractionResult ladle(ServerPlayer player, InteractionHand hand, PotHost host, BoilingPot pot) {
        Optional<ItemStack> serving = pot.takeServing(host);
        if (serving.isEmpty()) {
            return InteractionResult.PASS;
        }
        // Handing the serving over empties the stack, so the log line reads a copy.
        ItemStack bowled = serving.get().copy();
        ItemStack bowls = player.getItemInHand(hand);
        player.setItemInHand(hand, ItemUtils.createFilledResult(bowls, player, serving.get()));
        host.level().playSound(null, host.position(), SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, LADLE_VOLUME, 1.0F);
        logServing("serving bowled", player, bowled, pot);
        return InteractionResult.CONSUME;
    }

    /** Eats a serving straight from the pot, when the player could eat it from a bowl. */
    // Forge prefers a stack-aware food lookup; the vanilla one gives the same values for soups.
    @SuppressWarnings("deprecation")
    public static InteractionResult eatFromPot(ServerPlayer player, PotHost host, BoilingPot pot) {
        Optional<ItemStack> nextServing = pot.soup().map(SoupBatch::serving);
        FoodProperties food = nextServing.map(serving -> serving.getItem().getFoodProperties()).orElse(null);
        if (food == null || !player.canEat(food.canAlwaysEat())) {
            return InteractionResult.PASS;
        }
        ItemStack serving = pot.takeServing(host).orElseThrow();
        ItemStack eaten = serving.copy();
        ItemStack leftover = serving.finishUsingItem(host.level(), player);
        LoaderPlatform.current().postItemUseFinished(player, eaten, eaten.getUseDuration(), leftover);
        logServing("serving eaten", player, eaten, pot);
        return InteractionResult.CONSUME;
    }

    private static void logServing(String event, ServerPlayer player, ItemStack serving, BoilingPot pot) {
        LOG.debug(FoodLogCategories.SOUP, event, "player", player.getGameProfile().getName(), "soup", ResourceIds.itemId(serving.getItem()),
                "servingsLeft", pot.soup().map(SoupBatch::servingsLeft).orElse(0));
    }
}
