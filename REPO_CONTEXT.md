# Repo context prompt

Paste this at the start of a new Claude conversation about this repo to get oriented quickly.

---

This is "Better Item Examine," a RuneLite plugin (Java 11, Gradle) for Old School RuneScape.
It appends hidden/passive item effects to the in-game examine text — mechanics documented on
the OSRS Wiki but never explained in-game (e.g. Twisted bow's Magic-scaling accuracy/damage,
Dragon hunter lance's Draconic bonus, a Barrows set's proc-based heal).

## How it works
MenuOptionClicked (Examine) -> stores item ID -> ChatMessage (ITEM_EXAMINE)
  -> PassiveEffectRepository.getForItemId() -> appends matching passive effect text

## Key files
- src/main/java/com/betteritemexamine/BetterItemExaminePlugin.java — wires the examine flow
- src/main/java/com/betteritemexamine/BetterItemExamineConfig.java — display style, color
  highlighting, equippable-only filter
- src/main/java/com/betteritemexamine/PassiveEffect.java — data model: name, itemIds[],
  description, wikiUrl
- src/main/java/com/betteritemexamine/PassiveEffectRepository.java — loads every JSON file
  listed in DATA_FILES into one itemId -> PassiveEffect map at startup
- src/main/resources/com/betteritemexamine/data/{weapons,armour,jewellery,ammunition}.json —
  the actual content, one JSON array of PassiveEffect objects per category
- src/test/java/com/betteritemexamine/PassiveEffectDataValidationTest.java — validates every
  data file parses, has no missing fields, and no itemId claimed by two entries

## Adding/editing item entries (the most common task in this repo)
Append an object to the right category JSON file — no Java changes needed:
  { "name": "...", "itemIds": [12345], "description": "...", "wikiUrl": "https://oldschool.runescape.wiki/w/..." }
- itemIds must include every relevant variant (charged/uncharged, broken/locked, ornament kit) —
  verify each numeric ID against the item's own OSRS Wiki infobox, don't guess from memory.
  This repo has previously caught a wiki-agent reporting IDs shifted by one slot (Guthan's set).
- description should be terse and factual: state the concrete number/mechanic, written for
  someone who already knows what the item is. Only include effects NOT shown on the item's
  stats/examine screen — if a bonus is already displayed in the equipment stats interface,
  it doesn't belong here.
- Only add a whole new category file (and a DATA_FILES entry in PassiveEffectRepository) if an
  item genuinely doesn't fit weapons/armour/jewellery/ammunition; otherwise fold it into the
  closest existing file.
- After editing, run: ./gradlew test --tests "com.betteritemexamine.PassiveEffectDataValidationTest"
  This is the safety net for malformed JSON / duplicate itemIds across files.

## Environment note
This machine's default JDK (Corretto 26) crashes the pinned Lombok 1.18.30 annotation processor.
Use JAVA_HOME pointed at a JDK 17 install to run ./gradlew successfully.

## Project conventions (see AGENTS.md for the full list)
- Never use Thread.sleep, reflection, or blocking I/O on the client thread.
- Use net.runelite.api.gameval constants (ItemID, InterfaceID, etc.), never magic numbers.
- Don't mix reformatting with feature changes in a commit.
- I (the agent) cannot verify plugin behavior in-game — after a change, tell the user exactly
  what to test (which item, golden path, edge cases) and wait for their in-game confirmation
  rather than declaring the task done.
