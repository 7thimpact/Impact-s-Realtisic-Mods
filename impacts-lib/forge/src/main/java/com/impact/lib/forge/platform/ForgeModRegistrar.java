package com.impact.lib.forge.platform;

import com.impact.lib.registry.ModRegistrar;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/** One mod's registrations on Forge: a deferred register per registry, attached to the mod's event bus. */
final class ForgeModRegistrar implements ModRegistrar {
    private final String modId;
    private final IEventBus modEventBus;
    private final Map<ResourceKey<?>, DeferredRegister<?>> registersByRegistry = new ConcurrentHashMap<>();

    ForgeModRegistrar(String modId, IEventBus modEventBus) {
        this.modId = modId;
        this.modEventBus = modEventBus;
    }

    @Override
    public <R, T extends R> Supplier<T> register(ResourceKey<? extends Registry<R>> registryKey, String path, Supplier<T> factory) {
        return deferredRegisterFor(registryKey).register(path, factory);
    }

    @SuppressWarnings("unchecked")
    private <R> DeferredRegister<R> deferredRegisterFor(ResourceKey<? extends Registry<R>> registryKey) {
        return (DeferredRegister<R>) registersByRegistry.computeIfAbsent(registryKey, key -> {
            DeferredRegister<R> deferredRegister = DeferredRegister.create(registryKey, modId);
            deferredRegister.register(modEventBus);
            return deferredRegister;
        });
    }
}
