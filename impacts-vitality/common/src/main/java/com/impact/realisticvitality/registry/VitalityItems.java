package com.impact.realisticvitality.registry;

import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.CreativeTabs;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.item.CharcoalFilterItem;
import com.impact.realisticvitality.item.HydrationPackItem;
import com.impact.realisticvitality.item.WaterBottleItem;
import com.impact.realisticvitality.item.WaterContainerItem;
import com.impact.realisticvitality.thirst.BottledWater;
import com.impact.realisticvitality.thirst.WaterContainer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Vitality's items and its creative tab. */
public final class VitalityItems {
    private static final Map<BottledWater, Supplier<WaterBottleItem>> WATER_BOTTLES = new EnumMap<>(BottledWater.class);
    private static final Map<WaterContainer, Supplier<WaterContainerItem>> CONTAINERS = new EnumMap<>(WaterContainer.class);
    private static volatile Supplier<CharcoalFilterItem> charcoalFilter;

    private VitalityItems() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(VitalityMod.MOD_ID);
        for (BottledWater water : BottledWater.values()) {
            WATER_BOTTLES.put(water, registrar.register(Registries.ITEM, water.itemPath, () -> new WaterBottleItem(water)));
        }
        for (WaterContainer container : WaterContainer.values()) {
            CONTAINERS.put(container, registrar.register(Registries.ITEM, container.itemPath, () -> newContainerItem(container)));
        }
        charcoalFilter = registrar.register(Registries.ITEM, "charcoal_filter", CharcoalFilterItem::new);
        CreativeTabs.register(VitalityMod.MOD_ID, () -> bottle(BottledWater.PURIFIED), VitalityItems::creativeTabItems);
    }

    public static WaterBottleItem waterBottle(BottledWater water) {
        return WATER_BOTTLES.get(water).get();
    }

    public static ItemStack bottle(BottledWater water) {
        return new ItemStack(waterBottle(water));
    }

    public static WaterContainerItem container(WaterContainer container) {
        return CONTAINERS.get(container).get();
    }

    public static CharcoalFilterItem charcoalFilter() {
        return charcoalFilter.get();
    }

    public static List<Item> waterBottles() {
        return Arrays.stream(BottledWater.values()).map(VitalityItems::waterBottle).collect(Collectors.toList());
    }

    public static List<Item> containers() {
        return Arrays.stream(WaterContainer.values()).map(VitalityItems::container).collect(Collectors.toList());
    }

    public static Optional<BottledWater> bottledWater(ItemStack stack) {
        return stack.getItem() instanceof WaterBottleItem waterBottle ? Optional.of(waterBottle.water) : Optional.empty();
    }

    public static List<Item> creativeTabItems() {
        List<Item> items = new ArrayList<>(waterBottles());
        items.addAll(containers());
        VitalityBlocks.all().forEach(block -> items.add(block.asItem()));
        items.add(charcoalFilter());
        return items;
    }

    private static WaterContainerItem newContainerItem(WaterContainer container) {
        return container.isWorn ? new HydrationPackItem() : new WaterContainerItem(container);
    }
}
