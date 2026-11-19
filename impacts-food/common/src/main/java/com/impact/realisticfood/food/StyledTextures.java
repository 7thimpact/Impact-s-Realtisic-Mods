package com.impact.realisticfood.food;

import com.impact.lib.ResourceIds;
import com.impact.realisticfood.FoodMod;
import net.minecraft.resources.ResourceLocation;

/**
 * The textures a styled food's two-layer model uses: its base food's own texture, and an overlay sprite the client
 * makes from the style's shared overlay (see the client's {@code StyledOverlaySource}).
 */
public final class StyledTextures {
    private static final String ITEM_FOLDER = "item/";
    private static final String OVERLAY_FOLDER = "item/overlay/";
    private static final String STYLED_FOLDER = "item/styled/";

    private StyledTextures() {
    }

    /** The base item's texture: {@code minecraft:item/cooked_beef}, or Food's own {@code impactsfood:item/cooked_beef_cut}. */
    public static ResourceLocation baseTexture(FoodBase base) {
        ResourceLocation baseId = ResourceIds.itemId(base.baseItem());
        return ResourceIds.of(baseId.getNamespace(), ITEM_FOLDER + baseId.getPath());
    }

    /** The style's shared overlay, a texture file: {@code impactsfood:item/overlay/dried}. */
    public static ResourceLocation styleOverlay(FoodStyle style) {
        return ResourceIds.of(FoodMod.MOD_ID, OVERLAY_FOLDER + style.prefix);
    }

    /** The sprite the client makes for one styled food: {@code impactsfood:item/styled/toasted_cooked_beef}. */
    public static ResourceLocation overlaySprite(FoodBase base, FoodStyle style) {
        return ResourceIds.of(FoodMod.MOD_ID, STYLED_FOLDER + style.itemPath(base));
    }
}
