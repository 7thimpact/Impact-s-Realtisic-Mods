package com.impact.realisticfood.mixin;

import com.impact.realisticfood.food.StyledTagInheritance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Styled foods join their base food's item tags as the item tags are built. */
@Mixin(TagLoader.class)
public abstract class TagLoaderMixin<T> {
    private static final String ITEM_TAG_DIRECTORY = "tags/items";

    @Shadow
    @Final
    private String directory;

    @Inject(method = "build(Ljava/util/Map;)Ljava/util/Map;", at = @At("RETURN"))
    private void impactsfood$styledFoodsInheritTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> builders,
                                                     CallbackInfoReturnable<Map<ResourceLocation, Collection<T>>> callback) {
        if (ITEM_TAG_DIRECTORY.equals(directory)) {
            StyledTagInheritance.addStyledFoods(callback.getReturnValue());
        }
    }
}
