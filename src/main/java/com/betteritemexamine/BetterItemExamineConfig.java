package com.betteritemexamine;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(BetterItemExamineConfig.GROUP)
public interface BetterItemExamineConfig extends Config
{
	String GROUP = "betteritemexamine";

	@ConfigItem(
		keyName = "appendToExamineText",
		name = "Append to examine text",
		description = "If enabled, the passive effect is appended directly onto the item's examine chat line. "
			+ "If disabled, it is sent as a separate game message instead.",
		position = 1
	)
	default boolean appendToExamineText()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightColor",
		name = "Highlight color",
		description = "Color used to highlight the passive effect text in chat.",
		position = 2
	)
	default Color highlightColor()
	{
		return new Color(0, 200, 255);
	}

	@ConfigItem(
		keyName = "onlyEquippable",
		name = "Only equippable items",
		description = "If enabled, only show passive info for items that can be worn/wielded. "
			+ "Disable to also cover things like non-wearable passive-effect items (e.g. certain rings, tools).",
		position = 3
	)
	default boolean onlyEquippable()
	{
		return false;
	}
}
