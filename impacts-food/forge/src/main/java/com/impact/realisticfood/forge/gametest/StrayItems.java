package com.impact.realisticfood.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

/**
 * Dropped items near a test's spot, cleared before the test drops its own: in a crowded family setup a neighboring
 * test's campfire or pot can pop a cooked item that lands within reach of this test's item checks.
 */
final class StrayItems {
    private static final double CLEARED_RADIUS_BLOCKS = 3.0;

    private StrayItems() {
    }

    static void clearAround(GameTestHelper helper, BlockPos relativePosition) {
        AABB area = new AABB(helper.absolutePos(relativePosition)).inflate(CLEARED_RADIUS_BLOCKS);
        helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).forEach(Entity::discard);
    }
}
