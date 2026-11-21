package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.lang.CraftingTranslations;
import com.impact.realisticcrafting.registry.CraftingSounds;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;

/** Crafting's sounds.json: each of Crafting's sounds plays a vanilla sound event until a resource pack gives it its own. */
final class CraftingSoundDefinitions extends SoundDefinitionsProvider {
    CraftingSoundDefinitions(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, CraftingMod.MOD_ID, existingFiles);
    }

    @Override
    public void registerSounds() {
        borrow(CraftingSounds.hammerFinish(), CraftingSounds.HAMMER_FINISH, "block.anvil.land");
        borrow(CraftingSounds.craftFinish(), CraftingSounds.CRAFT_FINISH, "block.wooden_button.click_on");
        borrow(CraftingSounds.tableCreak(), CraftingSounds.TABLE_CREAK, "block.wood.break");
        borrow(CraftingSounds.vatFill(), CraftingSounds.VAT_FILL, "item.bucket.empty");
        borrow(CraftingSounds.vatDye(), CraftingSounds.VAT_DYE, "item.dye.use");
        borrow(CraftingSounds.vatDip(), CraftingSounds.VAT_DIP, "entity.generic.splash");
    }

    private void borrow(SoundEvent sound, String soundPath, String vanillaEvent) {
        add(sound, SoundDefinition.definition()
                .subtitle(CraftingTranslations.subtitle(soundPath))
                .with(SoundDefinition.Sound.sound(ResourceIds.of("minecraft", vanillaEvent), SoundDefinition.SoundType.EVENT)));
    }
}
