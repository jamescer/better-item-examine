package com.betteritemexamine;

import com.google.inject.Provides;
import java.awt.Color;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

/**
 * Adds "hidden" passive effects (things that appear on the OSRS Wiki but
 * are NOT explained anywhere in-game, e.g. the Twisted Bow's accuracy/damage
 * scaling with target Magic level) onto an item's examine text.
 *
 * How it works:
 *  1. MenuOptionClicked fires when the player clicks "Examine" on an item.
 *     We stash that item's id.
 *  2. Shortly after, the game sends a ChatMessage of type ITEM_EXAMINE with
 *     the vanilla examine text. We intercept it, look up whether we have
 *     passive-effect data for that item id, and if so, append (or send as a
 *     separate message) our extra text.
 *
 * All the actual item data lives in passive_effects.json - see
 * PassiveEffectRepository. That's the file to edit/extend; this class should
 * rarely need to change.
 */
@Slf4j
@PluginDescriptor(
	name = "Better Item Examine",
	description = "Appends hidden/passive item effects (e.g. Twisted Bow's Magic-scaling accuracy/damage) "
		+ "to examine text, since these mechanics aren't explained in-game.",
	tags = {"examine", "items", "wiki", "passive", "twisted bow", "tooltip"}
)
public class BetterItemExaminePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private BetterItemExamineConfig config;

	@Inject
	private PassiveEffectRepository passiveEffectRepository;

	/**
	 * Item id from the most recent "Examine" menu click, consumed by the
	 * next ITEM_EXAMINE chat message. -1 means "nothing pending".
	 *
	 * TODO(contributor): this naive single-slot approach assumes the next
	 * ITEM_EXAMINE message always corresponds to the last examine click.
	 * That holds for normal play but could theoretically race if something
	 * else generates an ITEM_EXAMINE message. A small FIFO queue would be a
	 * good first contribution if this ever proves unreliable.
	 */
	private int pendingExamineItemId = -1;

	@Provides
	BetterItemExamineConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BetterItemExamineConfig.class);
	}

	@Override
	protected void startUp()
	{
		passiveEffectRepository.load();
		pendingExamineItemId = -1;
	}

	@Override
	protected void shutDown()
	{
		pendingExamineItemId = -1;
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!"Examine".equals(event.getMenuOption()))
		{
			return;
		}

		int itemId = event.getItemId();
		if (itemId <= 0)
		{
			return;
		}

		pendingExamineItemId = itemId;
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.ITEM_EXAMINE)
		{
			return;
		}

		if (pendingExamineItemId == -1)
		{
			return;
		}

		int itemId = pendingExamineItemId;
		pendingExamineItemId = -1;

		PassiveEffect effect = passiveEffectRepository.getForItemId(itemId);
		if (effect == null)
		{
			return;
		}

		String addition = buildPassiveEffectText(effect);

		if (config.appendToExamineText())
		{
			String updated = event.getMessageNode().getValue() + " " + addition;
			event.getMessageNode().setValue(updated);
			client.refreshChat();
		}
		else
		{
			client.addChatMessage(ChatMessageType.CONSOLE, "", addition, null);
		}
	}

	private String buildPassiveEffectText(PassiveEffect effect)
	{
		Color color = config.highlightColor();
		String hex = String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
		return "<col=" + hex + ">[Passive: " + effect.getDescription() + "]</col>";
	}
}
