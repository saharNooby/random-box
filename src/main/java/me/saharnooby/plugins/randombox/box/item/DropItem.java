package me.saharnooby.plugins.randombox.box.item;

import lombok.Getter;
import lombok.NonNull;
import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.box.Box;
import me.saharnooby.plugins.randombox.box.effect.BoxEffect;
import me.saharnooby.plugins.randombox.util.ConfigUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class DropItem implements Cloneable {

	private final String name;

	private final ItemStack item;
	private final int boxId;
	private final Amount amount;

	private final int chance;

	private final String inBoxLoreFormat;
	private final List<DropCommand> commands = new ArrayList<>();
	private final boolean dontGiveItem;
	private final BoxEffect effect;
	
	public DropItem(@NonNull ConfigurationSection section) {
		this.name = ConfigUtil.getColoredString(section, "name");

		ConfigUtil.assertFalse(this.name == null || this.name.isEmpty(), "Item name is empty or does not exist");

		if (section.contains("box")) {
			this.item = null;
			this.boxId = section.getInt("box");
			this.amount = new Amount(1, 1);
		} else {
			this.item = ConfigUtil.parseItem(section, "item", !section.getBoolean("dontAssignName"));
			this.boxId = -1;
			this.amount = ConfigUtil.wrapExceptions(() -> new Amount(section, "amount"), "Invalid amount");
		}

		this.chance = section.getInt("chance", 0);

		ConfigUtil.assertFalse(this.chance <= 0, "There is no chance, or it is less than 1");

		this.inBoxLoreFormat = ConfigUtil.getColoredString(section, "inBoxLoreFormat");

		ConfigurationSection commands = section.getConfigurationSection("commands");

		if (commands != null) {
			for (String key : commands.getKeys(false)) {
				ConfigUtil.wrapExceptions(() -> this.commands.add(new DropCommand(commands.getConfigurationSection(key))), "Invalid command " + key);
			}
		}

		this.dontGiveItem = section.getBoolean("dontGiveItem");

		this.effect = section.isConfigurationSection("dropEffect") ? new BoxEffect(section.getConfigurationSection("dropEffect")) : BoxEffect.EMPTY;
	}

	public ItemStack createItem() {
		if (this.boxId != -1) {
			Box box = RandomBox.getInstance().getBoxStorage().getBox(this.boxId).orElse(null);

			if (box == null) {
				RandomBox.warn("Box " + this.boxId + " not found");

				return new ItemStack(Material.DIRT);
			} else {
				return box.getItem();
			}
		}

		ItemStack item = this.item.clone();
		item.setAmount(this.amount.getRandom());
		return item;
	}

	public DroppedItem toDropped() {
		return new DroppedItem(this, createItem());
	}

	public DroppedItem toDropped(int amount) {
		ItemStack stack = createItem();
		stack.setAmount(amount);
		return new DroppedItem(this, stack);
	}

}
