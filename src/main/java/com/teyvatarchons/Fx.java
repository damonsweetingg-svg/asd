package com.teyvatarchons;

import java.util.Set;

import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Particle, sound, damage and block-destruction helpers shared by every spell. */
public final class Fx {
    private Fx() {}

    // ---------- particles ----------
    public static DustParticleOptions dust(int rgb, float size) {
        return new DustParticleOptions(new Vector3f(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f), size);
    }

    public static DustColorTransitionOptions dust2(int from, int to, float size) {
        return new DustColorTransitionOptions(
                new Vector3f(((from >> 16) & 255) / 255f, ((from >> 8) & 255) / 255f, (from & 255) / 255f),
                new Vector3f(((to >> 16) & 255) / 255f, ((to >> 8) & 255) / 255f, (to & 255) / 255f), size);
    }

    public static void p(ServerLevel lv, ParticleOptions o, double x, double y, double z) {
        lv.sendParticles(o, x, y, z, 1, 0, 0, 0, 0);
    }

    public static void p(ServerLevel lv, ParticleOptions o, Vec3 v) {
        lv.sendParticles(o, v.x, v.y, v.z, 1, 0, 0, 0, 0);
    }

    public static void burst(ServerLevel lv, ParticleOptions o, Vec3 v, int count, double spread, double speed) {
        lv.sendParticles(o, v.x, v.y, v.z, count, spread, spread, spread, speed);
    }

    public static void ring(ServerLevel lv, ParticleOptions o, Vec3 c, double r, int pts, double yOff) {
        for (int i = 0; i < pts; i++) {
            double a = Math.PI * 2 * i / pts;
            p(lv, o, c.x + Math.cos(a) * r, c.y + yOff, c.z + Math.sin(a) * r);
        }
    }

    public static void ringTilted(ServerLevel lv, ParticleOptions o, Vec3 c, double r, int pts, Vec3 axis, double phase) {
        Vec3 u = axis.cross(Math.abs(axis.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0)).normalize();
        Vec3 v = axis.cross(u).normalize();
        for (int i = 0; i < pts; i++) {
            double a = phase + Math.PI * 2 * i / pts;
            p(lv, o, c.add(u.scale(Math.cos(a) * r)).add(v.scale(Math.sin(a) * r)));
        }
    }

    public static void sphere(ServerLevel lv, ParticleOptions o, Vec3 c, double r, int pts) {
        double ga = Math.PI * (3 - Math.sqrt(5));
        for (int i = 0; i < pts; i++) {
            double y = 1 - (i / (double) Math.max(1, pts - 1)) * 2;
            double rr = Math.sqrt(Math.max(0, 1 - y * y));
            double th = ga * i;
            p(lv, o, c.x + Math.cos(th) * rr * r, c.y + y * r, c.z + Math.sin(th) * rr * r);
        }
    }

    public static void line(ServerLevel lv, ParticleOptions o, Vec3 a, Vec3 b, double step) {
        Vec3 d = b.subtract(a);
        double len = d.length();
        if (len < 1e-4) return;
        Vec3 dir = d.scale(1 / len);
        for (double t = 0; t <= len; t += step) p(lv, o, a.add(dir.scale(t)));
    }

    public static void helix(ServerLevel lv, ParticleOptions o, Vec3 base, double r, double h, double turns, int pts, double phase) {
        for (int i = 0; i < pts; i++) {
            double t = i / (double) pts;
            double a = phase + t * turns * Math.PI * 2;
            p(lv, o, base.x + Math.cos(a) * r, base.y + t * h, base.z + Math.sin(a) * r);
        }
    }

    public static void cubeEdges(ServerLevel lv, ParticleOptions o, Vec3 c, double half, double yaw, double pitch) {
        Vec3[] v = new Vec3[8];
        int k = 0;
        for (int x = -1; x <= 1; x += 2)
            for (int y = -1; y <= 1; y += 2)
                for (int z = -1; z <= 1; z += 2) {
                    double px = x * half, py = y * half, pz = z * half;
                    double cy = Math.cos(yaw), sy = Math.sin(yaw);
                    double x1 = px * cy - pz * sy, z1 = px * sy + pz * cy;
                    double cp = Math.cos(pitch), sp = Math.sin(pitch);
                    double y1 = py * cp - z1 * sp, z2 = py * sp + z1 * cp;
                    v[k++] = c.add(x1, y1, z2);
                }
        for (int i = 0; i < 8; i++)
            for (int j = i + 1; j < 8; j++) {
                int diff = i ^ j;
                if (diff == 1 || diff == 2 || diff == 4) line(lv, o, v[i], v[j], half * 0.9);
            }
    }

    // ---------- sound ----------
    public static void sound(ServerLevel lv, Vec3 pos, String id, float vol, float pitch) {
        SoundEvent se = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(id));
        if (se != null) lv.playSound(null, pos.x, pos.y, pos.z, se, SoundSource.PLAYERS, vol, pitch);
    }

    public static void boom(ServerLevel lv, Vec3 pos, double r, Style s) {
        lv.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        lv.sendParticles(ParticleTypes.FLASH, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        burst(lv, s.a, pos, (int) Math.min(300, 40 + r * 8), Math.min(r * 0.4, 6), 0.35);
        burst(lv, s.b, pos, (int) Math.min(200, 20 + r * 5), Math.min(r * 0.3, 5), 0.2);
        sound(lv, pos, "entity.generic.explode", (float) Math.min(8, 2 + r * 0.2), 0.7f);
        sound(lv, pos, s.sound, (float) Math.min(8, 2 + r * 0.2), 0.8f);
    }

    public static void lightning(ServerLevel lv, Vec3 pos) {
        LightningBolt b = EntityType.LIGHTNING_BOLT.create(lv);
        if (b == null) return;
        b.moveTo(pos.x, pos.y, pos.z);
        b.setVisualOnly(true);
        lv.addFreshEntity(b);
    }

    // ---------- damage ----------
    public static void hurt(Ctx c, LivingEntity e, float dmg, Style s) {
        e.invulnerableTime = 0;
        e.hurt(c.level.damageSources().indirectMagic(c.player, c.player), dmg);
        if (s != null && s.hit != null && e.isAlive()) s.hit.accept(c, e);
    }

    public static void launch(LivingEntity e, Vec3 from, double strength, double up) {
        Vec3 d = e.position().subtract(from);
        d = new Vec3(d.x, 0, d.z);
        d = d.lengthSqr() < 1e-4 ? new Vec3(0, 0, 0) : d.normalize();
        e.setDeltaMovement(d.x * strength, up, d.z * strength);
        e.hurtMarked = true;
    }

    /** Damage every living thing (except the caster) in the given sphere. Returns how many were hit. */
    public static int hurtSphere(Ctx c, Vec3 ctr, double r, float dmg, Style s, double knock, double up) {
        int n = 0;
        AABB box = new AABB(ctr.x - r, ctr.y - r, ctr.z - r, ctr.x + r, ctr.y + r, ctr.z + r);
        for (LivingEntity e : c.level.getEntitiesOfClass(LivingEntity.class, box, x -> x != c.player && x.isAlive())) {
            if (e.position().add(0, e.getBbHeight() / 2, 0).distanceToSqr(ctr) > r * r) continue;
            hurt(c, e, dmg, s);
            if (knock > 0 || up > 0) launch(e, ctr, knock, up);
            n++;
        }
        return n;
    }

    /** Damage entities whose distance from ctr is in [r0, r1] (an expanding shockwave shell). */
    public static void hurtShell(Ctx c, Vec3 ctr, double r0, double r1, float dmg, Style s, Set<Integer> already) {
        AABB box = new AABB(ctr.x - r1, ctr.y - r1, ctr.z - r1, ctr.x + r1, ctr.y + r1, ctr.z + r1);
        for (LivingEntity e : c.level.getEntitiesOfClass(LivingEntity.class, box, x -> x != c.player && x.isAlive())) {
            if (already.contains(e.getId())) continue;
            double d = e.position().add(0, e.getBbHeight() / 2, 0).distanceTo(ctr);
            if (d >= r0 && d <= r1 + 1.0) {
                already.add(e.getId());
                hurt(c, e, dmg, s);
                launch(e, ctr, 1.4, 0.7);
            }
        }
    }

    public static int hurtSegment(Ctx c, Vec3 a, Vec3 b, double w, float dmg, Style s, Set<Integer> already) {
        int n = 0;
        AABB box = new AABB(a, b).inflate(w);
        Vec3 ab = b.subtract(a);
        double len2 = Math.max(1e-6, ab.lengthSqr());
        for (LivingEntity e : c.level.getEntitiesOfClass(LivingEntity.class, box, x -> x != c.player && x.isAlive())) {
            if (already.contains(e.getId())) continue;
            Vec3 p = e.position().add(0, e.getBbHeight() / 2, 0);
            double t = Math.max(0, Math.min(1, p.subtract(a).dot(ab) / len2));
            if (a.add(ab.scale(t)).distanceTo(p) <= w + e.getBbWidth() / 2) {
                already.add(e.getId());
                hurt(c, e, dmg, s);
                n++;
            }
        }
        return n;
    }

    // ---------- block destruction ----------
    private static final int FLAGS = 2 | 16;

    private static boolean breakable(ServerLevel lv, BlockPos pos, BlockState st) {
        if (st.isAir()) return false;
        float h = st.getDestroySpeed(lv, pos);
        return h >= 0;
    }

    /** Carve the shell r0 < dist <= r1 around ctr. Outer rim is lined with s.scar. */
    public static void carveShell(ServerLevel lv, Vec3 ctr, double r0, double r1, Block scar, double outerRim, double chance) {
        if (!Cfg.BLOCK_DAMAGE || r1 <= 0) return;
        r1 = Math.min(r1, Cfg.MAX_RADIUS);
        int ir = (int) Math.ceil(r1);
        BlockPos base = BlockPos.containing(ctr);
        BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();
        double r0s = r0 * r0, r1s = r1 * r1;
        double rim = Math.max(0, outerRim - 1.6);
        double rims = rim * rim;
        for (int dx = -ir; dx <= ir; dx++)
            for (int dy = -ir; dy <= ir; dy++)
                for (int dz = -ir; dz <= ir; dz++) {
                    double d2 = dx * dx + dy * dy + dz * dz;
                    if (d2 > r1s || d2 <= r0s) continue;
                    mp.set(base.getX() + dx, base.getY() + dy, base.getZ() + dz);
                    if (lv.isOutsideBuildHeight(mp) || !lv.hasChunkAt(mp)) continue;
                    BlockState st = lv.getBlockState(mp);
                    if (!breakable(lv, mp, st)) continue;
                    if (chance < 1.0 && lv.random.nextDouble() > chance) continue;
                    boolean onRim = scar != null && d2 > rims && lv.random.nextFloat() < 0.5f;
                    lv.setBlock(mp, (onRim ? scar : Blocks.AIR).defaultBlockState(), FLAGS);
                }
    }

    public static void carveSphere(ServerLevel lv, Vec3 ctr, double r, Block scar) {
        carveShell(lv, ctr, -1, r, scar, r, 1.0);
    }

    /** Vertical cylinder: from (base.y - down) to (base.y + up). */
    public static void carveColumn(ServerLevel lv, Vec3 base, double r, double down, double up, Block scar) {
        if (!Cfg.BLOCK_DAMAGE) return;
        r = Math.min(r, Cfg.MAX_RADIUS);
        int ir = (int) Math.ceil(r);
        BlockPos b = BlockPos.containing(base);
        BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();
        for (int dx = -ir; dx <= ir; dx++)
            for (int dz = -ir; dz <= ir; dz++) {
                double d2 = dx * dx + dz * dz;
                if (d2 > r * r) continue;
                boolean rim = d2 > (r - 1.3) * (r - 1.3);
                for (int dy = (int) -down; dy <= (int) up; dy++) {
                    mp.set(b.getX() + dx, b.getY() + dy, b.getZ() + dz);
                    if (lv.isOutsideBuildHeight(mp) || !lv.hasChunkAt(mp)) continue;
                    BlockState st = lv.getBlockState(mp);
                    if (!breakable(lv, mp, st)) continue;
                    boolean line = scar != null && rim && lv.random.nextFloat() < 0.4f;
                    lv.setBlock(mp, (line ? scar : Blocks.AIR).defaultBlockState(), FLAGS);
                }
            }
    }
}
