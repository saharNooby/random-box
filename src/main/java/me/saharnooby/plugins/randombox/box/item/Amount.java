package me.saharnooby.plugins.randombox.box.item;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.concurrent.ThreadLocalRandom;

public final class Amount {

	private final int min;
	private final int max;

	public Amount(@NonNull ConfigurationSection section, @NonNull String key) {
		if (section.isInt(key)) {
			this.min = section.getInt(key);
			this.max = section.getInt(key);
		} else if (section.isString(key) && section.getString(key).matches("\\d+-\\d+")) {
			String bounds = section.getString(key);
			String[] split = bounds.split("-");

			this.min = Integer.parseInt(split[0]);
			this.max = Integer.parseInt(split[1]);

			ConfigUtil.assertFalse(this.min > this.max, "Min amount is more than max amount");
		} else {
			throw new IllegalArgumentException("Amount should be a number or range like '1-10'");
		}

		ConfigUtil.assertFalse(this.min == 0 || this.max == 0, "Amount can't be 0");
	}

	public Amount(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public boolean isRange() {
		return this.min != this.max;
	}

	public int getRandom() {
		return isRange() ? ThreadLocalRandom.current().nextInt(this.max - this.min) + this.min : this.min;
	}

	@Override
	public String toString() {
		return isRange() ? this.min + "-" + this.max : String.valueOf(this.min);
	}

}
