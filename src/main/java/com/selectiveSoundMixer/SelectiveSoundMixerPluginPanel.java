package com.selectiveSoundMixer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ImageUtil;

@Slf4j
class SelectiveSoundMixerPluginPanel extends PluginPanel
{
	private static final ImageIcon ADD_ICON;
	private static final ImageIcon ADD_HOVER_ICON;

	private final JLabel soundMixAdd = new JLabel(ADD_ICON);
	private final JLabel title = new JLabel();
	private final PluginErrorPanel noSoundMixersPanel = new PluginErrorPanel();
	private final JPanel soundMixerView = new JPanel();

	private final Client client;
	private final SelectiveSoundMixerPlugin plugin;
	private final SelectiveSoundMixerConfig config;

	static
	{
		final BufferedImage addIcon = ImageUtil.loadImageResource(SelectiveSoundMixerPlugin.class, "add_icon.png");
		ADD_ICON = new ImageIcon(addIcon);
		ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53f));

		final BufferedImage visibleImg = ImageUtil.loadImageResource(SelectiveSoundMixerPlugin.class, "visible_icon.png");
		final BufferedImage invisibleImg = ImageUtil.loadImageResource(SelectiveSoundMixerPlugin.class, "invisible_icon.png");
	}

	public SelectiveSoundMixerPluginPanel(Client client, SelectiveSoundMixerPlugin plugin, SelectiveSoundMixerConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.setBorder(new EmptyBorder(1, 0, 10, 0));

		JPanel titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBorder(new EmptyBorder(1, 3, 10, 7));

		JPanel soundMixerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 3));

		title.setText("Selective Sound Mixer");
		title.setForeground(Color.WHITE);

		titlePanel.add(title, BorderLayout.WEST);
		titlePanel.add(soundMixerButtons, BorderLayout.EAST);

		northPanel.add(titlePanel, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		soundMixerView.setLayout(new BoxLayout(soundMixerView, BoxLayout.Y_AXIS));
		soundMixerView.setBackground(ColorScheme.DARK_GRAY_COLOR);

		noSoundMixersPanel.setVisible(false);

		soundMixerView.add(noSoundMixersPanel);

		soundMixAdd.setToolTipText("Add New Sound Mix");
		soundMixAdd.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				addSoundMix();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				soundMixAdd.setIcon(ADD_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				soundMixAdd.setIcon(ADD_ICON);
			}
		});

		soundMixerButtons.add(soundMixAdd);

		centerPanel.add(soundMixerView, BorderLayout.NORTH);

		add(northPanel, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
	}

	public void rebuild()
	{
		soundMixerView.removeAll();

		loadSoundMixPanels(soundMixer ->
		{
			soundMixerView.add(new SoundMixerPanel(plugin, config, soundMixer));
			soundMixerView.add(Box.createRigidArea(new Dimension(0, 10)));
		});

		boolean empty = soundMixerView.getComponentCount() == 0;
		noSoundMixersPanel.setContent("Selective Sound Mixer",
			"Click the '+' button to create a Sound Mix");
		noSoundMixersPanel.setVisible(empty);
		soundMixerView.add(noSoundMixersPanel);

		repaint();
		revalidate();
	}

	private void loadSoundMixPanels(Consumer<SoundMixer> consumer)
	{
		for (final SoundMixer soundMixer : plugin.getSoundMixers())
		{
			consumer.accept(soundMixer);
		}
	}

	private void addSoundMix()
	{
		noSoundMixersPanel.setVisible(false);
		final SoundMixer soundMix = plugin.addSoundMix();
		SwingUtilities.invokeLater(() ->
		{
			if (soundMix != null && soundMix.getPanel() != null)
			{
				Rectangle soundMixPosition = soundMix.getPanel().getBounds();
				soundMixPosition.setLocation(
					(int) soundMixPosition.getX(),
					(int) soundMixPosition.getY() + ((Component) client).getParent().getHeight() / 3);
				scrollRectToVisible(soundMixPosition);
			}
		});
	}
}
