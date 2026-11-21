package com.impact.realisticfood.boiling;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** What a {@link BoilingPot} needs from the block holding it: a cauldron, a campfire bucket or a stone-boiling pot. */
public interface PotHost {
    ServerLevel level();

    BlockPos position();

    PotKind kind();

    /** Whether the pot has any water left. A dry pot stops boiling at once and takes nothing. */
    boolean hasWater();

    /** Whether something under the pot gives heat right now. */
    boolean isHeated();

    int heatUpTicks();

    /** Ticks the pot keeps boiling once its heat is gone. */
    int coolDownTicks();

    /** Single items the pot boils at once. */
    int itemSlots();

    /** Soup ingredients the pot holds at once. */
    int soupIngredientSlots();

    /** Servings one soup batch makes at most. */
    int maxServings();

    /** Uses up water for servings taken (a cauldron level, or one of a bucket's boils). */
    void useWaterForServings();

    /** Uses up water for one item boiled on its own (one of a bucket's boils; a cauldron loses water by evaporating instead). */
    void useWaterForBoil();

    /** Ticks between losing water levels while boiling, or 0 when the pot doesn't evaporate. */
    int evaporationTicks();

    /** Takes one level of water away. False when the pot is dry afterwards. */
    boolean evaporateOneLevel();

    /** The middle of the water's surface, where particles start and boiled items pop out. */
    Vec3 surfaceCenter();

    /** Hands a finished item out of the pot. */
    void release(ItemStack boiled);
}
