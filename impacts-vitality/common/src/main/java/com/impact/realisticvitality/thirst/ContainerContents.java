package com.impact.realisticvitality.thirst;

import com.impact.lib.ResourceIds;
import com.impact.realisticvitality.VitalityMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Optional;

/**
 * What a drink container holds, saved on the stack: how many drinks, of what (water, Cold Water or a soup) and the
 * water's purity. Adding water mixes the purity down to the worst; only a container that keeps temperature keeps Cold
 * Water cold or holds soup, and a soup never mixes with anything else.
 */
public record ContainerContents(int drinks, Kind kind, WaterPurity purity, Optional<ResourceLocation> soup) {
    public static final String DRINKS_KEY = VitalityMod.MOD_ID + ":drinks";
    public static final String PURITY_KEY = VitalityMod.MOD_ID + ":purity";
    public static final String KIND_KEY = VitalityMod.MOD_ID + ":contents";
    public static final String SOUP_KEY = VitalityMod.MOD_ID + ":soup";
    public static final ContainerContents EMPTY = new ContainerContents(0, Kind.WATER, WaterPurity.PURIFIED, Optional.empty());

    public static ContainerContents read(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || tag.getInt(DRINKS_KEY) <= 0) {
            return EMPTY;
        }
        WaterPurity purity = WaterPurity.bySerializedName(tag.getString(PURITY_KEY)).orElse(WaterPurity.UNCLEAN);
        Kind kind = Kind.bySerializedName(tag.getString(KIND_KEY)).orElse(Kind.WATER);
        Optional<ResourceLocation> soup = kind == Kind.SOUP ? ResourceIds.tryParse(tag.getString(SOUP_KEY)) : Optional.empty();
        return new ContainerContents(tag.getInt(DRINKS_KEY), kind, purity, soup);
    }

    public static ContainerContents water(int drinks, WaterPurity purity) {
        return new ContainerContents(drinks, Kind.WATER, purity, Optional.empty());
    }

    /** Writes the contents onto the stack, or clears Vitality's keys when it's empty. Other mods' keys are left alone. */
    public void writeTo(ItemStack stack) {
        if (isEmpty()) {
            stack.removeTagKey(DRINKS_KEY);
            stack.removeTagKey(PURITY_KEY);
            stack.removeTagKey(KIND_KEY);
            stack.removeTagKey(SOUP_KEY);
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(DRINKS_KEY, drinks);
        tag.putString(PURITY_KEY, purity.serializedName);
        tag.putString(KIND_KEY, kind.serializedName);
        soup.ifPresentOrElse(soupId -> tag.putString(SOUP_KEY, soupId.toString()), () -> tag.remove(SOUP_KEY));
    }

    public boolean isEmpty() {
        return drinks <= 0;
    }

    /** These contents with more water added, or nothing when they can't take it (a soup is inside). */
    public Optional<ContainerContents> withWater(int addedDrinks, WaterPurity addedPurity, boolean isAddedCold, boolean keepsTemperature) {
        boolean staysCold = isAddedCold && keepsTemperature && (isEmpty() || kind == Kind.COLD_WATER);
        if (isEmpty()) {
            return Optional.of(new ContainerContents(addedDrinks, staysCold ? Kind.COLD_WATER : Kind.WATER, addedPurity, Optional.empty()));
        }
        if (kind == Kind.SOUP) {
            return Optional.empty();
        }
        Kind mixedKind = staysCold ? Kind.COLD_WATER : Kind.WATER;
        return Optional.of(new ContainerContents(drinks + addedDrinks, mixedKind, purity.mixedWith(addedPurity), Optional.empty()));
    }

    /** These contents with one bowl of soup added: only into an empty container, or one holding the same soup. */
    public Optional<ContainerContents> withSoup(ResourceLocation soupId) {
        boolean isSameSoup = kind == Kind.SOUP && soup.filter(soupId::equals).isPresent();
        if (!isEmpty() && !isSameSoup) {
            return Optional.empty();
        }
        return Optional.of(new ContainerContents(drinks + 1, Kind.SOUP, WaterPurity.PURIFIED, Optional.of(soupId)));
    }

    public ContainerContents minusOneDrink() {
        return minusDrinks(1);
    }

    public ContainerContents minusDrinks(int takenDrinks) {
        return drinks <= takenDrinks ? EMPTY : new ContainerContents(drinks - takenDrinks, kind, purity, soup);
    }

    public enum Kind {
        WATER("water"),
        COLD_WATER("cold_water"),
        SOUP("soup");

        public final String serializedName;

        Kind(String serializedName) {
            this.serializedName = serializedName;
        }

        static Optional<Kind> bySerializedName(String name) {
            return Arrays.stream(values()).filter(kind -> kind.serializedName.equals(name)).findFirst();
        }
    }
}
