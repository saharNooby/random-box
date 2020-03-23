package me.saharnooby.plugins.randombox.command;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.BoxAndPrice;
import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.box.Box;
import me.saharnooby.plugins.randombox.listener.RandomBlockEditListener;
import me.saharnooby.plugins.randombox.message.MessageKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public final class RandomBlockCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("randombox.randomblock")) {
			sendError(sender, MessageKey.NO_PERMISSIONS);
			return true;
		}

		if (!(sender instanceof Player)) {
			sendError(sender, MessageKey.NOT_A_PLAYER);
			return true;
		}

		Player player = (Player) sender;

		if (args.length == 1 && args[0].equalsIgnoreCase("remove")) {
			player.setMetadata(RandomBlockEditListener.REMOVE_KEY, new FixedMetadataValue(RandomBox.getInstance(), true));
			RandomBox.send(sender, MessageKey.ENTERING_REMOVAL_MODE);
			return true;
		}

		if (args.length != 3 || !args[0].equalsIgnoreCase("create")) {
			return false;
		}

		int id;

		try {
			id = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			sendError(sender, MessageKey.NO_SUCH_BOX);
			return true;
		}

		Box box = RandomBox.getInstance().getBoxStorage().getBox(id).orElse(null);

		if (box == null) {
			sendError(sender, MessageKey.NO_SUCH_BOX);
			return true;
		}

		double price;

		try {
			price = Double.valueOf(args[2]);
		} catch (NumberFormatException e) {
			return false;
		}

		player.setMetadata(RandomBlockEditListener.SET_KEY, new FixedMetadataValue(RandomBox.getInstance(), new BoxAndPrice(box, price)));

		RandomBox.send(sender, MessageKey.ENTERING_CREATING_MODE);

		return true;
	}

	private static void sendError(@NonNull CommandSender sender, @NonNull MessageKey key) {
		RandomBox.send(sender, MessageKey.BLOCK_CREATE_ERROR + ": " + key);
	}

}
