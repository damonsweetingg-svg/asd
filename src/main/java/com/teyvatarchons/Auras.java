package com.teyvatarchons;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Passive, purely visual effects shown around a player holding an unlocked figure's item. */
public final class Auras {
    private Auras() {}

    private static final ParticleOptions BLACK = Fx.dust(0x1A0A26, 1.5f);
    private static final ParticleOptions VIOLET = Fx.dust(0x8A35D6, 1.4f);
    private static final ParticleOptions RED = Fx.dust(0xFF1E2E, 1.0f);
    private static final ParticleOptions WHITE = Fx.dust(0xFFFFFF, 1.2f);
    private static final ParticleOptions PINK = Fx.dust(0xFF6EA0, 1.3f);
    private static final ParticleOptions GOLD = Fx.dust(0xFFD24D, 1.2f);

    public static void tick(ServerPlayer p, Figure f) {
        ServerLevel lv = p.serverLevel();
        long t = lv.getGameTime();
        Vec3 pos = p.position();
        Style st = FigureSpells.styleOf(f);
        switch (f) {
            case VENTI -> {
                // a small, gentle breeze swirling around the bard
                double a = t * 0.2;
                for (int i = 0; i < 2; i++) {
                    double aa = a + i * Math.PI;
                    Fx.p(lv, ParticleTypes.CLOUD, pos.x + Math.cos(aa) * 1.1, pos.y + 0.3 + 0.4 * Math.sin(t * 0.1 + i), pos.z + Math.sin(aa) * 1.1);
                }
                if (t % 3 == 0) Fx.helix(lv, st.dust, pos, 0.9, 2.0, 1.0, 10, t * 0.3);
                if (t % 20 == 0) Fx.burst(lv, ParticleTypes.POOF, pos.add(0, 0.2, 0), 4, 0.8, 0.02);
            }
            case ZHONGLI -> {
                if (t % 2 == 0) {
                    double rot = t * 0.05;
                    Vec3[] hex = new Vec3[6];
                    for (int i = 0; i < 6; i++) {
                        double a = rot + i * Math.PI / 3;
                        hex[i] = new Vec3(pos.x + Math.cos(a) * 1.8, pos.y + 1.0, pos.z + Math.sin(a) * 1.8);
                    }
                    for (int i = 0; i < 6; i++) Fx.line(lv, st.dust, hex[i], hex[(i + 1) % 6], 0.5);
                    Fx.ring(lv, st.a, pos, 1.3, 14, 0.1);
                    Fx.burst(lv, ParticleTypes.WAX_ON, pos.add(0, 1.2, 0), 1, 1.2, 0.0);
                }
            }
            case RAIDEN -> {
                Fx.burst(lv, ParticleTypes.ELECTRIC_SPARK, pos.add(0, 1.0, 0), 2, 0.6, 0.15);
                if (t % 2 == 0) {
                    Vec3[] orbs = new Vec3[3];
                    for (int i = 0; i < 3; i++) {
                        double a = t * 0.15 + i * Math.PI * 2 / 3;
                        orbs[i] = new Vec3(pos.x + Math.cos(a) * 1.4, pos.y + 1.3 + 0.2 * Math.sin(t * 0.2 + i), pos.z + Math.sin(a) * 1.4);
                        Fx.p(lv, st.dust, orbs[i]);
                        Fx.p(lv, st.b, orbs[i]);
                    }
                    if (t % 10 == 0) for (int i = 0; i < 3; i++) Fx.line(lv, ParticleTypes.ELECTRIC_SPARK, orbs[i], orbs[(i + 1) % 3], 0.4);
                }
            }
            case NAHIDA -> {
                if (t % 3 == 0) Fx.burst(lv, ParticleTypes.SPORE_BLOSSOM_AIR, pos.add(0, 1.8, 0), 3, 1.3, 0.0);
                if (t % 20 == 0) Fx.burst(lv, ParticleTypes.HAPPY_VILLAGER, pos.add(0, 1.0, 0), 4, 1.0, 0.0);
                if (t % 2 == 0) { // rotating Seed-of-Skandha mark above her head
                    Vec3 hc = pos.add(0, 2.7, 0);
                    Fx.ring(lv, st.dust, hc, 0.5, 12, 0);
                    for (int i = 0; i < 4; i++) {
                        double a = t * 0.1 + i * Math.PI / 2;
                        Fx.line(lv, st.dust, hc, hc.add(Math.cos(a) * 0.5, 0, Math.sin(a) * 0.5), 0.15);
                    }
                }
            }
            case FURINA -> {
                for (int i = 0; i < 4; i++) {
                    double a = t * 0.12 + i * Math.PI / 2;
                    Fx.p(lv, i % 2 == 0 ? ParticleTypes.SPLASH : ParticleTypes.BUBBLE_POP,
                            pos.x + Math.cos(a) * 1.3, pos.y + 0.6 + 0.5 * Math.sin(t * 0.15 + i), pos.z + Math.sin(a) * 1.3);
                }
                if (t % 3 == 0) Fx.ring(lv, st.dust, pos, 1.6, 12, 0.1);
                if (t % 6 == 0) Fx.burst(lv, ParticleTypes.FALLING_WATER, pos.add(0, 2.2, 0), 3, 0.7, 0.0);
            }
            case MAVUIKA -> {
                if (t % 2 == 0) {
                    for (int i = 0; i < 8; i++) {
                        double a = t * 0.2 + i * Math.PI / 4;
                        Fx.p(lv, ParticleTypes.FLAME, pos.x + Math.cos(a) * 1.2, pos.y + 0.1, pos.z + Math.sin(a) * 1.2);
                    }
                }
                lv.sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 0.4, pos.z, 2, 0.4, 0.2, 0.4, 0.04);
                if (t % 5 == 0) lv.sendParticles(ParticleTypes.SMALL_FLAME, pos.x, pos.y + 1.5, pos.z, 3, 0.5, 0.5, 0.5, 0.02);
                if (t % 12 == 0) Fx.burst(lv, ParticleTypes.LAVA, pos.add(0, 1.0, 0), 1, 0.6, 0);
            }
            case TSARITSA -> {
                lv.sendParticles(ParticleTypes.SNOWFLAKE, pos.x, pos.y + 1.2, pos.z, 3, 1.5, 0.9, 1.5, 0.01);
                if (t % 2 == 0) {
                    for (int i = 0; i < 6; i++) {
                        double a = t * 0.06 + i * Math.PI / 3;
                        Vec3 base = new Vec3(pos.x + Math.cos(a) * 1.6, pos.y + 0.8, pos.z + Math.sin(a) * 1.6);
                        Fx.line(lv, ParticleTypes.END_ROD, base, base.add(0, 0.9, 0), 0.3);
                        Fx.p(lv, st.dust, base.add(0, 1.0, 0));
                    }
                    Fx.ring(lv, st.dust, pos, 1.9, 16, 0.05);
                }
            }
            case ISTAROTH -> {
                if (t % 2 == 0) {
                    Vec3 c = pos.add(0, 0.08, 0);
                    Fx.ring(lv, GOLD, c, 1.7, 24, 0);
                    for (int i = 0; i < 12; i++) {
                        double a = i * Math.PI / 6;
                        Fx.p(lv, WHITE, c.x + Math.cos(a) * 1.5, c.y, c.z + Math.sin(a) * 1.5);
                    }
                    double hourA = t * 0.01, minA = t * 0.12;
                    Fx.line(lv, GOLD, c, c.add(Math.cos(hourA) * 0.8, 0, Math.sin(hourA) * 0.8), 0.2);
                    Fx.line(lv, WHITE, c, c.add(Math.cos(minA) * 1.3, 0, Math.sin(minA) * 1.3), 0.2);
                }
                if (t % 6 == 0) Fx.burst(lv, ParticleTypes.REVERSE_PORTAL, pos.add(0, 1.0, 0), 4, 0.8, 0.05);
            }
            case ASMODAY -> {
                // red cubes floating around her at a distance
                if (t % 2 == 0) {
                    for (int i = 0; i < 5; i++) {
                        double a = t * 0.04 + i * Math.PI * 2 / 5;
                        Vec3 c = new Vec3(pos.x + Math.cos(a) * 3.2, pos.y + 1.3 + 0.5 * Math.sin(t * 0.06 + i), pos.z + Math.sin(a) * 3.2);
                        Fx.cubeEdges(lv, RED, c, 0.28, t * 0.05 + i, t * 0.03);
                        Fx.p(lv, ParticleTypes.END_ROD, c);
                    }
                }
            }
            case RONOVA -> {
                if (t % 2 == 0) {
                    double yaw = Math.toRadians(p.yBodyRot);
                    Vec3 fwd = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
                    Vec3 right = new Vec3(-Math.cos(yaw), 0, -Math.sin(yaw));
                    Vec3 back = fwd.scale(-1);
                    Vec3 shoulder = pos.add(0, 1.35, 0).add(back.scale(0.25));
                    double flap = Math.sin(t * 0.1) * 0.25;
                    for (int side = -1; side <= 1; side += 2) {
                        for (int i = 0; i < 6; i++) {
                            double th = Math.toRadians(8 + i * 13) + flap;
                            double len = 2.7 - i * 0.28;
                            Vec3 dir = right.scale(side * Math.cos(th)).add(0, Math.sin(th), 0).add(back.scale(0.35)).normalize();
                            for (int k = 1; k <= 7; k++) {
                                double u = k / 7.0;
                                Vec3 pt = shoulder.add(dir.scale(len * u)).add(back.scale(0.4 * u * u)).add(0, -0.25 * u * u, 0);
                                Fx.p(lv, (k + i) % 3 == 0 ? VIOLET : BLACK, pt);
                                if (k == 7 && t % 8 == 0) Fx.p(lv, ParticleTypes.SOUL, pt);
                            }
                        }
                    }
                }
            }
            case RHINEDOTTIR -> {
                if (t % 2 == 0) {
                    Fx.helix(lv, GOLD, pos, 0.9, 2.2, 1.5, 24, t * 0.15);
                    Fx.helix(lv, PINK, pos, 0.9, 2.2, 1.5, 24, t * 0.15 + Math.PI);
                    // rotating alchemical hexagram
                    Vec3 c = pos.add(0, 0.08, 0);
                    for (int tri = 0; tri < 2; tri++) {
                        Vec3[] v = new Vec3[3];
                        for (int i = 0; i < 3; i++) {
                            double a = t * 0.03 + tri * Math.PI / 3 + i * Math.PI * 2 / 3;
                            v[i] = new Vec3(c.x + Math.cos(a) * 1.7, c.y, c.z + Math.sin(a) * 1.7);
                        }
                        for (int i = 0; i < 3; i++) Fx.line(lv, GOLD, v[i], v[(i + 1) % 3], 0.5);
                    }
                }
                if (t % 15 == 0) Fx.burst(lv, ParticleTypes.HEART, pos.add(0, 1.6, 0), 1, 0.5, 0.0);
            }
        }
    }
}
