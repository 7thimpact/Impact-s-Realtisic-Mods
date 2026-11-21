package com.impact.realisticinventory.client;

import com.impact.lib.ResourceIds;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.lang.InventoryTranslations;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** The small button above the back slot that opens the worn pack, the same as pressing B. Shown only while a pack is worn. */
final class OpenPackButton extends Button {
    static final int WIDTH = 14;
    static final int HEIGHT = 12;
    private static final ResourceLocation ICON_TEXTURE = ResourceIds.of(InventoryMod.MOD_ID, "textures/gui/open_pack.png");
    private static final int ICON_SIZE = 8;
    private static final int ICON_TEXTURE_SIZE = 8;

    OpenPackButton(int x, int y) {
        super(x, y, WIDTH, HEIGHT, Component.empty(), button -> InventoryKeys.requestOpenPack(), DEFAULT_NARRATION);
        setTooltip(Tooltip.create(Component.translatable(InventoryTranslations.OPEN_PACK_BUTTON, InventoryKeys.OPEN_PACK.getTranslatedKeyMessage())));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        int iconLeft = getX() + (WIDTH - ICON_SIZE) / 2;
        int iconTop = getY() + (HEIGHT - ICON_SIZE) / 2;
        graphics.blit(ICON_TEXTURE, iconLeft, iconTop, 0, 0, ICON_SIZE, ICON_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE);
    }
}
