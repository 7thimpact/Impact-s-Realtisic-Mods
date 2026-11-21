package com.impact.realisticfood.item;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.debug.FoodLogCategories;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A pebble heated on a lit campfire, for stone boiling (only obtainable with Impact's Realistic Crafting). It keeps the
 * pebble it was heated from, uses and all, and turns back into it after cooling for a while in a player's inventory or
 * once it has heated a stone-boiling pot.
 */
public final class HotPebbleItem extends Item {
    public static final String COOLING_TOOLTIP_KEY = "tooltip." + FoodMod.MOD_ID + ".hot_pebble.cooling";
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    private static final ResourceLocation CRAFTING_STONE_PEBBLE = ResourceIds.of(FoodMod.CRAFTING_MOD_ID, "stone_pebble");
    private static final String PEBBLE_KEY = "Pebble";
    private static final String COOL_TICKS_LEFT_KEY = "CoolTicksLeft";
    private static final int TICKS_PER_SECOND = 20;
    private static final int FULL_BAR_WIDTH = 13;
    private static final int GLOW_BAR_COLOR = 0xFF7A1F;

    public HotPebbleItem() {
        super(new Item.Properties().stacksTo(1));
    }

    /** A Hot Pebble heated from one of the pebbles in the stack, starting its full cool-down. */
    public static ItemStack heatedFrom(Item hotPebble, ItemStack pebble) {
        ItemStack heated = new ItemStack(hotPebble);
        CompoundTag tag = heated.getOrCreateTag();
        tag.put(PEBBLE_KEY, pebble.copyWithCount(1).save(new CompoundTag()));
        tag.putInt(COOL_TICKS_LEFT_KEY, FoodServerConfig.get().stoneBoiling.pebbleCoolDownTicks.get());
        return heated;
    }

    /**
     * The pebble it was heated from. A Hot Pebble that never was one (a command's) cools into Crafting's Stone Pebble,
     * or into nothing when Crafting isn't installed; callers keep the Hot Pebble then.
     */
    public static ItemStack cooled(ItemStack hotPebble) {
        CompoundTag tag = hotPebble.getTag();
        if (tag != null && tag.contains(PEBBLE_KEY)) {
            return ItemStack.of(tag.getCompound(PEBBLE_KEY));
        }
        return ResourceIds.item(CRAFTING_STONE_PEBBLE).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    /** Ticks until it cools; a Hot Pebble without a timer (a command's) has its whole cool-down ahead. */
    public static int coolTicksLeft(ItemStack hotPebble) {
        CompoundTag tag = hotPebble.getTag();
        boolean hasTimer = tag != null && tag.contains(COOL_TICKS_LEFT_KEY);
        return hasTimer ? tag.getInt(COOL_TICKS_LEFT_KEY) : FoodServerConfig.get().stoneBoiling.pebbleCoolDownTicks.get();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        int ticksLeft = coolTicksLeft(stack) - 1;
        if (ticksLeft > 0) {
            stack.getOrCreateTag().putInt(COOL_TICKS_LEFT_KEY, ticksLeft);
            return;
        }
        coolDownInInventory(player.getInventory(), stack);
    }

    // The tick only names a slot within one of the inventory's lists, so the stack is found among all of them by identity.
    private static void coolDownInInventory(Inventory inventory, ItemStack hotPebble) {
        ItemStack pebble = cooled(hotPebble);
        if (pebble.isEmpty()) {
            return;
        }
        for (int index = 0; index < inventory.getContainerSize(); index++) {
            if (inventory.getItem(index) == hotPebble) {
                inventory.setItem(index, pebble);
                LOG.debug(FoodLogCategories.STONE_BOILING, "pebble cooled", "player", inventory.player.getScoreboardName(),
                        "pebble", ResourceIds.itemId(pebble.getItem()));
                return;
            }
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    // The bar is the heat left, emptying as the pebble cools.
    @Override
    public int getBarWidth(ItemStack stack) {
        float heatLeft = (float) coolTicksLeft(stack) / FoodServerConfig.get().stoneBoiling.pebbleCoolDownTicks.get();
        return Math.round(FULL_BAR_WIDTH * Math.min(1.0F, heatLeft));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return GLOW_BAR_COLOR;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        int secondsLeft = (coolTicksLeft(stack) + TICKS_PER_SECOND - 1) / TICKS_PER_SECOND;
        lines.add(Component.translatable(COOLING_TOOLTIP_KEY, secondsLeft).withStyle(ChatFormatting.GOLD));
    }
}
