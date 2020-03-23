package me.saharnooby.plugins.randombox.command;

import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.box.Box;
import me.saharnooby.plugins.randombox.box.item.DropCommand;
import me.saharnooby.plugins.randombox.box.item.DropItem;
import me.saharnooby.plugins.randombox.message.MessageKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

public final class RandomBoxCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("randombox.main")) {
			MessageKey.NO_PERMISSIONS.send(sender);
			return true;
		}
		
		if (args.length == 0) {
			PluginDescriptionFile desc = RandomBox.getInstance().getDescription();
			sender.sendMessage("§6" + desc.getName() + " §b" + desc.getVersion() + " §bby §6" + String.join(", ", desc.getAuthors()));
			return true;
		}

		if (args[0].equalsIgnoreCase("reload")) {
			if (!sender.hasPermission("randombox.main.reload")) {
				MessageKey.NO_PERMISSIONS.send(sender);
				return true;
			}

			RandomBox.getInstance().reloadPlugin();

			MessageKey.CONFIG_RELOADED.send(sender);

			return true;
		}

		if (args[0].equalsIgnoreCase("list")) {
			MessageKey.BOX_LIST.send(sender);

			for (Box box : RandomBox.getInstance().getBoxStorage().getBoxes().values()) {
				sender.sendMessage("- id: " + box.getId() + ", name: " + box.getName());

				for (DropItem item : box.getItems()) {
					sender.sendMessage("  - id: " + item.createItem().getType() + ", name: " + item.getName());

					for (DropCommand cmd : item.getCommands()) {
						sender.sendMessage("    - line: " + cmd.getLine());
					}
				}
			}

			return true;
		}
		
		return false;
	}

}
