package me.saharnooby.plugins.randombox.box.gui.format.filler;

import lombok.Getter;
import lombok.NonNull;
import me.saharnooby.plugins.randombox.box.gui.view.filler.FillerView;
import me.saharnooby.plugins.randombox.box.gui.view.filler.AnimatedFillerView;
import me.saharnooby.plugins.randombox.util.ConfigUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public final class AnimatedFiller implements Filler {

	private final List<FillerFrame> frames = new ArrayList<>();

	public AnimatedFiller(@NonNull List<?> list) {
		for (Object object : list) {
			if (object instanceof Map) {
				ConfigUtil.wrapExceptions(() -> this.frames.add(new FillerFrame(ConfigUtil.mapToSection((Map<?, ?>) object))), "Invalid frame");
			}
		}

		ConfigUtil.assertFalse(this.frames.isEmpty(), "No valid frames");
	}

	@Override
	public FillerView createView() {
		return new AnimatedFillerView(this);
	}

}
