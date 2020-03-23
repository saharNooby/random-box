package me.saharnooby.plugins.randombox.box.effect;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.box.Box;
import me.saharnooby.plugins.randombox.box.item.DroppedItem;
import me.saharnooby.plugins.randombox.nms.NMSItemUtil;
import me.saharnooby.plugins.randombox.nms.RawChatUtil;
import me.saharnooby.plugins.randombox.util.ConfigUtil;
import me.saharnooby.plugins.randombox.util.Json;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BoxEffect {

	public static final BoxEffect EMPTY = new BoxEffect(null, null, null);

	private final BoxSound sound;
	private final BoxFirework firework;
	private final String broadcast;

	public BoxEffect(@NonNull ConfigurationSection section) {
		this(parseSound(section), parseFirework(section), ConfigUtil.getColoredString(section, "broadcast"));
	}

	private static BoxSound parseSound(@NonNull ConfigurationSection section) {
		if (section.isConfigurationSection("sound")) {
			return ConfigUtil.wrapExceptions(() -> new BoxSound(section.getConfigurationSection("sound")), "Invalid effect sound");
		}

		return null;
	}

	private static BoxFirework parseFirework(@NonNull ConfigurationSection section) {
		if (section.isConfigurationSection("firework")) {
			return ConfigUtil.wrapExceptions(() -> new BoxFirework(section.getConfigurationSection("firework")), "Invalid effect firework");
		}

		return null;
	}

	public void play(@NonNull Player player, @NonNull Box box, DroppedItem item) {
		Location loc = player.getEyeLocation();

		if (this.sound != null) {
			this.sound.play(loc);
		}

		if (this.firework != null) {
			this.firework.launch(loc);
		}

		if (this.broadcast != null) {
			broadcast(player, box, item);
		}
	}

	private void broadcast(@NonNull Player player, @NonNull Box box, DroppedItem item) {
		String message = this.broadcast.replace("%player%", player.getName());

		if (!message.contains("%box%") && !message.contains("%item%")) {
			Bukkit.broadcastMessage(message);
			return;
		}

		try {
			RawChatUtil.broadcastRawMessage(formatRawMessage(message, box, item));
		} catch (Exception e) {
			RandomBox.warn("Failed to broadcast a message: " + e);
		}
	}

	private static String formatRawMessage(@NonNull String format, @NonNull Box box, DroppedItem item) throws ReflectiveOperationException {
		List<Object> extra = new ArrayList<>();

		StringBuffer sb = new StringBuffer();

		Matcher matcher = Pattern.compile("%(box|item)%").matcher(format);

		while (matcher.find()) {
			sb.setLength(0);
			matcher.appendReplacement(sb, "");
			extra.add(toTextComponent(sb.toString()));

			Map<String, Object> component = new HashMap<>();

			switch (matcher.group(1)) {
				case "box":
					component.put("text", box.getName());
					component.put("hoverEvent", createHoverEvent(box.getItem()));
					break;
				case "item":
					if (item != null) {
						component.put("text", item.getItem().getName());
						component.put("hoverEvent", createHoverEvent(item.getStack()));
					} else {
						component.put("text", "%item%");
					}

					break;
			}

			extra.add(component);
		}

		sb.setLength(0);
		matcher.appendTail(sb);
		extra.add(toTextComponent(sb.toString()));

		Map<String, Object> parent = toTextComponent("");
		parent.put("extra", extra);
		return Json.toJson(parent);
	}

	private static Map<String, Object> toTextComponent(@NonNull String text) {
		Map<String, Object> map = new HashMap<>();
		map.put("text", text);
		return map;
	}

	private static Map<String, Object> createHoverEvent(ItemStack item) throws ReflectiveOperationException {
		Map<String, Object> map = new HashMap<>();
		map.put("action", "show_item");
		map.put("value", NMSItemUtil.saveToNBT(item));
		return map;
	}

	public boolean isEmpty() {
		return this.sound == null && this.firework == null && this.broadcast == null;
	}

}
