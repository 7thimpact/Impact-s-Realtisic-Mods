package com.impact.realisticcrafting.block;

import com.impact.lib.ResourceIds;
import com.impact.lib.item.ImpactsItems;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.CraftingFeature;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.item.WoodenWaterBucketItem;
import com.impact.realisticcrafting.recipe.CraftingRecipeTypes;
import com.impact.realisticcrafting.recipe.DyeMixes;
import com.impact.realisticcrafting.recipe.VatDyeingRecipe;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.registry.CraftingTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * What using the Dye Vat does with each held item. Water buckets fill it (a Water-filled Wooden Bucket comes back
 * empty), dyes color it, and dyeable items are dipped one at a time: holding right-click repeats the use, and each item
 * takes its dip time (10 ticks, 20 for Paper Pulp) and 1 charge. Shift-right-click with an empty hand empties it.
 */
public final class DyeVatInteractions {
    private static final String DISPLAY_KEY = "display";
    private static final String COLOR_KEY = "color";
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);
    // When each player last dipped an item, by game time, so a held right-click dips at the dip pace.
    private static final Map<UUID, Long> LAST_DIP_TICKS = new ConcurrentHashMap<>();

    private DyeVatInteractions() {
    }

    public static InteractionResult use(DyeVatBlockEntity vat, Level level, BlockPos position, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!CraftingFeature.DYE_VAT.isOn()) {
            return InteractionResult.PASS;
        }
        if (held.isEmpty()) {
            return player.isShiftKeyDown() && vat.hasWater() ? emptied(vat, level, position, player) : InteractionResult.PASS;
        }
        if (held.is(CraftingItems.woodenWaterBucket()) || held.is(Items.WATER_BUCKET)) {
            return filled(vat, level, position, player, hand, held);
        }
        if (held.getItem() instanceof DyeItem dye) {
            return dyed(vat, level, position, player, held, dye.getDyeColor());
        }
        return dipped(vat, level, position, player, hand, held);
    }

    /** Lets a test dip again right away instead of waiting out the dip time. */
    public static void forgetLastDip(Player player) {
        LAST_DIP_TICKS.remove(player.getUUID());
    }

    private static InteractionResult emptied(DyeVatBlockEntity vat, Level level, BlockPos position, Player player) {
        if (!level.isClientSide) {
            vat.empty();
            level.playSound(null, position, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.8F, 0.8F);
            LOG.debug(CraftingLogCategories.VAT, "emptied", "player", player.getScoreboardName(), "pos", position.toShortString());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static InteractionResult filled(DyeVatBlockEntity vat, Level level, BlockPos position, Player player, InteractionHand hand,
                                            ItemStack bucket) {
        if (vat.hasWater()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        vat.fillWithWater();
        level.playSound(null, position, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (!player.getAbilities().instabuild) {
            ItemStack empty = bucket.is(Items.WATER_BUCKET) ? new ItemStack(Items.BUCKET) : WoodenWaterBucketItem.emptied(bucket, player, level, position);
            player.setItemInHand(hand, empty);
        }
        LOG.debug(CraftingLogCategories.VAT, "water added", "player", player.getScoreboardName(), "bucket", ResourceIds.itemId(bucket.getItem()),
                "pos", position.toShortString());
        return InteractionResult.CONSUME;
    }

    private static InteractionResult dyed(DyeVatBlockEntity vat, Level level, BlockPos position, Player player, ItemStack dye, DyeColor added) {
        if (!vat.hasWater()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Optional<DyeColor> before = vat.color();
        Optional<DyeColor> mixed = before.flatMap(current -> DyeMixes.mix(current, added));
        if (!vat.addDye(added, mixed)) {
            LOG.debug(CraftingLogCategories.VAT, "dye refused", "player", player.getScoreboardName(), "dye", added.getName(),
                    "vat", before.map(DyeColor::getName).orElse("water"), "charges", vat.charges());
            return InteractionResult.FAIL;
        }
        if (!player.getAbilities().instabuild) {
            dye.shrink(1);
        }
        level.playSound(null, position, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        LOG.debug(CraftingLogCategories.VAT, mixed.isPresent() && before.isPresent() && before.get() != added ? "colors mixed" : "dye added",
                "player", player.getScoreboardName(), "dye", added.getName(), "color", vat.color().map(DyeColor::getName).orElse("water"),
                "charges", vat.charges());
        return InteractionResult.CONSUME;
    }

    private static InteractionResult dipped(DyeVatBlockEntity vat, Level level, BlockPos position, Player player, InteractionHand hand,
                                            ItemStack held) {
        Optional<Dip> dip = vat.hasWater() ? dipFor(vat, level, held) : Optional.empty();
        if (dip.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        long now = level.getGameTime();
        Long lastDip = LAST_DIP_TICKS.get(player.getUUID());
        if (lastDip != null && now - lastDip < dip.get().ticks() && now >= lastDip) {
            return InteractionResult.CONSUME;
        }
        LAST_DIP_TICKS.put(player.getUUID(), now);
        ItemStack result = dip.get().result();
        held.shrink(1);
        if (held.isEmpty()) {
            player.setItemInHand(hand, result);
        } else {
            ImpactsItems.giveOrDrop(player, result);
        }
        vat.spendCharges(dip.get().charges());
        level.playSound(null, position, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.4F, 1.2F);
        LOG.debug(CraftingLogCategories.VAT, "dipped", "player", player.getScoreboardName(), "input", ResourceIds.itemId(dip.get().input()),
                "result", ResourceIds.itemId(result.getItem()), "color", vat.color().map(DyeColor::getName).orElse("water"),
                "chargesLeft", vat.charges());
        return InteractionResult.CONSUME;
    }

    /** What dipping one of the held items gives in this vat, if anything. */
    private static Optional<Dip> dipFor(DyeVatBlockEntity vat, Level level, ItemStack held) {
        CraftingServerConfig.DyeVatSettings settings = CraftingServerConfig.get().dyeVat;
        Optional<DyeColor> color = vat.color();
        String colorKey = color.map(DyeColor::getName).orElse(VatDyeingRecipe.PLAIN_WATER_KEY);
        ItemStack one = held.copyWithCount(1);
        for (VatDyeingRecipe recipe : level.getRecipeManager().getAllRecipesFor(CraftingRecipeTypes.vatDyeing())) {
            Optional<ItemStack> result = recipe.resultFor(one, colorKey);
            if (result.isPresent()) {
                int ticks = color.isEmpty() && one.is(Items.SUGAR_CANE) ? settings.paperPulpTicks.get() : recipe.dipTicks();
                return Optional.of(new Dip(one.getItem(), result.get(), recipe.charges(), ticks));
            }
        }
        return leatherStyleDip(one, color).or(() -> color.isEmpty() ? washedBanner(one) : Optional.empty())
                .map(result -> new Dip(one.getItem(), result, 1, settings.dipTicks.get()));
    }

    // Leather-style items take the color the way vanilla dyes leather, blending with any color they had; water washes it off.
    private static Optional<ItemStack> leatherStyleDip(ItemStack one, Optional<DyeColor> color) {
        boolean isLeatherStyle = one.getItem() instanceof DyeableLeatherItem || one.is(CraftingTags.VAT_DYEABLE);
        if (!isLeatherStyle) {
            return Optional.empty();
        }
        if (color.isEmpty()) {
            return hasDisplayColor(one) ? Optional.of(washed(one)) : Optional.empty();
        }
        if (one.getItem() instanceof DyeableLeatherItem) {
            return Optional.of(DyeableLeatherItem.dyeArmor(one, List.of(DyeItem.byColor(color.get()))));
        }
        return Optional.of(withDisplayColor(one, color.get()));
    }

    // Plain water takes a banner's last pattern off, like vanilla's cauldron.
    private static Optional<ItemStack> washedBanner(ItemStack one) {
        if (!(one.getItem() instanceof BannerItem) || BannerBlockEntity.getPatternCount(one) <= 0) {
            return Optional.empty();
        }
        ItemStack washed = one.copy();
        BannerBlockEntity.removeLastPattern(washed);
        return Optional.of(washed);
    }

    private static boolean hasDisplayColor(ItemStack stack) {
        CompoundTag display = stack.getTagElement(DISPLAY_KEY);
        return display != null && display.contains(COLOR_KEY);
    }

    private static ItemStack washed(ItemStack stack) {
        ItemStack washed = stack.copy();
        CompoundTag display = washed.getTagElement(DISPLAY_KEY);
        if (display != null) {
            display.remove(COLOR_KEY);
        }
        return washed;
    }

    // For modded gear without vanilla's dyeable interface: the dye's color, averaged with any color it already had.
    private static ItemStack withDisplayColor(ItemStack stack, DyeColor color) {
        ItemStack dyed = stack.copy();
        int added = color.getTextColor();
        CompoundTag display = dyed.getOrCreateTagElement(DISPLAY_KEY);
        int blended = display.contains(COLOR_KEY) ? average(display.getInt(COLOR_KEY), added) : added;
        display.putInt(COLOR_KEY, blended);
        return dyed;
    }

    private static int average(int first, int second) {
        int red = (((first >> 16) & 0xFF) + ((second >> 16) & 0xFF)) / 2;
        int green = (((first >> 8) & 0xFF) + ((second >> 8) & 0xFF)) / 2;
        int blue = ((first & 0xFF) + (second & 0xFF)) / 2;
        return (red << 16) | (green << 8) | blue;
    }

    private record Dip(Item input, ItemStack result, int charges, int ticks) {
    }
}
