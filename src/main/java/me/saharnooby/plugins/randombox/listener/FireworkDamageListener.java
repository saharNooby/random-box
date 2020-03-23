package me.saharnooby.plugins.randombox.listener;

import me.saharnooby.plugins.randombox.RandomBox;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public final class FireworkDamageListener implements Listener {

	public static final String META_KEY = "RandomBox.disableDamageFromFirework";

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent e) {
		if (!RandomBox.getInstance().getPluginConfig().isDisableDamageFromBoxFirework()) {
			return;
		}

		if (!(e instanceof EntityDamageByEntityEvent)) {
			return;
		}

		Entity damager = ((EntityDamageByEntityEvent) e).getDamager();

		if (!(damager instanceof Firework)) {
			return;
		}

		if (!damager.hasMetadata(META_KEY)) {
			return;
		}

		e.setCancelled(true);
	}

}
