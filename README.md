# Better Item Examine

A RuneLite plugin that appends "hidden" passive effects to an item's examine
text — the kind of mechanic (Twisted Bow's Magic-scaling accuracy/damage,
Dragon Hunter weapons' Draconic bonuses, etc.) that's documented on the OSRS
Wiki but never explained anywhere in the actual game.

## How it works

```
MenuOptionClicked ("Examine")  --stores itemId-->  ChatMessage (ITEM_EXAMINE)
                                                            |
                                                            v
                                        PassiveEffectRepository.getForItemId()
                                                            |
                                                            v
                                          append/send passive effect text
```

- **`BetterItemExaminePlugin`** — wires the two RuneLite events above together.
  This is intentionally thin; it shouldn't need to change much.
- **`PassiveEffectRepository`** — loads `passive_effects.json` at startup into
  an `item id -> PassiveEffect` map.
- **`PassiveEffect`** — POJO for one entry (name, item ids, description, wiki
  link).
- **`src/main/resources/com/betteritemexamine/data/*.json`** — the actual
  data, split by category (`weapons.json`, `armour.json`, `ammunition.json`,
  `jewellery.json`). **These are the files you'll touch 95% of the time.**
- **`BetterItemExamineConfig`** — user-facing settings (append vs. separate
  message, highlight color, equippable-only filter).

## Setup

1. Open this folder as a Gradle project in IntelliJ IDEA (`File > Open`,
   select `build.gradle`).
2. Let Gradle sync — it pulls the RuneLite `client` artifact from
   `https://repo.runelite.net`.
3. To actually run/test it against a live client, either:
   - Drop this plugin into a full RuneLite dev checkout under
     `runelite-client/src/main/java/...` temporarily, or
   - Package it and load it via RuneLite's "sideload" / external plugin
     support once you've got the jar built (`./gradlew build`).
   - Simplest for iteration: clone the full [runelite/runelite](https://github.com/runelite/runelite)
     repo, add this module's source into it as an external plugin per the
     [RuneLite plugin development docs](https://github.com/runelite/runelite/wiki/Developing-Plugins),
     and run it from IntelliJ.

## Adding a new item's passive effect

You do **not** need to touch any Java to add an entry to an existing
category file. Open the relevant file under
`src/main/resources/com/betteritemexamine/data/` (`weapons.json`,
`armour.json`, `ammunition.json`, or `jewellery.json`) and add:

```json
{
  "name": "Item name",
  "itemIds": [12345, 67890],
  "description": "Short description of what the passive actually does.",
  "wikiUrl": "https://oldschool.runescape.wiki/w/Item_name"
}
```

Adding a **whole new category** (e.g. `gloves.json`)? Create the file, then
add its filename to `DATA_FILES` in `PassiveEffectRepository.java` — the one
place that still needs a code change.

Notes:
- `itemIds` should include every relevant variant (charged/uncharged,
  ornament kits, degraded versions) if you want them all covered.
- Keep `description` short — it gets appended directly to the chatbox
  examine line.
- **Verify item ids before trusting them.** The seed data in this repo was
  written from general knowledge of these items, not confirmed against a
  live client, so treat the ids as a starting point rather than ground
  truth. To verify: check the "Item ID" field in the infobox on the item's
  [OSRS Wiki](https://oldschool.runescape.wiki) page, or enable RuneLite's
  Developer Tools (launch with `--developer-mode`) which shows the item id
  when you hover/examine something in-game.
- Run `./gradlew test` after editing — `PassiveEffectDataValidationTest`
  checks every data file for duplicate item ids, missing fields, and
  malformed JSON, and will fail loudly (with a clear message) if something's
  wrong before you ever load the client.

## Known gaps / good first contributions

- **NPC/object examine text** isn't covered yet — only inventory/equipment/
  bank items via the "Examine" menu option. Widget-based examine (e.g.
  hovering items in some interfaces) may need extra `MenuAction` handling.
- **No overlay/tooltip mode** — currently only touches the chatbox message.
  A hover tooltip (via RuneLite's `Overlay` system) showing passive info
  without needing to examine would be a nice addition.
- **No dedupe/queueing** for `pendingExamineItemId` — see the TODO comment
  in `BetterItemExaminePlugin`. Fine for normal play, but a small queue
  would make it more robust.
- **Data coverage** — the seed data files cover a couple dozen well-known
  items across weapons, armour sets, ammo, and jewellery. There are many
  more items with undocumented-in-game passives worth adding (degradable
  weapon variants, more ammo types, more armour sets, boss-specific gear).
- **Item id accuracy** — see the verification note above. Treat every id in
  the seed data as unverified until checked against a live client or the
  wiki infobox.
- **Unit conversion for the `onlyEquippable` config option** is defined but
  not yet wired up — currently it's a no-op. Wiring it up would need an
  `ItemComposition` lookup (`client.getItemDefinition(itemId).isEquipable()`
  or similar) before appending the passive text.

## Testing

`PassiveEffectDataValidationTest` is the main safety net — it parses every
file in `DATA_FILES`, checks for duplicate item ids across files, flags
missing `name`/`description`/`itemIds`, and sanity-checks that the
repository loads and returns the Twisted bow entry correctly. Run via
`./gradlew test` after any data change; it's fast and doesn't need a live
client.
