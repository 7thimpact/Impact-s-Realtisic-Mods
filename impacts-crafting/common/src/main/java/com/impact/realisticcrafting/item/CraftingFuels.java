package com.impact.realisticcrafting.item;

import com.impact.lib.fuel.CompostRegistry;
import com.impact.lib.fuel.FuelRegistry;
import com.impact.lib.wood.WoodData;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntSupplier;

/** Which of Crafting's items burn and compost. Stone, string, leather and iron parts never burn. */
public final class CraftingFuels {
    private static final int MAKESHIFT_TABLE_BURN_TICKS = 300;
    private static final int WOODEN_TOOL_BURN_TICKS = 200;
    private static final int WOODEN_BUCKET_BURN_TICKS = 600;
    // Like the vanilla boat it becomes.
    private static final int LOOSE_BOAT_BURN_TICKS = 1200;

    private CraftingFuels() {
    }

    /** Called once, when every mod is ready. */
    public static void register() {
        for (CraftingPart part : CraftingPart.values()) {
            if (part.burnTicks() > 0) {
                registerWhenOn(CraftingItems.part(part), part::burnTicks);
            }
        }
        for (WoodPart part : WoodPart.values()) {
            WoodPartItem item = CraftingItems.woodPart(part);
            FuelRegistry.register(item, stack -> isOn() ? item.burnTicks(stack) : 0);
        }
        registerWhenOn(CraftingItems.woodenBucket(), () -> WOODEN_BUCKET_BURN_TICKS);
        FuelRegistry.register(CraftingItems.looseBoat(), stack -> isOn() && isFlammableWood(stack) ? LOOSE_BOAT_BURN_TICKS : 0);
        registerWhenOn(CraftingItems.table(MakeshiftTable.WOODEN), () -> MAKESHIFT_TABLE_BURN_TICKS);
        for (ToolKind kind : ToolKind.values()) {
            registerWhenOn(CraftingItems.toolPart(ToolMaterial.WOODEN, kind), () -> WOODEN_TOOL_BURN_TICKS);
            registerWhenOn(CraftingItems.looseTool(ToolMaterial.WOODEN, kind), () -> WOODEN_TOOL_BURN_TICKS);
        }
        CompostRegistry.register(() -> CraftingItems.part(CraftingPart.WOOD_SCRAPS),
                () -> CraftingServerConfig.get().woodScraps.compostChance.get().floatValue());
    }

    private static void registerWhenOn(Item item, IntSupplier burnTicks) {
        FuelRegistry.register(item, stack -> isOn() ? burnTicks.getAsInt() : 0);
    }

    private static boolean isFlammableWood(ItemStack stack) {
        return WoodData.get(stack).map(WoodData::isFlammable).orElse(true);
    }

    private static boolean isOn() {
        return CraftingServerConfig.get().isFuelOn.get();
    }
}
