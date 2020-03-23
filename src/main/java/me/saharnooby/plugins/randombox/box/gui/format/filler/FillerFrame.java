package me.saharnooby.plugins.randombox.box.gui.format.filler;

import lombok.Getter;
import lombok.NonNull;
import me.saharnooby.plugins.randombox.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

@Getter
public final class FillerFrame {

	private final ItemStack item;
	private final int delay;

	public FillerFrame(@NonNull ConfigurationSection section) {
		this.item = ConfigUtil.parseItem(section, "id", false);
		this.delay = section.getInt("delay", section.getInt("period", 1));
	}

}
