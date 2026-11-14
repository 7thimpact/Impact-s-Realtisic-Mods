package com.impact.lib.forge.config;

import com.impact.lib.config.ConfigFile;
import com.impact.lib.config.ConfigSpecBuilder;
import com.impact.lib.config.ConfigType;
import com.impact.lib.config.ConfigValue;
import com.impact.lib.forge.ForgeModConstruction;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Builds an Impact config file on top of {@link ForgeConfigSpec.Builder}. Must be used during the owning mod's
 * construction, inside {@link ForgeModConstruction#run}, because the file is registered with that mod and
 * listens on its event bus.
 * Numbers are defined without a Forge range: Forge would clamp an out-of-range value, and the specs ask for it
 * to fall back to its default, which {@link ForgeConfigValue} does.
 */
public final class ForgeConfigSpecBuilder implements ConfigSpecBuilder {
    private static final String DEFAULT_COMMENT_FORMAT = "Default: %s";
    private static final String RANGE_FORMAT = "%s ~ %s";
    private static final String RANGE_COMMENT_FORMAT = "Range: %s";
    private static final String NO_RANGE = "any";
    private static final String KEY_SEPARATOR = ".";

    private final ForgeConfigSpec.Builder forgeBuilder = new ForgeConfigSpec.Builder();
    private final List<String> sectionPath = new ArrayList<>();
    private final ForgeConfigFile file;

    public ForgeConfigSpecBuilder(String modId, ConfigType type) {
        this.file = new ForgeConfigFile(modId, type);
    }

    @Override
    public ConfigSpecBuilder pushSection(String name, String... comment) {
        if (comment.length > 0) {
            forgeBuilder.comment(comment);
        }
        forgeBuilder.push(name);
        sectionPath.add(name);
        return this;
    }

    @Override
    public ConfigSpecBuilder popSection() {
        forgeBuilder.pop();
        sectionPath.remove(sectionPath.size() - 1);
        return this;
    }

    @Override
    public ConfigValue<Boolean> defineBoolean(String key, boolean defaultValue, String... comment) {
        comment(comment, defaultValue, NO_RANGE);
        ForgeConfigSpec.BooleanValue forgeValue = forgeBuilder.define(key, defaultValue);
        return track(key, forgeValue, defaultValue, Boolean.class::cast, anyValue(), NO_RANGE);
    }

    @Override
    public ConfigValue<Integer> defineInteger(String key, int defaultValue, int minimum, int maximum, String... comment) {
        String allowedRange = String.format(RANGE_FORMAT, minimum, maximum);
        comment(comment, defaultValue, allowedRange);
        ForgeConfigSpec.ConfigValue<Integer> forgeValue = forgeBuilder.define(key, defaultValue,
                storedValue -> storedValue instanceof Integer);
        Predicate<Integer> isInRange = value -> value >= minimum && value <= maximum;
        return track(key, forgeValue, defaultValue, Integer.class::cast, isInRange, allowedRange);
    }

    @Override
    public ConfigValue<Double> defineDouble(String key, double defaultValue, double minimum, double maximum, String... comment) {
        String allowedRange = String.format(RANGE_FORMAT, minimum, maximum);
        comment(comment, defaultValue, allowedRange);
        // Accept any number, so a whole number such as 1 still reads as 1.0.
        ForgeConfigSpec.ConfigValue<Double> forgeValue = forgeBuilder.define(key, defaultValue,
                storedValue -> storedValue instanceof Number);
        Predicate<Double> isInRange = value -> value >= minimum && value <= maximum;
        return track(key, forgeValue, defaultValue, storedValue -> ((Number) storedValue).doubleValue(), isInRange, allowedRange);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConfigValue<List<String>> defineStringList(String key, List<String> defaultValue, String... comment) {
        comment(comment, defaultValue, NO_RANGE);
        ForgeConfigSpec.ConfigValue<List<? extends String>> forgeValue = forgeBuilder.defineListAllowEmpty(
                List.of(key), () -> defaultValue, element -> element instanceof String);
        // Forge has checked that every element is a string.
        Function<Object, List<String>> listReader = storedValue -> List.copyOf((List<String>) storedValue);
        return track(key, forgeValue, List.copyOf(defaultValue), listReader, anyValue(), NO_RANGE);
    }

    @Override
    public <E extends Enum<E>> ConfigValue<E> defineEnum(String key, E defaultValue, String... comment) {
        comment(comment, defaultValue, NO_RANGE);
        ForgeConfigSpec.EnumValue<E> forgeValue = forgeBuilder.defineEnum(key, defaultValue);
        return track(key, forgeValue, defaultValue, defaultValue.getDeclaringClass()::cast, anyValue(), NO_RANGE);
    }

    @Override
    public ConfigFile register() {
        ForgeConfigSpec spec = forgeBuilder.build();
        file.attachSpec(spec);
        FMLJavaModLoadingContext ownerContext = ForgeModConstruction.currentContext();
        ownerContext.registerConfig(forgeTypeOf(file.type()), spec, file.fileName());
        IEventBus ownerEventBus = ownerContext.getModEventBus();
        ownerEventBus.addListener((ModConfigEvent.Loading event) -> file.handleLoad(event.getConfig()));
        ownerEventBus.addListener((ModConfigEvent.Reloading event) -> file.handleLoad(event.getConfig()));
        ownerEventBus.addListener((ModConfigEvent.Unloading event) -> file.handleUnload(event.getConfig()));
        return file;
    }

    private void comment(String[] comment, Object defaultValue, String allowedRange) {
        List<String> commentLines = new ArrayList<>(Arrays.asList(comment));
        commentLines.add(String.format(DEFAULT_COMMENT_FORMAT, defaultValue));
        if (!allowedRange.equals(NO_RANGE)) {
            commentLines.add(String.format(RANGE_COMMENT_FORMAT, allowedRange));
        }
        forgeBuilder.comment(commentLines.toArray(String[]::new));
    }

    private <T> ConfigValue<T> track(String key, ForgeConfigSpec.ConfigValue<?> forgeValue, T defaultValue,
                                     Function<Object, T> storedValueReader, Predicate<T> isInAllowedRange, String allowedRange) {
        List<String> keyParts = new ArrayList<>(sectionPath);
        keyParts.add(key);
        String fullKey = String.join(KEY_SEPARATOR, keyParts);
        ForgeConfigValue<T> value = new ForgeConfigValue<>(file, fullKey, forgeValue, defaultValue,
                storedValueReader, isInAllowedRange, allowedRange);
        file.addValue(value);
        return value;
    }

    private static <T> Predicate<T> anyValue() {
        return value -> true;
    }

    private static ModConfig.Type forgeTypeOf(ConfigType type) {
        return switch (type) {
            case COMMON -> ModConfig.Type.COMMON;
            case SERVER -> ModConfig.Type.SERVER;
            case CLIENT -> ModConfig.Type.CLIENT;
        };
    }
}
