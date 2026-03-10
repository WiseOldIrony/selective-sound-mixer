package com.selectiveSoundMixer;

import lombok.Getter;
import lombok.Setter;

/**
 * Used for serialization of sound mixers.
 */
@Getter
@Setter
class SoundMixer implements Comparable<SoundMixer>
{
	private transient SoundMixerPanel panel;
	private int id;
	private String name;
	private boolean visible;
	private boolean collapsed;
	private int volume;

	SoundMixer(int id, String name, boolean visible, boolean collapsed, int volume) {
		this.id = id;
		this.name = name;
		this.visible = visible;
		this.collapsed = collapsed;
		this.volume = volume;
	}

	public static boolean isInvalid(SoundMixer soundMixer)
	{
		return soundMixer == null
			|| soundMixer.id < 0
			|| soundMixer.name == null
			|| soundMixer.volume < 0
			|| soundMixer.volume > 127;
	}

	@Override
	public int compareTo(SoundMixer other)
	{
		return this.name.compareTo(other.name);
	}

	public boolean equals(SoundMixer other)
	{
		return id == other.getId();
	}
}
