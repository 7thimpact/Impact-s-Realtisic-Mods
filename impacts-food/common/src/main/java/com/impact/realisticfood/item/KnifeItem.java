package com.impact.realisticfood.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.impact.lib.item.ConfiguredDurabilityItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A Flint or Iron Knife: the tool in every cutting recipe (it stays in the grid and takes 1 damage per cut), and a
 * weak weapon. Its durability comes from Food's server config.
 */
public final class KnifeItem extends Item implements ConfiguredDurabilityItem {
    private static final int PLAYER_BASE_ATTACK_DAMAGE = 1;
    // Attack speed 2: quicker than a sword (1.6), since a knife is light.
    private static final double ATTACK_SPEED_MODIFIER = -2.0;
    private static final int ATTACK_WEAR = 1;

    private final KnifeMaterial material;
    private final Multimap<Attribute, AttributeModifier> attackModifiers;

    public KnifeItem(KnifeMaterial material) {
        super(new Item.Properties().durability(material.durability().defaultValue()));
        this.material = material;
        this.attackModifiers = ImmutableMultimap.of(
                Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Knife modifier",
                        material.attackDamage - PLAYER_BASE_ATTACK_DAMAGE, AttributeModifier.Operation.ADDITION),
                Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Knife modifier",
                        ATTACK_SPEED_MODIFIER, AttributeModifier.Operation.ADDITION));
    }

    public KnifeMaterial material() {
        return material;
    }

    @Override
    public int configuredMaxDamage() {
        return material.durability().get();
    }

    // Forge's stack-aware version, which it prefers, calls this vanilla one.
    @SuppressWarnings("deprecation")
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? attackModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(ATTACK_WEAR, attacker, wielder -> wielder.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(material.repairItem);
    }
}
