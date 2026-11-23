package com.impact.realisticinventory.pack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A pack (see {@link PackKind}). Its contents live in its own NBT ({@code Items}, like a shulker box), so it keeps
 * everything when dropped, moved or placed; the Ender Pack holds nothing and opens its wearer's Ender Chest. Every pack
 * dyes like leather armor and stacks to 1.
 */
public final class PackItem extends Item implements DyeableLeatherItem {
    private static final int MAXIMUM_STACK_SIZE = 1;
    // Where vanilla's DyeableLeatherItem keeps the dye.
    private static final String DISPLAY_KEY = "display";
    private static final String COLOR_KEY = "color";

    private final PackKind kind;

    public PackItem(PackKind kind) {
        super(propertiesFor(kind));
        this.kind = kind;
    }

    // The Netherite Pack survives fire and lava, like netherite gear: dropped in lava on death, it can be fished out.
    private static Item.Properties propertiesFor(PackKind kind) {
        Item.Properties properties = new Item.Properties().stacksTo(MAXIMUM_STACK_SIZE);
        return kind.isFireproof() ? properties.fireResistant() : properties;
    }

    public PackKind kind() {
        return kind;
    }

    /** The dye color, or this kind's own default color while undyed. */
    @Override
    public int getColor(ItemStack stack) {
        CompoundTag display = stack.getTagElement(DISPLAY_KEY);
        return display != null && display.contains(COLOR_KEY, Tag.TAG_ANY_NUMERIC) ? display.getInt(COLOR_KEY) : kind.defaultColor();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        PackTooltip.append(stack, kind, lines);
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
