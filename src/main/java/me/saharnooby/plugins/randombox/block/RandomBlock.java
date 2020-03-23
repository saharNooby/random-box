package me.saharnooby.plugins.randombox.block;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.saharnooby.plugins.randombox.economy.VaultEconomy;
import me.saharnooby.plugins.randombox.util.Hand;
import me.saharnooby.plugins.randombox.message.MessageKey;
import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.box.Box;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class RandomBlock {

	@Getter
	private final Block block;
	private final Box box;
	private final double price;
	
	public void onClick(@NonNull Player player) {
		if (this.price == 0) {
			open(player);
			return;
		}

		VaultEconomy economy = RandomBox.getInstance().getEconomy();

		if (economy.isEnabled()) {
			if (economy.getEconomy().withdrawPlayer(player, this.price).transactionSuccess()) {
				open(player);
			} else {
				RandomBox.send(player, MessageKey.NOT_ENOUGH_MONEY);
			}
		} else {
			RandomBox.send(player, ChatColor.RED + "Vault economy not found. Contact server administrator.");
		}
	}

	private void open(@NonNull Player player) {
		this.box.open(player, Hand.MAIN, false);
	}

	@Override
	public String toString() {
		return this.block.getWorld().getName() + ":" + this.block.getX() + ":" + this.block.getY() + ":" + this.block.getZ() + ":" + this.box.getId() + ":" + this.price;
	}

}
