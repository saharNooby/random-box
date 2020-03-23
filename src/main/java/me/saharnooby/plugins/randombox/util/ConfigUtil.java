package me.saharnooby.plugins.randombox.util;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.nms.NMSItemUtil;
import me.saharnooby.plugins.randombox.nms.NMSUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class ConfigUtil {

	public static ConfigurationSection requireSection(@NonNull ConfigurationSection section, @NonNull String key) {
		if (section.isConfigurationSection(key)) {
			return section.getConfigurationSection(key);
		}

		throw new IllegalArgumentException("Expected '" + key + "' to be a section");
	}

	public static void assertFalse(boolean condition, @NonNull String message) {
		if (condition) {
			throw new IllegalArgumentException(message);
		}
	}

	public static <T> T wrapExceptions(@NonNull Supplier<T> supplier, @NonNull String outerMessage) {
		try {
			return supplier.get();
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(outerMessage, e);
		}
	}

	public static String getColoredString(@NonNull ConfigurationSection section, @NonNull String key) {
		String string = section.getString(key);

		return string == null ? null : ChatColor.translateAlternateColorCodes('&', string);
	}

	public static ItemStack parseItem(@NonNull ConfigurationSection section, @NonNull String materialKey, boolean parseName) {
		Material type = parseMaterial(section.getString(materialKey));

		assertFalse(type == null, "Invalid item material '" + section.getString(materialKey) + "'");

		int amount = section.getInt("amount", 1);

		ItemStack item = new ItemStack(type, amount, (short) section.getInt("data", 0));

		String nbt = section.getString("nbtTag");

		if (nbt != null) {
			NMSItemUtil.setNBT(item, nbt);
		}

		ItemMeta meta = ItemUtil.getOrCreateMeta(item);

		if (parseName) {
			meta.setDisplayName(getColoredString(section, "name"));
		}

		if (section.isList("lore")) {
			addLore(meta, section.getStringList("lore"));
		}

		if (meta instanceof EnchantmentStorageMeta) {
			addStoredEnchants(meta, section.getConfigurationSection("storedEnchants"));
		}

		if (meta instanceof SkullMeta && section.isString("skullOwner")) {
			((SkullMeta) meta).setOwner(section.getString("skullOwner"));
		}

		item.setItemMeta(meta);

		addEnchants(item, section.getConfigurationSection("enchants"));

		String texture = section.getString("texture");

		if (texture != null) {
			if (NMSUtil.getMinorVersion() < 13) {
				// Player head
				item.setDurability((short) 3);
			}

			try {
				ItemUtil.setBase64EncodedTextures(item, texture);
			} catch (Exception e) {
				RandomBox.warn("Failed to set texture '" + texture + "' to " + item + ": " + e);
			}
		}

		if (section.contains("customModelData")) {
			ItemUtil.setCustomModelData(item, section.getInt("customModelData"));
		}

		return item;
	}

	public static Material parseMaterial(String id) {
		if (id == null || id.isEmpty()) {
			return null;
		}

		Material material = Material.getMaterial(id.toUpperCase());

		if (material == null && NMSUtil.getMinorVersion() < 13) {
			try {
				return Material.getMaterial(Integer.parseInt(id));
			} catch (NumberFormatException e) {
				return null;
			}
		}

		return material;
	}

	public static FireworkEffect parseFireworkEffect(@NonNull ConfigurationSection section) {
		FireworkEffect.Builder builder = FireworkEffect.builder();

		String type = section.getString("type");

		if (type != null) {
			try {
				builder.with(FireworkEffect.Type.valueOf(type.toUpperCase()));
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Firework type '" + type + "' is invalid");
			}
		} else {
			throw new IllegalArgumentException("Firework effect must have a type");
		}

		List<String> colors = section.getStringList("colors");

		if (colors != null && colors.size() > 0) {
			for (String elem : colors) {
				Color color = parseColor(elem);
				if (color != null) {
					builder.withColor(color);
				} else {
					RandomBox.warn("Invalid color '" + elem + "', use color name such as RED or RGB HEX value #xxxxxx");
				}
			}
		} else {
			throw new IllegalArgumentException("Firework effect must have at least one color");
		}

		List<String> fadeColors = section.getStringList("fadeColors");

		if (fadeColors != null) {
			for (String s : fadeColors) {
				Color color = parseColor(s);
				if (color != null) {
					builder.withFade(color);
				} else {
					RandomBox.warn("Invalid fade color '" + s + "', use color name such as RED or RGB HEX value #xxxxxx");
				}
			}
		}

		builder.flicker(section.getBoolean("flicker"));
		builder.trail(section.getBoolean("trail"));

		return builder.build();
	}

	@SuppressWarnings("unchecked")
	public static ConfigurationSection mapToSection(@NonNull Map map) {
		Configuration section = new YamlConfiguration();

		map.forEach((k, v) -> section.set((String) k, v instanceof Map ? mapToSection((Map) v) : v));

		return section;
	}

	private static void addLore(@NonNull ItemMeta meta, List<String> strings) {
		if (strings == null) {
			return;
		}

		List<String> lore = new ArrayList<>();

		for (String line : strings) {
			lore.add(ChatColor.translateAlternateColorCodes('&', line));
		}

		meta.setLore(lore);
	}

	private static void addEnchants(@NonNull ItemStack item, ConfigurationSection section) {
		if (section == null) {
			return;
		}

		for (String key : section.getKeys(false)) {
			Enchantment enchantment = ItemUtil.parseEnchantment(key);

			if (enchantment != null) {
				int level = section.getInt(key, 0);

				if (level >= 1) {
					item.addUnsafeEnchantment(enchantment, level);
				}
			} else {
				RandomBox.warn("Invalid enchantment '" + key + "'");
			}
		}
	}

	private static void addStoredEnchants(@NonNull ItemMeta meta, ConfigurationSection section) {
		if (section == null) {
			return;
		}

		for (String key : section.getKeys(false)) {
			Enchantment enchantment = ItemUtil.parseEnchantment(key);

			if (enchantment != null) {
				int level = section.getInt(key, 0);

				if (level >= 1) {
					((EnchantmentStorageMeta) meta).addStoredEnchant(enchantment, level, true);
				}
			} else {
				RandomBox.warn("Invalid stored enchantment '" + key + "'");
			}
		}
	}

	private static Color parseColor(@NonNull String str) {
		if (str.isEmpty()) {
			return null;
		}

		if (str.charAt(0) == '#' && str.length() == 7) {
			try {
				return Color.fromRGB((int) (Long.parseLong(str.substring(1), 16) & 0xFFFFFF));
			} catch (NumberFormatException e) {
				return null;
			}
		}

		switch (str.toUpperCase()) {
			case "WHITE":
				return Color.WHITE;
			case "SILVER":
				return Color.SILVER;
			case "GRAY":
				return Color.GRAY;
			case "BLACK":
				return Color.BLACK;
			case "RED":
				return Color.RED;
			case "MAROON":
				return Color.MAROON;
			case "YELLOW":
				return Color.YELLOW;
			case "OLIVE":
				return Color.OLIVE;
			case "LIME":
				return Color.LIME;
			case "GREEN":
				return Color.GREEN;
			case "AQUA":
				return Color.AQUA;
			case "TEAL":
				return Color.TEAL;
			case "BLUE":
				return Color.BLUE;
			case "NAVY":
				return Color.NAVY;
			case "FUCHSIA":
				return Color.FUCHSIA;
			case "PURPLE":
				return Color.PURPLE;
			case "ORANGE":
				return Color.ORANGE;
		}

		return null;
	}

}
