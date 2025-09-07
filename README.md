# Recall Dungeon

A Java-based dungeon recall project built with Maven.

Explore the mysterious dungeon where you have awakened. Decide whether you will go further down the dungeon or try to escape to the surface!

This project includes a custom Java-based game engine designed specifically for grid-based dungeon gameplay.
All game logic, entity systems, map parsing, and command handling are part of a custom Java game engine I am developing from scratch.

---

## Table of Contents

- [Roadmap for Features](#roadmap)
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
## Roadmap
- Turn based combat: Fight or flee from the monsters and other habitants of the dungeon.
- Interesting stat and resistance based combat: Match your gear to defeat the monsters efficiently.
- Interactive grid based world: Explore the dungeon and its many secrets.
- Inventory management: Choose and find gear that will help you during your visit.
- Many characters: Choose from many different starting champions that have different stats.
- A story mode and a roguelike mode. More handcrafted elements and story elements in the story mode and a fun roguelike mode with randomized floors, loot and enemies!

---

## Wishlist
- Add a graphical user interface (GUI) using JavaFX.
- Interesting story/lore.
- For the game to proudly call itself a roguelike dungeon crawler.

---

## Project Structure

```
recall-dungeon/
├─ LICENSE                    # MIT License (code)
├─ ASSETS-LICENSE             # CC BY-NC-ND 4.0 (assets)
├─ pom.xml                    # Maven build configuration
├─ README.md                  # Project overview and docs
└─ src
   ├─ main
   │  ├─ java
   │  │  └─ com/bapppis
   │  │     ├─ Main.java
   │  │     └─ core
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
   │  │        │  ├─ CommandParser.java
   │  │        │  ├─ Game.java
   │  │        │  └─ GameState.java
   │  │        ├─ item/...
   │  │        └─ property
   │  │           ├─ PropertyImpl.java
   │  │           └─ PropertyManager.java
   │  └─ resources
   │     └─ assets
   │        ├─ creatures
   │        │  ├─ players/...
   │        │  ├─ humanoids/...
   │        │  └─ ... (beasts, dragons, elementals, etc.)
   │        ├─ items
   │        │  ├─ armor/...
   │        │  ├─ weapons/...
   │        │  └─ consumables/...
   │        ├─ floors
   │        │  ├─ floor(20x20).txt
   │        │  └─ floor(50x50).txt
   │        └─ properties
   │           ├─ trait/...
   │           ├─ buff/...
   │           ├─ debuff/...
   │           └─ immunity/...
   └─ test
      ├─ java                    # Unit and integration tests (JUnit)
      └─ resources
         └─ assets
            ├─ floors
            │  ├─ testFloor(20x20).txt
            │  └─ testFloor(50x50).txt
            └─ properties/...
```

---

## Prerequisites

- Java 17+ (or your project's required version)
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

To run the main application (customize if you have a main class):

```sh
mvn exec:java -Dexec.mainClass="com.bapppis.core.Main"
```

Or, if you have a jar file:

```sh
java -jar target/recall-dungeon-<version>.jar
```

Version in POM.
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

All non-code assets (images, audio, story, text, data files, etc.) in this repository are licensed under the [Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)](ASSETS-LICENSE):

- You must give appropriate credit.
- You may not use the material for commercial purposes.
- You may not remix, transform, or build upon the material.

See the full license at: https://creativecommons.org/licenses/by-nc-nd/4.0/legalcode

---

## Manual

- To be done.

---

## Story

- Welcome to the world of Aurum. You have awoken in this completely new and glorious world but... in an eerie dungeon. Yet you remember being in this situation.
- Use this meta knowledge to explore and find the answers to the questions you have. Will you go deeper into the dungeon to put stop whatever is causing these weird things to happen, or will you seek your escape by going up and defeating whatever is guarding the way up to protect the lands?

---
