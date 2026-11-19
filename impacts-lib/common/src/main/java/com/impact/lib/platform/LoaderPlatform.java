package com.impact.lib.platform;

import com.impact.lib.config.ConfigSpecBuilder;
import com.impact.lib.config.ConfigType;
import com.impact.lib.event.WaterFilled;
import com.impact.lib.network.NetworkChannel;
import com.impact.lib.registry.MenuWithDataFactory;
import com.impact.lib.registry.ModRegistrar;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * What the running mod loader provides to loader-independent code. Each loader module ships one
 * implementation, listed in {@code META-INF/services/com.impact.lib.platform.LoaderPlatform}.
 */
public interface LoaderPlatform {
    static LoaderPlatform current() {
        return LoaderPlatformLookup.LOADED_PLATFORM;
    }

    /** The loader's name as written in logs, for example {@code forge}. */
    String loaderName();

    String loaderVersion();

    boolean isModLoaded(String modId);

    Optional<String> modVersion(String modId);

    /** The game directory: the folder that holds {@code config/}, {@code logs/} and {@code mods/}. */
    Path gameDirectory();

    /** Starts a config file for a mod. The builder's {@code register()} registers the file with the loader. */
    ConfigSpecBuilder configBuilder(String modId, ConfigType type);

    /** Registers an advancement trigger. Vanilla keeps this private; each loader opens it its own way. */
    void registerCriterionTrigger(CriterionTrigger<?> trigger);

    /** Creates a network channel. Call during mod construction. */
    NetworkChannel createChannel(ResourceLocation channelId, String protocolVersion);

    /** Whether this game instance is a client (single player or connected to a server). */
    boolean isClient();

    /** Whether the player is a loader's stand-in for a machine (Forge's FakePlayer), not a person. */
    boolean isFakePlayer(Player player);

    /** What an item leaves in a crafting grid after a craft (a milk bucket leaves a bucket), or an empty stack. */
    ItemStack craftingRemainder(ItemStack stack);

    /** Posts Lib's water-filled event on the loader's own event bus. */
    void postWaterFilled(WaterFilled event);

    /**
     * Tells other mods an entity finished using an item, as if it had held it for {@code useTicks} (Forge's
     * {@code LivingEntityUseItemEvent.Finish}). For eating that skips the usual hold, like Food's soup from the pot.
     */
    void postItemUseFinished(LivingEntity entity, ItemStack used, int useTicks, ItemStack result);

    /** The player taking a crafting result right now, when the loader tracks it (Forge does, around the take). */
    Optional<Player> craftingPlayer();

    /** The registrar for a mod's game objects. Call during that mod's construction. */
    ModRegistrar registrar(String modId);

    /** A block entity type for these blocks. Vanilla keeps the factory type private; each loader opens it its own way. */
    <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks);

    /**
     * The installed mods whose own files hold this data file, such as {@code minecraft:recipes/bread.json} for
     * {@code data/minecraft/recipes/bread.json}. Loaders merge mod data into one pack, so a resource can't say this itself.
     */
    Set<String> modsWithDataFile(ResourceLocation fileLocation);

    /** A builder for a mod's creative tab. Vanilla's builder asks for a row and column, which only fit vanilla's own tabs. */
    CreativeModeTab.Builder creativeTabBuilder();

    /** A menu type whose client copy is built from extra data the server sends (a slot count, a position, ...). */
    <M extends AbstractContainerMenu> MenuType<M> menuTypeWithData(MenuWithDataFactory<M> clientFactory);

    /** Opens a menu made with {@link #menuTypeWithData}, sending the extra data that its client factory reads. */
    void openMenuWithData(ServerPlayer player, MenuProvider menuProvider, Consumer<FriendlyByteBuf> extraDataWriter);
}
