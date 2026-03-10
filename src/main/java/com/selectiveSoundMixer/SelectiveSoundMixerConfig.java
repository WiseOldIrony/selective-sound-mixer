package com.selectiveSoundMixer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(SelectiveSoundMixerPlugin.CONFIG_GROUP)
public interface SelectiveSoundMixerConfig extends Config
{
	@ConfigItem(
			keyName = "hideNavButton",
			name = "Hide side panel button",
			description = "Whether to hide the side panel button to reduce clutter when not needing to modify sounds",
			position = 1
	)
	default boolean hideNavButton()
	{
		return false;
	}
}
