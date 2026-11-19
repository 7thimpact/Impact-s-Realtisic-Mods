package com.impact.realisticfood.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

/** Reads an item entity's thrower ID, which vanilla keeps private (it only resolves it to an entity). */
@Mixin(ItemEntity.class)
public interface ItemEntityAccessor {
    @Accessor("thrower")
    UUID impactsfood$getThrower();
}
