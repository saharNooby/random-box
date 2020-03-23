package me.saharnooby.plugins.randombox.config;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;

@Getter
public final class PluginConfig {

	private final String locale;
	private final boolean useInfinitePermission;
	private final boolean dropBoxWhenNotEnoughSpace;
	private final boolean disableDamageFromBoxFirework;

	public PluginConfig(@NonNull ConfigurationSection section) {
		this.locale = section.getString("locale", "en").toLowerCase();
		this.useInfinitePermission = section.getBoolean("useInfinitePermission");
		this.dropBoxWhenNotEnoughSpace = section.getBoolean("dropBoxWhenNotEnoughSpace");
		this.disableDamageFromBoxFirework = section.getBoolean("disableDamageFromBoxFirework", true);
	}

}
