package com.impact.lib.advancement;

import com.impact.lib.platform.LoaderPlatform;

/** Lib's advancement triggers. */
public final class LibTriggers {
    public static final JoinedAloneTrigger JOINED_ALONE = new JoinedAloneTrigger();
    public static final CountTrigger COUNT = new CountTrigger();
    public static final ToolCraftedTrigger TOOL_CRAFTED = new ToolCraftedTrigger();

    private LibTriggers() {
    }

    /** Registers every trigger with vanilla. Must run once, on the main thread, before any world loads. */
    public static void registerAll() {
        LoaderPlatform.current().registerCriterionTrigger(JOINED_ALONE);
        LoaderPlatform.current().registerCriterionTrigger(COUNT);
        LoaderPlatform.current().registerCriterionTrigger(TOOL_CRAFTED);
    }
}
