# Max Mod

A small Fabric client mod for Minecraft 26.3 with personal quality-of-life tweaks:

- **Ore x-ray**: highlights ores around the player through walls.
- **Inventory sorting**: sorts chests and the player inventory with a middle click.
- **Durability viewer**: shows the durability of armor and held items in the HUD and in tooltips.
- **Freecam**: detaches the camera from the player to fly freely through the world.

The mod is client-only. Servers don't need it, and it works in singleplayer and multiplayer.

## Requirements

- Minecraft 26.3
- [Fabric Loader](https://fabricmc.net/use/) 0.19.5 or newer
- [Fabric API](https://modrinth.com/mod/fabric-api)
- Java 25
- Optional: [Mod Menu](https://modrinth.com/mod/modmenu) adds a settings button to the mod list

## Installation

1. Download `maxmod-<version>.jar` from the [releases](https://github.com/maxischmaxi/minecraft-plugin/releases).
2. Put it in the `mods` folder of your Fabric instance, together with Fabric API.
3. Start the game.

## Features

### Ore x-ray

Press **X** or run `/xray` to toggle it. Ores within the radius are drawn as colored boxes on top of everything else.

| Command | Description |
| --- | --- |
| `/xray` | toggle x-ray |
| `/xray radius <1-64>` | set the scan radius in blocks (default 24) |
| `/xray settings` | open the settings screen |

The settings screen (also available through Mod Menu) has options for:

- glow intensity
- radius
- which ores are highlighted: coal, copper, iron, gold, redstone, lapis, diamond, emerald, nether quartz and ancient debris

With shader packs (e.g. Iris), only the outlines are visible. The translucent fill doesn't render with shaders on.

> Using x-ray on servers you don't own is usually considered cheating. Servers with anti-xray send fake or hidden ores, and the mod then shows wrong results.

### Inventory sorting

Open an inventory and **middle click** a slot. The section that slot belongs to gets sorted:

- the player's main inventory (the hotbar is left alone)
- chests, barrels, ender chests, shulker boxes, dispensers, droppers and hoppers

Partial stacks of the same item get merged first. Then items are ordered by their registry order, which keeps related items like stone variants together. Sorting is done with regular inventory clicks, so it works on servers too. Nothing happens while an item is held on the cursor or in the creative inventory.

All key bindings can be changed under *Options → Controls → Key Binds → Max Mod*.

### Durability viewer

The bottom left corner of the HUD lists the worn armor and the items in both hands with their remaining durability, e.g. `1402/1561`. The color goes from green to red like the vanilla durability bar. Items without durability are skipped. Visible chat messages are drawn on top of it.

Tooltips of items with durability always show the remaining durability, not only with advanced tooltips (F3+H). Tools also show their base mining speed, without Efficiency.

Both parts can be turned off in the settings screen.

### Freecam

Press **F4** to detach the camera from the player and fly through the world. Press F4 again to go back.

| Key | Action |
| --- | --- |
| movement keys | fly in the view direction |
| jump / sneak | fly up / down |
| sprint | fly faster |
| mouse wheel | change the speed (1-100 blocks/s, default 10) |

The camera flies through blocks. While in freecam the player stands still and can't attack, use or pick items. The camera can only show chunks the client has loaded around the player.

Freecam turns off on death, dimension change and disconnect. By default it also turns off when the player takes damage, which can be disabled in the settings screen.

### Configuration

Settings are stored in `config/maxmod.json`.

## Building

```sh
./gradlew build
```

The jar ends up in `build/libs/`. Gradle runs on JDK 25 via `gradle/gradle-daemon-jvm.properties` and downloads it if needed.

`./scripts/install-prism.sh` builds the mod and copies it into the Prism Launcher instance `26.3`.

## Releases

Pushing a tag that starts with `v` triggers the [release workflow](.github/workflows/release.yml). It builds the mod with the version taken from the tag and attaches the jar to a new GitHub release:

```sh
git tag v0.2.0
git push origin v0.2.0
```

Tags with a suffix like `v0.2.0-beta.1` are published as pre-releases.

## License

[MIT](LICENSE)
