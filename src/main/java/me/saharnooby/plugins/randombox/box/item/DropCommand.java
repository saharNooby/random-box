package me.saharnooby.plugins.randombox.box.item;

import lombok.Getter;
import lombok.NonNull;
import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

@Getter
public final class DropCommand {

	private final String line;
	private final boolean fromConsole;

	public DropCommand(@NonNull ConfigurationSection section) {
		this.line = section.getString("line");
		this.fromConsole = section.getBoolean("fromConsole");

		ConfigUtil.assertFalse(this.line == null, "Command line not present");
	}

	public void dispatch(@NonNull Player player) {
		String line = this.line.replace("%player%", player.getName());

		CommandSender sender = this.fromConsole ? Bukkit.getConsoleSender() : player;

		RandomBox.info("Dispatching '" + line + "' from " + sender.getName());

		Bukkit.dispatchCommand(sender, line);
	}
	
}
