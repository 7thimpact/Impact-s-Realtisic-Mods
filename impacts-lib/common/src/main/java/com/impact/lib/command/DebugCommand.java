package com.impact.lib.command;

import com.impact.lib.ImpactsLib;
import com.impact.lib.config.DebugConfigSection;
import com.impact.lib.config.LibCommonConfig;
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
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

/**
 * {@code /impacts debug [<mod|all> [on|off [<category>]]]}: shows and changes the Impact mods' debug switches.
 * Changes are saved to each mod's COMMON file. In single player the client shares those switches, so
 * client-side categories follow the command too.
 */
final class DebugCommand {
    private static final String LITERAL = "debug";
    private static final String ON_LITERAL = "on";
    private static final String OFF_LITERAL = "off";
    private static final String CATEGORY_ARGUMENT = "category";
    private static final String LIST_SEPARATOR = ", ";
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);

    private DebugCommand() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(LITERAL)
                .executes(context -> showAllStatus(context.getSource()))
                .then(ModArgument.argument(DebugCommand::knownModIds, true)
                        .executes(DebugCommand::showStatus)
                        .then(switchLiteral(ON_LITERAL, true))
                        .then(switchLiteral(OFF_LITERAL, false)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> switchLiteral(String literal, boolean isOn) {
        return Commands.literal(literal)
                .executes(context -> setDebug(context, isOn))
                .then(Commands.argument(CATEGORY_ARGUMENT, StringArgumentType.word())
                        .suggests(DebugCommand::suggestCategories)
                        .executes(context -> setCategory(context, isOn)));
    }

    private static int showAllStatus(CommandSourceStack source) {
        boolean isFamilyDebugOn = LibCommonConfig.get().isFamilyDebugOn.get();
        source.sendSuccess(() -> Component.translatable(LibTranslations.DEBUG_FAMILY_SWITCH, onOff(isFamilyDebugOn)), false);
        Collection<DebugConfigSection> sections = DebugConfigSection.allByModId().values();
        sections.forEach(section -> sendStatus(source, section));
        return sections.size();
    }

    private static int showStatus(CommandContext<CommandSourceStack> context) {
        Optional<List<DebugConfigSection>> targets = findTargets(context);
        targets.ifPresent(sections -> sections.forEach(section -> sendStatus(context.getSource(), section)));
        return targets.map(List::size).orElse(0);
    }

    private static int setDebug(CommandContext<CommandSourceStack> context, boolean isOn) {
        Optional<List<DebugConfigSection>> targets = findTargets(context);
        if (targets.isEmpty()) {
            return 0;
        }
        CommandSourceStack source = context.getSource();
        for (DebugConfigSection section : targets.get()) {
            section.setDebugEnabled(isOn);
            LOG.debug(ImpactsLog.CONFIG_CATEGORY, "debug switch changed by command",
                    "source", source.getTextName(), "mod", section.modId, "enabled", isOn);
            source.sendSuccess(() -> Component.translatable(LibTranslations.DEBUG_CHANGED, section.modId, onOff(isOn)), true);
        }
        warnIfFamilySwitchOff(source, isOn);
        return targets.get().size();
    }

    private static int setCategory(CommandContext<CommandSourceStack> context, boolean isOn) {
        Optional<List<DebugConfigSection>> targets = findTargets(context);
        if (targets.isEmpty()) {
            return 0;
        }
        CommandSourceStack source = context.getSource();
        String category = StringArgumentType.getString(context, CATEGORY_ARGUMENT);
        List<DebugConfigSection> sectionsWithCategory = targets.get().stream()
                .filter(section -> registeredCategories(section).contains(category))
                .toList();
        if (sectionsWithCategory.isEmpty()) {
            sendUnknownCategory(source, targets.get(), category);
            return 0;
        }
        sectionsWithCategory.forEach(section -> changeCategory(source, section, category, isOn));
        warnIfFamilySwitchOff(source, isOn);
        return sectionsWithCategory.size();
    }

    private static void changeCategory(CommandSourceStack source, DebugConfigSection section, String category, boolean isOn) {
        if (isOn) {
            section.turnCategoryOn(category);
        } else {
            section.turnCategoryOff(category);
        }
        LOG.debug(ImpactsLog.CONFIG_CATEGORY, "debug category changed by command",
                "source", source.getTextName(), "mod", section.modId, "category", category, "enabled", isOn);
        source.sendSuccess(() -> Component.translatable(LibTranslations.DEBUG_CATEGORY_CHANGED,
                section.modId, category, onOff(isOn)), true);
    }

    /** The sections the {@code <mod>} argument names, or empty after telling the source the mod is unknown. */
    private static Optional<List<DebugConfigSection>> findTargets(CommandContext<CommandSourceStack> context) {
        List<DebugConfigSection> sections = findTargetsQuietly(context);
        if (sections.isEmpty()) {
            ModArgument.sendUnknownMod(context.getSource(), ModArgument.typedName(context), knownModIds());
            return Optional.empty();
        }
        return Optional.of(sections);
    }

    private static void sendStatus(CommandSourceStack source, DebugConfigSection section) {
        String categoryList = String.join(LIST_SEPARATOR, section.categories.get());
        source.sendSuccess(() -> Component.translatable(LibTranslations.DEBUG_STATUS, section.modId,
                onOff(section.isEnabled.get()), categoryList, onOff(section.isVerbose.get())), false);
    }

    private static void sendUnknownCategory(CommandSourceStack source, List<DebugConfigSection> sections, String category) {
        boolean isSingleMod = sections.size() == 1;
        if (!isSingleMod) {
            source.sendFailure(Component.translatable(LibTranslations.DEBUG_UNKNOWN_CATEGORY_ANY_MOD, category));
            return;
        }
        DebugConfigSection section = sections.get(0);
        String categoryList = String.join(LIST_SEPARATOR, registeredCategories(section));
        source.sendFailure(Component.translatable(LibTranslations.DEBUG_UNKNOWN_CATEGORY, section.modId, category, categoryList));
    }

    private static void warnIfFamilySwitchOff(CommandSourceStack source, boolean isTurningOn) {
        boolean isHiddenByFamilySwitch = isTurningOn && !LibCommonConfig.get().isFamilyDebugOn.get();
        if (!isHiddenByFamilySwitch) {
            return;
        }
        source.sendFailure(Component.translatable(LibTranslations.DEBUG_FAMILY_SWITCH_OFF));
    }

    private static CompletableFuture<Suggestions> suggestCategories(CommandContext<CommandSourceStack> context, SuggestionsBuilder suggestions) {
        Set<String> categories = new TreeSet<>();
        findTargetsQuietly(context).forEach(section -> categories.addAll(registeredCategories(section)));
        return SharedSuggestionProvider.suggest(categories, suggestions);
    }

    private static List<DebugConfigSection> findTargetsQuietly(CommandContext<CommandSourceStack> context) {
        String typedName = ModArgument.typedName(context);
        if (typedName.equals(ModArgument.ALL_MODS)) {
            return List.copyOf(DebugConfigSection.allByModId().values());
        }
        return ModArgument.resolve(typedName, knownModIds()).flatMap(DebugConfigSection::forMod).map(List::of).orElse(List.of());
    }

    private static Set<String> registeredCategories(DebugConfigSection section) {
        return new TreeSet<>(ImpactsLog.of(section.modId).registeredCategories());
    }

    private static Collection<String> knownModIds() {
        return DebugConfigSection.allByModId().keySet();
    }

    private static Component onOff(boolean isOn) {
        return isOn ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
    }
}
