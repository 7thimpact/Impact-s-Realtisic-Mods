package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.item.LooseIron;
import com.impact.realisticcrafting.item.ToolKind;
import com.impact.realisticcrafting.item.ToolMaterial;
import com.impact.realisticcrafting.item.WoodPart;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Arrays;

/** Crafting's item models: flat sprites, held like tools for the grid tools. Per-wood parts share one tinted sprite. */
final class CraftingItemModels extends ItemModelProvider {
    CraftingItemModels(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, CraftingMod.MOD_ID, existingFiles);
    }

    private static final String HANDHELD_PARENT = "item/handheld";
    private static final String GENERATED_PARENT = "item/generated";

    @Override
    protected void registerModels() {
        CraftingItems.pebbles().forEach(this::basicItem);
        Arrays.stream(WoodPart.values()).map(CraftingItems::woodPart).forEach(this::basicItem);
        basicItem(CraftingItems.woodenBucket());
        basicItem(CraftingItems.woodenWaterBucket());
        basicItem(CraftingItems.comforter());
        CraftingItems.sheets().forEach(sheet -> withExistingParent(ResourceIds.itemId(sheet).getPath(), GENERATED_PARENT)
                .texture("layer0", modLoc("item/sheet")));
        CraftingItems.bannerCloths().forEach(cloth -> withExistingParent(ResourceIds.itemId(cloth).getPath(), GENERATED_PARENT)
                .texture("layer0", modLoc("item/banner_cloth")));
        for (LooseIron looseIron : LooseIron.values()) {
            withExistingParent(looseIron.path, GENERATED_PARENT).texture("layer0", ResourceIds.tryParse(looseIron.vanillaSprite).orElseThrow())
                    .texture("layer1", modLoc("item/loose_binding"));
        }
        withExistingParent("loose_boat", GENERATED_PARENT).texture("layer0", mcLoc("item/oak_boat")).texture("layer1", modLoc("item/loose_binding"));
        Arrays.stream(CraftingPart.values()).map(CraftingItems::part).forEach(this::basicItem);
        Arrays.stream(CraftingTool.values()).map(CraftingItems::tool).forEach(this::handheld);
        for (ToolMaterial material : ToolMaterial.values()) {
            for (ToolKind kind : ToolKind.values()) {
                basicItem(CraftingItems.toolPart(material, kind));
                looseTool(material, kind);
            }
        }
    }

    // The vanilla tool's sprite with a loose binding drawn over it.
    private void looseTool(ToolMaterial material, ToolKind kind) {
        withExistingParent(kind.loosePath(material), HANDHELD_PARENT)
                .texture("layer0", mcLoc("item/" + kind.vanillaPath(material)))
                .texture("layer1", modLoc("item/loose_binding"));
    }

    private void handheld(Item tool) {
        String path = ResourceIds.itemId(tool).getPath();
        withExistingParent(path, HANDHELD_PARENT).texture("layer0", modLoc("item/" + path));
    }
}
