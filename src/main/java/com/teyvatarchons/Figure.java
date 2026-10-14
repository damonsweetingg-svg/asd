package com.teyvatarchons;

/** The seven Archons and four Shades. Each owns one item, one Style and ten Spells. */
public enum Figure {
    VENTI("anemo_gnosis", "Venti", "Barbatos, Anemo Archon", 0x5CE0C0, false),
    ZHONGLI("geo_gnosis", "Zhongli", "Morax, Geo Archon", 0xF0B030, false),
    RAIDEN("electro_gnosis", "Raiden Ei", "Baal, Electro Archon", 0xA055F0, false),
    NAHIDA("dendro_gnosis", "Nahida", "Buer, Dendro Archon", 0x7BD43B, false),
    FURINA("hydro_gnosis", "Furina", "Focalors, Hydro Archon", 0x3A8DFF, false),
    MAVUIKA("pyro_gnosis", "Mavuika", "Pyro Archon of Natlan", 0xFF5A1E, false),
    TSARITSA("cryo_gnosis", "The Tsaritsa", "Cryo Archon", 0x9FE8FF, false),
    ISTAROTH("shade_time", "Istaroth", "Shade of Time", 0xFFD966, true),
    ASMODAY("shade_space", "Asmoday", "Shade of Space", 0xFF2030, true),
    RONOVA("shade_death", "Ronova", "Shade of Death", 0x7A2CBF, true),
    RHINEDOTTIR("shade_life", "Rhinedottir", "Shade of Life", 0xFF6EA0, true);

    public final String itemId;
    public final String display;
    public final String title;
    public final int rgb;
    public final boolean shade;
    private Spell[] spells;

    Figure(String itemId, String display, String title, int rgb, boolean shade) {
        this.itemId = itemId;
        this.display = display;
        this.title = title;
        this.rgb = rgb;
        this.shade = shade;
    }

    public Spell[] spells() {
        if (spells == null) spells = FigureSpells.build(this);
        return spells;
    }

    public static Figure byItemId(String id) {
        for (Figure f : values()) if (f.itemId.equals(id)) return f;
        return null;
    }
}
