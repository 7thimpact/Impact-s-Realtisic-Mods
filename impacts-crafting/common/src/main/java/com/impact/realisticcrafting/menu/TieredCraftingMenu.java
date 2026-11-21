package com.impact.realisticcrafting.menu;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.block.MakeshiftTableBlockEntity;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A makeshift table's crafting menu: vanilla's 3×3 crafting menu (recipe book, shift-clicking and all) under the
 * table's own station, so the tier check knows which recipes to refuse. Each craft taken wears the table by one use.
 */
public final class TieredCraftingMenu extends CraftingMenu {
    private static final int RESULT_SLOT = 0;
    private static final int FIRST_GRID_SLOT = 1;
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);

    private final MakeshiftTable table;
    private final ContainerLevelAccess access;
    private final CraftingContainer grid;

    public TieredCraftingMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access, MakeshiftTable table) {
        super(containerId, playerInventory, access);
        this.table = table;
        this.access = access;
        this.grid = (CraftingContainer) getSlot(FIRST_GRID_SLOT).container;
        useWearingResultSlot(playerInventory.player);
    }

    /** The client's copy, opened from the table the server names. */
    public static TieredCraftingMenu fromNetwork(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        return new TieredCraftingMenu(containerId, playerInventory, ContainerLevelAccess.NULL, MakeshiftTable.values()[extraData.readVarInt()]);
    }

    public MakeshiftTable table() {
        return table;
    }

    public CraftingStation station() {
        return table.station;
    }

    public CraftingContainer grid() {
        return grid;
    }

    @Override
    public MenuType<?> getType() {
        return CraftingMenus.tieredCrafting();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, table.block());
    }

    /** Server, after a craft was taken: one use off the table, which falls apart at zero. */
    void onCraftTaken(Player player) {
        access.execute((level, position) -> {
            if (!(level.getBlockEntity(position) instanceof MakeshiftTableBlockEntity tableEntity)) {
                return;
            }
            boolean isWornOut = tableEntity.useOnce();
            LOG.debug(CraftingLogCategories.WEAR, "table used", "player", player.getScoreboardName(), "table", table.path,
                    "pos", position.toShortString(), "usesLeft", tableEntity.usesLeft());
            if (isWornOut) {
                fallApart(level, position, player);
            }
        });
    }

    // Like an anvil: the anvil's breaking sound, the grid's items on the ground, and nothing else drops.
    private void fallApart(Level level, BlockPos position, Player player) {
        BlockState tableState = level.getBlockState(position);
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            Containers.dropItemStack(level, position.getX(), position.getY(), position.getZ(), grid.removeItemNoUpdate(slot));
        }
        clearCraftingContent();
        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, position, Block.getId(tableState));
        level.removeBlock(position, false);
        level.levelEvent(LevelEvent.SOUND_ANVIL_BROKEN, position, 0);
        LOG.debug(CraftingLogCategories.WEAR, "table fell apart", "player", player.getScoreboardName(), "table", table.path,
                "pos", position.toShortString());
    }

    // Vanilla's result slot, swapped for one that reports each craft taken (shift-clicking takes one craft at a time).
    private void useWearingResultSlot(Player player) {
        Slot vanillaResultSlot = slots.get(RESULT_SLOT);
        TableResultSlot wearingResultSlot = new TableResultSlot(player, grid, vanillaResultSlot.container, vanillaResultSlot.x,
                vanillaResultSlot.y, this);
        wearingResultSlot.index = RESULT_SLOT;
        slots.set(RESULT_SLOT, wearingResultSlot);
    }
}
