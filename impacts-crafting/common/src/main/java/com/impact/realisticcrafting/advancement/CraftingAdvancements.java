package com.impact.realisticcrafting.advancement;

import com.impact.lib.ResourceIds;
import com.impact.lib.advancement.HoldActionTrigger;
import com.impact.lib.advancement.ImpactsAdvancementBuilder;
import com.impact.lib.advancement.ToolCraftedTrigger;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.item.LooseIron;
import com.impact.realisticcrafting.item.PebbleKind;
import com.impact.realisticcrafting.item.ToolKind;
import com.impact.realisticcrafting.item.ToolMaterial;
import com.impact.realisticcrafting.item.WoodPart;
import com.impact.realisticcrafting.recipe.CraftingRecipeTypes;
import com.impact.realisticcrafting.registry.CraftingBlocks;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.registry.CraftingTags;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.RecipeCraftedTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The "Realistic Crafting" advancement tab: 26 achievements along the progression pebble → Makeshift Table → stone
 * tools → Makeshift Stone Table → iron tools → Crafting Table, with side branches for containers, dyes, beds and
 * redstone. Goals mark big steps; challenges are long grinds and give 50 XP.
 */
public final class CraftingAdvancements {
    public static final ResourceLocation HUMBLE_BEGINNINGS = id("humble_beginnings");
    public static final ResourceLocation ROCK_SOLID = id("rock_solid");
    public static final ResourceLocation CUT_TO_SIZE = id("cut_to_size");
    public static final ResourceLocation SCRAP_COLLECTOR = id("scrap_collector");
    public static final ResourceLocation MAKESHIFT_MINDSET = id("makeshift_mindset");
    public static final ResourceLocation PATIENCE_IS_A_VIRTUE = id("patience_is_a_virtue");
    public static final ResourceLocation FALLING_APART = id("falling_apart");
    public static final ResourceLocation LOOSE_ENDS = id("loose_ends");
    public static final ResourceLocation HAMMER_TIME = id("hammer_time");
    public static final ResourceLocation STONE_AGE = id("stone_age");
    public static final ResourceLocation HINGED = id("hinged");
    public static final ResourceLocation BUCKET_LIST = id("bucket_list");
    public static final ResourceLocation STORAGE_SOLUTION = id("storage_solution");
    public static final ResourceLocation SET_IN_STONE = id("set_in_stone");
    public static final ResourceLocation TOOLS_OF_THE_TRADE = id("tools_of_the_trade");
    public static final ResourceLocation THE_REAL_DEAL = id("the_real_deal");
    public static final ResourceLocation GRIND_SET = id("grind_set");
    public static final ResourceLocation COLOR_THEORY = id("color_theory");
    public static final ResourceLocation DIP_IT = id("dip_it");
    public static final ResourceLocation SEW_WHAT = id("sew_what");
    public static final ResourceLocation MAKING_THE_BED = id("making_the_bed");
    public static final ResourceLocation FULL_SPECTRUM = id("full_spectrum");
    public static final ResourceLocation MECHANICALLY_INCLINED = id("mechanically_inclined");
    public static final ResourceLocation PUSHING_IT = id("pushing_it");
    public static final ResourceLocation EVERY_BRANCH = id("every_branch");
    public static final ResourceLocation MASTER_CRAFTSMAN = id("master_craftsman");
    private static final ResourceLocation TAB_BACKGROUND = ResourceIds.of("minecraft", "textures/block/cobblestone.png");
    private static final ResourceLocation CHEST_RECIPE = id("chest");
    private static final ResourceLocation CRAFTING_TABLE_RECIPE = id("crafting_table");

    private CraftingAdvancements() {
    }

    /** Every achievement in the tab. Call once items are registered (during data generation). */
    public static List<ImpactsAdvancementBuilder> all() {
        List<ImpactsAdvancementBuilder> advancements = new ArrayList<>(earlyDays());
        advancements.addAll(stoneAndIron());
        advancements.addAll(sideBranches());
        return advancements;
    }

    private static List<ImpactsAdvancementBuilder> earlyDays() {
        return List.of(
                ImpactsAdvancementBuilder.of(HUMBLE_BEGINNINGS, CraftingItems.stonePebble()).root(TAB_BACKGROUND)
                        .criterion("has_pebble", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(CraftingTags.PEBBLES).build())),
                rockSolid(),
                ImpactsAdvancementBuilder.of(CUT_TO_SIZE, CraftingItems.trimmedPlanks()).parent(HUMBLE_BEGINNINGS)
                        .criterion("trimmed_planks", ToolCraftedTrigger.TriggerInstance.resultMatching(anyOf(CraftingItems.trimmedPlanks()))),
                ImpactsAdvancementBuilder.of(SCRAP_COLLECTOR, CraftingItems.part(CraftingPart.WOOD_SCRAPS)).parent(CUT_TO_SIZE)
                        .criterion("collected_scraps", CraftingTriggers.SCRAPS_COLLECTED.anyPlayer()),
                ImpactsAdvancementBuilder.of(MAKESHIFT_MINDSET, CraftingItems.table(MakeshiftTable.WOODEN)).parent(HUMBLE_BEGINNINGS)
                        .criterion("placed_table", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(CraftingBlocks.table(MakeshiftTable.WOODEN))),
                ImpactsAdvancementBuilder.of(PATIENCE_IS_A_VIRTUE, Items.CLOCK).parent(MAKESHIFT_MINDSET)
                        .criterion("long_craft", CraftingTriggers.LONG_CRAFT.anyPlayer()),
                ImpactsAdvancementBuilder.of(FALLING_APART, CraftingItems.table(MakeshiftTable.WOODEN)).parent(MAKESHIFT_MINDSET)
                        .criterion("table_broken", CraftingTriggers.TABLE_BROKEN.anyPlayer()),
                ImpactsAdvancementBuilder.of(LOOSE_ENDS, CraftingItems.looseTool(ToolMaterial.WOODEN, ToolKind.PICKAXE)).parent(MAKESHIFT_MINDSET)
                        .criterion("has_loose_tool", InventoryChangeTrigger.TriggerInstance.hasItems(anyOf(looseTools(ToolMaterial.values()).toArray(Item[]::new)))),
                ImpactsAdvancementBuilder.of(HAMMER_TIME, CraftingItems.stonePebble()).parent(LOOSE_ENDS)
                        .criterion("hammered_tool", hammered(looseTools(ToolMaterial.values()))),
                ImpactsAdvancementBuilder.of(STONE_AGE, Items.STONE_PICKAXE).parent(HAMMER_TIME)
                        .criterion("hammered_stone_tool", hammered(looseTools(ToolMaterial.STONE))),
                ImpactsAdvancementBuilder.of(HINGED, CraftingItems.part(CraftingPart.STONE_HINGE)).parent(ROCK_SOLID)
                        .criterion("has_hinge", hasItems(CraftingItems.part(CraftingPart.STONE_HINGE))),
                ImpactsAdvancementBuilder.of(BUCKET_LIST, CraftingItems.woodenBucket()).parent(CUT_TO_SIZE)
                        .criterion("has_bucket", hasItems(CraftingItems.woodenBucket())),
                ImpactsAdvancementBuilder.of(STORAGE_SOLUTION, Items.CHEST).parent(BUCKET_LIST)
                        .criterion("crafted_chest", RecipeCraftedTrigger.TriggerInstance.craftedItem(CHEST_RECIPE)));
    }

    // Any kind of pebble packed into its block.
    private static ImpactsAdvancementBuilder rockSolid() {
        ImpactsAdvancementBuilder rockSolid = ImpactsAdvancementBuilder.of(ROCK_SOLID, Items.COBBLESTONE).parent(HUMBLE_BEGINNINGS);
        Arrays.stream(PebbleKind.values()).forEach(kind ->
                rockSolid.criterion(kind.packingRecipePath(), RecipeCraftedTrigger.TriggerInstance.craftedItem(id(kind.packingRecipePath()))));
        return rockSolid.anyCriterion();
    }

    private static List<ImpactsAdvancementBuilder> stoneAndIron() {
        Item saw = CraftingItems.tool(CraftingTool.IRON_SAW);
        Item hammer = CraftingItems.tool(CraftingTool.IRON_HAMMER);
        Item tweezers = CraftingItems.tool(CraftingTool.IRON_TWEEZERS);
        return List.of(
                ImpactsAdvancementBuilder.of(SET_IN_STONE, CraftingItems.table(MakeshiftTable.STONE)).parent(STONE_AGE).goal()
                        .criterion("placed_stone_table", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(CraftingBlocks.table(MakeshiftTable.STONE))),
                ImpactsAdvancementBuilder.of(TOOLS_OF_THE_TRADE, hammer).parent(SET_IN_STONE)
                        .criterion("has_iron_tools", hasItems(saw, hammer, tweezers)),
                ImpactsAdvancementBuilder.of(THE_REAL_DEAL, Items.CRAFTING_TABLE).parent(TOOLS_OF_THE_TRADE).goal()
                        .criterion("crafted_crafting_table", RecipeCraftedTrigger.TriggerInstance.craftedItem(CRAFTING_TABLE_RECIPE)),
                ImpactsAdvancementBuilder.of(MECHANICALLY_INCLINED, CraftingItems.part(CraftingPart.REDSTONE_MECHANISM)).parent(TOOLS_OF_THE_TRADE)
                        .criterion("has_mechanism", hasItems(CraftingItems.part(CraftingPart.REDSTONE_MECHANISM))),
                ImpactsAdvancementBuilder.of(PUSHING_IT, Items.PISTON).parent(MECHANICALLY_INCLINED)
                        .criterion("hammered_piston", hammered(List.of(CraftingItems.looseIron(LooseIron.PISTON)))),
                everyBranch(),
                ImpactsAdvancementBuilder.of(MASTER_CRAFTSMAN, Items.CRAFTING_TABLE).parent(THE_REAL_DEAL).challenge()
                        .criterion("crafted_many", CraftingTriggers.MASTER_CRAFTSMAN.anyPlayer()));
    }

    private static List<ImpactsAdvancementBuilder> sideBranches() {
        ImpactsAdvancementBuilder fullSpectrum = ImpactsAdvancementBuilder.of(FULL_SPECTRUM, CraftingItems.sheet(DyeColor.MAGENTA))
                .parent(MAKING_THE_BED).challenge();
        Arrays.stream(DyeColor.values()).forEach(color -> fullSpectrum.criterion(color.getName(), BedAssembledTrigger.TriggerInstance.inColor(color)));
        return List.of(
                ImpactsAdvancementBuilder.of(GRIND_SET, Items.RED_DYE).parent(CUT_TO_SIZE)
                        .criterion("ground_dye", CraftingTriggers.DYE_GROUND.anyPlayer()),
                ImpactsAdvancementBuilder.of(COLOR_THEORY, Items.ORANGE_DYE).parent(GRIND_SET)
                        .criterion("mixed_dyes", CraftingTriggers.DYES_MIXED.anyPlayer()),
                ImpactsAdvancementBuilder.of(DIP_IT, CraftingItems.dyeVat()).parent(GRIND_SET)
                        .criterion("vat_dyed", CraftingTriggers.VAT_DYED.anyPlayer()),
                ImpactsAdvancementBuilder.of(SEW_WHAT, CraftingItems.tool(CraftingTool.BONE_NEEDLE)).parent(HUMBLE_BEGINNINGS)
                        .criterion("sewed", ToolCraftedTrigger.TriggerInstance.craftedWith(anyOf(CraftingItems.tool(CraftingTool.BONE_NEEDLE)))),
                ImpactsAdvancementBuilder.of(MAKING_THE_BED, Items.RED_BED).parent(SEW_WHAT)
                        .criterion("bed_assembled", BedAssembledTrigger.TriggerInstance.anyColor()),
                fullSpectrum);
    }

    // Generated from the parts library, so a new part joins the achievement on its own.
    private static ImpactsAdvancementBuilder everyBranch() {
        ImpactsAdvancementBuilder everyBranch = ImpactsAdvancementBuilder.of(EVERY_BRANCH, CraftingItems.part(CraftingPart.TWINE))
                .parent(THE_REAL_DEAL).challenge();
        everyBranchParts().forEach((criterionName, part) -> everyBranch.criterion(criterionName, InventoryChangeTrigger.TriggerInstance.hasItems(part)));
        return everyBranch;
    }

    /** Every shared part, by criterion name: the parts library minus Wood Scraps (a byproduct), any Sheet and any Banner Cloth. */
    public static Map<String, ItemPredicate> everyBranchParts() {
        Map<String, ItemPredicate> parts = new LinkedHashMap<>();
        Arrays.stream(CraftingPart.values()).filter(part -> part != CraftingPart.WOOD_SCRAPS)
                .forEach(part -> parts.put(part.path, anyOf(CraftingItems.part(part))));
        Arrays.stream(WoodPart.values()).forEach(part -> parts.put(part.path, anyOf(CraftingItems.woodPart(part))));
        parts.put("wooden_bucket", anyOf(CraftingItems.woodenBucket()));
        parts.put("comforter", anyOf(CraftingItems.comforter()));
        parts.put("sheet", anyOf(CraftingItems.sheets().toArray(Item[]::new)));
        parts.put("banner_cloth", anyOf(CraftingItems.bannerCloths().toArray(Item[]::new)));
        return parts;
    }

    private static HoldActionTrigger.TriggerInstance hammered(List<? extends Item> looseItems) {
        return HoldActionTrigger.TriggerInstance.holding(CraftingRecipeTypes.HAMMERING_ID, anyOf(looseItems.toArray(Item[]::new)));
    }

    private static List<Item> looseTools(ToolMaterial... materials) {
        List<Item> looseTools = new ArrayList<>();
        for (ToolMaterial material : materials) {
            Arrays.stream(ToolKind.values()).forEach(kind -> looseTools.add(CraftingItems.looseTool(material, kind)));
        }
        return looseTools;
    }

    private static InventoryChangeTrigger.TriggerInstance hasItems(ItemLike... items) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(items);
    }

    private static ItemPredicate anyOf(ItemLike... items) {
        return ItemPredicate.Builder.item().of(items).build();
    }

    private static ResourceLocation id(String path) {
        return ResourceIds.of(CraftingMod.MOD_ID, path);
    }
}
