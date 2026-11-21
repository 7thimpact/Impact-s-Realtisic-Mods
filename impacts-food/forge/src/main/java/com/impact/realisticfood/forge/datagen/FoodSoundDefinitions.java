package com.impact.realisticfood.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.lang.FoodTranslations;
import com.impact.realisticfood.registry.FoodSounds;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;

/** Food's sounds.json: each of Food's sounds plays a vanilla sound event until a resource pack gives it its own. */
final class FoodSoundDefinitions extends SoundDefinitionsProvider {
    FoodSoundDefinitions(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, FoodMod.MOD_ID, existingFiles);
    }

    @Override
    public void registerSounds() {
        borrow(FoodSounds.potSimmer(), FoodSounds.POT_SIMMER, "block.bubble_column.bubble_pop");
        borrow(FoodSounds.potSplash(), FoodSounds.POT_SPLASH, "entity.generic.splash");
        borrow(FoodSounds.soupLadle(), FoodSounds.SOUP_LADLE, "item.bottle.fill");
        borrow(FoodSounds.spitSizzle(), FoodSounds.SPIT_SIZZLE, "block.fire.extinguish");
    }

    private void borrow(SoundEvent sound, String soundPath, String vanillaEvent) {
        add(sound, SoundDefinition.definition()
                .subtitle(FoodTranslations.subtitle(soundPath))
                .with(SoundDefinition.Sound.sound(ResourceIds.of("minecraft", vanillaEvent), SoundDefinition.SoundType.EVENT)));
    }
}
