package me.saharnooby.plugins.randombox.listener;

import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.block.RandomBlock;
import me.saharnooby.plugins.randombox.block.RandomBlockStorage;
import me.saharnooby.plugins.randombox.BoxAndPrice;
import me.saharnooby.plugins.randombox.message.MessageKey;
import me.saharnooby.plugins.randombox.nms.NMSUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class RandomBlockEditListener implements Listener {

	public static final String SET_KEY = "RandomBox.randomBlock.set";
	public static final String REMOVE_KEY = "RandomBox.randomBlock.remove";

	@EventHandler(priority = EventPriority.LOW)
	public void onRightClick(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		if (NMSUtil.getMinorVersion() >= 9 && e.getHand() != EquipmentSlot.HAND) {
			return;
		}

		RandomBlockStorage storage = RandomBox.getInstance().getRandomBlockStorage();

		Player player = e.getPlayer();

		if (player.hasMetadata(SET_KEY)) {
			e.setCancelled(true);

			BoxAndPrice data = (BoxAndPrice) player.getMetadata(SET_KEY).get(0).value();

			player.removeMetadata(SET_KEY, RandomBox.getInstance());

			storage.addBlock(new RandomBlock(e.getClickedBlock(), data.getBox(), data.getPrice()));

			RandomBox.send(player, MessageKey.RANDOM_BLOCK_CREATED);
		} else if (player.hasMetadata(REMOVE_KEY)) {
			e.setCancelled(true);

			RandomBlock block = storage.findBlock(e.getClickedBlock()).orElse(null);

			if (block != null) {
				storage.removeBlock(block);

				RandomBox.send(player, MessageKey.RANDOM_BLOCK_REMOVED);
			} else {
				RandomBox.send(player, MessageKey.NOT_A_RANDOM_BLOCK);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onLeftClick(PlayerInteractEvent e) {
		if (!e.getAction().name().startsWith("LEFT_CLICK_")) {
			return;
		}

		if (NMSUtil.getMinorVersion() >= 9 && e.getHand() != EquipmentSlot.HAND) {
			return;
		}

		Player player = e.getPlayer();

		if (!player.hasMetadata(REMOVE_KEY)) {
			return;
		}

		e.setCancelled(true);

		player.removeMetadata(REMOVE_KEY, RandomBox.getInstance());

		RandomBox.send(player, MessageKey.EXITING_REMOVAL_MODE);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		player.removeMetadata(SET_KEY, RandomBox.getInstance());
		player.removeMetadata(REMOVE_KEY, RandomBox.getInstance());
	}
	
}
