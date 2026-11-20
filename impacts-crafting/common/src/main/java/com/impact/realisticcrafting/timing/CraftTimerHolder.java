package com.impact.realisticcrafting.timing;

/** A crafting menu with a craft timer: vanilla's crafting and inventory menus get one through Crafting's mixins. */
public interface CraftTimerHolder {
    CraftTimer impactscrafting$craftTimer();
}
