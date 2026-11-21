package com.impact.lib.forge;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Runs a mod's loader-independent construction steps with its Forge loading context available to Lib, so Lib
 * can register that mod's config files with the right mod. Every Impact mod's Forge entry point does:
 * {@code ForgeModConstruction.run(context, MyModStartup::onModConstruction)}.
 */
public final class ForgeModConstruction {
    private static final ThreadLocal<FMLJavaModLoadingContext> CONSTRUCTING_MOD_CONTEXT = new ThreadLocal<>();

    private ForgeModConstruction() {
    }

    public static void run(FMLJavaModLoadingContext context, Runnable constructionSteps) {
        CONSTRUCTING_MOD_CONTEXT.set(context);
        try {
            constructionSteps.run();
        } finally {
            CONSTRUCTING_MOD_CONTEXT.remove();
        }
    }

    /** The loading context of the mod being constructed on this thread. */
    public static FMLJavaModLoadingContext currentContext() {
        FMLJavaModLoadingContext context = CONSTRUCTING_MOD_CONTEXT.get();
        if (context == null) {
            throw new IllegalStateException(
                    "Impact config files and registrations must happen during mod construction, inside ForgeModConstruction.run(...).");
        }
        return context;
    }
}
