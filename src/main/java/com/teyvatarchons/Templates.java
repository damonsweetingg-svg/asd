package com.teyvatarchons;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Reusable animated spell shapes. Every figure's ten spells are built from these, driven by
 * Scale (so spell 1 is small and spell 10 is enormous) and Style (so each element looks different).
 */
public final class Templates {
    private Templates() {}

    public enum Origin { SELF, TARGET }

    private static Vec3 org(Ctx c, Origin o) {
        return o == Origin.SELF ? c.feet : c.target;
    }

    /** Tilt a direction by a (right) and b (up) radians. */
    private static Vec3 jitter(Vec3 look, double a, double b) {
        Vec3 up = Math.abs(look.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = look.cross(up).normalize();
        Vec3 up2 = right.cross(look).normalize();
        return look.add(right.scale(Math.tan(a))).add(up2.scale(Math.tan(b))).normalize();
    }

    private static double groundY(Ctx c, double x, double z, double fallback) {
        BlockPos hp = c.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(x, fallback, z));
        return Math.abs(hp.getY() - fallback) > 10 ? fallback : hp.getY();
    }

    // ================= combinators =================
    public static Spell.Cast all(Spell.Cast... casts) {
        return c -> {
            for (Spell.Cast x : casts) x.run(c);
        };
    }

    public static Spell.Cast delay(int ticks, Spell.Cast cast) {
        return c -> Tasks.after(ticks, () -> cast.run(c));
    }

    // ================= CONE =================
    public static Spell.Cast cone(Style s, int l, double len) {
        return c -> {
            double L = Math.min(100, len * (1 + l * 0.15) * Cfg.RADIUS_MULT);
            double ang = Math.toRadians(35 + l * 3);
            float dmg = Scale.dmg(l);
            Vec3 o = c.eye.add(0, -0.3, 0);
            Set<Integer> hit = new HashSet<>();
            Fx.sound(c.level, o, s.sound, 1.5f + l * 0.2f, 1.2f - l * 0.05f);
            RandomSource rnd = c.level.random;
            Tasks.add(age -> {
                double r1 = L * age / 4.0, r0 = L * (age - 1) / 4.0;
                int n = 14 + l * 6;
                for (int i = 0; i < n; i++) {
                    Vec3 d = jitter(c.look, (rnd.nextDouble() - 0.5) * 2 * ang, (rnd.nextDouble() - 0.5) * 2 * ang);
                    Vec3 pt = o.add(d.scale(r1));
                    Fx.p(c.level, i % 3 == 0 ? s.b : s.a, pt);
                    if (i % 2 == 0) Fx.p(c.level, s.dust, pt);
                }
                AABB box = new AABB(o, o.add(c.look.scale(r1))).inflate(r1 * Math.tan(ang) + 1);
                for (LivingEntity e : c.level.getEntitiesOfClass(LivingEntity.class, box, x -> x != c.player && x.isAlive() && !hit.contains(x.getId()))) {
                    Vec3 v = e.position().add(0, e.getBbHeight() / 2, 0).subtract(o);
                    double d = v.length();
                    if (d < r0 || d > r1 + 1 || d < 1e-3) continue;
                    if (Math.acos(Math.max(-1, Math.min(1, v.normalize().dot(c.look)))) > ang) continue;
                    hit.add(e.getId());
                    Fx.hurt(c, e, dmg, s);
                    Fx.launch(e, o, 1.2 + l * 0.15, 0.4 + l * 0.05);
                }
                if (l >= 2) {
                    double cr = (1 + l * 0.45) * Scale.carve(l) * 1.4;
                    Fx.carveSphere(c.level, o.add(c.look.scale(r1 * 0.8)), cr, s.scar);
                }
                return age >= 4;
            });
        };
    }

    // ================= BEAM =================
    public static Spell.Cast beam(Style s, int l, double len, double thick) {
        return c -> {
            double L = Math.min(140, len * (1 + l * 0.3) * Cfg.RADIUS_MULT);
            double T = thick * (0.6 + l * 0.35);
            float dmg = Scale.dmg(l);
            Vec3 o = c.eye.add(c.look.scale(1.2)).add(0, -0.3, 0);
            Set<Integer> hit = new HashSet<>();
            double speed = 7.0;
            int travel = (int) Math.ceil(L / speed);
            double carveR = T * Scale.carve(l) * 1.1;
            Fx.sound(c.level, o, s.sound, 3f, 0.6f);
            Tasks.add(age -> {
                double a0 = Math.min(L, (age - 1) * speed), a1 = Math.min(L, age * speed);
                for (double t = a0; t <= a1; t += 0.8) {
                    Vec3 pt = o.add(c.look.scale(t));
                    Fx.p(c.level, s.a, pt);
                    Fx.p(c.level, s.dust, pt);
                    Fx.ringTilted(c.level, s.dust, pt, T, 6 + (int) T, c.look, t * 0.7);
                    if (((int) (t * 1.25)) % 3 == 0) Fx.p(c.level, s.b, pt.add((Math.random() - 0.5) * T, (Math.random() - 0.5) * T, (Math.random() - 0.5) * T));
                }
                Fx.hurtSegment(c, o.add(c.look.scale(a0)), o.add(c.look.scale(a1)), T + 0.8, dmg, s, hit);
                if (carveR >= 1.0) {
                    for (double t = a0; t <= a1; t += Math.max(1.5, carveR * 0.8)) Fx.carveSphere(c.level, o.add(c.look.scale(t)), carveR, s.scar);
                }
                if (age == travel) Fx.boom(c.level, o.add(c.look.scale(L)), T * 2, s);
                return age >= travel + 5;
            });
        };
    }

    // ================= BARRAGE =================
    public static Spell.Cast barrage(Style s, int l, int count) {
        return c -> {
            int total = count + l * 3;
            int perTick = Math.max(1, total / 14);
            int dur = (int) Math.ceil(total / (double) perTick);
            float dmg = Scale.dmg(l) * 0.55f;
            double R = 1.5 + Scale.rad(l) * 0.3;
            RandomSource rnd = c.level.random;
            ServerLevel lv = c.level;
            Tasks.add(age -> {
                for (int i = 0; i < perTick; i++) {
                    Vec3 o = c.eye.add(c.look.scale(1)).add((rnd.nextDouble() - 0.5) * 3, rnd.nextDouble() * 1.2 - 0.4, (rnd.nextDouble() - 0.5) * 3);
                    Vec3 d = jitter(c.look, (rnd.nextDouble() - 0.5) * 0.5, (rnd.nextDouble() - 0.5) * 0.4);
                    BlockHitResult r = lv.clip(new ClipContext(o, o.add(d.scale(70)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, c.player));
                    Vec3 end = r.getType() == HitResult.Type.MISS ? o.add(d.scale(70)) : r.getLocation();
                    Fx.line(lv, s.a, o, end, 0.7);
                    Fx.line(lv, s.dust, o, end, 1.4);
                    Fx.burst(lv, s.a, end, 14, 0.5, 0.15);
                    Fx.burst(lv, s.b, end, 6, 0.4, 0.1);
                    if (rnd.nextInt(3) == 0) Fx.sound(lv, end, s.sound, 1.0f, 1.2f + rnd.nextFloat() * 0.4f);
                    Fx.hurtSphere(c, end, R, dmg, s, 0.8, 0.4);
                    if (l >= 1) Fx.carveSphere(lv, end, Math.max(1.0, R * Scale.carve(l) * 0.8), s.scar);
                }
                return age >= dur;
            });
        };
    }

    // ================= DASH =================
    public static Spell.Cast dash(Style s, int l, double dist) {
        return c -> {
            double D = Math.min(120, dist * (1 + l * 0.12) * Cfg.RADIUS_MULT);
            Vec3 start = c.feet.add(0, 0.2, 0);
            Vec3 end = start.add(c.look.scale(D));
            if (end.y < c.level.getMinBuildHeight() + 2) end = new Vec3(end.x, c.level.getMinBuildHeight() + 2, end.z);
            final Vec3 fend = end;
            double w = 2 + l * 0.5;
            float dmg = Scale.dmg(l);
            Fx.sound(c.level, start, s.sound, 2f, 1.5f);
            Set<Integer> hit = new HashSet<>();
            double cr = 1.7 + l * 0.45;
            for (double t = 0; t <= D; t += Math.max(1.0, cr * 0.7)) {
                Fx.carveSphere(c.level, start.add(c.look.scale(t)).add(0, 1, 0), cr, s.scar);
            }
            Fx.hurtSegment(c, start, fend, w, dmg, s, hit);
            c.player.connection.teleport(fend.x, fend.y, fend.z, c.player.getYRot(), c.player.getXRot());
            c.player.setDeltaMovement(c.look.scale(0.5));
            c.player.fallDistance = 0;
            c.player.hurtMarked = true;
            Fx.boom(c.level, fend, w * 1.5, s);
            Tasks.add(age -> {
                for (double t = 0; t <= D; t += 1.2) {
                    Vec3 pt = start.add(c.look.scale(t)).add(0, 1, 0);
                    Fx.p(c.level, s.a, pt);
                    if (age % 2 == 0) Fx.ringTilted(c.level, s.dust, pt, w * (1 - age / 10.0), 8, c.look, t);
                }
                return age >= 8;
            });
        };
    }

    // ================= NOVA =================
    public static Spell.Cast nova(Style s, int l, Origin o, double rm) {
        return c -> {
            Vec3 ctr = org(c, o).add(0, o == Origin.SELF ? 0.8 : 1.0, 0);
            double R = Math.min(Cfg.MAX_RADIUS, Scale.rad(l) * rm);
            float dmg = Scale.dmg(l);
            int dur = 6 + (int) (R / 2);
            double carve = R * Scale.carve(l);
            Set<Integer> hit = new HashSet<>();
            Fx.sound(c.level, ctr, s.sound, 3f, 0.6f);
            Tasks.add(age -> {
                double r1 = R * Math.min(1.0, age / (double) dur);
                double r0 = R * Math.min(1.0, (age - 1) / (double) dur);
                Fx.sphere(c.level, s.a, ctr, r1, (int) Math.min(260, 30 + r1 * 12));
                Fx.sphere(c.level, s.dust, ctr, r1, (int) Math.min(200, 20 + r1 * 8));
                Fx.ring(c.level, s.b, ctr, r1, (int) Math.min(120, 16 + r1 * 4), -0.6);
                Fx.hurtShell(c, ctr, r0, r1, dmg, s, hit);
                if (carve >= 1.5) Fx.carveShell(c.level, ctr, Math.min(r0, carve), Math.min(r1, carve), s.scar, carve, 1.0);
                if (age == dur) Fx.boom(c.level, ctr, R, s);
                return age >= dur + 2;
            });
        };
    }

    // ================= PILLARS =================
    private static void pillarAt(Ctx c, Style s, int l, Vec3 ctr, double r, double h, int telegraph) {
        float dmg = Scale.dmg(l) * 1.2f;
        Tasks.add(age -> {
            if (age <= telegraph) {
                double rr = r * (1.0 - (age - 1) / (double) telegraph * 0.5);
                Fx.ring(c.level, s.dust, ctr, rr, 18, 0.2);
                Fx.ring(c.level, s.b, ctr, rr * 0.6, 10, 0.2);
                return false;
            }
            for (double y = 0; y <= h; y += 1.0) {
                Fx.ring(c.level, s.a, ctr.add(0, y, 0), r * 0.8, 10, 0);
                Fx.p(c.level, s.dust, ctr.add(0, y, 0));
            }
            Fx.helix(c.level, s.b, ctr, r * 0.9, h, 3, 60, 0);
            Fx.boom(c.level, ctr.add(0, h * 0.2, 0), r * 1.5, s);
            AABB box = new AABB(ctr.x - r, ctr.y - 2, ctr.z - r, ctr.x + r, ctr.y + h, ctr.z + r);
            for (LivingEntity e : c.level.getEntitiesOfClass(LivingEntity.class, box, x -> x != c.player && x.isAlive())) {
                if (Math.hypot(e.getX() - ctr.x, e.getZ() - ctr.z) > r) continue;
                Fx.hurt(c, e, dmg, s);
                e.setDeltaMovement(e.getDeltaMovement().add(0, 1.2 + l * 0.2, 0));
                e.hurtMarked = true;
            }
            if (l >= 1) Fx.carveColumn(c.level, ctr, Math.max(1.5, r * Math.max(0.5, Scale.carve(l))), 3, h * 0.6, s.scar);
            return true;
        });
    }

    public static Spell.Cast pillar(Style s, int l, Origin o, double rm, double h) {
        return c -> {
            Vec3 ctr = org(c, o);
            Fx.sound(c.level, ctr, s.sound, 2f, 0.8f);
            pillarAt(c, s, l, ctr, Scale.rad(l) * 0.55 * rm, h * (1 + l * 0.3), 12);
        };
    }

    public static Spell.Cast pillars(Style s, int l, Origin o, int count, double ringRm) {
        return c -> {
            Vec3 ctr = org(c, o);
            double R = Scale.rad(l) * ringRm;
            double pr = Math.max(2.0, Scale.rad(l) * 0.32);
            double h = 8 + l * 2.5;
            Fx.sound(c.level, ctr, s.sound, 2.5f, 0.7f);
            for (int i = 0; i < count; i++) {
                double a = Math.PI * 2 * i / count;
                Vec3 p = new Vec3(ctr.x + Math.cos(a) * R, ctr.y, ctr.z + Math.sin(a) * R);
                Tasks.after(i * 2, () -> pillarAt(c, s, l, p, pr, h, 10));
            }
            Tasks.after(count * 2 + 6, () -> pillarAt(c, s, l, ctr, pr * 1.8, h * 1.6, 8));
        };
    }

    // ================= STORM =================
    public static Spell.Cast storm(Style s, int l, Origin o, int seconds, double rm) {
        return c -> {
            Vec3 ctr = org(c, o);
            double R = Math.min(Cfg.MAX_RADIUS, Scale.rad(l) * rm * 1.3);
            int dur = seconds * 20;
            int per = 1 + l / 3;
            double br = 1.8 + l * 0.55;
            float dmg = Scale.dmg(l) * 0.7f;
            RandomSource rnd = c.level.random;
            Fx.sound(c.level, ctr, s.sound, 3f, 0.5f);
            Tasks.add(age -> {
                if (age % 3 == 0) {
                    Fx.ring(c.level, s.b, ctr.add(0, 26, 0), R, (int) Math.min(80, 20 + R * 2), 0);
                    Fx.ring(c.level, s.dust, ctr.add(0, 26, 0), R * 0.6, 30, 0);
                }
                if (age % 2 == 0) {
                    for (int i = 0; i < per; i++) {
                        double rr = R * Math.sqrt(rnd.nextDouble());
                        double a = rnd.nextDouble() * Math.PI * 2;
                        double x = ctr.x + Math.cos(a) * rr, z = ctr.z + Math.sin(a) * rr;
                        Vec3 pos = new Vec3(x, groundY(c, x, z, ctr.y), z);
                        Fx.line(c.level, s.a, pos.add(0, 32, 0), pos, 0.8);
                        Fx.line(c.level, s.dust, pos.add(0, 32, 0), pos, 1.6);
                        Fx.burst(c.level, s.a, pos.add(0, 0.5, 0), 20, br * 0.3, 0.25);
                        Fx.burst(c.level, s.b, pos.add(0, 0.5, 0), 10, br * 0.2, 0.15);
                        Fx.ring(c.level, s.dust, pos, br, 16, 0.3);
                        if (s.bolt) Fx.lightning(c.level, pos);
                        else if (rnd.nextInt(2) == 0) Fx.sound(c.level, pos, s.sound, 1.4f, 0.9f + rnd.nextFloat() * 0.4f);
                        Fx.hurtSphere(c, pos, br, dmg, s, 1.0, 0.5);
                        if (l >= 1) Fx.carveSphere(c.level, pos, Math.max(1.0, br * Scale.carve(l) * 1.1), s.scar);
                    }
                }
                return age >= dur;
            });
        };
    }

    // ================= VORTEX =================
    public static Spell.Cast vortex(Style s, int l, Origin o, int seconds, double rm) {
        return c -> {
            Vec3 ctr = org(c, o);
            double R = Math.min(Cfg.MAX_RADIUS, Scale.rad(l) * rm * 1.1);
            double H = R * 1.2 + 4;
            int dur = seconds * 20;
            float dmg = Scale.dmg(l);
            Fx.sound(c.level, ctr, s.sound, 3f, 0.5f);
            Tasks.add(age -> {
                double ph = age * 0.35;
                for (int arm = 0; arm < 3; arm++) {
                    for (int i = 0; i < 40; i++) {
                        double t = i / 40.0;
                        double a = ph + arm * Math.PI * 2 / 3 + t * 6;
                        double rr = R * 0.25 + t * R * 0.75;
                        Vec3 pt = new Vec3(ctr.x + Math.cos(a) * rr, ctr.y + t * H, ctr.z + Math.sin(a) * rr);
                        Fx.p(c.level, i % 2 == 0 ? s.a : s.dust, pt);
                    }
                }
                Fx.ring(c.level, s.b, ctr, R, 40, 0.3);
                AABB box = new AABB(ctr.x - R * 1.3, ctr.y - 3, ctr.z - R * 1.3, ctr.x + R * 1.3, ctr.y + H, ctr.z + R * 1.3);
                for (LivingEntity e : c.level.getEntitiesOfClass(LivingEntity.class, box, x -> x != c.player && x.isAlive())) {
                    Vec3 to = ctr.add(0, H * 0.4, 0).subtract(e.position());
                    if (to.lengthSqr() < 1e-3) continue;
                    Vec3 tan = new Vec3(-to.z, 0, to.x).normalize().scale(0.25);
                    e.setDeltaMovement(to.normalize().scale(0.45).add(tan));
                    e.hurtMarked = true;
                    if (age % 8 == 0) Fx.hurt(c, e, dmg * 0.35f, s);
                }
                if (age % 10 == 0 && l >= 3) Fx.carveSphere(c.level, ctr.add(0, 1, 0), Math.max(1.5, R * 0.5 * Scale.carve(l)), s.scar);
                if (age >= dur) {
                    Fx.boom(c.level, ctr.add(0, 2, 0), R, s);
                    Fx.hurtSphere(c, ctr.add(0, 2, 0), R, dmg * 1.6f, s, 2.5, 1.2);
                    Fx.carveSphere(c.level, ctr.add(0, 2, 0), Math.max(2, R * Scale.carve(l) * 0.9), s.scar);
                    return true;
                }
                return false;
            });
        };
    }

    // ================= METEOR =================
    private static void oneMeteor(Ctx c, Style s, int l, double size, Vec3 impact) {
        int fall = 24;
        Vec3 start = impact.add((c.level.random.nextDouble() - 0.5) * 20, 70, (c.level.random.nextDouble() - 0.5) * 20);
        double R = Math.min(Cfg.MAX_RADIUS, Scale.rad(l) * size);
        double mr = size * (1.2 + l * 0.25);
        float dmg = Scale.dmg(l) * 2.2f;
        Fx.sound(c.level, impact, s.sound, 3f, 0.4f);
        Tasks.add(age -> {
            double t = Math.min(1.0, age / (double) fall);
            double e = t * t;
            Vec3 pos = start.add(impact.subtract(start).scale(e));
            Fx.sphere(c.level, s.a, pos, mr, 36);
            Fx.sphere(c.level, s.dust, pos, mr * 0.7, 24);
            Fx.burst(c.level, s.b, pos, 20, mr * 0.5, 0.2);
            Vec3 back = start.subtract(impact).normalize();
            Fx.line(c.level, s.dust, pos, pos.add(back.scale(14)), 1.0);
            if (age < fall) return false;
            Fx.boom(c.level, impact, R, s);
            Fx.hurtSphere(c, impact, R, dmg, s, 3.0, 1.5);
            Fx.carveSphere(c.level, impact, Math.max(2, R * Scale.carve(l) * 0.8), s.scar);
            Tasks.add(a2 -> {
                double rr = R * a2 / 8.0;
                Fx.ring(c.level, s.a, impact, rr, (int) Math.min(160, 20 + rr * 5), 0.4);
                Fx.ring(c.level, s.dust, impact, rr * 0.8, 40, 1.2);
                return a2 >= 8;
            });
            return true;
        });
    }

    public static Spell.Cast meteor(Style s, int l, int count, double size) {
        return c -> {
            double R = Scale.rad(l) * 0.8;
            for (int i = 0; i < count; i++) {
                Vec3 impact;
                if (i == 0) {
                    impact = c.target;
                } else {
                    double a = c.level.random.nextDouble() * Math.PI * 2;
                    double rr = R * Math.sqrt(c.level.random.nextDouble());
                    impact = new Vec3(c.target.x + Math.cos(a) * rr, c.target.y, c.target.z + Math.sin(a) * rr);
                }
                final Vec3 imp = impact;
                Tasks.after(i * 7, () -> oneMeteor(c, s, l, size, imp));
            }
        };
    }
}
