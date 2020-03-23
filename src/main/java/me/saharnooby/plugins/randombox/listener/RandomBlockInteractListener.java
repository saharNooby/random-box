package me.saharnooby.plugins.randombox.listener;

import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.nms.NMSUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class RandomBlockInteractListener implements Listener {
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		if (NMSUtil.getMinorVersion() >= 9 && e.getHand() != EquipmentSlot.HAND) {
			return;
		}

		Player player = e.getPlayer();

		if (player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) {
			// У игрока уже открыт какой-то инвентарь, скорее всего это коробка
			return;
		}

		RandomBox.getInstance().getRandomBlockStorage().findBlock(e.getClickedBlock()).ifPresent(block -> {
			e.setCancelled(true);

			block.onClick(player);
		});
	}
	
}
