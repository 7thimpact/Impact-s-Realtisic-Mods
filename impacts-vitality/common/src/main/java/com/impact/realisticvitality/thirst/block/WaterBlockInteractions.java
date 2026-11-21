package com.impact.realisticvitality.thirst.block;

import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.thirst.BottledWater;
import com.impact.realisticvitality.thirst.ContainerContents;
import com.impact.realisticvitality.thirst.DrinkValues;
import com.impact.realisticvitality.thirst.WaterContainer;
import com.impact.realisticvitality.thirst.WaterContainers;
import com.impact.realisticvitality.thirst.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Optional;

/** Moving water between a held item and a block's tank: taking it out into bottles and containers, and pouring it in. */
public final class WaterBlockInteractions {
    private WaterBlockInteractions() {
    }

    /** Whether the item takes water out of a block or pours water into one: glass bottles, water bottles, water buckets and containers. */
    public static boolean isWaterHandling(ItemStack held) {
        return held.is(Items.GLASS_BOTTLE) || held.is(Items.WATER_BUCKET) || DrinkValues.isVanillaWaterBottle(held)
                || VitalityItems.bottledWater(held).isPresent() || WaterContainers.containerOf(held).isPresent();
    }

    /** Fills the held glass bottle or container from the tank. Returns the drinks taken (0 when the held item can't take any). */
    public static int takeInto(Level level, BlockPos position, Player player, InteractionHand hand, WaterTank tank) {
        ItemStack held = player.getItemInHand(hand);
        if (tank.isEmpty()) {
            return 0;
        }
        if (held.is(Items.GLASS_BOTTLE)) {
            ItemStack bottle = VitalityItems.bottle(BottledWater.filledWith(tank.purity()));
            player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, bottle));
            tank.take(1);
            playSound(level, position, SoundEvents.BOTTLE_FILL);
            return 1;
        }
        Optional<WaterContainer> container = WaterContainers.containerOf(held);
        if (container.isEmpty()) {
            return 0;
        }
        ContainerContents contents = ContainerContents.read(held);
        int taken = Math.min(tank.drinks(), WaterContainers.capacity(container.get()) - contents.drinks());
        Optional<ContainerContents> filled = contents.withWater(taken, tank.purity(), false, container.get().keepsTemperature);
        if (taken <= 0 || filled.isEmpty()) {
            return 0;
        }
        filled.get().writeTo(held);
        tank.take(taken);
        playSound(level, position, SoundEvents.BOTTLE_FILL);
        return taken;
    }

    /**
     * Pours the held water into the tank: a water bucket (4 drinks, unclean), a bottle of water (1 drink) or a
     * container's water. Returns the drinks poured (0 when there's no room or nothing to pour).
     */
    public static int pourFrom(Level level, BlockPos position, Player player, InteractionHand hand, WaterTank tank, int capacity) {
        ItemStack held = player.getItemInHand(hand);
        int room = capacity - tank.drinks();
        int bucketDrinks = VitalityServerConfig.get().waterBlocks.bucketDrinks.get();
        if (held.is(Items.WATER_BUCKET) && room >= bucketDrinks) {
            tank.add(bucketDrinks, WaterPurity.UNCLEAN);
            replaceHeld(player, hand, held, new ItemStack(Items.BUCKET));
            playSound(level, position, SoundEvents.BUCKET_EMPTY);
            return bucketDrinks;
        }
        Optional<WaterPurity> bottlePurity = VitalityItems.bottledWater(held).map(water -> water.purity)
                .or(() -> DrinkValues.isVanillaWaterBottle(held) ? Optional.of(WaterPurity.UNCLEAN) : Optional.empty());
        if (bottlePurity.isPresent() && room >= 1) {
            tank.add(1, bottlePurity.get());
            replaceHeld(player, hand, held, new ItemStack(Items.GLASS_BOTTLE));
            playSound(level, position, SoundEvents.BOTTLE_EMPTY);
            return 1;
        }
        return pourContainer(level, position, held, tank, room);
    }

    private static int pourContainer(Level level, BlockPos position, ItemStack held, WaterTank tank, int room) {
        ContainerContents contents = ContainerContents.read(held);
        boolean holdsWater = WaterContainers.containerOf(held).isPresent() && !contents.isEmpty() && contents.kind() != ContainerContents.Kind.SOUP;
        int poured = Math.min(room, contents.drinks());
        if (!holdsWater || poured <= 0) {
            return 0;
        }
        tank.add(poured, contents.purity());
        contents.minusDrinks(poured).writeTo(held);
        playSound(level, position, SoundEvents.BOTTLE_EMPTY);
        return poured;
    }

    // As with a vanilla bucket or bottle: creative players keep what they hold.
    private static void replaceHeld(Player player, InteractionHand hand, ItemStack held, ItemStack emptied) {
        player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, emptied));
    }

    private static void playSound(Level level, BlockPos position, SoundEvent sound) {
        level.playSound(null, position, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
