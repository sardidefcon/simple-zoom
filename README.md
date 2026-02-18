# SimpleZoom

Simple Minecraft plugin for Paper/Spigot that gives players a spyglass in their off hand for zooming.

## Features

- All configuration is read from `config.yml`
- `/zoom` puts a spyglass (minecraft:spyglass) in your off hand
- Native spyglass zoom works when held and right-clicked
- Permission: `szoom.use` (default: op)

## Requirements

- Java 21 (LTS)
- Paper or Spigot server (tested with `api-version: "1.21"`)
- Maven 3.x (to build)

## Build

From the project root, run:

```bash
mvn clean package
```

The plugin JAR will be generated at:

`target/SimpleZoom-1.0.0.jar`

## Installation

1. Copy the built JAR to your Paper/Spigot server `plugins` folder
2. Start or restart the server
3. The `config.yml` file will be created automatically in `plugins/SimpleZoom/` if it does not exist

## Commands & Permissions

| Command | Description |
|---------|-------------|
| `/zoom` | Puts a spyglass in your off hand |
| `/zoom reload` | Reloads the configuration |

- **szoom.use**: Required to use `/zoom` (default: op)
- **szoom.reload**: Required to use `/zoom reload` (default: op)

## Configuration

- **prefix**: Prefix for plugin messages
- **remove-on-move**: Remove spyglass when player moves (default: true)
- **remove-on-hotbar-switch**: Remove spyglass when player switches hotbar slot (default: true)
- **remove-on-stop-zoom**: Remove spyglass when player releases right-click / stops zooming (default: false)
- **messages**: no-permission, player-only, reload-success, reload-no-permission

**Important:** At least one removal option must be enabled, or the plugin will not load. When the spyglass is removed, the previous off-hand item is restored with all its data (enchantments, custom name, shield design, etc.).
