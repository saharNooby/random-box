package me.saharnooby.plugins.randombox.box.gui.view.filler;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.box.gui.format.filler.FillerConfig;
import org.bukkit.inventory.ItemStack;

public final class CompoundFillerView implements FillerView {

	private final FillerView filler;
	private final FillerView stopFiller;
	private boolean stopped;
	private boolean updated;

	public CompoundFillerView(@NonNull FillerConfig filler) {
		this.filler = filler.getFiller().createView();
		this.stopFiller = filler.getStopFiller() != null ? filler.getStopFiller().createView() : null;
	}

	@Override
	public void tick() {
		getActiveView().tick();
	}

	@Override
	public ItemStack getItem() {
		this.updated = false;

		return getActiveView().getItem();
	}

	@Override
	public boolean isUpdated() {
		return this.updated || getActiveView().isUpdated();
	}

	public void stop() {
		if (this.stopped) {
			throw new IllegalStateException("Already stopped");
		}

		if (this.stopFiller != null) {
			this.updated = true;
		}

		this.stopped = true;
	}

	private FillerView getActiveView() {
		return this.stopped && this.stopFiller != null ? this.stopFiller : this.filler;
	}

}
