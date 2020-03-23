package me.saharnooby.plugins.randombox.box;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.util.ColorCodeUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public final class BoxStorage {

	private final Map<Integer, Box> boxes = new HashMap<>();
	private final Map<Integer, String> parseErrors = new HashMap<>();

	public void load() {
		this.boxes.clear();

		File[] files = new File(RandomBox.getInstance().getDataFolder(), "boxes").listFiles();

		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					continue;
				}

				String name = file.getName();

				if (!name.endsWith(".yml")) {
					continue;
				}

				int id;

				try {
					id = Integer.parseInt(name.substring(0, name.length() - 4));
				} catch (NumberFormatException e) {
					RandomBox.warn("Can't parse box " + name + ": box id must be numerical");
					continue;
				}

				RandomBox.info("Parsing box " + id);

				try {
					this.boxes.put(id, new BoxParser(id, YamlConfiguration.loadConfiguration(file)).getBox());
				} catch (Exception e) {
					logBoxParseError(id, e);

					this.parseErrors.put(id, combineMessages(e));
				}
			}
		}

		RandomBox.info(this.boxes.size() + " boxes have been loaded");
	}

	public Optional<String> getErrorMessage(int id) {
		return Optional.ofNullable(this.parseErrors.get(id));
	}

	public Optional<Box> getBox(int id) {
		return Optional.ofNullable(this.boxes.get(id));
	}

	public Optional<Box> getBox(ItemStack item) {
		if (item == null) {
			return Optional.empty();
		}

		if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
			return Optional.empty();
		}

		List<String> lore = item.getItemMeta().getLore();

		if (lore.isEmpty()) {
			return Optional.empty();
		}

		OptionalInt id = ColorCodeUtil.decodeColorCodes(lore.get(0));

		if (!id.isPresent()) {
			return Optional.empty();
		}

		Box box = this.boxes.get(id.getAsInt());

		if (box == null || item.getType() != box.getType()) {
			return Optional.empty();
		}

		return Optional.of(box);
	}

	public Map<Integer, Box> getBoxes() {
		return Collections.unmodifiableMap(this.boxes);
	}

	private static void logBoxParseError(int id, @NonNull Throwable e) {
		StringBuilder builder = new StringBuilder("Failed to parse box " + id + ": " + e.getMessage());
		appendReasons(builder, e.getCause(), "");
		RandomBox.getInstance().getLogger().log(Level.WARNING, builder.toString());
	}

	private static String combineMessages(@NonNull Throwable e) {
		StringBuilder builder = new StringBuilder(e.getMessage());
		appendReasons(builder, e.getCause(), "§c");
		return builder.toString();
	}

	private static void appendReasons(StringBuilder builder, Throwable e, @NonNull String prefix) {
		while (e != null) {
			builder.append("\n").append(prefix).append("Reason of the above: ").append(e.getMessage());

			e = e.getCause();
		}
	}

}
