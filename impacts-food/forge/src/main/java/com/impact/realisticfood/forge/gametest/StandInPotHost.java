package com.impact.realisticfood.forge.gametest;

import com.impact.realisticfood.boiling.PotHost;
import com.impact.realisticfood.boiling.PotKind;
import com.impact.realisticfood.config.FoodServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** A pot host with no block behind it, so tests can switch the heat on and off and count what comes out. */
final class StandInPotHost implements PotHost {
    private final ServerLevel level;
    private final BlockPos position;
    private final int evaporationTicks;
    private final List<ItemStack> released = new ArrayList<>();
    private boolean isHeated;
    private int evaporatedLevels;

    StandInPotHost(ServerLevel level, BlockPos position, int evaporationTicks) {
        this.level = level;
        this.position = position;
        this.evaporationTicks = evaporationTicks;
    }

    void setHeated(boolean heated) {
        isHeated = heated;
    }

    List<ItemStack> released() {
        return released;
    }

    int evaporatedLevels() {
        return evaporatedLevels;
    }

    @Override
    public ServerLevel level() {
        return level;
    }

    @Override
    public BlockPos position() {
        return position;
    }

    @Override
    public PotKind kind() {
        return PotKind.CAULDRON;
    }

    @Override
    public boolean hasWater() {
        return true;
    }

    @Override
    public boolean isHeated() {
        return isHeated;
    }

    @Override
    public int heatUpTicks() {
        return FoodServerConfig.get().boiling.heatUpTicks.get();
    }

    @Override
    public int coolDownTicks() {
        return FoodServerConfig.get().boiling.coolDownTicks.get();
    }

    @Override
    public int itemSlots() {
        return FoodServerConfig.get().boiling.cauldronItemSlots.get();
    }

    @Override
    public int soupIngredientSlots() {
        return FoodServerConfig.get().soup.cauldronIngredientSlots.get();
    }

    @Override
    public int maxServings() {
        return FoodServerConfig.get().soup.maxBowls.get();
    }

    @Override
    public void useWaterForServings() {
        evaporatedLevels++;
    }

    @Override
    public void useWaterForBoil() {
        // The stand-in counts only evaporation and servings.
    }

    @Override
    public int evaporationTicks() {
        return evaporationTicks;
    }

    @Override
    public boolean evaporateOneLevel() {
        evaporatedLevels++;
        return true;
    }

    @Override
    public Vec3 surfaceCenter() {
        return Vec3.atCenterOf(position);
    }

    @Override
    public void release(ItemStack boiled) {
        released.add(boiled);
    }
}
