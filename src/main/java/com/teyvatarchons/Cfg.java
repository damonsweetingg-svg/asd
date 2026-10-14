package com.teyvatarchons;

/** Tweak these constants to taste, then rebuild. */
public final class Cfg {
    private Cfg() {}

    /** Set false to keep every spell visual + damaging but never break blocks. */
    public static boolean BLOCK_DAMAGE = true;
    /** Multiplies all spell damage. */
    public static double DAMAGE_MULT = 1.0;
    /** Multiplies all spell radii / lengths. */
    public static double RADIUS_MULT = 1.0;
    /** Hard cap on any single sphere radius (protects servers from absurd lag). */
    public static double MAX_RADIUS = 48.0;
}
