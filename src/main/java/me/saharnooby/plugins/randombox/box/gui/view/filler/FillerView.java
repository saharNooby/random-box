package me.saharnooby.plugins.randombox.box.gui.view.filler;

import org.bukkit.inventory.ItemStack;

public interface FillerView {

	/**
	 * Resets isUpdated.
	 */
	ItemStack getItem();

	boolean isUpdated();

	default void tick() {

	}

}
