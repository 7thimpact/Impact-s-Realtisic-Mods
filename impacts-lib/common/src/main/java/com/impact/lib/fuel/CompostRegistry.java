package com.impact.lib.fuel;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.log.ImpactsLog;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Items a composter accepts, with their chance to raise its level. Mods register during construction; Lib adds them to
 * vanilla's composter once every mod is ready (items exist and the main thread is free), so registration order never
 * matters.
 */
public final class CompostRegistry {
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);
    private static final Map<Supplier<? extends ItemLike>, Float> PENDING_CHANCES = new ConcurrentHashMap<>();

    private CompostRegistry() {
    }

    /** Burnt Food composts at 0.3, Mush at 0.65, Wood Scraps at 0.3. */
    public static void register(Supplier<? extends ItemLike> item, float chance) {
        PENDING_CHANCES.put(item, chance);
    }

    /** Called by Lib once every mod is ready, on the main thread. */
    public static void applyAll() {
        PENDING_CHANCES.forEach((item, chance) -> {
            ComposterBlock.COMPOSTABLES.put(item.get().asItem(), chance.floatValue());
            LOG.debug(LibLogCategories.REGISTRY, "compostable registered", "item", ResourceIds.itemId(item.get().asItem()), "chance", chance);
        });
    }
}
