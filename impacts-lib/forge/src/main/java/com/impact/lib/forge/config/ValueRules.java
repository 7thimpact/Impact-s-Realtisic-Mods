package com.impact.lib.forge.config;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * How one config value is stored, typed and checked: reading the stored object, parsing typed text, the allowed
 * range, and that range in words.
 */
record ValueRules<T>(Function<Object, T> storedValueReader,
                     Function<String, Optional<T>> textParser,
                     Predicate<T> isInAllowedRange,
                     String allowedValues) {
}
