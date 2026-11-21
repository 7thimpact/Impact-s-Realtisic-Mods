package com.impact.lib.hold;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.advancement.LibTriggers;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.log.ImpactsLog;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hold-to-use actions, built on vanilla's item-use hold so that letting go, switching hands, eating and dying all end
 * them the vanilla way. An item opts in by registering its action and delegating its use methods here:
 * {@code use} to {@link #start}, {@code onUseTick} to {@link #tick}, {@code releaseUsing} to {@link #release},
 * {@code getUseDuration} to {@link #USE_DURATION_TICKS} and {@code getUseAnimation} to {@link #USE_ANIMATION}. The
 * animation is never vanilla's BLOCK pose, which looks like (and other mods read as) shield blocking.
 */
public final class HoldToUse {
    /** Long enough to hold indefinitely, like a bow or a spyglass. */
    public static final int USE_DURATION_TICKS = 72000;
    public static final UseAnim USE_ANIMATION = UseAnim.NONE;
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);
    private static final Map<Item, HoldAction> ACTIONS_BY_ITEM = new ConcurrentHashMap<>();
    // Separate maps per side: in single player the client and server players share a UUID.
    private static final Map<UUID, Integer> CLIENT_PROGRESS = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> SERVER_PROGRESS = new ConcurrentHashMap<>();

    private HoldToUse() {
    }

    public static void register(Item item, HoldAction action) {
        ACTIONS_BY_ITEM.put(item, action);
        LOG.debug(LibLogCategories.HOLD_TO_USE, "action registered", "item", ResourceIds.itemId(item), "action", action.id());
    }

    public static Optional<HoldAction> actionFor(ItemStack stack) {
        return Optional.ofNullable(ACTIONS_BY_ITEM.get(stack.getItem()));
    }

    /** From the item's {@code use}: starts holding when the action can run, and passes otherwise. */
    public static InteractionResultHolder<ItemStack> start(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        Optional<HoldAction> action = actionFor(held);
        if (action.isEmpty() || !action.get().canContinue(player, held)) {
            return InteractionResultHolder.pass(held);
        }
        progressMap(level).put(player.getUUID(), 0);
        player.startUsingItem(hand);
        if (!level.isClientSide()) {
            LOG.debug(LibLogCategories.HOLD_TO_USE, "started", "player", player.getGameProfile().getName(), "action", action.get().id());
        }
        return InteractionResultHolder.consume(held);
    }

    /** From the item's {@code onUseTick}: advances the cycle and finishes it on the server. */
    public static void tick(Level level, LivingEntity user, ItemStack held) {
        Optional<HoldAction> action = actionFor(held);
        if (!(user instanceof Player player) || action.isEmpty()) {
            return;
        }
        if (!action.get().canContinue(player, held)) {
            player.releaseUsingItem();
            return;
        }
        int progress = progressMap(level).merge(player.getUUID(), 1, Integer::sum);
        if (progress < action.get().cycleTicks(player, held)) {
            return;
        }
        progressMap(level).put(player.getUUID(), 0);
        if (player instanceof ServerPlayer serverPlayer) {
            finishCycle(serverPlayer, held, action.get());
        }
    }

    /** From the item's {@code releaseUsing}: the cycle in progress is lost, nothing else. */
    public static void release(Level level, LivingEntity user) {
        Integer progress = progressMap(level).remove(user.getUUID());
        if (!level.isClientSide() && progress != null && progress > 0 && user instanceof Player player) {
            LOG.debug(LibLogCategories.HOLD_TO_USE, "cancelled", "player", player.getGameProfile().getName(), "ticksDone", progress);
        }
    }

    /** Client side: how far the local player's current cycle is, from 0 to 1, or nothing when not holding. */
    public static Optional<Float> clientProgress(Player player) {
        ItemStack held = player.getUseItem();
        Integer progress = CLIENT_PROGRESS.get(player.getUUID());
        Optional<HoldAction> action = actionFor(held);
        if (!player.isUsingItem() || progress == null || action.isEmpty()) {
            return Optional.empty();
        }
        int cycleTicks = Math.max(1, action.get().cycleTicks(player, held));
        return Optional.of(Math.min(1.0F, (float) progress / cycleTicks));
    }

    private static void finishCycle(ServerPlayer player, ItemStack held, HoldAction action) {
        boolean isContinuing = action.completeCycle(player, held);
        player.swing(player.getUsedItemHand(), true);
        LibTriggers.HOLD_ACTION.trigger(player, action.id());
        if (!isContinuing) {
            player.stopUsingItem();
        }
    }

    private static Map<UUID, Integer> progressMap(Level level) {
        return level.isClientSide() ? CLIENT_PROGRESS : SERVER_PROGRESS;
    }
}
