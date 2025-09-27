# Recall Dungeon

![Demo — tiles & sprites](https://media0.giphy.com/media/v1.Y2lkPTc5MGI3NjExZnJkcW56Ymt6Zml5ZWl0eXZzZmVzc3FkaTE4YzViZ3VqcTlpbzFqdSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/dfahiJN1ducOisBcLV/giphy.gif)

<sub>GIF embedded from GIPHY — <a href="https://giphy.com/gifs/dfahiJN1ducOisBcLV">via GIPHY</a></sub>

A Java-based roguelike dungeon crawler built with Maven and LibGDX.

Explore the mysterious dungeon where you have awakened. Decide whether you will go further down the dungeon or try to escape to the surface!

This project includes a custom Java-based "Recall engine" designed specifically for grid-based dungeon gameplay.
All game logic, entity systems, map parsing, and command handling are part of a custom Java game engine I am developing from scratch.

Now includes a LibGDX desktop client (LWJGL3) with Scene2D/VisUI for menus.

---

## Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Wishlist](#wishlist)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup & Installation](#setup--installation)
- [Running the Project](#running-the-project)
- [Running Tests](#running-tests)
- [Contributing](#contributing)
- [License](#license)
- [Manual](#manual)
- [Story](#story)

---
## Features
### Turn-based Dungeon Exploration

- Procedurally loaded dungeon floors with fog-of-war (undiscovered tiles are hidden until explored)
- Player vision range determines how much of the map is revealed
- Creatures have multiple attacks that are decided semi-randomly via weighted randomness

### Creature System

- Multiple creature types (players, enemies, beasts, undead, etc.)
- Creatures have stats (Strength, Dexterity, Constitution, etc.) and resistances (Fire, Ice, Bludgeoning, etc.)
- Level and XP system with customizable XP progression

### Inventory & Equipment

- Inventory system for items, weapons, armor, and consumables
- Equipment slots (weapon, shield, armor, helmet, legwear, etc.)
- Support for two-handed and versatile weapons
- Weapons have physical and optional magical damage types (with dice notation, e.g., 2d6)
- Weapons like creatures have multiple attacks that are decided semi-randomly via weighted randomness
- Items and equipment can modify stats, resistances, and traits
- Loot and monster pools work (but not yet fully implemented) this makes possible for unique experiences for every play-through 

### Combat System

- Turn-based combat loop between player and enemies
- Player chooses actions (attack, flee, etc.) each turn
- Dice-based damage calculation (e.g., 2d6 for physical, 1d8 for magic)
- Damage types interact with creature resistances for a complex, old-school combat system

### Properties & Traits

- Buffs, debuffs, immunities, and traits can be applied to creatures
- Properties can modify stats, resistances, and vision range

### Asset-driven Design

- Creatures, items, floors, and properties are loaded from JSON files for easy customization and extension
- Extensible mod support for the future

### Rendering & Sprites

- Tile and creature rendering: ASCII map characters are mapped to sprite regions so the LibGDX map view renders tiles and creatures using graphics instead of plain text.
- Per-character sprite binding: player and creature JSON may include a `"sprite"` field to select a specific sprite for that character (for example, different starting characters can show different player sprites).
- Loading strategy: the renderer prefers a packed `TextureAtlas` (region names from `tiles.json`) and falls back to individual PNG files in `src/main/resources/assets/sprite_pngs/` when an atlas region is not available.
- Default behavior: when a player character has no sprite defined or loading fails, the runtime falls back to a built-in `player_default` sprite.
- Asset locations: runtime sprite art and per-PNG fallbacks are kept in `src/main/resources/assets/`. Full third-party packs are archived under `src/main/resources/assets/thirdparty/` and are noted in `ATTRIBUTION.md`.

### Extensible Command System

- Command parser for player input (move, look, up, down, etc.)
- Easily extendable for new commands and actions

-
## Tech Stack

- Java 17
- Maven 3.8+
- LibGDX 1.13.1 (core, LWJGL3 backend, desktop natives)
- VisUI 1.5.5 (Scene2D UI widgets/skin)
- JUnit 5 (testing)

## Roadmap
- Possibly a class system for players
- Skills and abilities that can be used in and out of combat
- Mana, stamina and other resources tied to class abilities, skills and spells 
- For every player Stat to have a meaningful use.
- Many choices for player characters who each feel different, fun and balanced within their own play-styles. 
- Sprite variety for environment.
- Gear, armor and weapons would affect how the player looks.
- Animations for characters and environment.
- Randomized dungeon layouts.
- Deepen the element based combat system, for example statuses for bleeding, stuns, shocked etc. Elements would build up to statuses.
- Interactive grid based world: Explore the dungeon and its many secrets.
- Making enemies intelligent, having them move around the map searching for the player and making the smarter during the combat.
- Traps, consumables, events and story elements. (Potions have been implemented) 
- A story mode and a roguelike mode. More handcrafted elements and story elements in the story mode and a fun roguelike mode with randomized floors, loot and enemies!
- Sound and music.

---

## Wishlist
- Interesting story/lore.
- Balanced and fun gameplay.
- A huge compendium of armor, weapons, items and monsters.
- For the game to proudly call itself a roguelike dungeon crawler.
- For the game to use mostly self-made sprites.

---

## Project Structure

```
recall-dungeon/
├─ LICENSE                    # MIT License (code)
├─ ASSETS-LICENSE             # CC BY-NC-ND 4.0 (assets)
├─ pom.xml                    # Maven build configuration
├─ README.md                  # Project overview and docs
├─ assets/                    # LibGDX runtime assets (skins, fonts, textures)
│  ├─ uiskin.json
│  ├─ uiskin.atlas
   │  │        │  └─ util/
   │  │        │     └─ Dice.java                # Utility for parsing and rolling NdM-style dice strings
│  ├─ uiskin.png
   ├─ main
   │  ├─ java
   │  │  └─ com/bapppis
   │  │     ├─ Main.java
   │  │     └─ core
   │  │        ├─ gfx
   │  │        │  ├─ DesktopLauncher.java     # LibGDX desktop entrypoint
   │  │        │  ├─ RecallDungeon.java       # LibGDX ApplicationAdapter (UI)
   │  │        │  └─ MapActor.java            # Scene2D actor that renders the ASCII map
   │  │        ├─ creature
   │  │        │  ├─ Creature.java
   │  │        │  ├─ Enemy.java
   │  │        │  ├─ CreatureLoader.java
   │  │        │  └─ player/Player.java
   │  │        ├─ dungeon
   │  │        │  ├─ Coordinate.java
   │  │        │  ├─ Floor.java
   │  │        │  ├─ Tile.java
   │  │        │  ├─ MapPrinter.java
   │  │        │  └─ mapparser/MapParser.java
   │  │        ├─ event/...
   │  │        ├─ game
   │  │        │  ├─ Combat.java
   │  │        │  ├─ CommandParser.java
   │  │        │  ├─ Game.java
   │  │        │  └─ GameState.java
   │  │        ├─ item/...
   │  │        ├─ spell
   │  │        │  └─ Spell.java
   │  │        └─ property
   │  │           ├─ PropertyImpl.java
   │  │           └─ PropertyManager.java
   │  └─ resources
   │     └─ assets
   │        ├─ IDS.md
   │        ├─ sprite_pngs/
   │        ├─ sprites/
   │        ├─ thirdparty/
   │        └─ tiles/
   └─ resources
      ├─ data
      │  ├─ creatures/
      │  │  ├─ README.md
      │  │  ├─ beasts/
      │  │  ├─ constructs/
      │  │  ├─ dragons/
      │  │  ├─ elementals/
      │  │  ├─ humanoids/
      │  │  ├─ players/
      │  │  ├─ plants/
      │  │  ├─ undead/
      │  │  └─ unknown/
      │  ├─ floors/
   │  ├─ loot_pools/
   │  │  ├─ treasure_chest_basic.json
   │  │  └─ small_weapon_cache.json
   │  └─ monster_pools/
   │     └─ cave_goblins.json
      │  ├─ items/
      │  │  ├─ armor/
      │  │  │  └─ armor/
      │  │  │     ├─ Armor of bones.json
      │  │  │     └─ Armor of Water.json
      │  │  ├─ consumables/
      │  │  └─ weapons/
      │  │     ├─ blunt weapons/
      │  │     ├─ magic weapons/
      │  │     ├─ piercing weapons/
      │  │     ├─ ranged weapons/
      │  │     └─ slash weapons/
      │  └─ properties/
      │     ├─ buff/
      │     ├─ debuff/
      │     │  └─ Afraid.json
      │     ├─ immunity/
      │     └─ trait/
      │        ├─ Coward.json
      │        └─ HumanAdaptability.json
   └─ test
      ├─ java                    # Unit and integration tests (JUnit)
      └─ resources
         └─ assets
            ├─ floors
            └─ properties/...
```

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Git

---

## Setup & Installation

1. **Clone the repository:**
   ```sh
   git clone https://github.com/Bapppis/recall-dungeon.git
   cd recall-dungeon
   ```

2. **Build the project:**
   ```sh
   mvn clean install
   ```

---

## Running the Project

Desktop (LibGDX LWJGL3):

```sh
mvn -q -DskipTests package
mvn exec:java -Dexec.mainClass="com.bapppis.core.gfx.DesktopLauncher"
```

Tips:
- Ensure the working directory is the project root so LibGDX sees `assets/`.
- VS Code users can use `.vscode/launch.json` with `"cwd": "${workspaceFolder}"`.
---

## Running Tests

To run all tests:

```sh
mvn test
```

---

## Contributing

- This is a personal project developed for my portfolio. I am building this project solo to demonstrate my individual capabilities and am not seeking contributions.

- I want realize my crazy ideas, learn from my victories and defeats.

- If you have feedback, ideas, questions, general curiosity, feel free to message me!
- Email: Heinonen.sasha@gmail.com
- Discord: Bappis
---

## License

### Code License

All source code in this repository is licensed under the [MIT License](LICENSE):

```
MIT License

Copyright (c) 2025 Sasha Sebastian Heinonen

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### Assets License

This repository contains two categories of non-code assets:

- **Third-party UI assets (VisUI):** The UI library `VisUI` and its built-in skin are licensed under the **Apache License 2.0**. VisUI may be used, modified, and distributed in commercial products; include VisUI's Apache 2.0 text when distributing.

- **Project assets (my own):** All other non-code assets created for this project (images, JSON data files, fonts, floor definitions, etc.) are licensed under **Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 (CC BY‑NC‑ND 4.0)** and are stored in `src/main/resources/assets/` and the top-level `assets/` folder.

   Note: You have added two third-party sprite packs into `src/main/resources/assets/thirdparty/` and recorded them in `ATTRIBUTION.md`. Those sprite packs are not authored by me and are licensed by their respective authors (see `ATTRIBUTION.md` and the files under `assets/thirdparty/` for exact license text). When distributing this project, follow the license terms in those packs and include their license files and attribution as required.

Key points about this mixed licensing:

- You may use VisUI (Apache 2.0) in commercial releases. Include VisUI's license file and attribution when distributing.
- The project-owned assets under **CC BY‑NC‑ND 4.0** are **not permitted** for commercial use and cannot be included in products you sell. If you plan to sell the game, you must either replace these assets with commercially-licensed assets, obtain a commercial license from the asset authors (if any of those assets are third-party), or re-license them under a commercial-permissive license if you own them and wish to do so.
- If you want to keep the CC BY‑NC‑ND assets in the repository for development but avoid accidental inclusion in commercial distributions, remove them from the distribution package (or from Git tracking) and rely on VisUI or replacement assets in released builds.

Project-owned JSON data files
- The JSON files that define creatures, floors, items and properties (the structured game data under `src/main/resources/assets/creatures/`, `floors/`, `items/`, and `properties/`) are authored by the project and licensed under **CC BY‑NC‑ND 4.0**. This means:
   - You may share and redistribute them for non-commercial use.
   - You must give attribution when required by the CC terms.
   - You may not use them in commercial products or redistribute modified versions for commercial use.

Project data location

- The project-owned JSON data (creatures, floors, items, and properties) has already been moved to `src/main/resources/data/` to separate authored game data from runtime art. This makes packaging and licensing clearer.

See `ASSETS-LICENSE` for the full CC BY‑NC‑ND text for the project-owned assets.

---

## Manual

- To be done.

---

## Story

- Welcome to the world of Aurum. You have awoken in this completely new and glorious world but... in an eerie dungeon. Yet you remember being in this situation.
- Use this meta knowledge to explore and find the answers to the questions you have. Will you go deeper into the dungeon to put stop whatever is causing these weird things to happen, or will you seek your escape by going up and defeating whatever is guarding the way up to protect the lands?

---
