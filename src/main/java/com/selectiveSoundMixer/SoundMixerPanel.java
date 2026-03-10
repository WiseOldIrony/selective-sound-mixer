package com.selectiveSoundMixer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.FlatTextField;
import net.runelite.client.ui.components.TitleCaseListCellRenderer;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

class SoundMixerPanel extends JPanel
{
	private static final Border NAME_BOTTOM_BORDER = new CompoundBorder(
		BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
		BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR));

	private static final ImageIcon BORDER_COLOR_ICON;
	private static final ImageIcon BORDER_COLOR_HOVER_ICON;
	private static final ImageIcon NO_BORDER_COLOR_ICON;
	private static final ImageIcon NO_BORDER_COLOR_HOVER_ICON;

	private static final ImageIcon VISIBLE_ICON;
	private static final ImageIcon VISIBLE_HOVER_ICON;
	private static final ImageIcon INVISIBLE_ICON;
	private static final ImageIcon INVISIBLE_HOVER_ICON;

	private static final ImageIcon DELETE_ICON;
	private static final ImageIcon DELETE_HOVER_ICON;

	private static final ImageIcon COLLAPSE_ICON;
	private static final ImageIcon COLLAPSE_HOVER_ICON;
	private static final ImageIcon EXPAND_ICON;
	private static final ImageIcon EXPAND_HOVER_ICON;

	private final SelectiveSoundMixerPlugin plugin;
	private final SelectiveSoundMixerConfig config;
	private final SoundMixer soundMixer;

	private final JPanel containerSpawn = new JPanel(new BorderLayout());
	private final JPanel containerSoundId = new JPanel(new BorderLayout());
	private final JPanel containerVolume = new JPanel(new BorderLayout());
	private final JLabel visibilitySoundMix = new JLabel();
	private final JLabel deleteLabel = new JLabel();
	private final JButton expandToggle;

	private final FlatTextField nameInput = new FlatTextField();
	private final JLabel save = new JLabel("Save");
	private final JLabel cancel = new JLabel("Cancel");
	private final JLabel rename = new JLabel("Rename");

	private final JSpinner spinnerSoundId = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));
	private final JSpinner spinnerVolume = new JSpinner(new SpinnerNumberModel(127, 0, 127, 1));

	static
	{
		final BufferedImage borderImg = ImageUtil.loadImageResource(SelectiveSoundMixerPlugin.class, "border_color_icon.png");
		final BufferedImage borderImgHover = ImageUtil.luminanceOffset(borderImg, -150);
		BORDER_COLOR_ICON = new ImageIcon(borderImg);
		BORDER_COLOR_HOVER_ICON = new ImageIcon(borderImgHover);

		NO_BORDER_COLOR_ICON = new ImageIcon(borderImgHover);
		NO_BORDER_COLOR_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(borderImgHover, -100));

		final BufferedImage visibleImg = ImageUtil.loadImageResource(SelectiveSoundMixerPlugin.class, "enabled_icon.png");
		VISIBLE_ICON = new ImageIcon(visibleImg);
		VISIBLE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(visibleImg, -100));

		final BufferedImage invisibleImg = ImageUtil.loadImageResource(SelectiveSoundMixerPlugin.class, "disabled_icon.png");
		INVISIBLE_ICON = new ImageIcon(invisibleImg);
		INVISIBLE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(invisibleImg, -100));

		final BufferedImage deleteImg = ImageUtil.loadImageResource(SelectiveSoundMixerPlugin.class, "delete_icon.png");
		DELETE_ICON = new ImageIcon(deleteImg);
		DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImg, -100));

		BufferedImage retractIcon = ImageUtil.loadImageResource(SelectiveSoundMixerPlugin.class, "arrow_right.png");
		retractIcon = ImageUtil.luminanceOffset(retractIcon, -121);
		EXPAND_ICON = new ImageIcon(retractIcon);
		EXPAND_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(retractIcon, -100));
		final BufferedImage expandIcon = ImageUtil.rotateImage(retractIcon, Math.PI / 2);
		COLLAPSE_ICON = new ImageIcon(expandIcon);
		COLLAPSE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(expandIcon, -100));
	}

	SoundMixerPanel(SelectiveSoundMixerPlugin plugin, SelectiveSoundMixerConfig config, SoundMixer soundMixer)
	{
		this.plugin = plugin;
		this.config = config;
		this.soundMixer = soundMixer;

		containerSpawn.setLayout(new BoxLayout(containerSpawn, BoxLayout.Y_AXIS));

		soundMixer.setPanel(this);

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(new EmptyBorder(0, 0, 0, 0));

		JPanel nameWrapper = new JPanel(new BorderLayout());
		nameWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameWrapper.setBorder(NAME_BOTTOM_BORDER);

		JPanel nameActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 3));
		nameActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		save.setVisible(false);
		save.setFont(FontManager.getRunescapeSmallFont());
		save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
		save.setBorder(new EmptyBorder(3, 0, 0, 3));
		save.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				save();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR.darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
			}
		});

		cancel.setVisible(false);
		cancel.setFont(FontManager.getRunescapeSmallFont());
		cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
		cancel.setBorder(new EmptyBorder(3, 0, 0, 3));
		cancel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				cancel();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR.darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
			}
		});

		rename.setFont(FontManager.getRunescapeSmallFont());
		rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
		rename.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				nameInput.setEditable(true);
				updateNameActions(true);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker().darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
			}
		});

		nameInput.setText(soundMixer.getName());
		nameInput.setBorder(null);
		nameInput.setEditable(false);
		nameInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameInput.setPreferredSize(new Dimension(0, 24));
		nameInput.getTextField().setForeground(Color.WHITE);
		nameInput.getTextField().setBorder(new EmptyBorder(0, 5, 0, 0));
		nameInput.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					save();
				}
				else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					cancel();
				}
			}
		});
		nameInput.getTextField().addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
				{
					final boolean open = containerSpawn.isVisible();
					soundMixer.setCollapsed(open);
					updateCollapsed();
					plugin.saveSoundMixers();
				}
			}
		});

		final JPanel[] containers = new JPanel[]
		{
			containerSoundId,
			containerVolume
		};
		for (JPanel container : containers)
		{
			container.setBorder(new EmptyBorder(5, 0, 5, 0));
			container.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		}
		containerSoundId.setBorder(new EmptyBorder(5, 26, 5, 26));
		containerVolume.setBorder(new EmptyBorder(5, 26, 5, 26));

		JPanel leftActionsSoundId = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		leftActionsSoundId.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel leftActionsVolume = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		leftActionsVolume.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		spinnerSoundId.setValue(soundMixer.getId());
		spinnerVolume.setValue(Math.min(soundMixer.getVolume(), 127));

		final JSpinner[] spinners = new JSpinner[]
		{
			spinnerSoundId,
			spinnerVolume
		};
		for (JSpinner spinner : spinners)
		{
			spinner.addChangeListener(ce -> updateSoundMix());
		}
		spinnerSoundId.setPreferredSize(new Dimension(80, 20));
		spinnerVolume.setPreferredSize(new Dimension(80, 20));
		spinnerVolume.setToolTipText("Volume (0-127). Set it to 0 to mute the Sound ID.");

		JLabel labelSoundId = new JLabel("Sound ID");
		JLabel labelVolume = new JLabel("Volume");

		final JLabel[] labels = new JLabel[]
		{
			labelSoundId,
			labelVolume
		};
		for (JLabel label : labels)
		{
			label.setFont(FontManager.getRunescapeSmallFont());
			label.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
		}
		labelSoundId.setPreferredSize(new Dimension(58, 21));
		labelVolume.setPreferredSize(new Dimension(58, 21));

		leftActionsSoundId.add(labelSoundId);
		leftActionsVolume.add(labelVolume);

		JPanel rightActionsSoundId = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightActionsSoundId.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel rightActionsVolume = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightActionsVolume.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		expandToggle = new JButton(soundMixer.isCollapsed() ? COLLAPSE_ICON : EXPAND_ICON);
		expandToggle.setRolloverIcon(soundMixer.isCollapsed() ? COLLAPSE_HOVER_ICON : EXPAND_HOVER_ICON);
		expandToggle.setPreferredSize(new Dimension(15, 0));
		expandToggle.setBorder(new EmptyBorder(0, 6, 1, 0));
		expandToggle.setToolTipText((soundMixer.isCollapsed() ? "Expand" : "Collapse") + " Sound Mix");
		SwingUtil.removeButtonDecorations(expandToggle);
		expandToggle.addActionListener(actionEvent ->
		{
			final boolean open = containerSpawn.isVisible();
			soundMixer.setCollapsed(open);
			updateCollapsed();
			plugin.saveSoundMixers();
		});

		visibilitySoundMix.setToolTipText((soundMixer.isVisible() ? "Hide" : "Show") + " Sound Mix");
		visibilitySoundMix.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				soundMixer.setVisible(!soundMixer.isVisible());
				updateVisibility();
				plugin.saveSoundMixers();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				visibilitySoundMix.setIcon(soundMixer.isVisible() ? VISIBLE_HOVER_ICON : INVISIBLE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				updateVisibility();
			}
		});

		deleteLabel.setIcon(DELETE_ICON);
		deleteLabel.setToolTipText("Delete Sound mMix");
		deleteLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				plugin.removeSoundMixer(soundMixer);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				deleteLabel.setIcon(DELETE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				deleteLabel.setIcon(DELETE_ICON);
			}
		});

		nameActions.add(rename);
		nameActions.add(cancel);
		nameActions.add(save);
		nameActions.add(visibilitySoundMix);
		nameActions.add(deleteLabel);

		nameWrapper.add(expandToggle, BorderLayout.WEST);
		nameWrapper.add(nameInput, BorderLayout.CENTER);
		nameWrapper.add(nameActions, BorderLayout.EAST);

		rightActionsSoundId.add(spinnerSoundId);
		rightActionsVolume.add(spinnerVolume);

		containerSoundId.add(leftActionsSoundId, BorderLayout.WEST);
		containerSoundId.add(rightActionsSoundId, BorderLayout.EAST);

		containerVolume.add(leftActionsVolume, BorderLayout.WEST);
		containerVolume.add(rightActionsVolume, BorderLayout.EAST);

		containerSpawn.add(containerSoundId);
		containerSpawn.add(containerVolume);

		JPanel soundMixerContainer = new JPanel();
		soundMixerContainer.setLayout(new BoxLayout(soundMixerContainer, BoxLayout.Y_AXIS));
		soundMixerContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		soundMixerContainer.add(nameWrapper);
		soundMixerContainer.add(containerSpawn);

		add(soundMixerContainer);

		updateVisibility();
		updateCollapsed();
	}

	private void save()
	{
		soundMixer.setName(nameInput.getText());
		plugin.saveSoundMixers();

		nameInput.setEditable(false);
		updateNameActions(false);
		requestFocusInWindow();
	}

	private void cancel()
	{
		nameInput.setEditable(false);
		nameInput.setText(soundMixer.getName());
		requestFocusInWindow();
	}

	private void updateNameActions(boolean saveAndCancel)
	{
		save.setVisible(saveAndCancel);
		cancel.setVisible(saveAndCancel);
		rename.setVisible(!saveAndCancel);
		expandToggle.setVisible(!saveAndCancel);
		visibilitySoundMix.setVisible(!saveAndCancel);
		deleteLabel.setVisible(!saveAndCancel);

		if (saveAndCancel)
		{
			nameInput.getTextField().requestFocusInWindow();
			nameInput.getTextField().selectAll();
		}
	}

	private void updateSoundMix()
	{
		soundMixer.setId((int) spinnerSoundId.getValue());
		soundMixer.setVolume((int) spinnerVolume.getValue());

		updateVisibility();

		plugin.saveSoundMixers();
	}

	private void updateVisibility()
	{
		visibilitySoundMix.setIcon(soundMixer.isVisible() ? VISIBLE_ICON : INVISIBLE_ICON);
	}

	private void updateCollapsed()
	{
		final boolean open = !soundMixer.isCollapsed();

		rename.setVisible(open);

		containerSpawn.setVisible(open);

		expandToggle.setIcon(open ? COLLAPSE_ICON : EXPAND_ICON);
		expandToggle.setRolloverIcon(open ? COLLAPSE_HOVER_ICON : EXPAND_HOVER_ICON);
		expandToggle.setToolTipText((open ? "Collapse" : "Expand") + " Sound Mix");
	}
}