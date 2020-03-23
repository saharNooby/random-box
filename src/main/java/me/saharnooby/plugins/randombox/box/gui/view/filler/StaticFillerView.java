package me.saharnooby.plugins.randombox.box.gui.view.filler;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.box.gui.format.filler.StaticFiller;
import org.bukkit.inventory.ItemStack;

public final class StaticFillerView implements FillerView {

	private final StaticFiller filler;

	public StaticFillerView(@NonNull StaticFiller filler) {
		this.filler = filler;
	}

	@Override
	public ItemStack getItem() {
		return filler.getItem();
	}

	@Override
	public boolean isUpdated() {
		return false;
	}

}
