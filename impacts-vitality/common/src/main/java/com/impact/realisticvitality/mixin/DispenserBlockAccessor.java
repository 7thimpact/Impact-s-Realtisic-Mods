package com.impact.realisticvitality.mixin;

import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/** Reads the dispenser's behavior table, so Vitality can wrap the glass bottle's behavior instead of replacing it. */
@Mixin(DispenserBlock.class)
public interface DispenserBlockAccessor {
    @Accessor("DISPENSER_REGISTRY")
    static Map<Item, DispenseItemBehavior> impactsvitality$behaviors() {
        throw new AssertionError("Replaced by Mixin");
    }
}
