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
 * matters, and again at every server start, so chances read from a world's server config apply.
 */
public final class CompostRegistry {
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);
    private static final Map<Supplier<? extends ItemLike>, Supplier<Float>> CHANCES_BY_ITEM = new ConcurrentHashMap<>();

    private CompostRegistry() {
    }

    /** Burnt Food composts at 0.3, Mush at 0.65, Wood Scraps at 0.3. */
    public static void register(Supplier<? extends ItemLike> item, float chance) {
        register(item, () -> chance);
    }

    /** A chance read from config each time Lib applies the chances. */
    public static void register(Supplier<? extends ItemLike> item, Supplier<Float> chance) {
        CHANCES_BY_ITEM.put(item, chance);
    }

    /** Called by Lib once every mod is ready and at every server start, on the main thread. */
    public static void applyAll() {
        CHANCES_BY_ITEM.forEach((item, chance) -> {
            float currentChance = chance.get();
            ComposterBlock.COMPOSTABLES.put(item.get().asItem(), currentChance);
            LOG.debug(LibLogCategories.REGISTRY, "compostable registered", "item", ResourceIds.itemId(item.get().asItem()), "chance", currentChance);
        });
    }
}
