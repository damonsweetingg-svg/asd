package com.teyvatarchons;

import java.util.function.BiConsumer;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;

/** The look-and-feel of one figure: particles, sound, crater lining and elemental on-hit effect. */
public final class Style {
    public final ParticleOptions a;   // main particle
    public final ParticleOptions b;   // accent particle
    public final ParticleOptions dust; // coloured dust
    public final String sound;        // registry id of the cast / impact sound
    public final Block scar;          // blocks used to line craters (null = just air)
    public final boolean bolt;        // spawn real lightning visuals on strikes
    public final BiConsumer<Ctx, LivingEntity> hit; // elemental effect on every hit

    public Style(ParticleOptions a, ParticleOptions b, ParticleOptions dust, String sound, Block scar, boolean bolt,
                 BiConsumer<Ctx, LivingEntity> hit) {
        this.a = a;
        this.b = b;
        this.dust = dust;
        this.sound = sound;
        this.scar = scar;
        this.bolt = bolt;
        this.hit = hit;
    }
}
