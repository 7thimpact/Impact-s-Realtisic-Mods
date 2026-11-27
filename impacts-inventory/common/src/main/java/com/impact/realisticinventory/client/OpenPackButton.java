package com.impact.realisticinventory.client;

import com.impact.lib.ResourceIds;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.lang.InventoryTranslations;
import com.impact.realisticinventory.pack.WornOn;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A small button by the main hand that opens a worn pack, the same as its key: right of the hand for the pack on the
 * back (B), left of it for the pack on the chest (N). Shown only while a pack is worn there.
 */
final class OpenPackButton extends Button {
    static final int WIDTH = 14;
    static final int HEIGHT = 12;
    private static final ResourceLocation ICON_TEXTURE = ResourceIds.of(InventoryMod.MOD_ID, "textures/gui/open_pack.png");
    private static final int ICON_SIZE = 8;
    private static final int ICON_TEXTURE_SIZE = 8;

    private final WornOn place;

    OpenPackButton(int x, int y, WornOn place) {
        super(x, y, WIDTH, HEIGHT, Component.empty(), button -> InventoryKeys.requestOpenPack(place), DEFAULT_NARRATION);
        this.place = place;
        String tooltipKey = place == WornOn.BACK ? InventoryTranslations.OPEN_PACK_BUTTON : InventoryTranslations.OPEN_CHEST_PACK_BUTTON;
        setTooltip(Tooltip.create(Component.translatable(tooltipKey, InventoryKeys.keyFor(place).getTranslatedKeyMessage())));
    }

    WornOn place() {
        return place;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        int iconLeft = getX() + (WIDTH - ICON_SIZE) / 2;
        int iconTop = getY() + (HEIGHT - ICON_SIZE) / 2;
        graphics.blit(ICON_TEXTURE, iconLeft, iconTop, 0, 0, ICON_SIZE, ICON_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE);
    }
}
