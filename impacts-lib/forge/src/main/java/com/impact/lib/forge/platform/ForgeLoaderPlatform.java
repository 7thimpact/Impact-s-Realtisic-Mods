package com.impact.lib.forge.platform;

import com.impact.lib.config.ConfigSpecBuilder;
import com.impact.lib.config.ConfigType;
import com.impact.lib.forge.ForgeModConstruction;
import com.impact.lib.forge.config.ForgeConfigSpecBuilder;
import com.impact.lib.network.NetworkChannel;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.MenuWithDataFactory;
import com.impact.lib.registry.ModRegistrar;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkHooks;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/** Forge's answers to {@link LoaderPlatform}. Found through {@code META-INF/services}. */
public final class ForgeLoaderPlatform implements LoaderPlatform {
    private static final String LOADER_NAME = "forge";
    private static final Map<String, ModRegistrar> REGISTRARS_BY_MOD = new ConcurrentHashMap<>();

    @Override
    public String loaderName() {
        return LOADER_NAME;
    }

    @Override
    public String loaderVersion() {
        return FMLLoader.versionInfo().forgeVersion();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public Optional<String> modVersion(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(container -> container.getModInfo().getVersion().toString());
    }

    @Override
    public Path gameDirectory() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public ConfigSpecBuilder configBuilder(String modId, ConfigType type) {
        return new ForgeConfigSpecBuilder(modId, type);
    }

    @Override
    public void registerCriterionTrigger(CriterionTrigger<?> trigger) {
        // Forge's access transformer makes this vanilla method public.
        CriteriaTriggers.register(trigger);
    }

    @Override
    public NetworkChannel createChannel(ResourceLocation channelId, String protocolVersion) {
        return new ForgeNetworkChannel(channelId, protocolVersion);
    }

    @Override
    public boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    /** The first call for a mod must come during its construction, so its deferred registers reach its event bus. */
    @Override
    public ModRegistrar registrar(String modId) {
        return REGISTRARS_BY_MOD.computeIfAbsent(modId,
                newModId -> new ForgeModRegistrar(newModId, ForgeModConstruction.currentContext().getModEventBus()));
    }

    // Forge's access transformer makes vanilla's block entity factory type public.
    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
        return BlockEntityType.Builder.of(factory::apply, blocks).build(null);
    }

    @Override
    public <M extends AbstractContainerMenu> MenuType<M> menuTypeWithData(MenuWithDataFactory<M> clientFactory) {
        return IForgeMenuType.create(clientFactory::create);
    }

    @Override
    public void openMenuWithData(ServerPlayer player, MenuProvider menuProvider, Consumer<FriendlyByteBuf> extraDataWriter) {
        NetworkHooks.openScreen(player, menuProvider, extraDataWriter);
    }
}
