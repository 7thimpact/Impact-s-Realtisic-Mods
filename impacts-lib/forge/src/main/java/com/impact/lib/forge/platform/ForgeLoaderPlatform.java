package com.impact.lib.forge.platform;

import com.impact.lib.config.ConfigSpecBuilder;
import com.impact.lib.config.ConfigType;
import com.impact.lib.forge.config.ForgeConfigSpecBuilder;
import com.impact.lib.network.NetworkChannel;
import com.impact.lib.platform.LoaderPlatform;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.Optional;

/** Forge's answers to {@link LoaderPlatform}. Found through {@code META-INF/services}. */
public final class ForgeLoaderPlatform implements LoaderPlatform {
    private static final String LOADER_NAME = "forge";

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
}
