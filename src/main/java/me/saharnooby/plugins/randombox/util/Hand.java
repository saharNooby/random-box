package me.saharnooby.plugins.randombox.util;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public enum Hand {

	MAIN, OFF;

	public ItemStack get(@NonNull Player player) {
		return this == MAIN ? player.getItemInHand() : player.getInventory().getItemInOffHand();
	}

	public void set(@NonNull Player player, ItemStack stack) {
		if (this == MAIN) {
			player.setItemInHand(stack);
		} else {
			player.getInventory().setItemInOffHand(stack);
		}
	}

	public void removeOne(@NonNull Player player) {
		ItemStack stack = get(player);

		if (stack == null) {
			throw new IllegalStateException("Expected an item in " + this + " hand");
		}

		if (stack.getAmount() < 2) {
			set(player, new ItemStack(Material.AIR));
		} else {
			stack = stack.clone();
			stack.setAmount(stack.getAmount() - 1);
			set(player, stack);
		}
	}

}
