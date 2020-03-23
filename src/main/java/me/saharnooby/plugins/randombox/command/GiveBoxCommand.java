package me.saharnooby.plugins.randombox.command;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.box.Box;
import me.saharnooby.plugins.randombox.message.MessageKey;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class GiveBoxCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("randombox.givebox")) {
			sendError(sender, MessageKey.NO_PERMISSIONS);
			return true;
		}

		if (args.length != 2 && args.length != 3) {
			return false;
		}

		int id;

		try {
			id = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			sendError(sender, MessageKey.ID_MUST_BE_NUMERICAL);
			return true;
		}

		Box box = RandomBox.getInstance().getBoxStorage().getBox(id).orElse(null);

		if (box == null) {
			sendError(sender, MessageKey.NO_SUCH_BOX);

			RandomBox.getInstance().getBoxStorage().getErrorMessage(id).ifPresent(message -> RandomBox.send(sender, "Error was: §c" + message));

			return true;
		}

		int amount;

		if (args.length == 3) {
			try {
				amount = Integer.valueOf(args[2]);
			} catch (NumberFormatException e) {
				return false;
			}
		} else {
			amount = 1;
		}

		List<Player> players;

		if (args[0].startsWith("@")) {
			try {
				players = new TargetSelector(sender, args[0]).select();
			} catch (IllegalArgumentException e) {
				RandomBox.send(sender, MessageKey.BOX_GIVE_ERROR + ": Invalid target selector: " + e.getMessage());
				return true;
			}
		} else {
			Player player = Bukkit.getPlayer(args[0]);

			if (player == null) {
				sendError(sender, MessageKey.PLAYER_NOT_FOUND);
				return true;
			}

			players = Collections.singletonList(player);
		}

		int given = 0;

		for (Player player : players) {
			for (int i = 0; i < amount; i++) {
				ItemStack stack = box.getItem();

				Map<Integer, ItemStack> remains = player.getInventory().addItem(stack);

				if (!remains.isEmpty()) {
					if (RandomBox.getInstance().getPluginConfig().isDropBoxWhenNotEnoughSpace()) {
						player.getWorld().dropItemNaturally(player.getLocation().add(0, 1, 0), remains.get(0));

						RandomBox.send(player, MessageKey.BOX_WAS_DROPPED_TO_GROUND);

						given++;
					} else {
						RandomBox.send(sender, MessageKey.NOT_ENOUGH_SPACE);
					}
				} else {
					given++;
				}
			}
		}

		if (given == 1) {
			RandomBox.send(sender, MessageKey.BOX_GIVEN);
		} else {
			RandomBox.send(sender, MessageKey.MULTIPLE_BOXES_GIVEN.toString().replace("%amount%", String.valueOf(given)));
		}

		return true;
	}

	private static void sendError(@NonNull CommandSender sender, @NonNull MessageKey message) {
		RandomBox.send(sender, MessageKey.BOX_GIVE_ERROR + ": " + message);
	}

}
