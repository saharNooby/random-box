package me.saharnooby.plugins.randombox.message;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.RandomBox;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public enum MessageKey {

	// Prefix
	PREFIX,
	
	// Messages
	ITEMS_TO_DROP,
	CONFIG_RELOADED,
	BOX_GIVEN,
	MULTIPLE_BOXES_GIVEN,
	ITEMS_DROPPED,
	BOX_LIST,
	ENTERING_CREATING_MODE,
	RANDOM_BLOCK_CREATED,
	RANDOM_BLOCK_REMOVED,
	ENTERING_REMOVAL_MODE,
	EXITING_REMOVAL_MODE,
	NOT_A_RANDOM_BLOCK,
	
	// General errors
	NO_PERMISSIONS,
	BOX_OPEN_ERROR,
	BOX_GIVE_ERROR,
	BLOCK_CREATE_ERROR,
	
	// Detailed errors
	PLAYER_NOT_FOUND,
	ID_MUST_BE_NUMERICAL,
	NOT_ENOUGH_SPACE,
	BOX_WAS_DROPPED_TO_GROUND,
	NO_SUCH_BOX,
	NOT_A_BOX,
	NOT_A_PLAYER,
	NO_PERMISSIONS_TO_OPEN,
	NOT_ENOUGH_MONEY,
	OPEN_ONLY_AFTER;
	
	private final String id;

	MessageKey() {
		this.id = nameToKey(name());
	}

	public void send(@NonNull CommandSender sender) {
		RandomBox.send(sender, toString());
	}
	
	@Override
	public String toString() {
		String message = RandomBox.getInstance().getLocale().getString(this.id);

		return message != null ? ChatColor.translateAlternateColorCodes('&', message) : "§c" + this.id;
	}

	private static String nameToKey(@NonNull String name) {
		StringBuilder sb = new StringBuilder();

		for (String part : name.split("_")) {
			sb.append(part.charAt(0)).append(part.substring(1).toLowerCase());
		}

		sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));

		return sb.toString();
	}

}
