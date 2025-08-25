# Recall Dungeon

A Java-based dungeon recall project built with Maven.

Explore the mysterious dungeon where you have awakened. Decide whether you will go further down the dungeon or try to escape to the surface!

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
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── bapppis/
│   │               └── core/
│   │                   ├── creatures/
│   │                   │   ├── Creature.java
│   │                   │   ├── Goblin.java
│   │                   │   └── ...
│   │                   ├── dungeon/
│   │                   │   ├── Dungeon.java
│   │                   │   ├── Tile.java
│   │                   │   └── ...
│   │                   ├── mapparser/
│   │                   │   ├── MapParser.java
│   │                   │   └── ...
│   │                   ├── game/
│   │                   │   ├── Game.java
│   │                   │   └── ...
│   │                   └── ... (other submodules/classes)
│   ├── test/
│   │   └── java/
│   │       └── com/
│   │           └── bapppis/
│   │               └── core/
│   │                   ├── creatures/
│   │                   │   ├── CreatureTest.java
│   │                   │   └── ...
│   │                   ├── dungeon/
│   │                   │   └── DungeonTest.java
│   │                   └── ...
├── target/                # Maven build output (ignored in git)
├── pom.xml                # Maven build file
├── .gitignore
├── LICENSE
└── README.md
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

- This project is licensed under a custom Non-Commercial License.
You may use, modify, and share for personal and educational purposes only.
Commercial use is not permitted. See the [LICENSE](LICENSE) file for details.
- For other use cases, questions or anything else, feel free to message me!

---

## Manual

- To be done.

---

## Story

- Welcome to the world of Aurum. You have awoken in this completely new and glorious world but... in an eerie dungeon. Yet you remember being in this situation.
- Use this meta knowledge to explore and find the answers to the questions you have. Will you go deeper into the dungeon to put stop whatever is causing these weird things to happen, or will you seek your escape by going up and defeating whatever is guarding the way up to protect the lands?

---