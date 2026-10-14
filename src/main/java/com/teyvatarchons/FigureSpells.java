package com.teyvatarchons;

import static com.teyvatarchons.Templates.*;
import static com.teyvatarchons.Templates.Origin.SELF;
import static com.teyvatarchons.Templates.Origin.TARGET;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** All 110 spells. Each figure: ten entries, weakest to most destructive. */
public final class FigureSpells {
    private FigureSpells() {}

    private static final Style ANEMO = new Style(ParticleTypes.CLOUD, ParticleTypes.POOF, Fx.dust(0x5CE0C0, 1.6f),
            "entity.breeze.wind_burst", null, false, (c, e) -> {
                e.setDeltaMovement(e.getDeltaMovement().add(0, 1.0, 0));
                e.hurtMarked = true;
            });

    private static final Style GEO = new Style(Fx.dust(0xF0B030, 1.8f), ParticleTypes.WAX_ON, Fx.dust(0xD08A20, 1.5f),
            "block.deepslate_bricks.break", Blocks.RAW_GOLD_BLOCK, false, (c, e) ->
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 4)));

    private static final Style ELECTRO = new Style(ParticleTypes.ELECTRIC_SPARK, Fx.dust(0xE0A0FF, 1.2f), Fx.dust(0x9B4DFF, 1.8f),
            "entity.lightning_bolt.thunder", Blocks.AMETHYST_BLOCK, true, (c, e) -> {
                e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2));
            });

    private static final Style DENDRO = new Style(ParticleTypes.HAPPY_VILLAGER, ParticleTypes.SPORE_BLOSSOM_AIR, Fx.dust(0x7BD43B, 1.6f),
            "block.moss.break", Blocks.MOSS_BLOCK, false, (c, e) -> {
                e.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 2));
                e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
            });

    private static final Style HYDRO = new Style(ParticleTypes.SPLASH, ParticleTypes.BUBBLE_POP, Fx.dust(0x3A8DFF, 1.6f),
            "entity.generic.splash", Blocks.PRISMARINE, false, (c, e) ->
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1)));

    private static final Style PYRO = new Style(ParticleTypes.FLAME, ParticleTypes.LARGE_SMOKE, Fx.dust(0xFF5A1E, 1.7f),
            "entity.blaze.shoot", Blocks.MAGMA_BLOCK, false, (c, e) -> e.setRemainingFireTicks(400));

    private static final Style CRYO = new Style(ParticleTypes.SNOWFLAKE, ParticleTypes.END_ROD, Fx.dust(0x9FE8FF, 1.6f),
            "entity.player.hurt_freeze", Blocks.BLUE_ICE, false, (c, e) -> {
                e.setTicksFrozen(e.getTicksRequiredToFreeze() + 200);
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 3));
            });

    private static final Style TIME = new Style(ParticleTypes.END_ROD, ParticleTypes.REVERSE_PORTAL, Fx.dust(0xFFD966, 1.5f),
            "block.bell.use", Blocks.LIGHT_BLUE_STAINED_GLASS, false, (c, e) -> {
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 6));
                e.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 3));
                e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 2));
            });

    private static final Style SPACE = new Style(ParticleTypes.PORTAL, ParticleTypes.END_ROD, Fx.dust(0xFF2030, 1.4f),
            "entity.enderman.teleport", Blocks.CRYING_OBSIDIAN, false, (c, e) ->
            e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 50, 2)));

    private static final Style DEATH = new Style(ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.SOUL, Fx.dust(0x5A1E8C, 1.7f),
            "entity.wither.ambient", Blocks.SCULK, false, (c, e) -> {
                e.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 3));
                e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
            });

    private static final Style LIFE = new Style(ParticleTypes.TOTEM_OF_UNDYING, ParticleTypes.CHERRY_LEAVES, Fx.dust(0xFFD24D, 1.6f),
            "block.amethyst_block.chime", Blocks.GOLD_BLOCK, false, (c, e) -> {
                c.player.heal(2.0f); // Life Drain: every hit feeds the caster
                e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
            });

    private static Spell s(String name, Spell.Cast cast) {
        return new Spell(name, cast);
    }

    public static Style styleOf(Figure f) {
        return switch (f) {
            case VENTI -> ANEMO;
            case ZHONGLI -> GEO;
            case RAIDEN -> ELECTRO;
            case NAHIDA -> DENDRO;
            case FURINA -> HYDRO;
            case MAVUIKA -> PYRO;
            case TSARITSA -> CRYO;
            case ISTAROTH -> TIME;
            case ASMODAY -> SPACE;
            case RONOVA -> DEATH;
            case RHINEDOTTIR -> LIFE;
        };
    }

    public static Spell[] build(Figure f) {
        return switch (f) {
            case VENTI -> new Spell[] {
                s("Breeze Slash", cone(ANEMO, 0, 16)),
                s("Skyward Sonnet", barrage(ANEMO, 1, 8)),
                s("Windblade Dash", dash(ANEMO, 2, 16)),
                s("Hold the Wind", vortex(ANEMO, 3, TARGET, 4, 0.8)),
                s("Cyclone Chorus", pillars(ANEMO, 4, TARGET, 6, 0.9)),
                s("Wind's Grand Ode", all(vortex(ANEMO, 5, TARGET, 6, 1.0), storm(ANEMO, 5, TARGET, 6, 0.8))),
                s("Stormeye Requiem", all(nova(ANEMO, 6, SELF, 1.0), beam(ANEMO, 6, 40, 1.2))),
                s("Four Winds' Ascension", all(pillars(ANEMO, 7, TARGET, 10, 1.0), barrage(ANEMO, 7, 20))),
                s("Dvalin's Tempest", storm(ANEMO, 8, TARGET, 8, 1.2)),
                s("Barbatos Unbound: Hurricane of Mondstadt", all(vortex(ANEMO, 9, TARGET, 10, 1.0),
                        delay(20, storm(ANEMO, 9, TARGET, 8, 1.0)), delay(60, nova(ANEMO, 9, TARGET, 1.0))))
            };
            case ZHONGLI -> new Spell[] {
                s("Dominus Lapidis", pillar(GEO, 0, TARGET, 1.0, 6)),
                s("Geo Barricade", pillars(GEO, 1, TARGET, 4, 0.9)),
                s("Starshatter", barrage(GEO, 2, 8)),
                s("Jade Shield Pulse", nova(GEO, 3, SELF, 0.9)),
                s("Rockfall Bombardment", storm(GEO, 4, TARGET, 5, 0.9)),
                s("Hex of Petrification", all(vortex(GEO, 5, TARGET, 5, 0.9), pillar(GEO, 5, TARGET, 1.0, 10))),
                s("Stele Array", pillars(GEO, 6, TARGET, 8, 1.0)),
                s("Rex Lapis Tremor", all(nova(GEO, 7, TARGET, 1.0), beam(GEO, 7, 35, 1.2))),
                s("Meteor of Liyue", meteor(GEO, 8, 3, 1.0)),
                s("Planet Befall", all(meteor(GEO, 9, 1, 1.8), delay(30, pillars(GEO, 9, TARGET, 12, 1.0)),
                        delay(45, nova(GEO, 9, TARGET, 1.0))))
            };
            case RAIDEN -> new Spell[] {
                s("Musou no Hitotachi", cone(ELECTRO, 0, 18)),
                s("Baleful Omen", barrage(ELECTRO, 1, 10)),
                s("Eye of Stormy Judgement", storm(ELECTRO, 2, TARGET, 3, 0.8)),
                s("Iaido Dash", dash(ELECTRO, 3, 20)),
                s("Thunderbolt Rain", storm(ELECTRO, 4, TARGET, 5, 1.0)),
                s("Musou Shinsetsu", beam(ELECTRO, 5, 45, 1.4)),
                s("Euthymia Maelstrom", all(vortex(ELECTRO, 6, TARGET, 5, 0.9), storm(ELECTRO, 6, TARGET, 5, 0.7))),
                s("Plane of Euthymia", nova(ELECTRO, 7, SELF, 1.0)),
                s("Narukami's Wrath", storm(ELECTRO, 8, TARGET, 8, 1.1)),
                s("Musou Isshin: Eternity", all(beam(ELECTRO, 9, 50, 1.4), delay(10, nova(ELECTRO, 9, SELF, 1.0)),
                        delay(20, storm(ELECTRO, 9, TARGET, 8, 1.0))))
            };
            case NAHIDA -> new Spell[] {
                s("All Schemes to Know", barrage(DENDRO, 0, 6)),
                s("Seed of Skandha", pillar(DENDRO, 1, TARGET, 1.0, 6)),
                s("Karmic Bloom", nova(DENDRO, 2, TARGET, 0.8)),
                s("Mayajaal Dash", dash(DENDRO, 3, 16)),
                s("Shrine of Maya", vortex(DENDRO, 4, TARGET, 5, 0.9)),
                s("Tri-Karma Purification", storm(DENDRO, 5, TARGET, 6, 1.0)),
                s("Spirit of Kalpa", pillars(DENDRO, 6, TARGET, 8, 1.0)),
                s("Sprout of Sumeru", beam(DENDRO, 7, 40, 1.5)),
                s("Kusanali's Dream", all(storm(DENDRO, 8, TARGET, 8, 1.0), vortex(DENDRO, 8, TARGET, 8, 0.8))),
                s("Theory of Illusory Worlds", all(vortex(DENDRO, 9, TARGET, 10, 1.0), delay(20, storm(DENDRO, 9, TARGET, 8, 1.0)),
                        delay(50, nova(DENDRO, 9, TARGET, 1.0))))
            };
            case FURINA -> new Spell[] {
                s("Salon Solitaire", barrage(HYDRO, 0, 6)),
                s("Surintendante Chevalmarin", cone(HYDRO, 1, 16)),
                s("Bubble Dance", vortex(HYDRO, 2, TARGET, 3, 0.7)),
                s("Let the People Rejoice", nova(HYDRO, 3, SELF, 1.0)),
                s("Ousia Torrent", beam(HYDRO, 4, 38, 1.2)),
                s("Fountain of Lucine", pillars(HYDRO, 5, TARGET, 6, 0.9)),
                s("Tidal Judgment", storm(HYDRO, 6, TARGET, 6, 1.0)),
                s("Hydro Archon's Aria", vortex(HYDRO, 7, TARGET, 7, 1.0)),
                s("Focalors' Deluge", all(storm(HYDRO, 8, TARGET, 8, 1.0), nova(HYDRO, 8, TARGET, 1.0))),
                s("Judgment of the Oceanic Throne", all(beam(HYDRO, 9, 50, 1.5), delay(15, nova(HYDRO, 9, SELF, 1.0)),
                        delay(30, storm(HYDRO, 9, TARGET, 10, 1.0))))
            };
            case MAVUIKA -> new Spell[] {
                s("Flames Weave Life", cone(PYRO, 0, 16)),
                s("Flamestrider Dash", dash(PYRO, 1, 18)),
                s("Fighting Spirit Burst", nova(PYRO, 2, SELF, 0.9)),
                s("Sacred Phoenix Volley", barrage(PYRO, 3, 12)),
                s("Ring of Searing Radiance", vortex(PYRO, 4, TARGET, 5, 0.9)),
                s("Natlan Overcharge", pillars(PYRO, 5, TARGET, 6, 0.9)),
                s("Solar Flare", beam(PYRO, 6, 45, 1.4)),
                s("Divine Throne Ignition", storm(PYRO, 7, TARGET, 7, 1.0)),
                s("Fire Archon's Ascension", meteor(PYRO, 8, 4, 1.0)),
                s("Sun of Natlan", all(meteor(PYRO, 9, 1, 1.8), delay(25, nova(PYRO, 9, TARGET, 1.0)),
                        delay(35, storm(PYRO, 9, TARGET, 6, 0.9))))
            };
            case TSARITSA -> new Spell[] {
                s("Frost Lance", beam(CRYO, 0, 30, 0.6)),
                s("Icicle Barrage", barrage(CRYO, 1, 10)),
                s("Glacial Dash", dash(CRYO, 2, 18)),
                s("Frozen Domain", nova(CRYO, 3, SELF, 1.0)),
                s("Crystal Prison", pillar(CRYO, 4, TARGET, 1.2, 10)),
                s("Blizzard Descent", storm(CRYO, 5, TARGET, 6, 1.0)),
                s("Cryo Spire Array", pillars(CRYO, 6, TARGET, 8, 1.0)),
                s("Absolute Zero", vortex(CRYO, 7, TARGET, 6, 0.9)),
                s("Glaciation of Snezhnaya", all(nova(CRYO, 8, TARGET, 1.0), beam(CRYO, 8, 45, 1.4))),
                s("Winter Eternal", all(storm(CRYO, 9, TARGET, 10, 1.0), delay(30, vortex(CRYO, 9, TARGET, 8, 0.9)),
                        delay(60, nova(CRYO, 9, SELF, 1.0))))
            };
            case ISTAROTH -> new Spell[] {
                s("Stutter", cone(TIME, 0, 14)),
                s("Rewind Pulse", nova(TIME, 1, SELF, 1.0)),
                s("Temporal Slip", dash(TIME, 2, 20)),
                s("Slowing Field", vortex(TIME, 3, TARGET, 4, 0.8)),
                s("Chrono Barrage", barrage(TIME, 4, 16)),
                s("Hourglass Collapse", pillar(TIME, 5, TARGET, 1.2, 14)),
                s("Temporal Rift", beam(TIME, 6, 42, 1.2)),
                s("Time Stop", vortex(TIME, 7, TARGET, 7, 1.0)),
                s("Epoch's Fall", storm(TIME, 8, TARGET, 8, 1.0)),
                s("The End of Time", all(nova(TIME, 9, SELF, 1.0), delay(15, nova(TIME, 9, TARGET, 1.0)),
                        delay(30, storm(TIME, 9, TARGET, 8, 1.0))))
            };
            case ASMODAY -> new Spell[] {
                s("Cube Strike", barrage(SPACE, 0, 8)),
                s("Spatial Cut", beam(SPACE, 1, 30, 0.5)),
                s("Warp Step", dash(SPACE, 2, 22)),
                s("Cubic Prison", pillar(SPACE, 3, TARGET, 1.0, 8)),
                s("Dimensional Rift", vortex(SPACE, 4, TARGET, 5, 0.9)),
                s("Cube Rain", storm(SPACE, 5, TARGET, 6, 1.0)),
                s("Void Fold", nova(SPACE, 6, TARGET, 1.0)),
                s("Temple of Space", pillars(SPACE, 7, TARGET, 10, 1.0)),
                s("Spatial Collapse", all(vortex(SPACE, 8, TARGET, 8, 1.0), nova(SPACE, 8, TARGET, 0.8))),
                s("Authority of Space: Falling Stars", all(meteor(SPACE, 9, 5, 1.2), delay(40, nova(SPACE, 9, TARGET, 1.0)),
                        delay(50, storm(SPACE, 9, TARGET, 8, 1.0))))
            };
            case RONOVA -> new Spell[] {
                s("Death's Touch", cone(DEATH, 0, 16)),
                s("Soul Reap", barrage(DEATH, 1, 10)),
                s("Withering Curse", pillar(DEATH, 2, TARGET, 1.0, 6)),
                s("Shade Wing Dash", dash(DEATH, 3, 22)),
                s("Grave Call", pillars(DEATH, 4, TARGET, 6, 0.9)),
                s("Deathly Aura", nova(DEATH, 5, SELF, 1.0)),
                s("Reaper's Beam", beam(DEATH, 6, 45, 1.3)),
                s("Divine Throne Verdict", vortex(DEATH, 7, TARGET, 7, 1.0)),
                s("Death Rain", storm(DEATH, 8, TARGET, 8, 1.1)),
                s("Ruler of Death: Final Verdict", all(nova(DEATH, 9, SELF, 1.0), beam(DEATH, 9, 50, 1.5),
                        delay(20, storm(DEATH, 9, TARGET, 8, 1.0))))
            };
            case RHINEDOTTIR -> new Spell[] {
                s("Transmutation Bolt", barrage(LIFE, 0, 8)),
                s("Golden Lash", beam(LIFE, 1, 32, 0.7)),
                s("Vital Dash", dash(LIFE, 2, 18)),
                s("Life Drain", vortex(LIFE, 3, TARGET, 5, 0.8)),
                s("Crimson Bloom", pillar(LIFE, 4, TARGET, 1.1, 10)),
                s("Alchemic Array", pillars(LIFE, 5, TARGET, 7, 0.9)),
                s("Mass Transmutation", nova(LIFE, 6, TARGET, 1.0)),
                s("Homunculus Swarm", storm(LIFE, 7, TARGET, 7, 1.0)),
                s("Life's End", all(vortex(LIFE, 8, TARGET, 8, 0.9), nova(LIFE, 8, TARGET, 0.9))),
                s("Devourer of Naberius", all(storm(LIFE, 9, TARGET, 10, 1.0), delay(20, vortex(LIFE, 9, TARGET, 8, 1.0)),
                        delay(60, nova(LIFE, 9, TARGET, 1.0))))
            };
        };
    }
}
