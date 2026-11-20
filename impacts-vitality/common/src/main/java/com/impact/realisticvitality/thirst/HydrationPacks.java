package com.impact.realisticvitality.thirst;

import com.impact.lib.backslot.BackSlot;
import com.impact.realisticvitality.item.HydrationPackItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Sipping from a worn Hydration Pack with its key, without holding it. */
public final class HydrationPacks {
    /** As long as drinking from a held container takes. */
    private static final int SIP_COOLDOWN_TICKS = 32;

    private HydrationPacks() {
    }

    /** Drinks one drink from the pack in the back slot, or else the chest slot. Returns whether the player drank. */
    public static boolean sipWorn(ServerPlayer player) {
        boolean isOnBack = BackSlot.get(player).getItem() instanceof HydrationPackItem;
        ItemStack pack = isOnBack ? BackSlot.get(player).copy() : player.getItemBySlot(EquipmentSlot.CHEST);
        boolean isWorn = pack.getItem() instanceof HydrationPackItem;
        if (!isWorn || ContainerContents.read(pack).isEmpty() || player.getCooldowns().isOnCooldown(pack.getItem())) {
            return false;
        }
        Drinking.onUseFinished(player, pack.copy());
        WaterContainers.finishDrink(player, pack);
        if (isOnBack) {
            BackSlot.set(player, pack);
        }
        player.getCooldowns().addCooldown(pack.getItem(), SIP_COOLDOWN_TICKS);
        player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5F, 1.0F);
        return true;
    }
}
