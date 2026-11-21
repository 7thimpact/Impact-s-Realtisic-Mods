package com.impact.lib.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

/**
 * Registers one mod's game objects (items, blocks, menus, recipe serializers, ...) with the running loader. Get it
 * with {@code LoaderPlatform.current().registrar(modId)} and register everything during mod construction.
 */
public interface ModRegistrar {
    /**
     * Registers the object made by {@code factory} as {@code <modId>:<path>}. The returned supplier gives the object
     * once the loader has filled the registries; don't call it before then.
     */
    <R, T extends R> Supplier<T> register(ResourceKey<? extends Registry<R>> registryKey, String path, Supplier<T> factory);
}
