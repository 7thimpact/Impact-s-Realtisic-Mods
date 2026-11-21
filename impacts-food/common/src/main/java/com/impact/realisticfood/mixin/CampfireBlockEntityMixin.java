package com.impact.realisticfood.mixin;

import com.impact.realisticfood.spit.Spit;
import com.impact.realisticfood.spit.SpitHolder;
import com.impact.realisticfood.spit.Spits;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A vanilla campfire can hold a spit: saved and synced with the campfire, and cooked by the campfire's own tick. */
@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin implements SpitHolder {
    @Unique
    private static final String SPIT_KEY = "impactsfood:spit";

    @Unique
    private Spit impactsfood$spit;

    @Override
    public Spit impactsfood$getSpit() {
        return impactsfood$spit;
    }

    @Override
    public void impactsfood$setSpit(Spit spit) {
        impactsfood$spit = spit;
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void impactsfood$loadSpit(CompoundTag tag, CallbackInfo callback) {
        impactsfood$spit = tag.contains(SPIT_KEY) ? Spit.load(tag.getCompound(SPIT_KEY)) : null;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void impactsfood$saveSpit(CompoundTag tag, CallbackInfo callback) {
        if (impactsfood$spit != null) {
            tag.put(SPIT_KEY, impactsfood$spit.save());
        }
    }

    // Clients draw the spit from the campfire's update tag.
    @Inject(method = "getUpdateTag", at = @At("RETURN"))
    private void impactsfood$syncSpit(CallbackInfoReturnable<CompoundTag> callback) {
        if (impactsfood$spit != null) {
            callback.getReturnValue().put(SPIT_KEY, impactsfood$spit.save());
        }
    }

    @Inject(method = "cookTick", at = @At("TAIL"))
    private static void impactsfood$cookSpit(Level level, BlockPos position, BlockState state, CampfireBlockEntity campfire, CallbackInfo callback) {
        Spits.cookTick(level, position, state, campfire);
    }
}
