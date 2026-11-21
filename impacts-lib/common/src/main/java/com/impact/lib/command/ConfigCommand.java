package com.impact.lib.command;

import com.impact.lib.ImpactsLib;
import com.impact.lib.config.ConfigFile;
import com.impact.lib.config.ConfigFileRegistry;
import com.impact.lib.config.ConfigType;
import com.impact.lib.config.ConfigValue;
import com.impact.lib.lang.LibTranslations;
import com.impact.lib.log.ImpactsLog;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * {@code /impacts config <mod> [key] [value]}: shows a mod's SERVER config values, or changes one and saves it
 * to the world's file. A value that can't be read, or is outside its allowed range, is refused.
 */
final class ConfigCommand {
    private static final String LITERAL = "config";
    private static final String KEY_ARGUMENT = "key";
    private static final String VALUE_ARGUMENT = "value";
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);

    private ConfigCommand() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(LITERAL)
                .then(ModArgument.argument(ConfigCommand::modsWithServerConfig, false)
                        .executes(ConfigCommand::listValues)
                        .then(Commands.argument(KEY_ARGUMENT, StringArgumentType.word())
                                .suggests(ConfigCommand::suggestKeys)
                                .executes(ConfigCommand::showValue)
                                .then(Commands.argument(VALUE_ARGUMENT, StringArgumentType.greedyString())
                                        .executes(ConfigCommand::setValue))));
    }

    private static int listValues(CommandContext<CommandSourceStack> context) {
        Optional<ConfigFile> file = findServerFile(context);
        if (file.isEmpty()) {
            return 0;
        }
        CommandSourceStack source = context.getSource();
        List<ConfigValue<?>> values = file.get().values();
        if (values.isEmpty()) {
            source.sendSuccess(() -> Component.translatable(LibTranslations.CONFIG_NO_VALUES, file.get().modId()), false);
            return 0;
        }
        values.forEach(value -> sendValue(source, value));
        return values.size();
    }

    private static int showValue(CommandContext<CommandSourceStack> context) {
        Optional<ConfigValue<?>> value = findServerFile(context).flatMap(file -> findValue(context, file));
        value.ifPresent(foundValue -> sendValue(context.getSource(), foundValue));
        return value.isPresent() ? 1 : 0;
    }

    private static int setValue(CommandContext<CommandSourceStack> context) {
        Optional<ConfigFile> file = findServerFile(context);
        Optional<ConfigValue<?>> value = file.flatMap(foundFile -> findValue(context, foundFile));
        if (value.isEmpty()) {
            return 0;
        }
        String typedValue = StringArgumentType.getString(context, VALUE_ARGUMENT);
        return applyTypedValue(context.getSource(), file.get(), value.get(), typedValue);
    }

    private static <T> int applyTypedValue(CommandSourceStack source, ConfigFile file, ConfigValue<T> value, String typedValue) {
        Optional<T> newValue = value.parse(typedValue).filter(value::isAllowed);
        if (newValue.isEmpty()) {
            source.sendFailure(Component.translatable(LibTranslations.CONFIG_INVALID_VALUE, typedValue, value.key(), value.allowedValues()));
            return 0;
        }
        value.set(newValue.get());
        LOG.debug(ImpactsLog.CONFIG_CATEGORY, "config value set by command",
                "source", source.getTextName(), "file", file.fileName(), "key", value.key(), "value", newValue.get());
        source.sendSuccess(() -> Component.translatable(LibTranslations.CONFIG_CHANGED,
                value.key(), String.valueOf(newValue.get()), file.fileName()), true);
        return 1;
    }

    /** The {@code <mod>} argument's SERVER file, or empty after telling the source the mod has none. */
    private static Optional<ConfigFile> findServerFile(CommandContext<CommandSourceStack> context) {
        String typedName = ModArgument.typedName(context);
        Collection<String> knownModIds = modsWithServerConfig();
        Optional<ConfigFile> file = ModArgument.resolve(typedName, knownModIds)
                .flatMap(modId -> ConfigFileRegistry.find(modId, ConfigType.SERVER));
        if (file.isEmpty()) {
            ModArgument.sendUnknownMod(context.getSource(), typedName, knownModIds);
        }
        return file;
    }

    private static Optional<ConfigValue<?>> findValue(CommandContext<CommandSourceStack> context, ConfigFile file) {
        String key = StringArgumentType.getString(context, KEY_ARGUMENT);
        Optional<ConfigValue<?>> value = file.values().stream().filter(candidate -> candidate.key().equals(key)).findFirst();
        if (value.isEmpty()) {
            context.getSource().sendFailure(Component.translatable(LibTranslations.CONFIG_UNKNOWN_KEY, file.modId(), key));
        }
        return value;
    }

    private static void sendValue(CommandSourceStack source, ConfigValue<?> value) {
        source.sendSuccess(() -> Component.translatable(LibTranslations.CONFIG_VALUE, value.key(),
                String.valueOf(value.get()), String.valueOf(value.defaultValue()), value.allowedValues()), false);
    }

    private static CompletableFuture<Suggestions> suggestKeys(CommandContext<CommandSourceStack> context, SuggestionsBuilder suggestions) {
        List<String> keys = ModArgument.resolve(ModArgument.typedName(context), modsWithServerConfig())
                .flatMap(modId -> ConfigFileRegistry.find(modId, ConfigType.SERVER))
                .map(file -> file.values().stream().map(ConfigValue::key).toList())
                .orElse(List.of());
        return SharedSuggestionProvider.suggest(keys, suggestions);
    }

    private static Collection<String> modsWithServerConfig() {
        return ConfigFileRegistry.ofType(ConfigType.SERVER).stream().map(ConfigFile::modId).sorted().toList();
    }
}
