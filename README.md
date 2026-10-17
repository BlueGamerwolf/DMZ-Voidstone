# DMZ Voidstone

A Forge 1.20.1 addon for DragonMineZ 2.1.3 that adds the Void class and Tsukisora race progression/story content.

## Requirements

- Minecraft 1.20.1
- Minecraft Forge 47.x
- Java 17
- DragonMineZ 2.1.3
- The dependencies declared in `build.gradle`

## Included content

- A bundled global `Void` class definition at `dmz-voidstone/classes/void.json`.
- Tsukisora race stats and form configuration under `dmz-voidstone/races/tsukisora/`.
- Server-side guards restricting Tsukisora and the Void class to the configured owner UUID.
- Void Training and Voidstone Ascension localization entries.

## Configuration installation

Bundled JSON resources are copied into the instance's `config/dragonminez/` directory:

- `dmz-voidstone/classes/**` → `config/dragonminez/classes/**`
- `dmz-voidstone/races/**` → `config/dragonminez/races/**`

New files are installed when they do not already exist. Existing files are preserved to avoid overwriting customizations. For existing race `stats.json` files, missing class definitions are merged without replacing class entries that are already present.

**Legacy configs:** if an older version added a `Void` entry directly to `config/dragonminez/races/tsukisora/stats.json`, back up and review that file after upgrading. Remove the obsolete race-specific `Void` entry only after confirming the global `config/dragonminez/classes/void.json` exists.

The installer runs at Forge's load-complete lifecycle event and then calls DragonMineZ's `ConfigManager.reload()`. Check `logs/latest.log` for the install summary if a config does not appear.

## Build

Run from the project root with a Java 17 JDK:

```powershell
.\gradlew.bat build
```

## Owner restrictions

The owner UUID is configured in `TsukisoraAccess.java`. Change it only if you intend to transfer the exclusive race/class to another player. Access checks are enforced on the server; client-side visibility is not the security boundary.

## Troubleshooting

- Confirm the Minecraft, Forge, DragonMineZ, and Java versions.
- Check `logs/latest.log` for the Voidstone config installation summary.
- Back up config files before making manual changes.
- If forms do not unlock, verify that group names and prerequisite references match the schema supported by DragonMineZ 2.1.3.
