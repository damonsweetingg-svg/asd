package com.teyvatarchons;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/** Unlock flags and selected spell per figure, stored in the player's persistent data (survives death/relog). */
public final class PlayerData {
    private PlayerData() {}

    private static final String ROOT = "teyvat_archons";

    private static CompoundTag root(Player p) {
        CompoundTag d = p.getPersistentData();
        if (!d.contains(ROOT)) d.put(ROOT, new CompoundTag());
        return d.getCompound(ROOT);
    }

    public static boolean unlocked(Player p, Figure f) {
        return root(p).getBoolean("u_" + f.itemId);
    }

    public static void unlock(Player p, Figure f) {
        root(p).putBoolean("u_" + f.itemId, true);
    }

    public static int selected(Player p, Figure f) {
        return Mth.clamp(root(p).getInt("s_" + f.itemId), 0, 9);
    }

    public static void select(Player p, Figure f, int idx) {
        root(p).putInt("s_" + f.itemId, Mth.clamp(idx, 0, 9));
    }

    public static void copy(Player from, Player to) {
        CompoundTag fd = from.getPersistentData();
        if (fd.contains(ROOT)) to.getPersistentData().put(ROOT, fd.getCompound(ROOT).copy());
    }
}
