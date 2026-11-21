package com.impact.realisticvitality.item;

import com.impact.lib.item.ConfiguredDurabilityItem;
import com.impact.realisticvitality.config.VitalityServerConfig;
import net.minecraft.world.item.Item;

/** The Charcoal Filter: wears down in the Water Filter (per drink) and in a Respirator (per second of smoky air). */
public final class CharcoalFilterItem extends Item implements ConfiguredDurabilityItem {
    /** Any positive number makes vanilla treat it as damageable; the real life comes from the config. */
    private static final int REGISTERED_DURABILITY = 600;

    public CharcoalFilterItem() {
        super(new Item.Properties().durability(REGISTERED_DURABILITY));
    }

    @Override
    public int configuredMaxDamage() {
        return VitalityServerConfig.get().filters.charcoalFilterLife.get();
    }
}
