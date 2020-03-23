package me.saharnooby.plugins.randombox.box.gui.format.filler;

import lombok.Getter;
import lombok.NonNull;
import me.saharnooby.plugins.randombox.box.gui.view.filler.FillerView;
import me.saharnooby.plugins.randombox.box.gui.view.filler.StaticFillerView;
import me.saharnooby.plugins.randombox.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

@Getter
public final class StaticFiller implements Filler {

	private final ItemStack item;

	public StaticFiller(@NonNull ConfigurationSection section) {
		this.item = ConfigUtil.parseItem(section, "id", false);
	}

	public StaticFiller(@NonNull ItemStack item) {
		this.item = item;
	}

	@Override
	public FillerView createView() {
		return new StaticFillerView(this);
	}

}
