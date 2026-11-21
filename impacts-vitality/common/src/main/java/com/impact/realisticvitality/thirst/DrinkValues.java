package com.impact.realisticvitality.thirst;

import com.impact.lib.ResourceIds;
import com.impact.realisticvitality.config.TaggedEntries;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.registry.VitalityItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

import java.util.List;
import java.util.Optional;

/**
 * Looks up what a drink or food does to thirst, from the server config: {@code drinks.overrides} first, then
 * {@code drinks.values} in order. Entries name an item ({@code minecraft:potion=3,1}) or a tag
 * ({@code #impactsvitality:dry_foods=-1,0}). A vanilla water bottle is not a potion here: it drinks as Unclean Water.
 */
public final class DrinkValues {
    private DrinkValues() {
    }

    public static Optional<DrinkValue> of(ItemStack stack) {
        ItemStack lookedUp = asDrink(stack);
        VitalityServerConfig.DrinkSettings settings = VitalityServerConfig.get().drinks;
        return firstMatch(settings.overrides.get(), lookedUp).or(() -> firstMatch(settings.values.get(), lookedUp));
    }

    /** The purity a drink carries: Vitality's water, a container's water, or a vanilla water bottle (unclean). Soups have none. */
    public static Optional<WaterPurity> purityOf(ItemStack stack) {
        if (isVanillaWaterBottle(stack)) {
            return Optional.of(WaterPurity.UNCLEAN);
        }
        Optional<ContainerContents> contents = WaterContainers.containerOf(stack).map(container -> ContainerContents.read(stack));
        if (contents.isPresent()) {
            return contents.filter(held -> !held.isEmpty() && held.kind() != ContainerContents.Kind.SOUP).map(ContainerContents::purity);
        }
        return VitalityItems.bottledWater(stack).map(water -> water.purity);
    }

    public static boolean isVanillaWaterBottle(ItemStack stack) {
        return stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER;
    }

    /** What a drink counts as: a vanilla water bottle as Unclean Water, a container as a bottle of its water or its soup, anything else as itself. */
    public static ItemStack asDrink(ItemStack stack) {
        if (isVanillaWaterBottle(stack)) {
            return VitalityItems.bottle(BottledWater.UNCLEAN);
        }
        if (WaterContainers.containerOf(stack).isEmpty()) {
            return stack;
        }
        ContainerContents contents = ContainerContents.read(stack);
        return switch (contents.kind()) {
            case COLD_WATER -> VitalityItems.bottle(BottledWater.COLD);
            case SOUP -> contents.soup().flatMap(ResourceIds::item).map(ItemStack::new).orElse(ItemStack.EMPTY);
            case WATER -> contents.isEmpty() ? ItemStack.EMPTY : VitalityItems.bottle(BottledWater.filledWith(contents.purity()));
        };
    }

    private static Optional<DrinkValue> firstMatch(List<String> entries, ItemStack stack) {
        ResourceLocation itemId = ResourceIds.itemId(stack.getItem());
        return TaggedEntries.findValue(entries, itemId::equals, tagId -> stack.is(TagKey.create(Registries.ITEM, tagId)))
                .flatMap(DrinkValue::parse);
    }
}
