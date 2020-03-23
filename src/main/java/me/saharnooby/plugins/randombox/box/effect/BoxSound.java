package me.saharnooby.plugins.randombox.box.effect;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.nms.NMSUtil;
import me.saharnooby.plugins.randombox.util.ConfigUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

public final class BoxSound {

	private final Sound sound;
	private final float volume;
	private final float pitch;

	public BoxSound(@NonNull ConfigurationSection section) {
		String sound = section.getString("sound");

		ConfigUtil.assertFalse(sound == null, "Section doesn't contain 'sound' string");

		try {
			this.sound = Sound.valueOf(fixSoundId(sound.toUpperCase()));
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid sound '" + sound + "'");
		}

		this.volume = (float) section.getDouble("volume", 1);
		this.pitch = (float) section.getDouble("pitch", 1);
	}

	public void play(@NonNull Location loc) {
		loc.getWorld().playSound(loc, this.sound, this.volume, this.pitch);
	}

	// Костыль для того, чтобы стд конфиг работал на 1.8 (с 1.9 работает нормально).
	private static String fixSoundId(@NonNull String id) {
		if (NMSUtil.getMinorVersion() < 9) {
			switch (id) {
				case "BLOCK_ANVIL_LAND":
					return "ANVIL_LAND";
				case "BLOCK_CHEST_OPEN":
					return "CHEST_OPEN";
				case "BLOCK_CHEST_CLOSE":
					return "CHEST_CLOSE";
				case "UI_BUTTON_CLICK":
					return "CLICK";
				case "ENTITY_GENERIC_EXPLODE":
					return "EXPLODE";
			}
		}

		return id;
	}

}
