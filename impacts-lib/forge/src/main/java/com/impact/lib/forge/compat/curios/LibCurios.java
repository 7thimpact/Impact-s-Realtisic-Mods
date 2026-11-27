package com.impact.lib.forge.compat.curios;

import com.impact.lib.ImpactsLib;
import com.impact.lib.backslot.BackSlot;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.log.ImpactsLog;

/** Lib's optional Curios support. Loaded only when Curios is installed. */
public final class LibCurios {
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);

    private LibCurios() {
    }

    /** Once every mod is ready: when no Impact mod provides the back slot (Inventory isn't installed), Curios's back slot is it. */
    public static void provideBackSlotIfNoneDoes() {
        if (BackSlot.hasProvider()) {
            return;
        }
        BackSlot.registerBackSlotProvider(ImpactsLib.MOD_ID, new CuriosBackSlotProvider());
        LOG.debug(LibLogCategories.PROVIDERS, "curios back slot used", "slotType", CuriosBackSlotProvider.BACK_SLOT_TYPE);
    }
}
