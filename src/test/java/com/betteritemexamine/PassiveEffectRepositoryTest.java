package com.betteritemexamine;

import com.google.gson.Gson;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Sanity-checks that passive_effects.json parses cleanly and that the
 * Twisted bow entry (the item this plugin was originally built for) is
 * present. Extend this when you add new items if you want regression
 * coverage.
 */
public class PassiveEffectRepositoryTest
{
	private static final int TWISTED_BOW_ITEM_ID = 20997;

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
	public void loadsWithoutError()
	{
		assertFalse("Expected at least one passive effect to be loaded", repository.getAll().isEmpty());
	}

	@Test
	public void twistedBowIsPresent()
	{
		PassiveEffect effect = repository.getForItemId(TWISTED_BOW_ITEM_ID);
		assertNotNull("Twisted bow entry should exist in passive_effects.json", effect);
		assertTrue(effect.getDescription().toLowerCase().contains("magic"));
	}

	@Test
	public void unknownItemReturnsNull()
	{
		assertNotNull(repository.getForItemId(TWISTED_BOW_ITEM_ID));
		assertTrue(repository.getForItemId(-1) == null);
	}
}
