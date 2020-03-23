package me.saharnooby.plugins.randombox.box.gui.format;

import lombok.Getter;
import lombok.NonNull;
import me.saharnooby.plugins.randombox.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

@Getter
public final class GuiIteration {

	private final int delay;
	private final int iterations;

	public GuiIteration(@NonNull ConfigurationSection section) {
		this.delay = section.getInt("delay", section.getInt("period", -1));
		this.iterations = section.getInt("iterations", -1);

		ConfigUtil.assertFalse(this.delay < 0, "Invalid iteration: delay is < 0");
		ConfigUtil.assertFalse(this.iterations < 0, "Invalid iteration: iterations is < 0");
	}

}
