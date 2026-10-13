# DMZ Voidstone — Voidstone Story Content

The mod bundles two world-save sagas for DragonMineZ 2.1.3:

- **Void Training** — a Tsukisora / Void-class-only challenge. Its final reward grants Ki Control.
- **Voidstone Ascension** — five escalating trials that reward the five Void forms in order.

## Installation

Install DMZ Voidstone on the server and client as usual. On server startup, the mod copies the bundled files into the active world's `dragonminez/sagas` and `dragonminez/quests` folders.

The installer deliberately preserves existing files. If you need to refresh a saga after updating the mod, stop the server and back up/remove only the corresponding files under the world's `dragonminez` folder, then start the server again. Run `/dmzreload story` if you need to reload story content without restarting.

## Ki Control restriction

The server-side mixin blocks the normal PURCHASE and UPGRADE packet actions for `kicontrol` when the active character is Tsukisora. The final Void Training quest grants `kicontrol` as a `SKILL` reward, bypassing the normal shop route. Other races are not affected.

## Compatibility notes

- The story files use the DragonMineZ 2.1.3 saga/quest JSON schema and supported KILL objectives (not custom wave objective types).
- Combat numbers are intentionally extreme and need in-game balance testing.
- Existing world files are never overwritten automatically.
- `party_scaling: false` disables stat scaling; it does not independently prevent parties from joining. Tsukisora's existing owner restriction is expected to limit who can play the race.
