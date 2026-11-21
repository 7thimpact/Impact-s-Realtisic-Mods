package com.impact.realisticvitality.temperature;

import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityServerConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The three linings sewn into armor, stored as {@code impactsvitality:lining} on the piece: Wool (warmth), Fur (more
 * warmth, from rabbit hide) and Leather (cooling). One lining per piece; a new one replaces the old.
 */
public enum ArmorLining {
    WOOL("wool", stack -> stack.is(ItemTags.WOOL), settings -> new Insulation(settings.woolLiningWarmth.get(), 0.0),
            settings -> settings.woolLiningMaterials.get()),
    FUR("fur", stack -> stack.is(Items.RABBIT_HIDE), settings -> new Insulation(settings.furLiningWarmth.get(), 0.0),
            settings -> settings.furLiningMaterials.get()),
    LEATHER("leather", stack -> stack.is(Items.LEATHER), settings -> new Insulation(0.0, settings.leatherLiningCooling.get()),
            settings -> settings.leatherLiningMaterials.get());

    public static final String NBT_KEY = VitalityMod.MOD_ID + ":lining";

    public final String serializedName;
    private final Predicate<ItemStack> isMaterial;
    private final Function<VitalityServerConfig.InsulationSettings, Insulation> insulation;
    private final Function<VitalityServerConfig.InsulationSettings, Integer> materialCount;

    ArmorLining(String serializedName, Predicate<ItemStack> isMaterial, Function<VitalityServerConfig.InsulationSettings, Insulation> insulation,
                Function<VitalityServerConfig.InsulationSettings, Integer> materialCount) {
        this.serializedName = serializedName;
        this.isMaterial = isMaterial;
        this.insulation = insulation;
        this.materialCount = materialCount;
    }

    public Insulation insulation() {
        return insulation.apply(VitalityServerConfig.get().insulation);
    }

    public int materialCount() {
        return materialCount.apply(VitalityServerConfig.get().insulation);
    }

    public static Optional<ArmorLining> ofMaterial(ItemStack material) {
        return Arrays.stream(values()).filter(lining -> lining.isMaterial.test(material)).findFirst();
    }

    public static Optional<ArmorLining> of(ItemStack armor) {
        CompoundTag tag = armor.getTag();
        if (tag == null || !tag.contains(NBT_KEY)) {
            return Optional.empty();
        }
        String name = tag.getString(NBT_KEY);
        return Arrays.stream(values()).filter(lining -> lining.serializedName.equals(name)).findFirst();
    }

    /** A copy of the armor (enchantments, damage, trims and name kept) with this lining sewn in. */
    public ItemStack sewnInto(ItemStack armor) {
        ItemStack lined = armor.copyWithCount(1);
        lined.getOrCreateTag().putString(NBT_KEY, serializedName);
        return lined;
    }
}
