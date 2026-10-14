# Archons of Teyvat (Minecraft 1.21.1 / Forge 52.x)

Seven Archons + four Shades, ten spells each (110 total), escalating from small to world-shaking.
**No mana. No cooldowns.**

| Item | Power | Passive visual |
|---|---|---|
| Anemo Gnosis | Venti | gentle breeze swirling around you |
| Geo Gnosis | Zhongli | rotating golden Geo hexagon + shards |
| Electro Gnosis | Raiden Ei | crackling sparks, orbiting purple orbs |
| Dendro Gnosis | Nahida | drifting spores, Seed-of-Skandha mark overhead |
| Hydro Gnosis | Furina | orbiting droplets and bubbles |
| Pyro Gnosis | Mavuika | flame ring + rising embers |
| Cryo Gnosis | The Tsaritsa | snowfall, ice crystal ring |
| Hourglass of Istaroth | Shade of Time | spinning clock face with live hands |
| Cube of Asmoday | Shade of Space | red cubes floating around you |
| Feather of Ronova | Shade of Death | death wings of black/violet particles |
| Heart of Rhinedottir | Shade of Life | gold/pink life helix + alchemy circle |

## Controls
- **Right-click** the item the first time: unlocks that figure's ten spells.
- **Right-click** afterwards: casts the selected spell. Spam it as fast as you like.
- **Shift + right-click**: next spell.  **V** (rebindable): radial spell wheel (also keys 1-9 / 0 inside it).
- `/archons select <1-10>`, and for ops `/archons unlockall`.
- Hold the item (main hand or offhand) to show its aura.

## Getting the .jar with GitHub (no setup on your PC)
1. Create a new GitHub repo and upload **everything in this folder** (keep the `.github` folder).
2. Open the **Actions** tab -> "Build mod jar" runs automatically (or click *Run workflow*).
3. When it's green, open the run and download the **archons-jar** artifact -> `archons-1.0.0.jar`.
4. Drop that jar into `.minecraft/mods` of a **Forge 1.21.1** install (52.1.0 or newer). That's it.
   Tag a release (`git tag v1.0.0 && git push --tags`) and the jar is also attached to a GitHub Release.

Building locally instead: Java 21 + Gradle 8.8, then `gradle build` -> `build/libs/archons-1.0.0.jar`.

## Tuning (`src/main/java/com/teyvatarchons/Cfg.java`)
`BLOCK_DAMAGE`, `DAMAGE_MULT`, `RADIUS_MULT`, `MAX_RADIUS`. Spell 10 carves craters up to ~30-48 blocks wide;
lower `MAX_RADIUS` on weaker servers. Curve lives in `Scale.java`, spell lists in `FigureSpells.java`,
animation shapes in `Templates.java`, passive effects in `Auras.java`, textures from `tools/gen_textures.py`.

## Making it even flashier with other mods
The mod itself only needs Forge, so it can never fail to load because of a missing dependency. To go further
you can run it alongside client-side visual mods (Particular, Visuality, shader packs via Oculus, etc.) and
the particle storms will look much better. Deeper integration (GeckoLib models, custom particle textures) can
be added as `compileOnly` + required dependencies in `build.gradle` / `mods.toml`.
