package me.saharnooby.plugins.randombox.listener;

import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.nms.NMSUtil;
import me.saharnooby.plugins.randombox.util.Hand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class BoxInteractListener implements Listener {
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (!e.getAction().name().startsWith("RIGHT_CLICK_")) {
			return;
		}

		Player player = e.getPlayer();

		if (player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) {
			// У игрока уже открыт какой-то инвентарь, скорее всего это коробка
			return;
		}

		RandomBox.getInstance().getBoxStorage().getBox(e.getItem()).ifPresent(box -> {
			e.setCancelled(true);

			if (box.isOpenWhenClicked()) {
				Hand hand = NMSUtil.getMinorVersion() >= 9 && e.getHand() == EquipmentSlot.OFF_HAND ? Hand.OFF : Hand.MAIN;

				box.open(player, hand, true);
			}
		});
	}
	
}
