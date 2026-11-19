package com.impact.realisticinventory.pack;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/** The pack menu's type, and opening the worn pack (the B key, or the button beside the back slot). */
public final class PackMenus {
    private static final int MINIMUM_SLOTS = 1;
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);
    private static Supplier<MenuType<PackMenu>> packMenuType;

    private PackMenus() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        packMenuType = LoaderPlatform.current().registrar(InventoryMod.MOD_ID)
                .register(Registries.MENU, "pack", () -> LoaderPlatform.current().menuTypeWithData(PackMenus::createClientMenu));
    }

    public static MenuType<PackMenu> packMenuType() {
        return packMenuType.get();
    }

    /** Server: opens the worn pack in its own screen. Nothing happens without a worn pack. */
    public static void openWornPack(ServerPlayer player) {
        Optional<ItemStack> wornPack = WornPack.of(player);
        if (wornPack.isEmpty() || !WornPack.arePacksEnabled()) {
            LOG.debug(InventoryLogCategories.PACKS, "pack open refused", "player", player.getGameProfile().getName(),
                    "reason", wornPack.isEmpty() ? "no pack worn" : "packs disabled");
            return;
        }
        ItemStack pack = wornPack.get();
        int slotCount = WornPack.slotCount(pack);
        handOutItemsBeyondSize(player, pack, slotCount);
        NonNullList<ItemStack> slots = PackContents.read(pack, slotCount, new ArrayList<>());
        PackContainer container = new PackContainer(player, pack, slots);
        SimpleMenuProvider menuProvider = new SimpleMenuProvider((containerId, playerInventory, menuPlayer) ->
                new PackMenu(containerId, playerInventory, container), pack.getHoverName());
        LoaderPlatform.current().openMenuWithData(player, menuProvider, buffer -> buffer.writeVarInt(slotCount));
        LOG.debug(InventoryLogCategories.PACKS, "pack opened", "player", player.getGameProfile().getName(),
                "item", ResourceIds.itemId(pack.getItem()), "slots", slotCount, "stacks", PackContents.stacksIn(pack).size());
    }

    // Trimmed and written back first, so handing the extra items out (which may fill the pack) can't be overwritten.
    private static void handOutItemsBeyondSize(ServerPlayer player, ItemStack pack, int slotCount) {
        List<ItemStack> itemsBeyondSize = new ArrayList<>();
        NonNullList<ItemStack> slots = PackContents.read(pack, slotCount, itemsBeyondSize);
        if (itemsBeyondSize.isEmpty()) {
            return;
        }
        PackContents.write(pack, slots);
        PackOverflow.handOut(player, itemsBeyondSize);
    }

    private static PackMenu createClientMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        int slotCount = Mth.clamp(extraData.readVarInt(), MINIMUM_SLOTS, PackRules.MAXIMUM_SLOTS);
        return new PackMenu(containerId, playerInventory, new SimpleContainer(slotCount));
    }
}
