package com.impact.lib.grinding;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.item.ConfiguredDurabilityItem;
import com.impact.lib.item.ImpactsItems;
import com.impact.lib.lang.LibTranslations;
import com.impact.lib.log.ImpactsLog;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The grinder bowl, held in the off hand. Load it by right-clicking with a grindable item in the main hand, or by
 * right-clicking the item onto it in any inventory screen, like a bundle. Sneak-right-click with an empty main hand
 * empties it. It loses 1 durability per item ground.
 */
public final class GrinderBowlItem extends Item implements ConfiguredDurabilityItem {
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);

    private final GrinderTier tier;

    public GrinderBowlItem(GrinderTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public GrinderTier tier() {
        return tier;
    }

    @Override
    public int configuredMaxDamage() {
        return tier.durability();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bowl = player.getItemInHand(hand);
        ItemStack mainHand = player.getMainHandItem();
        if (hand != InteractionHand.OFF_HAND) {
            return InteractionResultHolder.pass(bowl);
        }
        if (player.isSecondaryUseActive() && mainHand.isEmpty()) {
            emptyInto(player, bowl);
            return InteractionResultHolder.sidedSuccess(bowl, level.isClientSide());
        }
        boolean hasLoaded = Grinding.isGrindable(level, mainHand) && BowlContents.load(bowl, mainHand) > 0;
        if (hasLoaded && !level.isClientSide()) {
            LOG.debug(LibLogCategories.GRINDER, "bowl loaded", "player", player.getGameProfile().getName(), "item", ResourceIds.itemId(bowl.getItem()),
                    "contents", describe(bowl));
        }
        return hasLoaded ? InteractionResultHolder.sidedSuccess(bowl, level.isClientSide()) : InteractionResultHolder.pass(bowl);
    }

    /** Bundle-style: right-click an item onto the bowl to load it, or right-click the bowl with an empty cursor to take items out. */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack bowl, ItemStack carried, Slot slot, ClickAction action, Player player, SlotAccess carriedAccess) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }
        if (carried.isEmpty()) {
            return takeOutInto(bowl, carriedAccess);
        }
        return Grinding.isGrindable(player.level(), carried) && BowlContents.load(bowl, carried) > 0;
    }

    @Override
    public void appendHoverText(ItemStack bowl, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        List<ItemStack> contents = BowlContents.read(bowl);
        if (contents.isEmpty()) {
            lines.add(Component.translatable(LibTranslations.BOWL_EMPTY).withStyle(ChatFormatting.GRAY));
            return;
        }
        contents.forEach(stack -> lines.add(Component.translatable(LibTranslations.BOWL_ENTRY, stack.getHoverName(), stack.getCount())
                .withStyle(ChatFormatting.GRAY)));
    }

    private static boolean takeOutInto(ItemStack bowl, SlotAccess carriedAccess) {
        List<ItemStack> contents = BowlContents.read(bowl);
        if (contents.isEmpty()) {
            return false;
        }
        ItemStack taken = contents.remove(contents.size() - 1);
        BowlContents.write(bowl, contents);
        carriedAccess.set(taken);
        return true;
    }

    private static void emptyInto(Player player, ItemStack bowl) {
        List<ItemStack> contents = BowlContents.read(bowl);
        BowlContents.write(bowl, List.of());
        if (player.level().isClientSide()) {
            return;
        }
        contents.forEach(stack -> ImpactsItems.giveOrDrop(player, stack));
        LOG.debug(LibLogCategories.GRINDER, "bowl emptied", "player", player.getGameProfile().getName(), "stacks", contents.size());
    }

    private static String describe(ItemStack bowl) {
        return BowlContents.read(bowl).stream().map(stack -> ResourceIds.itemId(stack.getItem()) + "x" + stack.getCount()).toList().toString();
    }
}
