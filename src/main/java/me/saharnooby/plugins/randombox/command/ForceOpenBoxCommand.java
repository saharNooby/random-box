package me.saharnooby.plugins.randombox.command;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.box.Box;
import me.saharnooby.plugins.randombox.message.MessageKey;
import me.saharnooby.plugins.randombox.util.Hand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ForceOpenBoxCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("randombox.forceopenbox")) {
			sendError(sender, MessageKey.NO_PERMISSIONS);
			return true;
		}

		if (!(sender instanceof Player)) {
			sendError(sender, MessageKey.NOT_A_PLAYER);
			return true;
		}

		if (args.length != 1) {
			return false;
		}

		int id;

		try {
			id = Integer.valueOf(args[0]);
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

		box.open((Player) sender, Hand.MAIN, false);

		return true;
	}

	private static void sendError(@NonNull CommandSender sender, @NonNull MessageKey key) {
		RandomBox.send(sender, MessageKey.BOX_OPEN_ERROR + ": " + key);
	}

}
