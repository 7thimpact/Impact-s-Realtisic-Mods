package com.impact.realisticinventory.pack;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Putting packs on and taking them off, and noticing every change of what's worn (however it happened). */
public final class PackWearing {
    private static final float EQUIP_VOLUME = 1.0F;
    private static final float EQUIP_PITCH = 1.0F;
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private PackWearing() {
    }

    /** Right-click with a pack: it goes on the back, and whatever was worn comes to the hand. */
    public static InteractionResultHolder<ItemStack> wearFromHand(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!BackSlotStorage.isEnabled() || !WornPack.arePacksEnabled()) {
            return InteractionResultHolder.pass(held);
        }
        if (level.isClientSide()) {
            return InteractionResultHolder.success(held);
        }
        ItemStack previouslyWorn = BackSlotStorage.get(player);
        BackSlotStorage.set(player, held.copyAndClear());
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS,
                EQUIP_VOLUME, EQUIP_PITCH);
        return InteractionResultHolder.consume(previouslyWorn);
    }

    /** Called by the back-slot sync when the worn item's kind changed: logs packs put on and taken off. */
    public static void onWornItemChanged(ServerPlayer player, ItemStack previouslyWorn, ItemStack nowWorn) {
        if (ItemStack.isSameItem(previouslyWorn, nowWorn)) {
            return;
        }
        String playerName = player.getGameProfile().getName();
        if (PackRules.isPack(previouslyWorn)) {
            LOG.debug(InventoryLogCategories.PACKS, "pack taken off", "player", playerName, "item", ResourceIds.itemId(previouslyWorn.getItem()),
                    "stacks", PackContents.stacksIn(previouslyWorn).size());
        }
        if (PackRules.isPack(nowWorn)) {
            LOG.debug(InventoryLogCategories.PACKS, "pack worn", "player", playerName, "item", ResourceIds.itemId(nowWorn.getItem()),
                    "stacks", PackContents.stacksIn(nowWorn).size());
        }
    }
}
