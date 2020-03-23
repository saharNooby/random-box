package me.saharnooby.plugins.randombox.box.item;

import lombok.Data;
import lombok.NonNull;
import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.message.MessageKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

@Data
public final class DroppedItem {

	private final DropItem item;
	private final ItemStack stack;

	public void give(@NonNull Player player) {
		if (!this.item.isDontGiveItem()) {
			Map<Integer, ItemStack> remains = player.getInventory().addItem(this.stack);

			if (!remains.isEmpty()) {
				player.getWorld().dropItemNaturally(player.getLocation(), remains.get(0));
			}
		}

		for (DropCommand command : this.item.getCommands()) {
			command.dispatch(player);
		}
	}

	public static void giveAndSendMessage(@NonNull Player player, @NonNull List<DroppedItem> items) {
		if (items.isEmpty()) {
			throw new IllegalArgumentException();
		}

		StringBuilder builder = new StringBuilder();

		for (DroppedItem item : items) {
			item.give(player);

			builder.append(item.getItem().getName()).append("§r");

			int amount = item.getStack().getAmount();

			if (amount > 1) {
				builder.append(" x").append(amount);
			}

			builder.append(", ");
		}

		builder.setLength(builder.length() - 2);

		RandomBox.send(player, MessageKey.ITEMS_DROPPED + builder.toString());
	}

}
