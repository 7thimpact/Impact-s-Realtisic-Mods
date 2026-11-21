package com.impact.lib.forge.datagen;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.grinding.Grinding;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/** Item models for Lib's shared items: the grinder bowls are flat items, the grinding sticks are held like tools. */
final class LibItemModelProvider extends ItemModelProvider {
    private static final String FLAT_ITEM_PARENT = "item/generated";
    private static final String HELD_TOOL_PARENT = "item/handheld";

    LibItemModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, ImpactsLib.MOD_ID, existingFiles);
    }

    @Override
    protected void registerModels() {
        flatItem(Grinding.woodenBowl(), FLAT_ITEM_PARENT);
        flatItem(Grinding.stoneBowl(), FLAT_ITEM_PARENT);
        flatItem(Grinding.woodenStick(), HELD_TOOL_PARENT);
        flatItem(Grinding.stoneStick(), HELD_TOOL_PARENT);
    }

    private void flatItem(Item item, String parent) {
        String path = ResourceIds.itemId(item).getPath();
        withExistingParent(path, mcLoc(parent)).texture("layer0", modLoc(ITEM_FOLDER + "/" + path));
    }
}
