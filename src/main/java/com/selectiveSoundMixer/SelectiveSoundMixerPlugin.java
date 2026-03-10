package com.selectiveSoundMixer;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JOptionPane;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.AreaSoundEffectPlayed;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.api.Preferences;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "SelectiveSoundMixer"
)
public class SelectiveSoundMixerPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "selectiveSoundMixer";
	private static final String CONFIG_KEY = "soundMixers";
	private static final String ICON_FILE = "panel_icon2.png";
	private static final String PLUGIN_NAME = "Selective Sound Mixer";
	private static final String DEFAULT_SOUND_MIX_NAME = "SoundMix";

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private com.selectiveSoundMixer.SelectiveSoundMixerConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Gson gson;

	private NavigationButton navigationButton;

	private List<SoundMixer> soundMixers = new ArrayList<>();

	private SelectiveSoundMixerPluginPanel pluginPanel;

	@Override
	protected void startUp() throws Exception
	{
		loadSoundMixers();
		pluginPanel = new SelectiveSoundMixerPluginPanel(client, this, config);
		pluginPanel.rebuild();

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), ICON_FILE);

		navigationButton = NavigationButton.builder()
				.tooltip(PLUGIN_NAME)
				.icon(icon)
				.priority(5)
				.panel(pluginPanel)
				.build();


		if (!config.hideNavButton())
		{
			clientToolbar.addNavigation(navigationButton);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		soundMixers.clear();
		clientToolbar.removeNavigation(navigationButton);
		pluginPanel = null;
		navigationButton = null;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
		if (!configChanged.getGroup().equals(CONFIG_GROUP))
		{
			return;
		}
		if (configChanged.getKey().equals("hideNavButton"))
		{
			loadSoundMixers();
			if (config.hideNavButton())
			{
				clientToolbar.removeNavigation(navigationButton);
			}
			else
			{
				clientToolbar.addNavigation(navigationButton);
			}
		}
	}

	@Subscribe
	public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed areaSoundEffectPlayed) {
		int soundId = areaSoundEffectPlayed.getSoundId();
		log.debug("Hearing Area Sound ID: " + soundId);
		SoundMixer foundSoundMix = findSoundMixer(soundId);
		if (foundSoundMix != null) {
			log.debug("Area Sound ID Found: " + soundId + " " + foundSoundMix.getVolume());
			areaSoundEffectPlayed.consume();
			playSound(foundSoundMix);
		}
	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed soundEffectPlayed) {
		int soundId = soundEffectPlayed.getSoundId();
		log.debug("Hearing Effect Sound ID: " + soundId);
		SoundMixer foundSoundMix = findSoundMixer(soundId);
		if (foundSoundMix != null) {
			log.debug("Sound Effect ID Found: " + soundId + " " + foundSoundMix.getVolume());
			soundEffectPlayed.consume();
			playSound(foundSoundMix);
		}
	}

	private void playSound(SoundMixer soundMixer)
	{
		Preferences preferences = client.getPreferences();
		int previousVolume = preferences.getSoundEffectVolume();
		preferences.setSoundEffectVolume(soundMixer.getVolume());
		client.playSoundEffect(soundMixer.getId(), soundMixer.getVolume());
		preferences.setSoundEffectVolume(previousVolume);
	}

	@Provides
    com.selectiveSoundMixer.SelectiveSoundMixerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(com.selectiveSoundMixer.SelectiveSoundMixerConfig.class);
	}

	private void loadSoundMixers()
	{
		soundMixers.clear();
		String json = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY);

		if (Strings.isNullOrEmpty(json))
		{
			return;
		}

		try
		{
			List<SoundMixer> loaded = gson.fromJson(json, new TypeToken<List<SoundMixer>>(){}.getType());
			loaded.removeIf(SoundMixer::isInvalid);
			soundMixers.addAll(loaded);
		}
		catch (IllegalStateException | JsonSyntaxException ignore)
		{
			JOptionPane.showConfirmDialog(pluginPanel,
					"The sound mixes you are trying to load from your config are malformed",
					"Warning", JOptionPane.OK_CANCEL_OPTION);
		}
	}

	public void saveSoundMixers()
	{
		saveSoundMixers(getSoundMixers());
	}

	private void saveSoundMixers(Collection<SoundMixer> soundMixers)
	{
		if (soundMixers == null || soundMixers.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_KEY);
			return;
		}

		String json = gson.toJson(soundMixers);
		configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY, json);
	}

	public SoundMixer addSoundMix()
	{
		final SoundMixer mix = new SoundMixer(
				1,
				DEFAULT_SOUND_MIX_NAME + " " + (soundMixers.size() + 1),
				true,
				false,
				64
		);

		List<SoundMixer> thisSoundMixers = new ArrayList<>(getSoundMixers());
		if (!thisSoundMixers.contains(mix))
		{
			thisSoundMixers.add(mix);
		}
		saveSoundMixers(thisSoundMixers);
		loadSoundMixers();
		pluginPanel.rebuild();
		return mix;
	}

	public void removeSoundMixer(SoundMixer soundMix)
	{
		List<SoundMixer> thisSoundMixers = new ArrayList<>(getSoundMixers());
		thisSoundMixers.remove(soundMix);
		saveSoundMixers(thisSoundMixers);
		loadSoundMixers();
		pluginPanel.rebuild();
	}

	public List<SoundMixer> getSoundMixers() {
		return this.soundMixers;
	}

	public SoundMixer findSoundMixer(int soundId)
	{
		for (SoundMixer soundMixer : soundMixers)
		{
			if (soundMixer.getId() == soundId && soundMixer.isVisible())
			{
				return soundMixer;
			}
		}
		return null;
	}
}
