#!/usr/bin/env python3
"""Generates the 32x32 item textures for Archons of Teyvat.
Run from the repo root:  python3 tools/gen_textures.py
Requires Pillow (pip install pillow). Edit colours/shapes freely."""
import math, os
from PIL import Image, ImageDraw

OUT = "src/main/resources/assets/archons/textures/item"
S = 32
os.makedirs(OUT, exist_ok=True)

GOLD_D, GOLD, GOLD_L = (120, 84, 20, 255), (214, 170, 52, 255), (255, 233, 140, 255)
OUTLINE = (24, 18, 30, 255)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (255,)


def new():
    im = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    return im, ImageDraw.Draw(im)


def orb(d, cx, cy, r, base):
    """Shaded sphere with a highlight."""
    for y in range(cy - r, cy + r + 1):
        for x in range(cx - r, cx + r + 1):
            dx, dy = x - cx, y - cy
            dist = math.hypot(dx, dy)
            if dist <= r:
                # light from upper-left
                l = 1.25 - (math.hypot(dx + r * 0.4, dy + r * 0.4) / (r * 1.7))
                d.point((x, y), shade(base, max(0.35, min(1.35, l))))
    d.ellipse((cx - r, cy - r, cx + r, cy + r), outline=shade(base, 0.35))
    d.rectangle((cx - r // 2 - 1, cy - r // 2 - 1, cx - r // 2, cy - r // 2), fill=(255, 255, 255, 230))


def gnosis_frame(d):
    """Golden ornamental holder: wings + ring + base."""
    # wings
    for side in (-1, 1):
        for i in range(5):
            x = 16 + side * (11 + i)
            y0 = 6 + i * 2
            d.line((16 + side * 9, 14, x, y0), fill=GOLD, width=1)
            d.point((x, y0 - 1), GOLD_L)
    # ring
    d.ellipse((6, 6, 25, 25), outline=GOLD)
    d.ellipse((5, 5, 26, 26), outline=GOLD_D)
    # crown & base
    d.polygon([(14, 3), (16, 0), (18, 3)], fill=GOLD, outline=GOLD_D)
    d.polygon([(11, 27), (21, 27), (19, 31), (13, 31)], fill=GOLD, outline=GOLD_D)
    d.line((13, 28, 19, 28), fill=GOLD_L)


def glyph(d, kind, col):
    c = (255, 255, 255, 255)
    k = kind
    if k == "anemo":  # swirl
        pts = []
        for t in range(0, 60):
            a = t * 0.28
            rr = 1 + t * 0.12
            pts.append((16 + rr * math.cos(a), 16 + rr * math.sin(a)))
        d.line(pts, fill=c)
    elif k == "geo":  # hexagon / diamond
        d.polygon([(16, 10), (21, 16), (16, 22), (11, 16)], outline=c)
        d.polygon([(16, 13), (18, 16), (16, 19), (14, 16)], fill=c)
    elif k == "electro":  # bolt
        d.polygon([(18, 9), (13, 17), (16, 17), (14, 23), (20, 14), (17, 14)], fill=c)
    elif k == "dendro":  # leaf
        d.polygon([(11, 21), (13, 13), (21, 10), (20, 18)], fill=c)
        d.line((11, 21, 19, 12), fill=shade(col, 0.5))
    elif k == "hydro":  # droplet
        d.polygon([(16, 9), (21, 17), (16, 23), (11, 17)], fill=c)
        d.ellipse((11, 15, 21, 24), fill=c)
    elif k == "pyro":  # flame
        d.polygon([(16, 9), (21, 16), (19, 23), (13, 23), (11, 16), (14, 15)], fill=c)
        d.polygon([(16, 15), (18, 20), (14, 20)], fill=shade(col, 0.8))
    elif k == "cryo":  # snowflake
        for a in range(0, 180, 60):
            r = math.radians(a)
            d.line((16 - 7 * math.cos(r), 16 - 7 * math.sin(r), 16 + 7 * math.cos(r), 16 + 7 * math.sin(r)), fill=c)
        d.point((16, 16), c)


def gnosis(name, kind, base):
    im, d = new()
    gnosis_frame(d)
    orb(d, 16, 16, 8, base)
    glyph(d, kind, base)
    im.save(f"{OUT}/{name}.png")


# ---------- Shade items ----------
def hourglass(name):  # Istaroth - time
    im, d = new()
    sand = (255, 214, 90, 255)
    glass = (170, 210, 255, 255)
    d.rectangle((7, 3, 24, 5), fill=GOLD, outline=GOLD_D)
    d.rectangle((7, 26, 24, 28), fill=GOLD, outline=GOLD_D)
    d.polygon([(9, 6), (22, 6), (17, 15), (22, 25), (9, 25), (14, 15)], fill=(120, 160, 220, 140), outline=glass)
    d.polygon([(11, 8), (20, 8), (16, 14)], fill=sand)
    d.polygon([(11, 24), (20, 24), (16, 19)], fill=sand)
    d.line((16, 15, 16, 22), fill=sand)
    d.rectangle((8, 5, 9, 26), fill=GOLD_D)
    d.rectangle((22, 5, 23, 26), fill=GOLD_D)
    for i in range(3):
        d.point((13 + i * 3, 29 + 0), (200, 255, 255, 255))
    im.save(f"{OUT}/{name}.png")


def cube(name):  # Asmoday - space, red cube
    im, d = new()
    top, left, right = (255, 90, 90, 255), (200, 30, 40, 255), (130, 10, 30, 255)
    d.polygon([(16, 4), (28, 10), (16, 16), (4, 10)], fill=top, outline=OUTLINE)
    d.polygon([(4, 10), (16, 16), (16, 29), (4, 23)], fill=left, outline=OUTLINE)
    d.polygon([(28, 10), (16, 16), (16, 29), (28, 23)], fill=right, outline=OUTLINE)
    # glowing edges / sigil
    d.line((16, 16, 16, 29), fill=(255, 200, 200, 255))
    d.line((4, 10, 16, 16), fill=(255, 200, 200, 255))
    d.line((28, 10, 16, 16), fill=(255, 200, 200, 255))
    d.ellipse((13, 18, 19, 24), outline=(255, 220, 220, 255))
    im.save(f"{OUT}/{name}.png")


def feather(name):  # Ronova - death
    im, d = new()
    dark, mid, glow = (40, 12, 60, 255), (96, 30, 140, 255), (200, 120, 255, 255)
    d.polygon([(6, 28), (4, 14), (10, 5), (22, 2), (28, 4), (24, 12), (18, 22), (10, 28)], fill=mid, outline=OUTLINE)
    d.polygon([(8, 26), (7, 15), (12, 8), (22, 5), (21, 12), (15, 20)], fill=dark)
    for i in range(6):
        d.line((6 + i, 27 - i * 2, 25 - i * 1, 5 + i * 2), fill=shade(mid, 1.2 - i * 0.12))
    d.line((5, 29, 26, 3), fill=glow)
    d.point((26, 3), (255, 255, 255, 255))
    im.save(f"{OUT}/{name}.png")


def lifeseed(name):  # Rhinedottir - life/alchemy
    im, d = new()
    pink, gold = (255, 110, 160, 255), (255, 214, 90, 255)
    d.ellipse((4, 4, 27, 27), outline=gold)
    d.polygon([(16, 26), (6, 15), (7, 9), (11, 7), (16, 11), (21, 7), (25, 9), (26, 15)], fill=pink, outline=shade(pink, 0.4))
    d.polygon([(16, 22), (10, 15), (11, 11), (14, 10), (16, 13)], fill=shade(pink, 1.25))
    for a in range(0, 360, 60):
        r = math.radians(a)
        d.point((16 + 11 * math.cos(r), 16 + 11 * math.sin(r)), (255, 255, 255, 255))
    d.line((16, 13, 16, 22), fill=gold)
    im.save(f"{OUT}/{name}.png")


gnosis("anemo_gnosis", "anemo", (70, 220, 190, 255))
gnosis("geo_gnosis", "geo", (230, 170, 40, 255))
gnosis("electro_gnosis", "electro", (160, 70, 230, 255))
gnosis("dendro_gnosis", "dendro", (110, 200, 50, 255))
gnosis("hydro_gnosis", "hydro", (50, 130, 240, 255))
gnosis("pyro_gnosis", "pyro", (240, 80, 30, 255))
gnosis("cryo_gnosis", "cryo", (150, 220, 255, 255))
hourglass("shade_time")
cube("shade_space")
feather("shade_death")
lifeseed("shade_life")
print("textures written to", OUT)
