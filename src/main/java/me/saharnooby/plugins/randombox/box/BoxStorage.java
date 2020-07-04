package me.saharnooby.plugins.randombox.box;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.nms.NMSUtil;
import me.saharnooby.plugins.randombox.util.ColorCodeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BoxStorage {

	private static final Pattern ID_LINE = Pattern.compile("(?:§.)*RB(\\d+)(?:-\\d+)?");

	private final Map<Integer, Box> boxes = new HashMap<>();
	private final Map<String, Integer> boxesByItemName = new HashMap<>();
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

		if (!item.hasItemMeta()) {
			return Optional.empty();
		}

		ItemMeta meta = item.getItemMeta();

		if (!meta.hasLore()) {
			return Optional.empty();
		}

		List<String> lore = Objects.requireNonNull(meta.getLore());

		if (lore.isEmpty()) {
			return Optional.empty();
		}

		// Try old method with id encoded as color codes (1.15 and older)
		OptionalInt idFromColorCodes = ColorCodeUtil.decodeColorCodes(lore.get(0));

		if (idFromColorCodes.isPresent()) {
			return getBox(item, idFromColorCodes.getAsInt());
		}

		// Try find id in the last line (new method)
		String last = lore.get(lore.size() - 1);

		Matcher matcher = ID_LINE.matcher(last);

		if (matcher.matches()) {
			int idFromLastLine = Integer.parseInt(matcher.group(1));

			return getBox(item, idFromLastLine);
		}

		if (NMSUtil.getMinorVersion() >= 16) {
			// Try find a box by the item name, if this box item was created in older
			// version of the plugin and does not contain an id in the last line

			if (this.boxesByItemName.isEmpty()) {
				// Init cache
				this.boxes.values().forEach(box -> {
					// Sanitize box name, removing all unused color codes specified by the user
					ItemMeta testMeta = Bukkit.getItemFactory().getItemMeta(Material.STONE);
					testMeta.setDisplayName(box.getName());
					this.boxesByItemName.put(testMeta.getDisplayName(), box.getId());
				});
			}

			Integer idByName = this.boxesByItemName.get(meta.getDisplayName());

			if (idByName != null) {
				return getBox(item, idByName);
			}
		}

		return Optional.empty();
	}

	private Optional<Box> getBox(@NonNull ItemStack item, int id) {
		Box box = this.boxes.get(id);

		return box != null && item.getType() == box.getType() ? Optional.of(box) : Optional.empty();
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
