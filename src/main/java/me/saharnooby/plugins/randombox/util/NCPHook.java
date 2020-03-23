package me.saharnooby.plugins.randombox.util;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.RandomBox;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public final class NCPHook {

	public static void exemptPermanently(@NonNull Player player) {
		try {
			getManagerClass().getMethod("exemptPermanently", Player.class).invoke(null, player);
		} catch (ClassNotFoundException e) {
			// NCP is not installed
		} catch (ReflectiveOperationException e) {
			RandomBox.getInstance().getLogger().log(Level.WARNING, "Failed to exempt player " + player.getName(), e);
		}
	}

	public static void unexempt(@NonNull Player player) {
		try {
			getManagerClass().getMethod("unexempt", Player.class).invoke(null, player);
		} catch (ClassNotFoundException e) {
			// NCP is not installed
		} catch (ReflectiveOperationException e) {
			RandomBox.getInstance().getLogger().log(Level.WARNING, "Failed to unexempt player " + player.getName(), e);
		}
	}

	private static Class<?> getManagerClass() throws ClassNotFoundException {
		return Class.forName("fr.neatmonster.nocheatplus.hooks.NCPExemptionManager");
	}

}
