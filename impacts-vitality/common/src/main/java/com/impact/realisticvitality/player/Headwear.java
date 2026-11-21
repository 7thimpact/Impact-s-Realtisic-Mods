package com.impact.realisticvitality.player;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * What a player wears on the head: the helmet slot first, then Curios's head slot when Curios is installed (the
 * loader module registers it). Vitality's masks and the Straw Hat work from either.
 */
public final class Headwear {
    private static volatile ExtraHeadSlots extraSlots = player -> List.of();

    private Headwear() {
    }

    /** Called once by the loader module when Curios is installed. */
    public static void useExtraSlots(ExtraHeadSlots slots) {
        extraSlots = slots;
    }

    /** Every non-empty item worn on the head, the helmet slot first. The stacks are the worn ones, so changes to them stick. */
    public static List<ItemStack> worn(Player player) {
        List<ItemStack> worn = new ArrayList<>();
        ItemStack helmetSlot = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!helmetSlot.isEmpty()) {
            worn.add(helmetSlot);
        }
        worn.addAll(wornInExtraSlots(player));
        return worn;
    }

    /** Only what's worn in the head slots beyond the helmet slot (Curios's), non-empty. */
    public static List<ItemStack> wornInExtraSlots(Player player) {
        return extraSlots.wornOnHead(player).stream().filter(stack -> !stack.isEmpty()).toList();
    }

    /** The first worn head item the test accepts. */
    public static Optional<ItemStack> find(Player player, Predicate<ItemStack> test) {
        return worn(player).stream().filter(test).findFirst();
    }

    /** Head slots beyond the helmet slot. */
    @FunctionalInterface
    public interface ExtraHeadSlots {
        List<ItemStack> wornOnHead(Player player);
    }
}
