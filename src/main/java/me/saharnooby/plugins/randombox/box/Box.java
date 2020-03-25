package me.saharnooby.plugins.randombox.box;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.box.effect.BoxEffect;
import me.saharnooby.plugins.randombox.box.gui.format.GuiFormat;
import me.saharnooby.plugins.randombox.box.gui.format.GuiIteration;
import me.saharnooby.plugins.randombox.box.gui.view.GuiView;
import me.saharnooby.plugins.randombox.box.item.DropItem;
import me.saharnooby.plugins.randombox.box.item.DroppedItem;
import me.saharnooby.plugins.randombox.box.limit.Limit;
import me.saharnooby.plugins.randombox.message.MessageKey;
import me.saharnooby.plugins.randombox.util.Hand;
import me.saharnooby.plugins.randombox.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Box {

	private final int id;

	// Fields are non-final and package private for the BoxParser.
	String name;
	@Getter(AccessLevel.NONE)
	ItemStack item;

	final List<DropItem> items = new ArrayList<>();
	int dropCount;

	boolean unstackable;
	boolean openWhenClicked;
	boolean checkPermission;

	BoxEffect openEffect;
	BoxEffect moveEffect;
	BoxEffect dropEffect;
	BoxEffect closeEffect;

	final List<GuiIteration> iterations = new ArrayList<>();
	GuiFormat guiFormat;

	final List<Limit> limits = new ArrayList<>();

	public Material getType() {
		return this.item.getType();
	}

	public ItemStack getItem() {
		ItemStack item = this.item.clone();

		if (this.unstackable) {
			ItemUtil.setDisplayName(item, this.name + randomString());
		}

		return item;
	}

	private static String randomString() {
		Random r = ThreadLocalRandom.current();

		return "§" + r.nextInt(10) + "§" + r.nextInt(10) + "§" + r.nextInt(10) + "§" + r.nextInt(10);
	}

	public void open(@NonNull Player player, @NonNull Hand hand, boolean removeItem) {
		if (this.checkPermission && !player.hasPermission("randombox.open.*") && !player.hasPermission("randombox.open." + this.id)) {
			RandomBox.send(player, MessageKey.BOX_OPEN_ERROR + ": " + MessageKey.NO_PERMISSIONS_TO_OPEN);

			return;
		}

		long passed = System.currentTimeMillis() - RandomBox.getInstance().getTimestampStorage().get(player, this).orElse(0);

		Limit maxLimit = null;

		for (Limit limit : this.limits) {
			if (!limit.mustCheck(player)) {
				continue;
			}

			if (passed < limit.getInterval() && (maxLimit == null || limit.getInterval() > maxLimit.getInterval())) {
				maxLimit = limit;
			}
		}

		if (maxLimit != null) {
			player.sendMessage(maxLimit.formatMessage(maxLimit.getInterval() - passed));
			return;
		}

		RandomBox.getInstance().getTimestampStorage().onOpen(player, this);

		if (removeItem && (!RandomBox.getInstance().getPluginConfig().isUseInfinitePermission() || !player.hasPermission("randombox.infinitebox"))) {
			hand.removeOne(player);
		}

		if (this.guiFormat != null) {
			this.openEffect.play(player, this, null);

			new GuiView(this, player);

			return;
		}

		List<DroppedItem> items = dropUniqueRandomItems();

		playDropEffect(player, items);

		DroppedItem.giveAndSendMessage(player, items);
	}

	public DropItem getRandomItem() {
		int rand = ThreadLocalRandom.current().nextInt(getChanceSum());
		int from = 0;
		
		for (DropItem item : this.items) {
			if (rand >= from && rand < from + item.getChance()) {
				return item;
			}

			from += item.getChance();
		}
		
		throw new RuntimeException();
	}

	private List<DroppedItem> dropUniqueRandomItems() {
		List<DropItem> items = new ArrayList<>(this.items);
		List<DroppedItem> result = new ArrayList<>();

		for (int i = 0; i < this.dropCount; i++) {
			// 'items' will always be non-empty since 'dropCount' can't be bigger than total item count.
			int from = 0;
			int rand = ThreadLocalRandom.current().nextInt(items.stream().mapToInt(DropItem::getChance).sum());

			for (DropItem item : items) {
				if (rand >= from && rand < from + item.getChance()) {
					result.add(item.toDropped());
					items.remove(item);
					break;
				}

				from += item.getChance();
			}
		}

		return result;
	}

	int getChanceSum() {
		int sum = 0;

		for (DropItem item : this.items) {
			sum += item.getChance();
		}

		return sum;
	}

	public void playDropEffect(@NonNull Player player, @NonNull List<DroppedItem> items) {
		DroppedItem max = null;

		for (DroppedItem dropped : items) {
			DropItem item = dropped.getItem();

			if (!item.getEffect().isEmpty() && (max == null || item.getChance() > max.getItem().getChance())) {
				max = dropped;
			}
		}

		(max != null ? max.getItem().getEffect() : this.dropEffect).play(player, this, max);
	}

}
