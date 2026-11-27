package com.impact.realisticinventory.pack;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.advancement.InventoryTriggers;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Locale;

/** Putting packs on and taking them off, and noticing every change of what's worn (however it happened). */
public final class PackWearing {
    private static final float EQUIP_VOLUME = 1.0F;
    private static final float EQUIP_PITCH = 1.0F;
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private PackWearing() {
    }

    /**
     * Right-click with a pack: it goes on the back, or on the chest when sneaking (while the chest slot is usable), and
     * whatever was worn there comes to the hand.
     */
    public static InteractionResultHolder<ItemStack> wearFromHand(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        WornOn place = player.isSecondaryUseActive() && WornSlots.isUsable(player, WornOn.CHEST) ? WornOn.CHEST : WornOn.BACK;
        if (!WornPack.arePacksEnabled() || !WornSlots.isUsable(player, place)) {
            return InteractionResultHolder.pass(held);
        }
        if (level.isClientSide()) {
            return InteractionResultHolder.success(held);
        }
        ItemStack previouslyWorn = WornSlots.get(player, place);
        if (!WornSlots.set(player, place, held.copy())) {
            return InteractionResultHolder.pass(held);
        }
        held.setCount(0);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS,
                EQUIP_VOLUME, EQUIP_PITCH);
        return InteractionResultHolder.consume(previouslyWorn);
    }

    /** Called when a worn slot's item changed (Curios reports it): logs packs put on and taken off. */
    public static void onWornItemChanged(ServerPlayer player, WornOn place, ItemStack previouslyWorn, ItemStack nowWorn) {
        if (ItemStack.isSameItem(previouslyWorn, nowWorn)) {
            return;
        }
        String playerName = player.getGameProfile().getName();
        String placeName = place.name().toLowerCase(Locale.ROOT);
        if (PackRules.isPack(previouslyWorn)) {
            LOG.debug(InventoryLogCategories.PACKS, "pack taken off", "player", playerName, "on", placeName,
                    "item", ResourceIds.itemId(previouslyWorn.getItem()), "stacks", PackContents.stacksIn(previouslyWorn).size());
        }
        if (PackRules.isPack(nowWorn)) {
            LOG.debug(InventoryLogCategories.PACKS, "pack worn", "player", playerName, "on", placeName,
                    "item", ResourceIds.itemId(nowWorn.getItem()), "stacks", PackContents.stacksIn(nowWorn).size());
            InventoryTriggers.PACK_WORN.trigger(player, ((PackItem) nowWorn.getItem()).kind());
        }
    }
}
