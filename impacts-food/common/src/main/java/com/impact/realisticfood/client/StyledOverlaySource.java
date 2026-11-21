package com.impact.realisticfood.client;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.debug.FoodLogCategories;
import com.impact.realisticfood.food.FoodBase;
import com.impact.realisticfood.food.FoodStyle;
import com.impact.realisticfood.food.StyledTextures;
import com.impact.realisticfood.mixin.client.SpriteSourcesInvoker;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Makes each styled food's overlay sprite: the style's shared overlay (cracks, toast marks or a wet shine), trimmed to
 * the base food's own texture, so the marks land only on the food. Built from whatever textures are loaded, so
 * resource packs' food textures get matching overlays. The item atlas lists it as {@code impactsfood:styled_overlays}.
 */
public final class StyledOverlaySource implements SpriteSource {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    private static final StyledOverlaySource INSTANCE = new StyledOverlaySource();
    private static final Codec<StyledOverlaySource> CODEC = MapCodec.unit(INSTANCE).codec();
    private static final int TRANSPARENT = 0;
    private static final int ALPHA_SHIFT = 24;
    private static final int ALPHA_MASK = 0xFF;
    private static SpriteSourceType type;

    /** Called once on the client, during mod construction, before any atlas loads. */
    public static void registerType() {
        type = SpriteSourcesInvoker.impactsfood$register(ResourceIds.of(FoodMod.MOD_ID, "styled_overlays").toString(), CODEC);
    }

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        for (FoodBase base : FoodBase.values()) {
            for (FoodStyle style : FoodStyle.values()) {
                if (base.has(style)) {
                    ResourceLocation spriteId = StyledTextures.overlaySprite(base, style);
                    output.add(spriteId, () -> trimmedOverlay(resourceManager, spriteId, StyledTextures.baseTexture(base), StyledTextures.styleOverlay(style)));
                }
            }
        }
    }

    @Override
    public SpriteSourceType type() {
        return type;
    }

    // Forge deprecates this constructor for one taking its own texture metadata, which loader-independent code can't
    // name; this one leaves that metadata empty, which is what a plain texture has.
    @SuppressWarnings("deprecation")
    private static SpriteContents trimmedOverlay(ResourceManager resources, ResourceLocation spriteId, ResourceLocation baseTexture,
                                                 ResourceLocation overlayTexture) {
        try (NativeImage base = readTexture(resources, baseTexture); NativeImage overlay = readTexture(resources, overlayTexture)) {
            int size = base.getWidth();
            NativeImage trimmed = new NativeImage(size, size, true);
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    boolean isOnFood = alphaOf(base.getPixelRGBA(x, y)) > 0;
                    int overlayPixel = overlay.getPixelRGBA(x * overlay.getWidth() / size, y * overlay.getHeight() / size);
                    trimmed.setPixelRGBA(x, y, isOnFood ? overlayPixel : TRANSPARENT);
                }
            }
            return new SpriteContents(spriteId, new FrameSize(size, size), trimmed, AnimationMetadataSection.EMPTY);
        } catch (IOException missingTexture) {
            LOG.warn(FoodLogCategories.STYLES, "styled overlay not made", "sprite", spriteId, "reason", missingTexture.getMessage());
            return null;
        }
    }

    private static NativeImage readTexture(ResourceManager resources, ResourceLocation texture) throws IOException {
        Optional<Resource> resource = resources.getResource(TEXTURE_ID_CONVERTER.idToFile(texture));
        if (resource.isEmpty()) {
            throw new IOException("missing texture " + texture);
        }
        try (InputStream stream = resource.get().open()) {
            return NativeImage.read(stream);
        }
    }

    private static int alphaOf(int pixel) {
        return (pixel >>> ALPHA_SHIFT) & ALPHA_MASK;
    }
}
