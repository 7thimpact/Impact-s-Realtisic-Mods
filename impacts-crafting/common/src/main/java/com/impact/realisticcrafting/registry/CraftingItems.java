package com.impact.realisticcrafting.registry;

import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.CreativeTabs;
import com.impact.lib.registry.ModRegistrar;
import com.impact.lib.wood.WoodData;
import com.impact.lib.wood.WoodKind;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.item.CraftingToolItem;
import com.impact.realisticcrafting.item.LooseItem;
import com.impact.realisticcrafting.item.LooseWoodItem;
import com.impact.realisticcrafting.item.MakeshiftTableItem;
import com.impact.realisticcrafting.item.PebbleItem;
import com.impact.realisticcrafting.item.ToolKind;
import com.impact.realisticcrafting.item.ToolMaterial;
import com.impact.realisticcrafting.item.WoodPart;
import com.impact.realisticcrafting.item.WoodPartItem;
import com.impact.realisticcrafting.item.WoodenBucketItem;
import com.impact.realisticcrafting.item.WoodenWaterBucketItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Crafting's items and its creative tab, where per-wood parts get one entry per wood. */
public final class CraftingItems {
    private static final Map<MakeshiftTable, Supplier<MakeshiftTableItem>> TABLES = new EnumMap<>(MakeshiftTable.class);
    private static final Map<CraftingPart, Supplier<Item>> PARTS = new EnumMap<>(CraftingPart.class);
    private static final Map<CraftingTool, Supplier<CraftingToolItem>> TOOLS = new EnumMap<>(CraftingTool.class);
    private static final int TOOL_HEAD_STACK_SIZE = 16;
    private static final Map<ToolMaterial, Map<ToolKind, Supplier<Item>>> TOOL_PARTS = new EnumMap<>(ToolMaterial.class);
    private static final Map<ToolMaterial, Map<ToolKind, Supplier<LooseItem>>> LOOSE_TOOLS = new EnumMap<>(ToolMaterial.class);
    private static volatile Supplier<PebbleItem> stonePebble;
    private static final Map<WoodPart, Supplier<WoodPartItem>> WOOD_PARTS = new EnumMap<>(WoodPart.class);
    private static volatile Supplier<WoodenBucketItem> woodenBucket;
    private static volatile Supplier<WoodenWaterBucketItem> woodenWaterBucket;
    private static volatile Supplier<LooseWoodItem> looseBoat;

    private CraftingItems() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(CraftingMod.MOD_ID);
        stonePebble = registrar.register(Registries.ITEM, "stone_pebble", PebbleItem::new);
        for (MakeshiftTable table : MakeshiftTable.values()) {
            TABLES.put(table, registrar.register(Registries.ITEM, table.path, () -> new MakeshiftTableItem(CraftingBlocks.table(table))));
        }
        for (WoodPart part : WoodPart.values()) {
            WOOD_PARTS.put(part, registrar.register(Registries.ITEM, part.path, () -> new WoodPartItem(() -> part.burnTicks)));
        }
        woodenBucket = registrar.register(Registries.ITEM, "wooden_bucket", WoodenBucketItem::new);
        woodenWaterBucket = registrar.register(Registries.ITEM, "wooden_water_bucket", WoodenWaterBucketItem::new);
        looseBoat = registrar.register(Registries.ITEM, "loose_boat", LooseWoodItem::new);
        for (CraftingPart part : CraftingPart.values()) {
            PARTS.put(part, registrar.register(Registries.ITEM, part.path, () -> new Item(new Item.Properties())));
        }
        for (CraftingTool tool : CraftingTool.values()) {
            TOOLS.put(tool, registrar.register(Registries.ITEM, tool.path, () -> new CraftingToolItem(tool)));
        }
        for (ToolMaterial material : ToolMaterial.values()) {
            registerTools(registrar, material);
        }
        CreativeTabs.registerStacks(CraftingMod.MOD_ID, () -> new ItemStack(stonePebble()), CraftingItems::creativeTabStacks);
    }

    public static PebbleItem stonePebble() {
        return stonePebble.get();
    }

    public static MakeshiftTableItem table(MakeshiftTable table) {
        return TABLES.get(table).get();
    }

    public static WoodPartItem trimmedPlanks() {
        return woodPart(WoodPart.TRIMMED_PLANKS);
    }

    public static WoodPartItem woodPart(WoodPart part) {
        return WOOD_PARTS.get(part).get();
    }

    public static WoodenBucketItem woodenBucket() {
        return woodenBucket.get();
    }

    public static WoodenWaterBucketItem woodenWaterBucket() {
        return woodenWaterBucket.get();
    }

    public static LooseWoodItem looseBoat() {
        return looseBoat.get();
    }

    /** Woods that get boats (and so a Boat Hull and a loose boat): every wood except crimson and warped. */
    public static List<ResourceLocation> boatWoods() {
        return WoodData.allWoods().stream().filter(wood -> WoodData.vanillaItem(wood, WoodKind.BOAT).isPresent()).toList();
    }

    public static Item part(CraftingPart part) {
        return PARTS.get(part).get();
    }

    public static CraftingToolItem tool(CraftingTool tool) {
        return TOOLS.get(tool).get();
    }

    /** A tool's head, or the sword's blade. */
    public static Item toolPart(ToolMaterial material, ToolKind kind) {
        return TOOL_PARTS.get(material).get(kind).get();
    }

    public static LooseItem looseTool(ToolMaterial material, ToolKind kind) {
        return LOOSE_TOOLS.get(material).get(kind).get();
    }

    /** Every loose item Crafting registers. */
    public static List<LooseItem> looseItems() {
        List<LooseItem> looseItems = new ArrayList<>();
        for (ToolMaterial material : ToolMaterial.values()) {
            Arrays.stream(ToolKind.values()).map(kind -> looseTool(material, kind)).forEach(looseItems::add);
        }
        looseItems.add(looseBoat());
        return looseItems;
    }

    private static void registerTools(ModRegistrar registrar, ToolMaterial material) {
        Map<ToolKind, Supplier<Item>> parts = new EnumMap<>(ToolKind.class);
        Map<ToolKind, Supplier<LooseItem>> looseTools = new EnumMap<>(ToolKind.class);
        for (ToolKind kind : ToolKind.values()) {
            parts.put(kind, registrar.register(Registries.ITEM, kind.partPath(material), () -> new Item(new Item.Properties().stacksTo(TOOL_HEAD_STACK_SIZE))));
            looseTools.put(kind, registrar.register(Registries.ITEM, kind.loosePath(material), () -> new LooseItem(material.knock)));
        }
        TOOL_PARTS.put(material, parts);
        LOOSE_TOOLS.put(material, looseTools);
    }

    public static List<ItemStack> creativeTabStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(stonePebble()));
        Arrays.stream(MakeshiftTable.values()).map(table -> new ItemStack(table(table))).forEach(stacks::add);
        Arrays.stream(CraftingTool.values()).map(tool -> new ItemStack(tool(tool))).forEach(stacks::add);
        for (WoodPart part : WoodPart.values()) {
            List<ResourceLocation> woods = part.needsBoatWood ? boatWoods() : WoodData.allWoods();
            woods.forEach(wood -> stacks.add(WoodData.of(woodPart(part), wood)));
        }
        Arrays.stream(CraftingPart.values()).map(part -> new ItemStack(part(part))).forEach(stacks::add);
        stacks.add(new ItemStack(woodenBucket()));
        stacks.add(new ItemStack(woodenWaterBucket()));
        boatWoods().forEach(wood -> stacks.add(WoodData.of(looseBoat(), wood)));
        for (ToolMaterial material : ToolMaterial.values()) {
            Arrays.stream(ToolKind.values()).map(kind -> new ItemStack(toolPart(material, kind))).forEach(stacks::add);
            Arrays.stream(ToolKind.values()).map(kind -> new ItemStack(looseTool(material, kind))).forEach(stacks::add);
        }
        return stacks;
    }
}
