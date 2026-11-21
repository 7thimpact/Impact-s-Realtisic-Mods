package com.impact.realisticcrafting.registry;

import com.impact.lib.ResourceIds;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticcrafting.CraftingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

/**
 * Crafting's sound events. Each plays a vanilla sound by default (see the generated sounds.json), so resource packs can
 * give hammering, the tables and the Dye Vat their own sounds without touching vanilla's.
 */
public final class CraftingSounds {
    public static final String HAMMER_FINISH = "item.hammer.finish";
    public static final String CRAFT_FINISH = "ui.timed_craft.finish";
    public static final String TABLE_CREAK = "block.makeshift_table.creak";
    public static final String VAT_FILL = "block.dye_vat.fill";
    public static final String VAT_DYE = "block.dye_vat.dye";
    public static final String VAT_DIP = "block.dye_vat.dip";
    private static Supplier<SoundEvent> hammerFinish;
    private static Supplier<SoundEvent> craftFinish;
    private static Supplier<SoundEvent> tableCreak;
    private static Supplier<SoundEvent> vatFill;
    private static Supplier<SoundEvent> vatDye;
    private static Supplier<SoundEvent> vatDip;

    private CraftingSounds() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(CraftingMod.MOD_ID);
        hammerFinish = register(registrar, HAMMER_FINISH);
        craftFinish = register(registrar, CRAFT_FINISH);
        tableCreak = register(registrar, TABLE_CREAK);
        vatFill = register(registrar, VAT_FILL);
        vatDye = register(registrar, VAT_DYE);
        vatDip = register(registrar, VAT_DIP);
    }

    public static SoundEvent hammerFinish() {
        return hammerFinish.get();
    }

    public static SoundEvent craftFinish() {
        return craftFinish.get();
    }

    public static SoundEvent tableCreak() {
        return tableCreak.get();
    }

    public static SoundEvent vatFill() {
        return vatFill.get();
    }

    public static SoundEvent vatDye() {
        return vatDye.get();
    }

    public static SoundEvent vatDip() {
        return vatDip.get();
    }

    private static Supplier<SoundEvent> register(ModRegistrar registrar, String path) {
        return registrar.register(Registries.SOUND_EVENT, path, () -> SoundEvent.createVariableRangeEvent(ResourceIds.of(CraftingMod.MOD_ID, path)));
    }
}
