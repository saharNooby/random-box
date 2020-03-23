package me.saharnooby.plugins.randombox.box.gui.format.filler;

import lombok.Getter;
import lombok.NonNull;
import me.saharnooby.plugins.randombox.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

@Getter
public final class FillerConfig {

	private final Filler filler;
	private final Filler stopFiller;

	public FillerConfig(@NonNull ConfigurationSection section) {
		this.filler = getFiller(section);

		if (section.isConfigurationSection("onStop")) {
			this.stopFiller = getFiller(section.getConfigurationSection("onStop"));
		} else {
			this.stopFiller = null;
		}
	}

	private static Filler getFiller(@NonNull ConfigurationSection section) {
		if (section.isConfigurationSection("item")) {
			return ConfigUtil.wrapExceptions(() -> new StaticFiller(section.getConfigurationSection("item")), "Invalid filler item");
		} else if (section.isList("animated")) {
			return ConfigUtil.wrapExceptions(() -> new AnimatedFiller(section.getList("animated")), "Invalid animated filler");
		} else {
			return null;
		}
	}

}
