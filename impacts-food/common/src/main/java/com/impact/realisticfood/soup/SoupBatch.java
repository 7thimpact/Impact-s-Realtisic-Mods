package com.impact.realisticfood.soup;

import com.impact.lib.ResourceIds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/** One soup cooking or ready in a pot: what it is, how many servings it made, how many are gone, and how long it has cooked. */
public final class SoupBatch {
    private static final String RECIPE_KEY = "Recipe";
    private static final String SERVING_KEY = "Serving";
    private static final String SERVINGS_KEY = "Servings";
    private static final String TAKEN_KEY = "Taken";
    private static final String COOK_TICKS_KEY = "CookTicks";
    private static final String COOKED_KEY = "Cooked";

    private final ResourceLocation recipeId;
    private final ItemStack serving;
    private int servings;
    private int servingsTaken;
    private final int cookTicksNeeded;
    private int ticksCooked;

    public SoupBatch(ResourceLocation recipeId, ItemStack serving, int servings, int cookTicksNeeded) {
        this(recipeId, serving, servings, 0, cookTicksNeeded, 0);
    }

    private SoupBatch(ResourceLocation recipeId, ItemStack serving, int servings, int servingsTaken, int cookTicksNeeded, int ticksCooked) {
        this.recipeId = recipeId;
        this.serving = serving;
        this.servings = servings;
        this.servingsTaken = servingsTaken;
        this.cookTicksNeeded = cookTicksNeeded;
        this.ticksCooked = ticksCooked;
    }

    /** A batch that needs no cooking: Watery Soup, made from ingredients that already waited a full cook. */
    public static SoupBatch readyNow(ResourceLocation recipeId, ItemStack serving, int servings) {
        return new SoupBatch(recipeId, serving, servings, 0, 0, 0);
    }

    public ResourceLocation recipeId() {
        return recipeId;
    }

    public ItemStack serving() {
        return serving;
    }

    public int servings() {
        return servings;
    }

    public int servingsLeft() {
        return servings - servingsTaken;
    }

    public int servingsTaken() {
        return servingsTaken;
    }

    public boolean isReady() {
        return ticksCooked >= cookTicksNeeded;
    }

    public void addServings(int extraServings) {
        servings += extraServings;
    }

    /** One tick of cooking. True on the tick the soup becomes ready. */
    public boolean cookOneTick() {
        if (isReady()) {
            return false;
        }
        ticksCooked++;
        return isReady();
    }

    public ItemStack takeServing() {
        servingsTaken++;
        return serving.copy();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(RECIPE_KEY, recipeId.toString());
        tag.put(SERVING_KEY, serving.save(new CompoundTag()));
        tag.putInt(SERVINGS_KEY, servings);
        tag.putInt(TAKEN_KEY, servingsTaken);
        tag.putInt(COOK_TICKS_KEY, cookTicksNeeded);
        tag.putInt(COOKED_KEY, ticksCooked);
        return tag;
    }

    /** The saved batch, or nothing when its recipe ID can't be read. */
    public static Optional<SoupBatch> load(CompoundTag tag) {
        return ResourceIds.tryParse(tag.getString(RECIPE_KEY)).map(recipeId -> new SoupBatch(recipeId, ItemStack.of(tag.getCompound(SERVING_KEY)),
                tag.getInt(SERVINGS_KEY), tag.getInt(TAKEN_KEY), tag.getInt(COOK_TICKS_KEY), tag.getInt(COOKED_KEY)));
    }
}
