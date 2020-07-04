package me.saharnooby.plugins.randombox.box;

import lombok.Getter;
import lombok.NonNull;
import me.saharnooby.plugins.randombox.box.effect.BoxEffect;
import me.saharnooby.plugins.randombox.box.gui.format.GuiFormat;
import me.saharnooby.plugins.randombox.box.gui.format.GuiIteration;
import me.saharnooby.plugins.randombox.box.item.DropItem;
import me.saharnooby.plugins.randombox.box.limit.Limit;
import me.saharnooby.plugins.randombox.message.MessageKey;
import me.saharnooby.plugins.randombox.nms.NMSItemUtil;
import me.saharnooby.plugins.randombox.util.ConfigUtil;
import me.saharnooby.plugins.randombox.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BoxParser {

	@Getter
	private final Box box;
	
	public BoxParser(int id, @NonNull ConfigurationSection section) {
		Box box = this.box = new Box(id);

		box.name = ConfigUtil.getColoredString(section, "boxName");

		ConfigUtil.assertFalse(box.name == null || box.name.isEmpty(), "Box name not present or is empty");

		// region Box item

		Material type = ConfigUtil.parseMaterial(section.getString("boxItem"));

		ConfigUtil.assertFalse(type == null || type == Material.AIR, "Invalid box material '" + section.getString("boxItem") + "'");

		box.item = new ItemStack(type, 1, (short) section.getInt("data", 0));

		String nbt = section.getString("nbtTag");

		if (nbt != null) {
			NMSItemUtil.setNBT(box.item, nbt);
		}

		if (section.contains("customModelData")) {
			ItemUtil.setCustomModelData(box.item, section.getInt("customModelData"));
		}

		if (section.getBoolean("addGlow", true)) {
			box.item.addUnsafeEnchantment(Enchantment.LUCK, 1);
		}

		// endregion

		// region Drop items

		ConfigurationSection items = ConfigUtil.requireSection(section, "items");

		for (String key : items.getKeys(false)) {
			ConfigUtil.wrapExceptions(() -> box.items.add(new DropItem(items.getConfigurationSection(key))), "Invalid drop item " + key);
		}

		box.dropCount = section.getInt("itemsToSelectCount", 0);

		ConfigUtil.assertFalse(box.dropCount <= 0, "There is no itemsToSelectCount, or it is less than 1");

		ConfigUtil.assertFalse(box.items.isEmpty() || box.dropCount > box.items.size(), "Drop item count is less than drop count");

		// endregion

		// region Box item meta

		ItemMeta meta = ItemUtil.getOrCreateMeta(box.item);

		meta.setDisplayName(box.name);

		if (meta instanceof SkullMeta && section.isString("skullOwner")) {
			((SkullMeta) meta).setOwner(section.getString("skullOwner"));
		}

		List<String> lore;

		if (section.isList("lore")) {
			lore = getFormattedBoxLore(section.getStringList("lore"));
		} else {
			lore = getDefaultBoxLore();
		}

		lore.add("§8RB" + box.getId());

		meta.setLore(lore);

		box.item.setItemMeta(meta);

		// endregion

		box.unstackable = section.getBoolean("unstackable");
		box.checkPermission = section.getBoolean("checkPermission");
		box.openWhenClicked = section.getBoolean("openWhenClicked", true);

		// Effects
		box.openEffect = parseEffect(section, "effects.open");
		box.moveEffect = parseEffect(section, "effects.move");
		box.dropEffect = parseEffect(section, "effects.drop");
		box.closeEffect = parseEffect(section, "effects.close");

		// GUI
		if (section.isConfigurationSection("gui") && section.getBoolean("gui.enabled")) {
			parseGui(section.getConfigurationSection("gui"));
		}

		if (section.isList("limits")) {
			int index = 1;
			for (Object elem : section.getList("limits")) {
				if (elem instanceof Map) {
					box.limits.add(ConfigUtil.wrapExceptions(() -> new Limit(ConfigUtil.mapToSection((Map<?, ?>) elem)), "Invalid limit #" + index));
				}

				index++;
			}
		}

		if (section.isSet("cooldown")) {
			box.limits.add(new Limit(section.getInt("cooldown") * 1000));
		}
	}

	private List<String> getFormattedBoxLore(@NonNull List<String> format) {
		List<String> lore = new ArrayList<>();

		for (String line : format) {
			line = ChatColor.translateAlternateColorCodes('&', line);

			if (line.matches("items \\d+ .+")) {
				lore.addAll(formatItemList(line, false));
			} else {
				lore.add(line);
			}
		}

		return lore;
	}

	private List<String> getDefaultBoxLore() {
		List<String> lore = formatItemList("items 0 &7%ordinal%. &ex%amount% %name%", true);

		lore.add(0, MessageKey.ITEMS_TO_DROP.toString());

		return lore;
	}

	private List<String> formatItemList(@NonNull String format, boolean addMoreItemsString) {
		format = format.substring(6);

		int i = format.indexOf(' ');

		int limit = Integer.parseInt(format.substring(0, i));

		if (limit == 0) {
			limit = Integer.MAX_VALUE;
		}

		format = format.substring(i + 1);

		int chanceSum = this.box.getChanceSum();

		int ordinal = 1;

		List<String> lore = new ArrayList<>();

		for (DropItem item : this.box.items) {
			if (--limit < 0) {
				if (addMoreItemsString) {
					int remains = this.box.items.size() - ordinal + 1;

					if (remains > 0) {
						lore.add(String.format("§7...and §e%s§7 more", remains));
					}
				}

				break;
			}

			String itemFormat = item.getInBoxLoreFormat();

			if (itemFormat == null) {
				itemFormat = format;
			}

			itemFormat = itemFormat
					.replace("%ordinal%", String.valueOf(ordinal))
					.replace("%name%", item.getName())
					.replace("%amount%", String.valueOf(item.getAmount()))
					.replace("%chance%", String.valueOf(item.getChance()))
					.replace("%chancePercents%", String.valueOf(Math.round(100D * item.getChance() / chanceSum)) + '%');

			lore.add(itemFormat);

			ordinal++;
		}

		return lore;
	}

	private static BoxEffect parseEffect(@NonNull ConfigurationSection section, @NonNull String key) {
		return section.isConfigurationSection(key) ? new BoxEffect(section.getConfigurationSection(key)) : BoxEffect.EMPTY;
	}

	private void parseGui(@NonNull ConfigurationSection gui) {
		if (!gui.isList("rolling")) {
			throw new IllegalArgumentException("'rolling' list not present in 'gui' section");
		}

		List<?> rolling = gui.getList("rolling");

		for (Object elem : rolling) {
			if (elem instanceof Map) {
				ConfigUtil.wrapExceptions(() -> this.box.iterations.add(new GuiIteration(ConfigUtil.mapToSection((Map<?, ?>) elem))), "Invalid iteration");
			}
		}

		ConfigUtil.assertFalse(this.box.iterations.isEmpty(), "'rolling' list does not contain valid iterations");

		ConfigurationSection window = ConfigUtil.requireSection(gui, "window");

		this.box.guiFormat = ConfigUtil.wrapExceptions(() -> new GuiFormat(window), "Invalid window format");
	}

}
