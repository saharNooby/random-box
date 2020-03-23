package me.saharnooby.plugins.randombox.box.gui.view;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.box.Box;
import me.saharnooby.plugins.randombox.box.gui.format.GuiFormat;
import me.saharnooby.plugins.randombox.box.gui.view.filler.FillerView;
import me.saharnooby.plugins.randombox.box.item.DropItem;
import me.saharnooby.plugins.randombox.box.item.DroppedItem;
import me.saharnooby.plugins.randombox.box.gui.view.filler.CompoundFillerView;
import me.saharnooby.plugins.randombox.util.NCPHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public final class GuiView implements Runnable, Listener {

	private final Box box;
	private final Player player;

	private final GuiFormat format;
	private final Inventory inventory;
	private final CompoundFillerView[] fillers;
	private final DropItem[] items;

	private final int taskId;

	// State
	private int iterationIndex;
	private int iterations;
	private int ticks;

	private boolean stopped;

	public GuiView(@NonNull Box box, @NonNull Player player) {
		this.box = box;
		this.player = player;

		this.format = box.getGuiFormat();
		this.inventory = this.format.createInventory(box.getName());
		this.fillers = new CompoundFillerView[this.inventory.getSize()];
		this.items = new DropItem[this.inventory.getSize()];

		this.format.getFillers().forEach((slot, filler) -> {
			CompoundFillerView view = new CompoundFillerView(filler);
			this.fillers[slot] = view;
			this.inventory.setItem(slot, view.getItem());
		});

		for (int slot : this.format.getItemSlots()) {
			DropItem item = box.getRandomItem();
			this.items[slot] = item;
			this.inventory.setItem(slot, item.createItem());
		}

		this.iterations = box.getIterations().get(0).getIterations();
		this.ticks = box.getIterations().get(0).getDelay();

		player.openInventory(this.inventory);

		NCPHook.exemptPermanently(player);

		Bukkit.getPluginManager().registerEvents(this, RandomBox.getInstance());

		this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(RandomBox.getInstance(), this, 0, 1);
	}

	@Override
	public void run() {
		if (!this.stopped) {
			tickNotStopped();
		}

		for (int slot = 0; slot < this.fillers.length; slot++) {
			FillerView view = this.fillers[slot];

			if (view == null) {
				continue;
			}

			view.tick();

			if (view.isUpdated()) {
				this.inventory.setItem(slot, view.getItem());
			}
		}
	}

	private void tickNotStopped() {
		if (--this.ticks > 0) {
			return;
		}

		if (--this.iterations <= 0) {
			this.iterationIndex++;

			if (this.iterationIndex >= this.box.getIterations().size()) {
				stop(true);

				return;
			}

			this.iterations = this.box.getIterations().get(this.iterationIndex).getIterations();
		}

		this.ticks = this.box.getIterations().get(this.iterationIndex).getDelay();

		moveItems();
	}

	private void moveItems() {
		List<Integer> slots = this.format.getItemSlots();

		int lastIndex = slots.size() - 1;

		DropItem item = this.box.getRandomItem();
		this.inventory.setItem(slots.get(lastIndex), item.createItem());
		this.items[slots.get(lastIndex)] = item;

		for (int i = 0; i < lastIndex; i++) {
			int slot = slots.get(i);
			this.items[slot] = this.items[slots.get(i + 1)];
			this.inventory.setItem(slot, this.inventory.getItem(slots.get(i + 1)));
		}

		this.box.getMoveEffect().play(this.player, this.box, null);
	}

	private void stop(boolean normalStop) {
		if (this.stopped) {
			throw new IllegalStateException();
		}

		this.stopped = true;

		for (CompoundFillerView view : this.fillers) {
			if (view != null) {
				view.stop();
			}
		}

		for (int slot : this.format.getItemSlots()) {
			if (!this.format.getActiveSlots().contains(slot)) {
				this.inventory.setItem(slot, null);
			}
		}

		List<DroppedItem> items = getDroppedItems(normalStop);

		DroppedItem.giveAndSendMessage(this.player, items);

		this.box.playDropEffect(this.player, items);

		NCPHook.unexempt(this.player);
	}

	private List<DroppedItem> getDroppedItems(boolean takeFromInventory) {
		List<DroppedItem> items = new ArrayList<>();

		if (takeFromInventory) {
			for (int slot : this.format.getActiveSlots()) {
				items.add(this.items[slot].toDropped(this.inventory.getItem(slot).getAmount()));
			}
		} else {
			int activeCount = this.format.getActiveSlots().size();

			for (int i = 0; i < activeCount; i++) {
				items.add(this.box.getRandomItem().toDropped());
			}
		}

		return items;
	}

	private void unregister() {
		if (!this.stopped) {
			stop(false);
		}

		this.box.getCloseEffect().play(this.player, this.box, null);

		HandlerList.unregisterAll(this);

		Bukkit.getScheduler().cancelTask(this.taskId);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getWhoClicked() == this.player) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent e) {
		if (e.getWhoClicked() == this.player) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		if (e.getPlayer() == this.player) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if (e.getPlayer() == this.player) {
			unregister();
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		if (e.getPlayer() == this.player) {
			unregister();
		}
	}

}
