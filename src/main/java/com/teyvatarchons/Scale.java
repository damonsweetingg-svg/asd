package com.teyvatarchons;

/** How strong spell number l (0..9) is. Every figure uses the same curve, so spell 10 is always the biggest. */
public final class Scale {
    private Scale() {}

    /** ~8 damage at spell 1 up to ~2000 at spell 10. */
    public static float dmg(int l) {
        return (float) (Math.round(8 * Math.pow(1.85, l)) * Cfg.DAMAGE_MULT);
    }

    /** 3.5 blocks at spell 1 up to ~30 blocks at spell 10. */
    public static double rad(int l) {
        return Math.min(Cfg.MAX_RADIUS, (3.5 + l * 2.9) * Cfg.RADIUS_MULT);
    }

    /** Fraction of the blast radius that actually removes blocks. */
    public static double carve(int l) {
        return l < 1 ? 0.0 : Math.min(1.0, 0.25 + 0.075 * l);
    }
}
