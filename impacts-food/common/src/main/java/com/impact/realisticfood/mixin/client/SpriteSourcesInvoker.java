package com.impact.realisticfood.mixin.client;

import com.mojang.serialization.Codec;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Vanilla keeps its sprite source registration private, and Forge 1.20.1 has no event for it. */
@Mixin(SpriteSources.class)
public interface SpriteSourcesInvoker {
    @Invoker("register")
    static SpriteSourceType impactsfood$register(String name, Codec<? extends SpriteSource> codec) {
        throw new AssertionError("Replaced by Mixin");
    }
}
