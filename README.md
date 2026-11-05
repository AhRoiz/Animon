# Animon: Elemental Clash

Animon: Elemental Clash is a 1v1 local hotseat turn-based monster battle game written in Java. Two trainers share one device and take turns commanding teams of up to 6 Animon, managing Mana, item drafts, elemental weaknesses, delayed buffs, and mid-match shop phases until one trainer runs out of fighters.

The repository ships two interfaces over the same core battle engine:
- A Retro Game Boy (DMG-01) desktop interface built with Java Swing (`animon.gui.MainFrame`)
- A terminal command-line interface (`animon.Main`)

## How the Game Works

Each match pits Trainer 1 against Trainer 2 with `150G` starting gold, up to `6` Animon per team, and a `6`-slot item bag. Between every hotseat action, a privacy transition screen hides team stats and item picks while players pass the device.

### Combat Mechanics

- Elemental cycle (`Air > Api > Ice > Earth > Air`, while `Normal` stays neutral): hitting an elemental weakness deals `x2` damage and grants `ONE MORE` (one bonus action on the same turn, capped at once per turn). Hitting a resisted or matching element deals `x0.5` damage.
- Critical hits (`x1.5` damage) also trigger `ONE MORE` when the target survives.
- Physical type matchups (`Darat`, `Terbang`, `Air`, `Naga`, `Robot`) modify accuracy, critical chance, damage multipliers, or speed debuffs:

| Attacker | Defender | Matchup Effect |
| :--- | :--- | :--- |
| Darat | Terbang | `+35%` miss chance |
| Darat | Robot | `x0.75` damage |
| Terbang | Darat | `+15%` critical chance |
| Terbang | Robot | `x0.85` damage |
| Air | Darat | `50%` chance to apply `-30%` Speed for 2 turns |
| Air | Robot | `+25%` critical chance, `x1.2` damage |
| Air | Terbang | `+15%` miss chance |
| Naga | Naga | `+20%` critical chance, `x1.25` damage |
| Naga | Robot | `x0.8` damage |
| Naga | Other types | `x1.1` damage |
| Robot | Terbang | `-10%` miss chance |
| Robot | Air | `x0.85` damage |

- Role classes and passives:
  - `HewanDPS` deals `+10%` outgoing damage and uses `BURST` (bonus critical rate) or `PIERCE` (ignores part of target Defense) skills.
  - `HewanSupport` uses `LIFESTEAL` (absorbs HP from damage dealt) or `HEAL` (restores ally HP and clears negative status effects) skills.
  - `HewanTank` takes `-10%` incoming damage and uses `SHIELD` (temporary damage barrier) or `TAUNT` (blocks the target from casting skills) skills.
- Turn actions: `FIGHT` (0 MP Normal basic attack or Mana-cost elemental skill), `SWITCH` (swaps the active Animon; free when replacing a fainted fighter), `ITEM` (applies a bag item to any team member), and `DEFEND` (halves incoming damage until the trainer's next turn).
- Delayed Buff queue: items or skills configured with `delayTurn > 0` enter a FIFO queue in `BattleEngine` and activate at the start of the target owner's next turn.

### Drafting, Rewards, and Shop Economy

1. Team preparation: trainers choose team size (`1` to `6`), assemble rosters (snake draft or random in CLI; randomized pairs with re-roll in GUI), and pick their opening Animon.
2. Initial item draft: each trainer views `4` random gacha items and picks `2` in private.
3. Knockout rewards: defeating an opposing Animon awards `+60G` and opens a `4`-pick-`1` Kill Reward draft.
4. Full inventory rule: when a `6/6` bag receives a new item, the trainer must sell an existing slot at `50%` of its buy price, discard a slot, or decline the new item.
5. Shop phase: every `3` completed rounds, both trainers receive `+40G` and visit a restocked shop to buy items or sell inventory slots at `50%` value.

## Features

### Data-Driven Catalog
`GameSeeder` loads `13` Animon species from `data/data_hewan.json` and `17` items (`ItemHeal`, `ItemBuff`, and `ItemEvolusi`, including revive and permanent type/element evolution items) from `data/data_item.json` without hardcoded fighters in `Main`.

### Retro DMG-01 Swing GUI
- Seven dedicated views: Main Menu, Trainer Setup, Blind Turn Transition, Battle Arena, Gacha/Kill Reward Draft, Item Shop, and a separate Match History & Step Replay window.
- Four-shade DMG-01 LCD green palette (`#0f380f`, `#306230`, `#8bac0f`, `#9bbc0f`) tuned for WCAG AA text contrast (`>= 5.03:1`) with nearest-neighbor 64x64 sprite scaling (`Image.SCALE_REPLICATE`) and bundled pixel fonts (`PressStart2P` and `VT323`, with automatic bold `Monospaced` fallback).
- Live top-bar display and accessibility switches: toggle original vs. 4-shade Game Boy sprite filtering (`SPRITE: ASLI/DMG`), dot-matrix scanline overlay (`LCD: ON/OFF`), and UI animations (`GERAK: ON/OFF` or `--no-motion`).
- Full keyboard operation (`Tab`, `Shift+Tab`, `Enter`, `Space`, `Escape` modal/submenu dismissal, and `Alt` mnemonics) alongside mouse controls.

### Save/Load and Step-by-Step Replay
- Active matches auto-save to `data/savegame.json` at the start of every turn and can be saved manually at any time via `SIMPAN`, then resumed from the main menu via `LOAD GAME`.
- Completed matches record every action snapshot (round, acting/target Animon, HP before and after, and combat log lines) to MySQL via JDBC (`pertandingan` and `langkah_replay` tables) and mirror a local copy to `data/riwayat_pertandingan.json` so replays (`< PREV`, `NEXT >`, and pausable `AUTO PLAY`) work offline.

### Terminal CLI Mode
- Interactive text menus with optional snake-draft team selection, live damage/effectiveness previews, hotseat screen clearing, and ANSI color output.

## Tech Stack

| Layer | Technology |
| :--- | :--- |
| Language | Java (`java.base`, `java.desktop`, `java.sql`) |
| Desktop GUI | Java Swing & AWT (`JFrame`, `CardLayout`, `SwingWorker`, `javax.swing.Timer`, `javax.accessibility`) |
| Terminal UI | Standard I/O (`System.in` / `System.out`) with ANSI formatting (`animon.ui`, `animon.util.Teks`) |
| Data & Save Format | Custom zero-dependency JSON parser and writer (`JsonParser`, `JsonWriter`) over UTF-8 files |
| Match History DB | JDBC (`java.sql.*`) targeting MySQL (`animon_db`), with automatic local JSON fallback |
| Build Dependencies | None required (standard `javac` compiler; optional MySQL Connector/J `.jar` only for live MySQL) |

## Compatibility

| Requirement | Supported Environment |
| :--- | :--- |
| JDK Version | Java 11 or newer (verified against `--release 11` bytecode target and tested on JDK 21) |
| Operating Systems | Windows (`run.bat`, `run_gui.bat`), Linux, and macOS (`run.sh`, `run_gui.sh`) |
| Display (GUI) | Desktop session (`GraphicsEnvironment.isHeadless() == false`), minimum window size `920x660` (`1040x740` default) |
| Terminal (CLI) | Windows Terminal, PowerShell, `cmd.exe`, `bash`, `zsh` (use `--no-color --no-clear` on plain consoles) |
| Database (GUI) | Offline out of the box via `data/riwayat_pertandingan.json`; optional MySQL 8.0+ / MariaDB with Connector/J |

## How to Run

Run all commands from the repository root (`animon/`) so the default `./data` and `src/animon/assets` directories resolve automatically.

### 1. Desktop GUI (Swing)

On Windows:
```bat
run_gui.bat
```

On Linux or macOS:
```sh
chmod +x run_gui.sh
./run_gui.sh
```

With arguments (for example, deterministic RNG seed and reduced motion):
```bat
run_gui.bat --seed 42 --no-motion
```

### 2. Terminal CLI

On Windows:
```bat
run.bat
```

On Linux or macOS:
```sh
chmod +x run.sh
./run.sh
```

### 3. Manual Compile and Run

PowerShell (Windows):
```powershell
Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { '"' + (Resolve-Path -Relative $_.FullName).Replace('\','/') + '"' } | Set-Content sources.txt
javac -encoding UTF-8 -d out @sources.txt
Remove-Item sources.txt

# Launch Swing GUI
java -cp out animon.gui.MainFrame

# Or launch CLI
java -cp out animon.Main
```

Bash / Zsh (Linux & macOS):
```sh
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")

# Launch Swing GUI
java -cp out animon.gui.MainFrame

# Or launch CLI
java -cp out animon.Main
```

### Command-Line Options

| Flag | Applies To | Description |
| :--- | :--- | :--- |
| `--data <folder>` | GUI & CLI | Path to folder containing `data_hewan.json` and `data_item.json` (default: `./data`) |
| `--seed <number>` | GUI & CLI | Fixed `Random` seed for reproducible drafts, gacha rolls, and combat rolls |
| `--no-motion` | GUI | Starts with UI animations turned off (can also be toggled via `GERAK: ON/OFF`) |
| `--no-color` | CLI | Disables ANSI color escape codes |
| `--no-clear` | CLI | Skips console clear-screen calls between menus |
| `-h`, `--help` | CLI | Prints CLI usage help and exits |

### Optional MySQL Setup for Match History

Without MySQL, the GUI stores and loads all match replays in `data/riwayat_pertandingan.json`. To enable MySQL storage:

1. Start a MySQL server and ensure a MySQL Connector/J `.jar` (for example `mysql-connector-j-8.4.0.jar`) is available.
2. Configure connection settings via `data/db.properties` or environment variables:

```properties
db.url=jdbc:mysql://localhost:3306/animon_db?createDatabaseIfNotExist=true&serverTimezone=UTC
db.user=root
db.password=
```

Environment variables (`ANIMON_DB_URL`, `ANIMON_DB_USER`, `ANIMON_DB_PASS`) override `data/db.properties` when set.
3. Include the driver `.jar` on the classpath when launching `MainFrame`:

```bat
REM Windows (; separator)
java -cp "out;lib\mysql-connector-j-8.4.0.jar" animon.gui.MainFrame
```

```sh
# Linux / macOS (: separator)
java -cp "out:lib/mysql-connector-j-8.4.0.jar" animon.gui.MainFrame
```

`RiwayatRepository` creates the `pertandingan` and `langkah_replay` tables automatically on first connection.

## Project Structure

```text
animon/
├── data/
│   ├── data_hewan.json        # 13 Animon species definitions
│   └── data_item.json         # 17 consumable, buff, and evolution items
├── src/animon/
│   ├── Main.java              # CLI entry point
│   ├── assets/                # 64x64 pixel sprites and PressStart2P / VT323 fonts
│   ├── battle/                # BattleEngine, DamageCalculator, ElemenChart, TypeChart
│   ├── data/                  # GameSeeder JSON factory
│   ├── enums/                 # Role, Tipe, Elemen, StatType, Rarity, Efektivitas
│   ├── game/                  # CLI game flow controller (Permainan)
│   ├── gui/                   # Swing GUI (MainFrame, GameManager, HistoryFrame, panels, theme, persistence)
│   ├── item/                  # Abstract Item + ItemHeal, ItemBuff, ItemEvolusi
│   ├── kontrak/               # Interfaces (BisaBertarung, BisaDipakai, PenanganInventoryPenuh)
│   ├── model/                 # Trainer, Skill, Buff, abstract Hewan + DPS/Support/Tank subclasses
│   ├── sistem/                # Gacha and Shop logic
│   ├── ui/                    # CLI console rendering (Konsol, Tampilan)
│   └── util/                  # Zero-dependency JsonParser, RNG wrapper, ANSI text helpers
├── run.bat / run.sh           # Compile & launch CLI
└── run_gui.bat / run_gui.sh   # Compile & launch Swing GUI
```

