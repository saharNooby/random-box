package me.saharnooby.plugins.randombox.box.gui.format;

import lombok.Getter;
import lombok.NonNull;
import me.saharnooby.plugins.randombox.box.gui.format.filler.FillerConfig;
import me.saharnooby.plugins.randombox.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.*;

@Getter
public final class GuiFormat {

	private final InventoryType type;
	private final int rows;
	private final Map<Integer, FillerConfig> fillers = new HashMap<>();
	private final List<Integer> itemSlots = new ArrayList<>();
	private final Set<Integer> activeSlots = new HashSet<>();

	public GuiFormat(@NonNull ConfigurationSection section) {
		this.type = ConfigUtil.wrapExceptions(() -> InventoryType.valueOf(section.getString("type")), "Invalid window type");

		ConfigUtil.assertFalse(this.type != InventoryType.CHEST && this.type != InventoryType.HOPPER && this.type != InventoryType.DISPENSER, "Unexpected window type");

		List<String> format = section.getStringList("format");

		int cols;
		int minRows;
		int maxRows;

		switch (this.type) {
			case CHEST:
				cols = 9;
				minRows = 1;
				maxRows = 6;
				break;
			case HOPPER:
				cols = 5;
				minRows = maxRows = 1;
				break;
			case DISPENSER:
				cols = 3;
				minRows = maxRows = 3;
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + this.type);
		}

		ConfigUtil.assertFalse(format == null, "Format does not exist");

		if (format.size() < minRows || format.size() > maxRows) {
			String limit = minRows == maxRows ? "" + minRows : minRows + "-" + maxRows;

			throw new IllegalArgumentException("Invalid rows amount, must be " + limit);
		}

		for (String row : format) {
			ConfigUtil.assertFalse(row.length() != cols, "Invalid cols amount in row '" + row + "'");
		}

		if (this.type == InventoryType.CHEST) {
			this.rows = format.size();
		} else {
			this.rows = -1;
		}

		Map<Character, FillerConfig> fillers = new HashMap<>();

		if (section.isConfigurationSection("fillers")) {
			ConfigurationSection fillersSection = section.getConfigurationSection("fillers");

			for (String key : fillersSection.getKeys(false)) {
				ConfigUtil.assertFalse(key.length() != 1, "Name of the filler '" + key + "' must be a single character");

				ConfigurationSection subsection = fillersSection.getConfigurationSection(key);

				ConfigUtil.wrapExceptions(() -> fillers.put(key.charAt(0), new FillerConfig(subsection)), "Invalid filler '" + key + "'");
			}
		}

		int slot = 0;

		Map<Character, Integer> itemSlots = new HashMap<>();

		for (String line : format) {
			for (int i = 0; i < cols; i++, slot++) {
				char c = line.charAt(i);

				if (c == ' ') {
					continue;
				}

				if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
					itemSlots.put(Character.toLowerCase(c), slot);

					if (c >= 'A' && c <= 'Z') {
						this.activeSlots.add(slot);
					}
				} else if (fillers.containsKey(c)) {
					this.fillers.put(slot, fillers.get(c));
				} else {
					throw new IllegalArgumentException("Filler '" + c + "' is not specified");
				}
			}
		}

		ConfigUtil.assertFalse(itemSlots.size() == 0, "No slots with items (a-z) were found in the format");

		ConfigUtil.assertFalse(this.activeSlots.size() == 0, "No slots dropped items (A-Z) were found in the format");

		for (char c = 'a'; c <= 'z'; c++) {
			if (itemSlots.containsKey(c)) {
				this.itemSlots.add(itemSlots.get(c));
			}
		}
	}

	public Inventory createInventory(@NonNull String name) {
		return this.type == InventoryType.CHEST ? Bukkit.createInventory(null, this.rows * 9, name) : Bukkit.createInventory(null, this.type, name);
	}

}
