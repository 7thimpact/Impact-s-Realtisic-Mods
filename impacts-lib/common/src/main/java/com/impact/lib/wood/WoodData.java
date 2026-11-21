package com.impact.lib.wood;

import com.impact.lib.ResourceIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Wood type data on items: per-wood parts store {@code {"wood": "minecraft:oak"}}, the namespaced wood type name, and
 * resolve to the matching vanilla item ({@code minecraft:oak_door}). Bamboo's odd names (raft, chest raft, block) and
 * the nether stems are mapped by hand.
 */
public final class WoodData {
    public static final String WOOD_KEY = "wood";
    private static final String MINECRAFT_NAMESPACE = "minecraft";
    private static final String BAMBOO = "bamboo";
    private static final String WOOD_NAME_KEY_PREFIX = "wood.";
    private static final Map<WoodKind, String> BAMBOO_SUFFIXES = Map.of(WoodKind.BOAT, "raft", WoodKind.CHEST_BOAT, "chest_raft");
    private static final List<String> NETHER_WOODS = List.of("crimson", "warped");

    /** Vanilla's wood types, in the order the creative menu lists them. */
    public static final List<ResourceLocation> VANILLA_WOODS = Arrays.stream(new String[]{"oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
            "mangrove", "cherry", "bamboo", "crimson", "warped"}).map(path -> ResourceIds.of(MINECRAFT_NAMESPACE, path)).toList();

    private WoodData() {
    }

    public static Optional<ResourceLocation> get(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(WOOD_KEY)) {
            return Optional.empty();
        }
        return ResourceIds.tryParse(tag.getString(WOOD_KEY));
    }

    public static ItemStack set(ItemStack stack, ResourceLocation wood) {
        stack.getOrCreateTag().putString(WOOD_KEY, wood.toString());
        return stack;
    }

    /** A fresh stack of the item carrying the wood. */
    public static ItemStack of(ItemLike item, ResourceLocation wood) {
        return set(new ItemStack(item), wood);
    }

    /** The NBT a per-wood item of this wood carries, for recipe results and {@code forge:partial_nbt} ingredients. */
    public static CompoundTag tagFor(ResourceLocation wood) {
        CompoundTag tag = new CompoundTag();
        tag.putString(WOOD_KEY, wood.toString());
        return tag;
    }

    /** Every registered wood type, vanilla's first, then modded ones (registered under {@code modid:name}). */
    public static List<ResourceLocation> allWoods() {
        List<ResourceLocation> registered = WoodType.values()
                .map(woodType -> ResourceIds.tryParse(woodType.name()))
                .flatMap(Optional::stream)
                .filter(wood -> !VANILLA_WOODS.contains(wood))
                .collect(Collectors.toList());
        registered.addAll(0, VANILLA_WOODS);
        return List.copyOf(registered);
    }

    /** The vanilla-style item of this kind for the wood, if the wood has one (nether woods have no boats). */
    public static Optional<Item> vanillaItem(ResourceLocation wood, WoodKind kind) {
        String suffix = wood.getPath().equals(BAMBOO) ? BAMBOO_SUFFIXES.getOrDefault(kind, kind.suffix) : kind.suffix;
        return ResourceIds.item(ResourceIds.of(wood.getNamespace(), wood.getPath() + "_" + suffix));
    }

    /** Whether the wood burns: false for woods whose planks are in {@code #minecraft:non_flammable_wood} (crimson, warped). */
    public static boolean isFlammable(ResourceLocation wood) {
        // Vanilla's nether woods are named here too, so data generation (which runs without tags) gets them right.
        if (isVanillaNetherWood(wood)) {
            return false;
        }
        return vanillaItem(wood, WoodKind.PLANKS).map(planks -> !new ItemStack(planks).is(ItemTags.NON_FLAMMABLE_WOOD)).orElse(true);
    }

    private static boolean isVanillaNetherWood(ResourceLocation wood) {
        return wood.getNamespace().equals(MINECRAFT_NAMESPACE) && NETHER_WOODS.contains(wood.getPath());
    }

    /** The item tag of the wood's logs: {@code #minecraft:oak_logs}, {@code #minecraft:crimson_stems}, {@code #minecraft:bamboo_blocks}. */
    public static TagKey<Item> logTag(ResourceLocation wood) {
        String path = wood.getPath();
        String suffix = path.equals(BAMBOO) ? "_blocks" : isVanillaNetherWood(wood) ? "_stems" : "_logs";
        return TagKey.create(Registries.ITEM, ResourceIds.of(wood.getNamespace(), path + suffix));
    }

    /** The wood's display name ("Dark Oak"), from {@code wood.<namespace>.<name>} with the name itself as the fallback. */
    public static Component displayName(ResourceLocation wood) {
        return Component.translatableWithFallback(WOOD_NAME_KEY_PREFIX + wood.getNamespace() + "." + wood.getPath(), titleCase(wood.getPath()));
    }

    public static String nameKey(ResourceLocation wood) {
        return WOOD_NAME_KEY_PREFIX + wood.getNamespace() + "." + wood.getPath();
    }

    public static String titleCase(String woodName) {
        return Arrays.stream(woodName.split("_"))
                .map(word -> word.isEmpty() ? word : word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
