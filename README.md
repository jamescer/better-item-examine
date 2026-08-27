# Better Item Examine

A RuneLite plugin that appends hidden or passive item effects to the in-game examine text. It helps surface mechanics that are documented on the OSRS Wiki but not explained anywhere in-game, such as Twisted Bow damage scaling, Dragon Hunter bonuses, and other passive item effects.

## Why this plugin exists

Old School RuneScape often has equipment that grants useful bonuses without clearly explaining them in the examine text. Better Item Examine fills that gap by showing the relevant passive effect when an item is examined.

## Features

- Appends passive effect text to the standard item examine message
- Supports a configurable display style, including appended text or a separate message
- Lets users color-highlight passive effect text for readability
- Can be filtered to equippable items only
- Uses a data-driven repository so new item entries can be added without touching Java logic

## How it works

```text
MenuOptionClicked (Examine) --> stores item ID --> ChatMessage (ITEM_EXAMINE)
                                                   |
                                                   v
                             PassiveEffectRepository.getForItemId()
                                                   |
                                                   v
                                 appends passive effect text
```

Key parts of the project:

- `BetterItemExaminePlugin`: listens for the item examine flow and wires the plugin behavior together.
- `PassiveEffectRepository`: loads passive effect data at startup and resolves item IDs to matching entries.
- `PassiveEffect`: domain model for each item effect, including name, item IDs, description, and wiki link.
- `src/main/resources/com/betteritemexamine/data/`: JSON data files for categories such as weapons, armour, ammunition, and jewellery.
- `BetterItemExamineConfig`: user-facing settings for display behavior and filtering.

## Project structure

```text
.
├── build.gradle
├── runelite-plugin.properties
├── settings.gradle
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── gradlew
├── gradlew.bat
└── README.md
```

## Getting started

### Prerequisites

- JDK 11+
- Gradle wrapper included in the repo
- A RuneLite development environment if you want to run the plugin in-game

### Local validation

```bash
./gradlew test
```

This project includes validation tests that check the JSON data for malformed entries, missing fields, and duplicate item IDs before the plugin is used.

### Running in RuneLite

The repo is the plugin module itself, not a full RuneLite client checkout. To test it in-game, use one of these approaches:

1. Open this folder as a Gradle project in IntelliJ IDEA and use the RuneLite plugin development workflow.
2. Place the plugin module into a local RuneLite source checkout and run it from there.
3. Build a plugin jar and use RuneLite's external/sideload workflow.

For the most reliable development flow, follow the official RuneLite plugin guidelines and run the plugin from a local RuneLite checkout.

## Adding or updating item data

You usually do not need to change Java code to add a new item effect. Add a new entry to the relevant JSON file under `src/main/resources/com/betteritemexamine/data/`.

Example:

```json
{
  "name": "Item name",
  "itemIds": [12345, 67890],
  "description": "Short description of what the passive actually does.",
  "wikiUrl": "https://oldschool.runescape.wiki/w/Item_name"
}
```

Notes:

- Prefer including all relevant variants of an item, such as charged, uncharged, ornamented, or degraded forms.
- Keep descriptions concise so they read naturally in the examine line.
- Verify item IDs against a trusted source before committing data, such as the OSRS Wiki infobox or RuneLite developer tools.
- Run `./gradlew test` after changing data files.

## Contributing

Contributions are welcome, especially in the following areas:

- Adding missing passive effects for under-documented items
- Improving data accuracy and item coverage
- Tuning the display and configuration behavior
- Catching edge cases in the validation tests

## Testing

The main safety check is the automated validation suite, which verifies that data files are well-formed and item IDs are unique and usable. Run:

```bash
./gradlew test
```

This provides fast feedback without needing to launch the game client.
