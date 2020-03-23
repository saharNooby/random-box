package me.saharnooby.plugins.randombox;

import lombok.Data;
import me.saharnooby.plugins.randombox.box.Box;

@Data
public final class BoxAndPrice {

	private final Box box;
	private final double price;

}
