package com.impact.lib.registry;

import com.impact.lib.platform.LoaderPlatform;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.function.Supplier;

/** Each Impact mod's creative tab, titled by the translation key {@code itemGroup.<modId>}. */
public final class CreativeTabs {
    private static final String TAB_PATH = "items";
    private static final String TITLE_KEY_PREFIX = "itemGroup.";

    private CreativeTabs() {
    }

    /** The translation key of the mod's tab title. */
    public static String titleKey(String modId) {
        return TITLE_KEY_PREFIX + modId;
    }

    /**
     * Registers the mod's tab as {@code <modId>:items}, during mod construction. The suppliers are read when the tab
     * is filled, after every registry is ready.
     */
    public static Supplier<CreativeModeTab> register(String modId, Supplier<ItemStack> icon, Supplier<List<? extends ItemLike>> items) {
        LoaderPlatform platform = LoaderPlatform.current();
        return platform.registrar(modId).register(Registries.CREATIVE_MODE_TAB, TAB_PATH, () ->
                platform.creativeTabBuilder()
                        .title(Component.translatable(titleKey(modId)))
                        .icon(icon)
                        .displayItems((parameters, output) -> items.get().forEach(output::accept))
                        .build());
    }
}
