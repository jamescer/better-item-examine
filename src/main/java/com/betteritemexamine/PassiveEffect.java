package com.betteritemexamine;

import java.util.List;
import lombok.Data;

/**
 * Represents a single "hidden" passive effect entry, as loaded from
 * passive_effects.json. One entry can cover multiple item ids, since the
 * same passive often applies to several variants of an item
 * (e.g. broken/degraded charged versions, ornament kits, etc).
 */
@Data
public class PassiveEffect
{
	/**
	 * All item ids this entry applies to. Include every variant you want
	 * covered (uncharged/charged, ornamented, degraded, etc).
	 */
	private List<Integer> itemIds;

	/**
	 * Display name, purely for logging/debugging - not shown to the user.
	 */
	private String name;

	/**
	 * Short description of the passive effect, appended to the examine text.
	 * Keep this reasonably short so it fits comfortably in the chatbox.
	 */
	private String description;

	/**
	 * Optional link to the OSRS Wiki page for this item's mechanics.
	 * Not currently used at runtime, but useful for contributors to track
	 * where a description's numbers came from, and reserved for a future
	 * "open wiki page" right-click option.
	 */
	private String wikiUrl;
}
