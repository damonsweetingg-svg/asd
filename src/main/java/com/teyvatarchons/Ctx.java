package com.teyvatarchons;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;

/** Everything a spell needs to know about the moment it was cast. */
public final class Ctx {
    public final ServerPlayer player;
    public final ServerLevel level;
    public final Vec3 eye;
    public final Vec3 look;
    public final Vec3 feet;
    /** Where the caster is looking (block hit, or snapped to the ground at max range). */
    public final Vec3 target;

    public Ctx(ServerPlayer p) {
        this.player = p;
        this.level = p.serverLevel();
        this.eye = p.getEyePosition();
        this.look = p.getLookAngle().normalize();
        this.feet = p.position();
        Vec3 end = eye.add(look.scale(120));
        BlockHitResult r = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        if (r.getType() == HitResult.Type.MISS) {
            BlockPos hp = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(end));
            this.target = new Vec3(end.x, Math.max(hp.getY(), level.getMinBuildHeight()), end.z);
        } else {
            this.target = r.getLocation();
        }
    }
}
