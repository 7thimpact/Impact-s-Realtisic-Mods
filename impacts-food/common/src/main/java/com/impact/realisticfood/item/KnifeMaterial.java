package com.impact.realisticfood.item;

import com.impact.lib.config.ConfigValue;
import com.impact.realisticfood.config.FoodServerConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Function;

/** The two knives. Attack damage counts the player's own 1, as the item tooltip does. */
public enum KnifeMaterial {
    FLINT("flint_knife", 3, Items.FLINT, settings -> settings.flintDurability),
    IRON("iron_knife", 4, Items.IRON_INGOT, settings -> settings.ironDurability);

    public final String itemPath;
    public final int attackDamage;
    public final Item repairItem;
    private final Function<FoodServerConfig.KnifeSettings, ConfigValue<Integer>> durabilitySetting;

    KnifeMaterial(String itemPath, int attackDamage, Item repairItem,
                  Function<FoodServerConfig.KnifeSettings, ConfigValue<Integer>> durabilitySetting) {
        this.itemPath = itemPath;
        this.attackDamage = attackDamage;
        this.repairItem = repairItem;
        this.durabilitySetting = durabilitySetting;
    }

    public ConfigValue<Integer> durability() {
        return durabilitySetting.apply(FoodServerConfig.get().knives);
    }
}
