package me.saharnooby.plugins.randombox.economy;

import me.saharnooby.plugins.randombox.RandomBox;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;

public final class VaultEconomy {

	private final Economy economy;

	public VaultEconomy() {
		if (Bukkit.getPluginManager().isPluginEnabled("Vault") && Bukkit.getServicesManager().getRegistration(Economy.class) != null) {
			this.economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
		} else {
			this.economy = null;

			RandomBox.warn("Vault economy not found. Install Vault to make random blocks with price");
		}
	}

	public boolean isEnabled() {
		return this.economy != null;
	}
	
	public Economy getEconomy() {
		return this.economy;
	}
	
}
