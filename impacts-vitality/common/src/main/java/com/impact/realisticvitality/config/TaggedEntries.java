package com.impact.realisticvitality.config;

import com.impact.lib.ResourceIds;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Predicate;

/**
 * Reads config lists whose entries name a registry entry or a tag, with or without a value: {@code minecraft:desert},
 * {@code #minecraft:is_badlands}, {@code minecraft:lava=25} or {@code #minecraft:fire=15}. The first matching entry wins.
 */
public final class TaggedEntries {
    private static final String TAG_PREFIX = "#";
    private static final String VALUE_SEPARATOR = "=";

    private TaggedEntries() {
    }

    /** The value of the first entry naming the thing (by ID) or a tag it's in. */
    public static Optional<String> findValue(List<String> entries, Predicate<ResourceLocation> hasId, Predicate<ResourceLocation> isInTag) {
        for (String entry : entries) {
            String[] keyAndValue = entry.split(VALUE_SEPARATOR, 2);
            if (keyAndValue.length == 2 && matches(keyAndValue[0].trim(), hasId, isInTag)) {
                return Optional.of(keyAndValue[1].trim());
            }
        }
        return Optional.empty();
    }

    public static OptionalDouble findNumber(List<String> entries, Predicate<ResourceLocation> hasId, Predicate<ResourceLocation> isInTag) {
        Optional<String> value = findValue(entries, hasId, isInTag);
        return value.isPresent() ? parseNumber(value.get()) : OptionalDouble.empty();
    }

    /** Whether any entry (written without a value) names the thing or a tag it's in. */
    public static boolean contains(List<String> entries, Predicate<ResourceLocation> hasId, Predicate<ResourceLocation> isInTag) {
        return entries.stream().anyMatch(entry -> matches(entry.trim(), hasId, isInTag));
    }

    private static boolean matches(String key, Predicate<ResourceLocation> hasId, Predicate<ResourceLocation> isInTag) {
        if (key.startsWith(TAG_PREFIX)) {
            return ResourceIds.tryParse(key.substring(TAG_PREFIX.length())).filter(isInTag).isPresent();
        }
        return ResourceIds.tryParse(key).filter(hasId).isPresent();
    }

    private static OptionalDouble parseNumber(String text) {
        try {
            return OptionalDouble.of(Double.parseDouble(text));
        } catch (NumberFormatException notANumber) {
            return OptionalDouble.empty();
        }
    }
}
