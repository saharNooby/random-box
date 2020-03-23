package me.saharnooby.plugins.randombox.box.gui.view.filler;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.box.gui.format.filler.AnimatedFiller;
import org.bukkit.inventory.ItemStack;

public final class AnimatedFillerView implements FillerView {

	private final AnimatedFiller filler;
	private int frameIndex;
	private int ticks;
	private boolean updated;

	public AnimatedFillerView(@NonNull AnimatedFiller filler) {
		this.filler = filler;
		this.ticks = filler.getFrames().get(0).getDelay();
	}

	@Override
	public void tick() {
		this.ticks--;

		if (this.ticks > 0) {
			return;
		}

		this.updated = true;

		this.frameIndex = (this.frameIndex + 1) % this.filler.getFrames().size();

		this.ticks = this.filler.getFrames().get(this.frameIndex).getDelay();
	}

	@Override
	public ItemStack getItem() {
		this.updated = false;

		return this.filler.getFrames().get(this.frameIndex).getItem();
	}

	@Override
	public boolean isUpdated() {
		return this.updated;
	}

}
