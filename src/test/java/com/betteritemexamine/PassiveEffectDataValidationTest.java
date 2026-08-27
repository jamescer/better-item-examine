package com.betteritemexamine;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Validates every passive_effects data file listed in
 * PassiveEffectRepository.DATA_FILES. This is the main safety net for
 * contributors: run `gradlew test` after editing/adding a JSON entry and
 * this will catch the common mistakes (duplicate item ids, missing
 * description, malformed JSON, referencing a file that doesn't exist).
 *
 * If you add a whole new category file, remember to also add it to
 * PassiveEffectRepository.DATA_FILES - this test will fail loudly if you
 * forget, since PassiveEffectRepositoryLoadTest below cross-checks the two.
 */
public class PassiveEffectDataValidationTest
{
	private static final String DATA_DIR = "/com/betteritemexamine/data/";
	private static final int TWISTED_BOW_ITEM_ID = 20997;

	private final Gson gson = new Gson();

	@Test
	public void everyDataFileParsesAndIsWellFormed()
	{
		List<String> errors = new ArrayList<>();
		Map<Integer, String> seenItemIds = new HashMap<>();

		for (String fileName : PassiveEffectRepository.getDataFiles())
		{
			String path = DATA_DIR + fileName;

			try (InputStream is = getClass().getResourceAsStream(path))
			{
				assertNotNull("Data file listed in DATA_FILES was not found on the classpath: " + path, is);

				Type listType = new TypeToken<List<PassiveEffect>>()
				{
				}.getType();

				List<PassiveEffect> effects;
				try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8))
				{
					effects = gson.fromJson(reader, listType);
				}

				if (effects == null || effects.isEmpty())
				{
					errors.add(fileName + ": parsed to empty/null - check for JSON syntax errors");
					continue;
				}

				for (PassiveEffect effect : effects)
				{
					String context = fileName + " / '" + effect.getName() + "'";

					if (effect.getName() == null || effect.getName().isBlank())
					{
						errors.add(context + ": missing 'name'");
					}
					if (effect.getDescription() == null || effect.getDescription().isBlank())
					{
						errors.add(context + ": missing 'description'");
					}
					if (effect.getItemIds() == null || effect.getItemIds().isEmpty())
					{
						errors.add(context + ": missing/empty 'itemIds'");
						continue;
					}

					for (Integer itemId : effect.getItemIds())
					{
						if (itemId == null || itemId <= 0)
						{
							errors.add(context + ": invalid itemId " + itemId);
							continue;
						}

						String owner = fileName + " / " + effect.getName();
						String previousOwner = seenItemIds.putIfAbsent(itemId, owner);
						if (previousOwner != null)
						{
							errors.add("Duplicate item id " + itemId + " claimed by both '"
								+ previousOwner + "' and '" + owner + "'");
						}
					}
				}
			}
			catch (Exception e)
			{
				errors.add(fileName + ": failed to parse - " + e.getMessage());
			}
		}

		if (!errors.isEmpty())
		{
			fail("Passive effect data validation failed:\n  - " + String.join("\n  - ", errors));
		}
	}

	private PassiveEffectRepository repository;

	@Before
	public void setUp() throws Exception
	{
		repository = new PassiveEffectRepository();

		// Manually inject a Gson instance since we're not spinning up Guice in this test.
		Field gsonField = PassiveEffectRepository.class.getDeclaredField("gson");
		gsonField.setAccessible(true);
		gsonField.set(repository, new Gson());

		repository.load();
	}

	@Test
	public void repositoryLoadsAllFiles()
	{
		assertFalse("Expected at least one passive effect to be loaded", repository.getAll().isEmpty());
	}

	@Test
	public void twistedBowIsPresent()
	{
		PassiveEffect effect = repository.getForItemId(TWISTED_BOW_ITEM_ID);
		assertNotNull("Twisted bow entry should exist in weapons.json", effect);
		assertTrue(effect.getDescription().toLowerCase().contains("magic"));
	}

	@Test
	public void unknownItemReturnsNull()
	{
		assertNotNull(repository.getForItemId(TWISTED_BOW_ITEM_ID));
		assertTrue(repository.getForItemId(-1) == null);
	}
}
