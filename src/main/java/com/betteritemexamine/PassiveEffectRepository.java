package com.betteritemexamine;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Loads passive effect data from every file in DATA_FILES into a single
 * item-id -> PassiveEffect lookup map.
 *
 * Adding entries to any of the existing category files (weapons.json,
 * armour.json, ammunition.json, jewellery.json) needs zero Java changes.
 * Adding a whole new category file needs a one-line addition to DATA_FILES
 * below.
 */
@Slf4j
@Singleton
public class PassiveEffectRepository
{
	private static final String DATA_DIR = "/com/betteritemexamine/data/";

	/**
	 * Every JSON file under DATA_DIR that should be loaded. Add new category
	 * files here as they're created (e.g. "gloves.json").
	 */
	private static final List<String> DATA_FILES = List.of(
		"weapons.json",
		"armour.json",
		"ammunition.json",
		"jewellery.json"
	);

	private final Map<Integer, PassiveEffect> effectsByItemId = new HashMap<>();

	@Inject
	private Gson gson;

	/**
	 * (Re)loads all passive effect data from the bundled JSON resources.
	 * Safe to call multiple times, e.g. on plugin startup.
	 */
	public void load()
	{
		effectsByItemId.clear();

		List<PassiveEffect> allEffects = new ArrayList<>();
		for (String fileName : DATA_FILES)
		{
			allEffects.addAll(loadFile(fileName));
		}

		for (PassiveEffect effect : allEffects)
		{
			if (effect.getItemIds() == null || effect.getItemIds().isEmpty())
			{
				log.warn("Skipping passive effect entry with no itemIds: {}", effect.getName());
				continue;
			}

			for (Integer itemId : effect.getItemIds())
			{
				if (itemId == null)
				{
					continue;
				}

				PassiveEffect existing = effectsByItemId.get(itemId);
				if (existing != null && existing != effect)
				{
					log.warn(
						"Duplicate item id {} claimed by both '{}' and '{}' - the later entry wins. "
							+ "Fix this in the JSON data files.",
						itemId, existing.getName(), effect.getName());
				}

				effectsByItemId.put(itemId, effect);
			}
		}

		log.debug("Loaded {} passive effect entries covering {} item ids", allEffects.size(), effectsByItemId.size());
	}

	private List<PassiveEffect> loadFile(String fileName)
	{
		String path = DATA_DIR + fileName;

		try (InputStream is = getClass().getResourceAsStream(path))
		{
			if (is == null)
			{
				log.warn("Could not find passive effect data file {}", path);
				return Collections.emptyList();
			}

			Type listType = new TypeToken<List<PassiveEffect>>()
			{
			}.getType();

			try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8))
			{
				List<PassiveEffect> effects = gson.fromJson(reader, listType);
				return effects == null ? Collections.emptyList() : effects;
			}
		}
		catch (Exception e)
		{
			log.warn("Failed to load passive effect data from {}", path, e);
			return Collections.emptyList();
		}
	}

	/**
	 * @param itemId the canonical item id (as returned by the examine menu click)
	 * @return the passive effect for this item, or null if it has none registered
	 */
	public PassiveEffect getForItemId(int itemId)
	{
		return effectsByItemId.get(itemId);
	}

	public Map<Integer, PassiveEffect> getAll()
	{
		return Collections.unmodifiableMap(effectsByItemId);
	}

	/**
	 * Exposed for tests / tooling: the list of data files this repository
	 * loads, so a validation test can iterate the same set independently
	 * of the runtime-merged map.
	 */
	public static List<String> getDataFiles()
	{
		return DATA_FILES;
	}
}
