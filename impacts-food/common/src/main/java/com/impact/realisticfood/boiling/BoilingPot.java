package com.impact.realisticfood.boiling;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.debug.FoodLogCategories;
import com.impact.realisticfood.food.FixedFood;
import com.impact.realisticfood.food.StyleLog;
import com.impact.realisticfood.registry.FoodItems;
import com.impact.realisticfood.soup.SoupBatch;
import com.impact.realisticfood.soup.SoupMatcher;
import com.impact.realisticfood.soup.SoupRecipe;
import com.impact.realisticfood.soup.SoupRecipes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.block.SuspiciousEffectHolder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * The water in one pot: it warms up over a heat source, boils, keeps boiling for a while once the heat is gone, and
 * (in a cauldron) slowly boils away. Items in it boil on their own, or, once nothing has gone in for a moment, become
 * the soup that uses the most of them; soup ingredients nothing uses turn into Watery Soup. Ticked by the block that
 * holds it, through {@link PotHost}.
 */
public final class BoilingPot {
    /** Watery Soup has no recipe; this names it in logs and saves. */
    public static final ResourceLocation WATERY_SOUP_ID = ResourceIds.of(FoodMod.MOD_ID, "watery_soup");
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    private static final String HEAT_TICKS_KEY = "HeatTicks";
    private static final String BOILING_KEY = "Boiling";
    private static final String COOL_TICKS_KEY = "CoolTicks";
    private static final String EVAPORATION_TICKS_KEY = "EvaporationTicks";
    private static final String ITEMS_KEY = "Items";
    private static final String SOUP_KEY = "Soup";
    private static final String TICKS_SINCE_ADDED_KEY = "TicksSinceAdded";
    private static final String BOILING_TICKS_SINCE_ADDED_KEY = "BoilingTicksSinceAdded";

    private int heatTicks;
    private boolean isBoiling;
    private int coolTicks;
    private int evaporationTicks;
    private final List<PotItem> items = new ArrayList<>();
    private SoupBatch soup;
    private int ticksSinceAdded;
    private int boilingTicksSinceAdded;

    public boolean isBoiling() {
        return isBoiling;
    }

    /** Whether the pot is warming up or boiling, so it takes items. */
    public boolean isHot(PotHost host) {
        return isBoiling || host.isHeated();
    }

    public List<PotItem> items() {
        return List.copyOf(items);
    }

    public Optional<SoupBatch> soup() {
        return Optional.ofNullable(soup);
    }

    public boolean isSoupReady() {
        return soup != null && soup.isReady();
    }

    /** Whether the pot holds a soup or its ingredients, so its water mustn't be bottled. */
    public boolean holdsSoup() {
        return soup != null || items.stream().anyMatch(PotItem::isSoupIngredient);
    }

    /** Nothing left to track: cold, not boiling, and empty. */
    public boolean isIdle(PotHost host) {
        return !isBoiling && heatTicks == 0 && items.isEmpty() && soup == null && !host.isHeated();
    }

    /** Brings the water straight to a boil (a Hot Pebble does this). */
    public void boilNow(PotHost host) {
        if (!isBoiling) {
            isBoiling = true;
            logHeat(host, "boiling");
        }
        heatTicks = host.heatUpTicks();
        coolTicks = 0;
    }

    /** Adds one of the stack's items, when the pot is hot, has room, and a boiling or soup recipe takes it. */
    public boolean tryAdd(PotHost host, ItemStack stack) {
        if (!isHot(host) || stack.isEmpty()) {
            return false;
        }
        Optional<BoilingRecipe> boiling = BoilingRecipes.find(host.level(), stack, host.kind());
        boolean isSoupIngredient = canTakeSoupIngredients() && SoupRecipes.isIngredient(host.level(), stack, host.kind());
        if ((boiling.isEmpty() && !isSoupIngredient) || !hasRoomFor(host, isSoupIngredient)) {
            return false;
        }
        ItemStack input = stack.copyWithCount(1);
        int boilTicks = boiling.map(recipe -> BoilingRecipes.boilTicks(recipe, input)).orElse(0);
        items.add(new PotItem(input, boiling.map(recipe -> recipe.result().copy()), boilTicks, isSoupIngredient));
        ticksSinceAdded = 0;
        boilingTicksSinceAdded = 0;
        LOG.debug(isSoupIngredient ? FoodLogCategories.SOUP : FoodLogCategories.BOILING, "item added", "pot", host.kind().logName,
                "pos", host.position(), "input", ResourceIds.itemId(input.getItem()), "items", items.size());
        return true;
    }

    /** Takes one serving of a ready soup. Every few servings use a level of water. */
    public Optional<ItemStack> takeServing(PotHost host) {
        if (!isSoupReady()) {
            return Optional.empty();
        }
        ItemStack serving = soup.takeServing();
        boolean usesWater = soup.servingsTaken() % FoodServerConfig.get().soup.servingsPerWaterLevel.get() == 0;
        if (soup.servingsLeft() == 0) {
            soup = null;
        }
        if (usesWater) {
            host.useWaterForServings();
        }
        return Optional.of(serving);
    }

    /** Takes every item out unboiled, for when the pot is emptied or broken. A soup in it is lost. */
    public List<ItemStack> takeAllInputs() {
        List<ItemStack> inputs = items.stream().map(item -> item.input().copy()).toList();
        items.clear();
        if (soup != null) {
            LOG.debug(FoodLogCategories.SOUP, "soup lost", "soup", soup.recipeId(), "servingsLeft", soup.servingsLeft());
            soup = null;
        }
        return inputs;
    }

    public void tick(PotHost host) {
        updateHeat(host);
        ticksSinceAdded++;
        if (ticksSinceAdded == FoodServerConfig.get().soup.settleTicks.get()) {
            settleSoup(host);
        }
        if (!isBoiling) {
            return;
        }
        boilingTicksSinceAdded++;
        cookSoup(host);
        boilItems(host);
        turnLeftoversIntoWaterySoup(host);
        evaporate(host);
    }

    private boolean canTakeSoupIngredients() {
        return soup == null || !soup.isReady();
    }

    private boolean hasRoomFor(PotHost host, boolean isSoupIngredient) {
        long soupIngredients = items.stream().filter(PotItem::isSoupIngredient).count();
        long singleItems = items.size() - soupIngredients;
        return isSoupIngredient ? soupIngredients < host.soupIngredientSlots() : singleItems < host.itemSlots();
    }

    private void updateHeat(PotHost host) {
        if (host.isHeated()) {
            warmUp(host);
            return;
        }
        if (isBoiling) {
            coolOff(host);
            return;
        }
        heatTicks = 0;
    }

    private void warmUp(PotHost host) {
        coolTicks = 0;
        if (isBoiling) {
            return;
        }
        if (heatTicks == 0) {
            logHeat(host, "heating");
        }
        heatTicks++;
        if (heatTicks >= host.heatUpTicks()) {
            isBoiling = true;
            logHeat(host, "boiling");
        }
    }

    private void coolOff(PotHost host) {
        coolTicks++;
        if (coolTicks < host.coolDownTicks()) {
            return;
        }
        isBoiling = false;
        heatTicks = 0;
        coolTicks = 0;
        logHeat(host, "cooled off");
    }

    // Once nothing has gone in for a moment, the soup ingredients become the soup that uses the most of them, or more
    // sets of the soup already cooking.
    private void settleSoup(PotHost host) {
        List<PotItem> ingredients = items.stream().filter(PotItem::isSoupIngredient).toList();
        if (ingredients.isEmpty() || (soup != null && soup.isReady())) {
            return;
        }
        List<ItemStack> stacks = ingredients.stream().map(PotItem::input).toList();
        if (soup == null) {
            SoupMatcher.bestMatch(SoupRecipes.recipesFor(host.level(), host.kind()), stacks, host.maxServings())
                    .ifPresent(match -> startSoup(host, match, ingredients));
            return;
        }
        int servingsLeftToAdd = host.maxServings() - soup.servings();
        SoupRecipes.byId(host.level(), soup.recipeId())
                .flatMap(recipe -> SoupMatcher.mostSets(recipe, stacks, servingsLeftToAdd / recipe.bowlsPerSet()))
                .ifPresent(match -> addSets(host, match, ingredients));
    }

    private void startSoup(PotHost host, SoupMatcher.SoupMatch match, List<PotItem> ingredients) {
        SoupRecipe recipe = match.recipe();
        List<PotItem> used = removeUsed(match, ingredients);
        ItemStack serving = recipe.result().copy();
        addSuspiciousEffect(serving, used);
        soup = new SoupBatch(recipe.getId(), serving, match.sets() * recipe.bowlsPerSet(), recipe.cookTicks());
        LOG.debug(FoodLogCategories.SOUP, "batch matched", "pot", host.kind().logName, "pos", host.position(), "soup", recipe.getId(),
                "sets", match.sets(), "servings", soup.servings());
    }

    private void addSets(PotHost host, SoupMatcher.SoupMatch match, List<PotItem> ingredients) {
        removeUsed(match, ingredients);
        soup.addServings(match.sets() * match.recipe().bowlsPerSet());
        LOG.debug(FoodLogCategories.SOUP, "sets added", "pos", host.position(), "soup", soup.recipeId(), "servings", soup.servings());
    }

    private List<PotItem> removeUsed(SoupMatcher.SoupMatch match, List<PotItem> ingredients) {
        List<PotItem> used = match.usedIndexes().stream().map(ingredients::get).toList();
        items.removeAll(used);
        return used;
    }

    // A suspicious stew takes the effect of the flower that went into it, as in vanilla.
    private static void addSuspiciousEffect(ItemStack serving, List<PotItem> used) {
        if (!serving.is(Items.SUSPICIOUS_STEW)) {
            return;
        }
        used.stream().map(item -> SuspiciousEffectHolder.tryGet(item.input().getItem()))
                .filter(holder -> holder != null)
                .findFirst()
                .ifPresent(holder -> SuspiciousStewItem.saveMobEffect(serving, holder.getSuspiciousEffect(), holder.getEffectDuration()));
    }

    private void cookSoup(PotHost host) {
        if (soup != null && soup.cookOneTick()) {
            LOG.debug(FoodLogCategories.SOUP, "soup ready", "pot", host.kind().logName, "pos", host.position(), "soup", soup.recipeId(),
                    "servings", soup.servings());
        }
    }

    // Soup ingredients nothing used in a whole cook's time still give something: Watery Soup.
    private void turnLeftoversIntoWaterySoup(PotHost host) {
        boolean hasWaited = boilingTicksSinceAdded >= FoodServerConfig.get().soup.cookTicks.get();
        List<PotItem> leftovers = items.stream().filter(PotItem::isOnlySoupIngredient).toList();
        if (soup != null || !hasWaited || leftovers.isEmpty()) {
            return;
        }
        items.removeAll(leftovers);
        soup = SoupBatch.readyNow(WATERY_SOUP_ID, new ItemStack(FoodItems.fixedFood(FixedFood.WATERY_SOUP)), SoupRecipe.DEFAULT_BOWLS_PER_SET);
        LOG.debug(FoodLogCategories.SOUP, "watery soup", "pot", host.kind().logName, "pos", host.position(),
                "ingredients", leftovers.stream().map(item -> ResourceIds.itemId(item.input().getItem()).toString()).toList());
    }

    private void boilItems(PotHost host) {
        Iterator<PotItem> boiling = items.iterator();
        while (boiling.hasNext()) {
            PotItem item = boiling.next();
            if (!item.boilOneTick()) {
                continue;
            }
            boiling.remove();
            ItemStack result = item.boiledResult().orElseThrow();
            LOG.debug(FoodLogCategories.BOILING, "boiled", "pot", host.kind().logName, "pos", host.position(),
                    "input", ResourceIds.itemId(item.input().getItem()), "result", ResourceIds.itemId(result.getItem()), "ticks", item.ticksNeeded());
            StyleLog.onCooked(BoilingRecipes.ID.toString(), item.input(), result);
            host.release(result.copy());
        }
    }

    private void evaporate(PotHost host) {
        int interval = host.evaporationTicks();
        if (interval <= 0) {
            return;
        }
        evaporationTicks++;
        if (evaporationTicks < interval) {
            return;
        }
        evaporationTicks = 0;
        boolean hasWaterLeft = host.evaporateOneLevel();
        LOG.debug(FoodLogCategories.EVAPORATION, hasWaterLeft ? "level lost" : "boiled dry", "pot", host.kind().logName, "pos", host.position());
    }

    private void logHeat(PotHost host, String event) {
        if (LOG.isEnabled(FoodLogCategories.HEAT)) {
            LOG.debug(FoodLogCategories.HEAT, event, "pot", host.kind().logName, "pos", host.position(),
                    "source", ResourceIds.blockId(host.level().getBlockState(host.position().below()).getBlock()));
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(HEAT_TICKS_KEY, heatTicks);
        tag.putBoolean(BOILING_KEY, isBoiling);
        tag.putInt(COOL_TICKS_KEY, coolTicks);
        tag.putInt(EVAPORATION_TICKS_KEY, evaporationTicks);
        ListTag itemList = new ListTag();
        items.forEach(item -> itemList.add(item.save()));
        tag.put(ITEMS_KEY, itemList);
        if (soup != null) {
            tag.put(SOUP_KEY, soup.save());
        }
        tag.putInt(TICKS_SINCE_ADDED_KEY, ticksSinceAdded);
        tag.putInt(BOILING_TICKS_SINCE_ADDED_KEY, boilingTicksSinceAdded);
        return tag;
    }

    public static BoilingPot load(CompoundTag tag) {
        BoilingPot pot = new BoilingPot();
        pot.heatTicks = tag.getInt(HEAT_TICKS_KEY);
        pot.isBoiling = tag.getBoolean(BOILING_KEY);
        pot.coolTicks = tag.getInt(COOL_TICKS_KEY);
        pot.evaporationTicks = tag.getInt(EVAPORATION_TICKS_KEY);
        tag.getList(ITEMS_KEY, Tag.TAG_COMPOUND).forEach(itemTag -> pot.items.add(PotItem.load((CompoundTag) itemTag)));
        pot.soup = tag.contains(SOUP_KEY) ? SoupBatch.load(tag.getCompound(SOUP_KEY)).orElse(null) : null;
        pot.ticksSinceAdded = tag.getInt(TICKS_SINCE_ADDED_KEY);
        pot.boilingTicksSinceAdded = tag.getInt(BOILING_TICKS_SINCE_ADDED_KEY);
        return pot;
    }
}
