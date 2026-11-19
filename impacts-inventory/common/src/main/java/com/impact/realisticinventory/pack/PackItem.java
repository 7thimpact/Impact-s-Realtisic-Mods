package com.impact.realisticinventory.pack;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * A satchel, backpack or frame pack. Its contents live in its own NBT ({@code Items}, like a shulker box), so it keeps
 * everything when dropped, moved or placed. It dyes like leather armor and stacks to 1.
 */
public final class PackItem extends Item implements DyeableLeatherItem {
    private static final int MAXIMUM_STACK_SIZE = 1;

    private final PackKind kind;

    public PackItem(PackKind kind) {
        super(new Item.Properties().stacksTo(MAXIMUM_STACK_SIZE));
        this.kind = kind;
    }

    public PackKind kind() {
        return kind;
    }

    /** Sneak-right-click on a block sets the pack down there (when placed packs are on). */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        return PlacedPacks.placeFromHand(context);
    }

    /** Right-click puts the pack on, swapping with whatever is worn. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return PackWearing.wearFromHand(level, player, hand);
    }
}
