package com.impact.realisticvitality.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.lang.VitalityTranslations;
import com.impact.realisticvitality.registry.VitalitySounds;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;

/** Vitality's sounds.json: each sound plays a vanilla sound event until a resource pack gives it its own. */
final class VitalitySoundDefinitions extends SoundDefinitionsProvider {
    VitalitySoundDefinitions(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, VitalityMod.MOD_ID, existingFiles);
    }

    @Override
    public void registerSounds() {
        borrow(VitalitySounds.pant(), VitalitySounds.PANT, "entity.player.breath");
        borrow(VitalitySounds.shiver(), VitalitySounds.SHIVER, "entity.player.hurt_freeze");
        borrow(VitalitySounds.cough(), VitalitySounds.COUGH, "entity.player.hurt_drown");
    }

    private void borrow(SoundEvent sound, String soundPath, String vanillaEvent) {
        add(sound, SoundDefinition.definition()
                .subtitle(VitalityTranslations.subtitle(soundPath))
                .with(SoundDefinition.Sound.sound(ResourceIds.of("minecraft", vanillaEvent), SoundDefinition.SoundType.EVENT)));
    }
}
