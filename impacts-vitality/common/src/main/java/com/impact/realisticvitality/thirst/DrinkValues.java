package com.impact.realisticvitality.thirst;

import com.impact.lib.ResourceIds;
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
    private static final String TAG_PREFIX = "#";
    private static final String KEY_SEPARATOR = "=";

    private DrinkValues() {
    }

    public static Optional<DrinkValue> of(ItemStack stack) {
        ItemStack lookedUp = isVanillaWaterBottle(stack) ? uncleanWaterStandIn() : stack;
        VitalityServerConfig.DrinkSettings settings = VitalityServerConfig.get().drinks;
        return firstMatch(settings.overrides.get(), lookedUp).or(() -> firstMatch(settings.values.get(), lookedUp));
    }

    /** The purity a drink carries: Vitality's own water, or a vanilla water bottle, which is unclean. */
    public static Optional<WaterPurity> purityOf(ItemStack stack) {
        if (isVanillaWaterBottle(stack)) {
            return Optional.of(WaterPurity.UNCLEAN);
        }
        return VitalityItems.bottledWater(stack).map(water -> water.purity);
    }

    public static boolean isVanillaWaterBottle(ItemStack stack) {
        return stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER;
    }

    private static ItemStack uncleanWaterStandIn() {
        return VitalityItems.bottle(BottledWater.UNCLEAN);
    }

    private static Optional<DrinkValue> firstMatch(List<String> entries, ItemStack stack) {
        for (String entry : entries) {
            String[] keyAndValue = entry.split(KEY_SEPARATOR, 2);
            if (keyAndValue.length == 2 && matches(keyAndValue[0].trim(), stack)) {
                return DrinkValue.parse(keyAndValue[1]);
            }
        }
        return Optional.empty();
    }

    private static boolean matches(String key, ItemStack stack) {
        if (key.startsWith(TAG_PREFIX)) {
            return ResourceIds.tryParse(key.substring(TAG_PREFIX.length()))
                    .map(tagId -> stack.is(TagKey.create(Registries.ITEM, tagId)))
                    .orElse(false);
        }
        Optional<ResourceLocation> itemId = ResourceIds.tryParse(key);
        return itemId.isPresent() && ResourceIds.itemId(stack.getItem()).equals(itemId.get());
    }
}
