package com.impact.realisticvitality.registry;

import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.CreativeTabs;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.item.WaterBottleItem;
import com.impact.realisticvitality.thirst.BottledWater;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/** Vitality's items and its creative tab. */
public final class VitalityItems {
    private static final Map<BottledWater, Supplier<WaterBottleItem>> WATER_BOTTLES = new EnumMap<>(BottledWater.class);

    private VitalityItems() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(VitalityMod.MOD_ID);
        for (BottledWater water : BottledWater.values()) {
            WATER_BOTTLES.put(water, registrar.register(Registries.ITEM, water.itemPath, () -> new WaterBottleItem(water)));
        }
        CreativeTabs.register(VitalityMod.MOD_ID, () -> bottle(BottledWater.PURIFIED), VitalityItems::creativeTabItems);
    }

    public static WaterBottleItem waterBottle(BottledWater water) {
        return WATER_BOTTLES.get(water).get();
    }

    public static ItemStack bottle(BottledWater water) {
        return new ItemStack(waterBottle(water));
    }

    public static Optional<BottledWater> bottledWater(ItemStack stack) {
        return stack.getItem() instanceof WaterBottleItem waterBottle ? Optional.of(waterBottle.water) : Optional.empty();
    }

    public static List<Item> creativeTabItems() {
        List<Item> items = new ArrayList<>();
        for (BottledWater water : BottledWater.values()) {
            items.add(waterBottle(water));
        }
        return items;
    }
}
