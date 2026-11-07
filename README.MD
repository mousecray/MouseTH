# Mouse Translation Helper (MouseTH)

**Mouse Translation Helper** is an advanced, all-in-one localization toolkit and runtime translation engine for Minecraft 1.20.1 (
Fabric & Forge).

Unlike standard dumpers, MouseTH does not just extract missing keys from language files: it digs into compiled bytecode to extract
hardcoded text, scans dialogues and quests for raw JSON nodes, resolves dynamic game registries, and **applies translations on the fly
at runtime** without requiring you to repack assets into resource packs or modify other mods.

> ⚠️ **Disclaimer & Project Status**
> * **Work in Progress:** This mod has not been exhaustively tested across all possible mod combinations and environments. Edge cases,
    incompatibilities, or unexpected issues may occur. Bug reports and feedback are welcome!
> * **Target Version & Ports:** Currently, there are **no planned ports** to other Minecraft versions. If you desperately need a port
    to a specific version, feel free to reach out directly via email: [mousecray@gmail.com](mailto:mousecray@gmail.com).
> * **AI Assistance:** Artificial intelligence tools were employed during the development of this project - specifically, to create and
    adapt multilingual user interface translations for 10 languages.

---

## 📊 Comparison with Other Translation Tools

| Capability / Feature                                                                                               |                         **MouseTranslateHelper (MouseTH)**                          |                   Standard Translation Mods / Packs                   |                          External CLI / Parser Scripts                           |
|:-------------------------------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------:|:---------------------------------------------------------------------:|:--------------------------------------------------------------------------------:|
| **Comprehensive Source Scanning**<br>*(JAR bytecode, registries, non-lang JSONs, resource packs, world datapacks)* |             ✅ **Full & Automatic**<br>Scans all active sources in-game              |       ❌ **None / Manual**<br>Only parses existing `.json` files       |      ⚠️ **Partial**<br>Static file scan only; no access to live registries       |
| **Hardcoded Bytecode String Translation**<br>*(Translating hardcoded Java strings in UI/screens)*                  | ✅ **Native**<br>Low-level ASM transformation (ModLauncher on Forge, Knot on Fabric) |                          ❌ **Not Supported**                          | ⚠️ **Offline Patching Only**<br>Requires manual JAR editing (InClassTrans/Recaf) |
| **Raw JSON Literal (`"text"`) Translation**<br>*(Quests, dialogue trees, advancements, custom books)*              |         ✅ **Real-Time Stream Interception**<br>Patches JSON streams on read         |      ❌ **Not Supported**<br>Cannot be translated via `lang.json`      |              ❌ **Manual**<br>Requires overriding files in datapacks              |
| **Zero-Repack Runtime Translation**<br>*(Playing with translations without creating resource packs)*               |        ✅ **Built-in**<br>Loads translations directly from `MouseTH/` folder         | ⚠️ **Manual Setup**<br>Must assemble, zip, and enable a resource pack |                  ❌ **None**<br>Files must be packaged manually                   |
| **Centralized Datapack Translation**<br>*(Translate once, share across all worlds)*                                |         ✅ **Unified Root Storage**<br>Saved globally so all worlds benefit          |          ❌ **None**<br>Must be copied into every world save           |                                   ❌ **Manual**                                   |
| **One-Time Session vs. Persistent Loading**                                                                        |     ✅ **Supported**<br>Test translations per-session or enable persistent mode      |                          ❌ **Not Supported**                          |                               ❌ **Not Supported**                                |
| **Unified Multi-Loader Support**                                                                                   |       ✅ **Fabric & Forge**<br>Native low-level bytecode hooks on both loaders       |         ⚠️ **Fragmented**<br>Often locked to a single loader          |                         ➖ **Neutral**<br>(Offline tool)                          |
| **In-Game User Interface**                                                                                         |       ✅ **Full GUI**<br>Setup wizard, language picker, configs, world dialogs       |              ⚠️ **Minimal**<br>Basic config menu or none              |                       ❌ **None**<br>(CLI / terminal only)                        |
| **Smart & Aggressive Noise Filtering**                                                                             |  ✅ **Built-in**<br>Filters datagen, null-checks, logs (`{}`), regexes, UUIDs, IPs   |                              ❌ **None**                               |                       ⚠️ **Requires custom regex writing**                       |
| **Automatic Backup System**                                                                                        |    ✅ **Automatic ZIP Archive**<br>Backed up to `mdata/backups/` with timestamps     |             ❌ **None**<br>Files are overwritten silently              |                                   ❌ **Manual**                                   |
| **Flexible Pattern Matching in Bytecode**<br>*(Line numbers, wildcard `*`, `*+X` from start, `*-X` from end)*      |             ✅ **Full Support**<br>Tolerant matching with fallback modes             |                          ❌ **Not Supported**                          |                               ❌ **Not Supported**                                |

---

## 🚀 Key Features

### 1. Deep Multi-Source Extraction

* **Missing Translations (`translations.json`)**: Compares loaded `en_us.json` stacks across mods, resource packs, and translation mods
  against your selected language.
* **Registry Key Discovery**: Uses Minecraft's dynamic registry polymorphism (`getDescriptionId()`) across items, blocks, entities, mob
  effects, and enchantments to catch objects forgotten by mod authors.
* **Raw Bytecode Strings (`raw.json`)**: Scans compiled JVM bytecode (`.class`) instructions to find hardcoded UI labels, tooltips, and
  disconnect screens.
* **Raw JSON Literals (`rawjson.json`)**: Traverses dialogues, quests (FTB Quests, Patchouli), and custom advancements to extract
  unlocalized `"text"` components, preserving relative resource paths.
* **Resource Packs & World Datapacks**: Can scan active resource packs and local world saves for missing keys and text.

### 2. Runtime Translation (No Repacking Required)

* **Standard Keys**: Injected directly into the client's language storage on resource load.
* **Raw JSON Stream Patching**: Patches JSON streams in memory on demand through resource manager interception.
* **Bytecode String Patching**: Replaces LDC string constants natively:
    * **Forge**: Hooked into `cpw.mods.modlauncher.serviceapi.ILaunchPluginService`.
    * **Fabric**: Hooked into `net.fabricmc.loader.impl.launch.knot.KnotClassLoader`.
* **Session Testing**: Test translations for a single session using the in-game GUI or enable `loadMode` to load them automatically on
  startup.

### 3. Centralized Datapack Management

Translations for world datapacks are saved directly to the root `.minecraft/MouseTH/` directory. Once you translate a datapack from one
world, that translation is **automatically shared and applied to any other world** using that datapack. The world save folder only
maintains a minimal timestamp file tracking exports for that specific save.

### 4. Smart Noise Filtering

* **Baseline Filtering (Always active)**: Automatically strips modder-facing assertion errors (`cannot be null`, `must not be empty`),
  internal packages, fully qualified class names, and data generation classes (`/data/`, `/datagen/`, `Provider.class`).
* **Aggressive Filtering (Optional)**: Filters out SLF4J/Log4j logger placeholders (`{}`), regex patterns, format masks (`#####0.00`,
  date patterns), UUIDs, and IP addresses.

### 5. Safe Workflow & Automatic Backups

* **Overwrite Confirmation**: Displays a warning modal if existing translation data is found before exporting.
* **Timestamped Backups**: Automatically compresses previous exports into `.zip` archives stored in `mdata/backups/`.
* **Last Export Tracker**: Displays the exact timestamp of the last export directly in the UI.

---

## 📁 Export Directory Structure

All translations (mods, resource packs, and world datapacks) are centralized in the root game directory:

```text
.minecraft/
├── mdata/
│   ├── backups/
│   │   └── backup_2026-11-07_01-30-00.zip  # Timestamped ZIP backup
│   ├── last_export.txt                     # Timestamp of last global mod export
└── MouseTH/
    ├── <modid_or_datapack_id>/
    │   └── <target_language>/
    │       ├── translations.json           # Localization keys
    │       ├── raw.json                    # Bytecode hardcoded text
    │       └── rawjson.json                # Raw JSON text literals
    └── ...
```

### World Save Directory

World folders remain completely clean. Only a timestamp tracking when that specific world's datapacks were exported is kept:

```text
.minecraft/saves/<WorldName>/
└── mdata/
    └── last_export.txt
```

---

## 🛠 Deep Dive: Rules & Syntax in `raw.json`

Hardcoded strings in Java bytecode often appear multiple times within a single method or across multiple overloads. To solve this,
`raw.json` groups entries by:  
`Class -> MethodSignature(Name + Descriptor) -> Selector -> "Original": "Translation"`

### Selector Rules

| Selector                 | Rule / Behavior          | Description                                                                                                |
|:-------------------------|:-------------------------|:-----------------------------------------------------------------------------------------------------------|
| `"142"` *(Numeric)*      | **Specific Line Number** | Matches and replaces the string **only if** its instruction belongs to that specific bytecode line number. |
| `"*"`                    | **All Occurrences**      | Replaces **every** occurrence of the matching string inside this method.                                   |
| `"*+X"` *(e.g. `"*+2"`)* | **First X from Start**   | Replaces the first `X` occurrences of the string from the beginning of the method.                         |
| `"*-X"` *(e.g. `"*-1"`)* | **Last X from End**      | Replaces the last `X` occurrences of the string counting backwards from the end of the method.             |

### Tolerance & Safety

* **Graceful Fallback:** If you specify a count greater than the actual occurrences in the method (for example, `"*+5"` when the string
  only appears twice), MouseTH **will not crash or throw an error**. It will safely patch all available occurrences and continue
  execution.
* **Strict Line Matching (`bindToLineNumbers`):**
    * When `bindToLineNumbers` is **enabled**, selectors are strictly enforced. Numeric line selectors will be skipped if the line
      number does not match.
    * When `bindToLineNumbers` is **disabled**, line numbers and position constraints are ignored: the mod matches by method signature
      and replaces all matching strings in that method.

### `raw.json` Example

```json
{
  "net/example/mod/client/gui/CustomScreen": {
    "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V": {
      "42": {
        "Click to start machine": "Нажмите для запуска машины"
      },
      "*+1": {
        "Status: Idle": "Статус: Ожидание"
      },
      "*-1": {
        "Status: Running": "Статус: Работает"
      },
      "*": {
        "Energy: %s FE": "Энергия: %s FE"
      }
    }
  }
}
```

---

## 🛠 File Formats

### `translations.json`

Standard key-value pairs. Contains missing keys with original English text as a prompt:

```json
{
  "item.examplemod.wrench": "Mechanical Wrench",
  "gui.examplemod.energy": "Stored Energy"
}
```

### `rawjson.json`

Grouped by relative asset/data file path. Target `"text"` literals from quests, books, and dialogues:

```json
{
  "assets/examplemod/patchouli_books/guide/en_us/entries/intro.json": {
    "Welcome to the mod!": "Добро пожаловать в мод!"
  }
}
```

---

## 📋 Requirements

* **Minecraft**: `1.20.1`
* **Mod Loader**: Fabric (with Fabric API) or Minecraft Forge
* **Java**: `17`

---

## 📥 Installation & Getting Started

1. Place the `.jar` file into your `.minecraft/mods` folder.
2. Launch Minecraft.
3. On first startup, select your desired translation language and review the export settings.
4. Click **Export** to generate translation files in `.minecraft/MouseTH/`.
5. Edit the generated JSON files with your translations.
6. Click **Load Game** to play with your translations immediately, or enable **Load Mode** in the config to apply them permanently on
   every launch!

---

## ⚙️ Configuration Options

| Option                         | Default | Description                                                                                   |
|:-------------------------------|:-------:|:----------------------------------------------------------------------------------------------|
| `extractMissingTranslations`   | `true`  | Exports keys present in `en_us.json` but missing in the target language                       |
| `extractMissingKeys`           | `true`  | Scans registries for objects without localization entries                                     |
| `extractRawStrings`            | `true`  | Scans mod bytecode for hardcoded string constants                                             |
| `minRawStringLength`           |   `3`   | Minimum length required for a raw string to be extracted                                      |
| `aggressiveRawStringFiltering` | `true`  | Additional filtering for log placeholders, regexes, UUIDs, and masks                          |
| `extractJsonRawStrings`        | `true`  | Extracts unlocalized `"text"` literals from non-lang JSON files                               |
| `scanResourcePacks`            | `false` | Scans files inside `.minecraft/resourcepacks/`                                                |
| `loadMode`                     | `false` | When enabled, skips the setup screen and loads all translations automatically on launch       |
| `translateKeys`                | `true`  | Injects keys from `translations.json` into the game                                           |
| `translateRawStrings`          | `false` | Transforms bytecode LDC strings using `raw.json` (requires game launch with `loadMode: true`) |
| `bindToLineNumbers`            | `false` | Enforces strict line number matching during bytecode translation                              |
| `translateJsonRawStrings`      | `false` | Dynamically patches JSON `"text"` nodes using `rawjson.json`                                  |
| `loadResourcePacks`            | `false` | Loads translation files for custom resource packs                                             |
| `loadWorldDatapacks`           | `false` | Loads datapack translations saved in `.minecraft/MouseTH/`                                    |
| `showWorldSetupScreen`         | `true`  | Displays the localization setup dialog when joining a world                                   |

---

## 🌐 Supported Languages (UI)

The MouseTH in-game interface is translated into 13 languages:

* English (`en_us`)
* Russian (`ru_ru`)
* German (`de_de`)
* Spanish (`es_es`)
* French (`fr_fr`)
* Italian (`it_it`)
* Japanese (`ja_jp`)
* Korean (`ko_kr`)
* Polish (`pl_pl`)
* Portuguese (Brazil) (`pt_br`)
* Turkish (`tr_tr`)
* Ukrainian (`uk_ua`)
* Simplified Chinese (`zn_cn`)

---

## 🤝 Contributing

Contributions, bug reports, and pull requests are welcome!

1. Fork the repository.
2. Create a feature branch: `git checkout -b feature/new-feature`.
3. Commit your changes: `git commit -m "Add new feature"`.
4. Push to the branch: `git push origin feature/new-feature`.
5. Open a Pull Request.

---

## 📄 License

This project is licensed under the **GNU Lesser General Public License Version 3 (LGPL-3.0)**.