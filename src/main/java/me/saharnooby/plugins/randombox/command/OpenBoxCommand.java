package me.saharnooby.plugins.randombox.command;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.box.Box;
import me.saharnooby.plugins.randombox.message.MessageKey;
import me.saharnooby.plugins.randombox.nms.NMSUtil;
import me.saharnooby.plugins.randombox.util.Hand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class OpenBoxCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("randombox.openbox")) {
			sendError(sender, MessageKey.NO_PERMISSIONS);
			return true;
		}

		if (!(sender instanceof Player)) {
			sendError(sender, MessageKey.NOT_A_PLAYER);
			return true;
		}

		Player player = (Player) sender;

		Box box = RandomBox.getInstance().getBoxStorage().getBox(player.getItemInHand()).orElse(null);
		Hand hand = Hand.MAIN;

		if (box == null && NMSUtil.getMinorVersion() >= 9) {
			box = RandomBox.getInstance().getBoxStorage().getBox(player.getInventory().getItemInOffHand()).orElse(null);
			hand = Hand.OFF;
		}

		if (box == null) {
			sendError(sender, MessageKey.NOT_A_BOX);
			return true;
		}

		box.open(player, hand, true);

		return true;
	}

	private static void sendError(@NonNull CommandSender sender, @NonNull MessageKey key) {
		RandomBox.send(sender, MessageKey.BOX_OPEN_ERROR + ": " + key);
	}

}
