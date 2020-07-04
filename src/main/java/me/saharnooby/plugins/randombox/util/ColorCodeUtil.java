package me.saharnooby.plugins.randombox.util;

import lombok.NonNull;

import java.util.OptionalInt;

public final class ColorCodeUtil {

	public static OptionalInt decodeColorCodes(@NonNull String str) {
		int resetIndex = str.indexOf("§r");

		if (resetIndex <= 0) {
			return OptionalInt.empty();
		}

		String codes = str.substring(0, resetIndex);

		int value = 0;

		for (int i = 1; i < codes.length(); i += 2) {
			value *= 10;
			value += codes.charAt(i) - '0';
		}

		return OptionalInt.of(value);
	}

}
