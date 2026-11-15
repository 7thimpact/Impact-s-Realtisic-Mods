package com.impact.realisticinventory.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets Inventory's screens move slots: vanilla's slot positions are final. Only screens use them, on the client. */
@Mixin(Slot.class)
public interface SlotPositionAccessor {
    @Mutable
    @Accessor("x")
    void impactsinventory$setX(int x);

    @Mutable
    @Accessor("y")
    void impactsinventory$setY(int y);
}
