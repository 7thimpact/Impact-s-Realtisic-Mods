package com.impact.realisticinventory.advancement;

import com.impact.lib.ResourceIds;
import com.impact.lib.advancement.CountTrigger;
import com.impact.lib.advancement.ImpactsAdvancementBuilder;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.pack.InventoryItems;
import com.impact.realisticinventory.pack.PackKind;
import net.minecraft.advancements.critereon.DamagePredicate;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityHurtPlayerTrigger;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.List;

/** The "Realistic Inventory" advancement tab: 12 achievements. Goals mark big steps; challenges give 50 XP. */
public final class InventoryAdvancements {
    public static final ResourceLocation HANDS_FULL = id("hands_full");
    public static final ResourceLocation QUICK_DRAW = id("quick_draw");
    public static final ResourceLocation SWORD_AND_BOARD = id("sword_and_board");
    public static final ResourceLocation JUGGLER = id("juggler");
    public static final ResourceLocation SECOND_NATURE = id("second_nature");
    public static final ResourceLocation POCKET_SPACE = id("pocket_space");
    public static final ResourceLocation NO_ROOM_AT_ALL = id("no_room_at_all");
    public static final ResourceLocation PACKING_LIGHT = id("packing_light");
    public static final ResourceLocation PACK_MULE = id("pack_mule");
    public static final ResourceLocation HEAVY_LIFTING = id("heavy_lifting");
    public static final ResourceLocation HOME_AWAY_FROM_HOME = id("home_away_from_home");
    public static final ResourceLocation TRAVEL_LIGHT = id("travel_light");
    public static final int JUGGLER_SWITCHES = 10;
    public static final int SECOND_NATURE_SWITCHES = 1000;
    private static final ResourceLocation TAB_BACKGROUND = ResourceIds.of("minecraft", "textures/block/brown_wool.png");

    private InventoryAdvancements() {
    }

    /** Every achievement in the tab. Call once items are registered (during data generation). */
    public static List<ImpactsAdvancementBuilder> all() {
        return List.of(
                ImpactsAdvancementBuilder.of(HANDS_FULL, InventoryItems.satchel()).root(TAB_BACKGROUND)
                        .criterion("switched", SlotSwitchedTrigger.TriggerInstance.anySwitch()),
                ImpactsAdvancementBuilder.of(QUICK_DRAW, Items.IRON_SWORD).parent(HANDS_FULL)
                        .criterion("hit_after_switch", InventoryTriggers.QUICK_DRAW.anyPlayer()),
                ImpactsAdvancementBuilder.of(SWORD_AND_BOARD, Items.SHIELD).parent(HANDS_FULL)
                        .criterion("blocked_with_sword_drawn", blockedWithSwordAndShield()),
                ImpactsAdvancementBuilder.of(JUGGLER, Items.SLIME_BALL).parent(HANDS_FULL)
                        .criterion("ten_quick_switches", SlotSwitchedTrigger.TriggerInstance.recentSwitches(JUGGLER_SWITCHES)),
                ImpactsAdvancementBuilder.of(SECOND_NATURE, Items.CLOCK).parent(JUGGLER).challenge()
                        .criterion("thousand_switches", CountTrigger.TriggerInstance.total(InventoryTriggers.SLOT_SWITCHES_COUNTER, SECOND_NATURE_SWITCHES)),
                ImpactsAdvancementBuilder.of(POCKET_SPACE, Items.LEATHER).parent(HANDS_FULL)
                        .criterion("pockets_filled", InventoryTriggers.POCKETS_FILLED.anyPlayer()),
                ImpactsAdvancementBuilder.of(NO_ROOM_AT_ALL, Items.COBBLESTONE).parent(POCKET_SPACE).hidden()
                        .criterion("pickup_refused", InventoryTriggers.PICKUP_REFUSED.anyPlayer()),
                ImpactsAdvancementBuilder.of(PACKING_LIGHT, InventoryItems.satchel()).parent(POCKET_SPACE)
                        .criterion("wore_satchel", PackWornTrigger.TriggerInstance.wearing(PackKind.SATCHEL)),
                ImpactsAdvancementBuilder.of(PACK_MULE, InventoryItems.backpack()).parent(PACKING_LIGHT)
                        .criterion("wore_backpack", PackWornTrigger.TriggerInstance.wearing(PackKind.BACKPACK)),
                ImpactsAdvancementBuilder.of(HEAVY_LIFTING, InventoryItems.framePack()).parent(PACK_MULE).goal()
                        .criterion("wore_frame_pack", PackWornTrigger.TriggerInstance.wearing(PackKind.FRAME_PACK)),
                ImpactsAdvancementBuilder.of(HOME_AWAY_FROM_HOME, Items.CAMPFIRE).parent(PACKING_LIGHT)
                        .criterion("pack_placed", InventoryTriggers.PACK_PLACED.anyPlayer()),
                ImpactsAdvancementBuilder.of(TRAVEL_LIGHT, Items.FEATHER).parent(HANDS_FULL).challenge()
                        .criterion("entered_nether_light", InventoryTriggers.TRAVEL_LIGHT.anyPlayer()));
    }

    // Vanilla's hurt-player trigger, for a blocked hit while the main hand holds a sword and the off hand a shield.
    private static EntityHurtPlayerTrigger.TriggerInstance blockedWithSwordAndShield() {
        EntityEquipmentPredicate swordAndShield = EntityEquipmentPredicate.Builder.equipment()
                .mainhand(ItemPredicate.Builder.item().of(ItemTags.SWORDS).build())
                .offhand(ItemPredicate.Builder.item().of(Items.SHIELD).build())
                .build();
        EntityPredicate player = EntityPredicate.Builder.entity().equipment(swordAndShield).build();
        return new EntityHurtPlayerTrigger.TriggerInstance(EntityPredicate.wrap(player), DamagePredicate.Builder.damageInstance().blocked(true).build());
    }

    private static ResourceLocation id(String path) {
        return ResourceIds.of(InventoryMod.MOD_ID, path);
    }
}
