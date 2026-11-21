package com.impact.realisticfood.spit;

import net.minecraft.world.level.block.entity.CampfireBlockEntity;

import java.util.Optional;

/** Food's mixin makes every vanilla campfire block entity able to hold a spit. */
public interface SpitHolder {
    Spit impactsfood$getSpit();

    void impactsfood$setSpit(Spit spit);

    static Optional<Spit> spitOf(CampfireBlockEntity campfire) {
        return Optional.ofNullable(((SpitHolder) campfire).impactsfood$getSpit());
    }
}
