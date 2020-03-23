package me.saharnooby.plugins.randombox.util;

import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public final class IOUtil {

	public static void closeSilent(@NonNull InputStream in) {
		try {
			in.close();
		} catch (IOException ignored) {

		}
	}

	public static void mkdirs(@NonNull File dir) throws IOException {
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("Failed to create " + dir.getAbsolutePath());
		}
	}

}
