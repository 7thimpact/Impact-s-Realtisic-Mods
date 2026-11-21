package com.impact.realisticvitality.temperature;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityFeatures;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Sewing a lining into armor at an anvil, the way without Crafting (with Crafting it's sewn with the Bone Needle in a
 * crafting grid): armor on the left, 3 wool, 4 rabbit hide or 3 leather on the right, for 1 level.
 */
public final class Linings {
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);

    private Linings() {
    }

    public static boolean areOn() {
        return VitalityFeatures.byConditionName(VitalityFeatures.LININGS).orElseThrow().getAsBoolean();
    }

    /** The anvil's result for armor and a lining material, or nothing when the anvil should work as usual. */
    public static Optional<AnvilLining> anvilResult(ItemStack armor, ItemStack material) {
        boolean isArmor = armor.getItem() instanceof ArmorItem;
        Optional<ArmorLining> lining = ArmorLining.ofMaterial(material);
        if (!areOn() || ImpactsLib.isLoaded(VitalityMod.CRAFTING_MOD_ID) || !isArmor || lining.isEmpty()) {
            return Optional.empty();
        }
        int needed = lining.get().materialCount();
        if (material.getCount() < needed) {
            return Optional.empty();
        }
        return Optional.of(new AnvilLining(lining.get().sewnInto(armor), needed, VitalityServerConfig.get().insulation.anvilLiningLevels.get()));
    }

    public static void logSewn(Player player, ItemStack lined, String how) {
        ArmorLining.of(lined).ifPresent(lining -> LOG.debug(VitalityLogCategories.GEAR, "lining sewn", "player", player.getScoreboardName(),
                "armor", ResourceIds.itemId(lined.getItem()), "lining", lining.serializedName, "how", how));
    }

    /** The lined armor, how much material it takes, and its cost in levels. */
    public record AnvilLining(ItemStack lined, int materialCount, int levels) {
    }
}
