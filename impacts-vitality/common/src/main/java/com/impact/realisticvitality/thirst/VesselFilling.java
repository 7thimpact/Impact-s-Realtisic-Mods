package com.impact.realisticvitality.thirst;

import com.impact.lib.water.WaterVessels;
import com.impact.realisticvitality.registry.VitalityItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Filling a bottle or a container from another mod's water vessel through Lib (Food's campfire bucket): one bottle's
 * worth at a time, purified while Lib says the vessel boils, otherwise unclean.
 */
public final class VesselFilling {
    private VesselFilling() {
    }

    /** A click on a block with a glass bottle or container. PASS unless the block is a vessel that can give water now. */
    public static InteractionResult tryFill(Level level, BlockPos position, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean takesWater = held.is(Items.GLASS_BOTTLE) || WaterContainers.containerOf(held).isPresent();
        if (!ThirstDrain.isOn() || !takesWater || !WaterVessels.canFillBottle(level, position)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        WaterPurity purity = WaterSources.cauldronPurity(level, position);
        Optional<ItemStack> filled = filledVersion(held, purity);
        if (filled.isEmpty() || !WaterVessels.takeBottle(level, position)) {
            return InteractionResult.PASS;
        }
        player.setItemInHand(hand, held.is(Items.GLASS_BOTTLE) ? ItemUtils.createFilledResult(held, player, filled.get()) : filled.get());
        level.playSound(null, position, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        BottleFilling.logFilled(player, purity, "vessel");
        return InteractionResult.CONSUME;
    }

    // A glass bottle becomes a bottle of the vessel's water; a container gains one drink, if it has room and holds water.
    private static Optional<ItemStack> filledVersion(ItemStack held, WaterPurity purity) {
        if (held.is(Items.GLASS_BOTTLE)) {
            return Optional.of(VitalityItems.bottle(BottledWater.filledWith(purity)));
        }
        WaterContainer container = WaterContainers.containerOf(held).orElseThrow();
        ContainerContents contents = ContainerContents.read(held);
        if (contents.drinks() >= WaterContainers.capacity(container)) {
            return Optional.empty();
        }
        return contents.withWater(1, purity, false, container.keepsTemperature).map(more -> {
            ItemStack filled = held.copy();
            more.writeTo(filled);
            return filled;
        });
    }
}
