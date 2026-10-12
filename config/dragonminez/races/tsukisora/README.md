# Tsukisora DragonMineZ configuration

These are DragonMineZ 2.1.3 configuration templates for the custom `tsukisora` race and its `Void` class.

## Install for local development

Copy this directory's contents into:

```
run/config/dragonminez/races/tsukisora/
```

The resulting config directory should contain `character.json` and `stats.json`.

## Tsukisora custom model assets

The race is configured to use the custom model key `tsukisora`, with a single non-layered texture. DragonMineZ loads these files from the mod's/resource pack's normal assets namespace:

- Geometry (female): `src/main/resources/assets/dragonminez/geo/entity/races/tsukisora_female.geo.json`
- Texture (female): `src/main/resources/assets/dragonminez/textures/entity/races/tsukisora/tsukisora_female.png`

Export the Blockbench project as Bedrock-style geometry JSON; the `.bbmodel` project file itself is not the runtime geometry file. Rename/copy the exported geometry and texture to the exact paths and filenames above. The geometry must reference the texture correctly and use a compatible DragonMineZ/Bedrock geometry structure.

Because `hasGender` is currently `true`, DragonMineZ also looks for `tsukisora_male.geo.json` and `tsukisora_male.png` for male characters. If Tsukisora is meant to be female-only, enforce that in character creation/server-side logic; `hasGender` by itself only enables the gender choice.

## Important status

- The configuration defines the `Void` class and its baseline stat profile.
- The `passive` is intentionally disabled until the mod's custom passive is registered and tested.
- Race/class access restrictions and manifestation progression must also be enforced in server-side code; configuration alone is not a security boundary.
- Do not overwrite an existing local configuration without backing it up first.
