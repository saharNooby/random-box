package me.saharnooby.plugins.randombox.command;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public final class TargetSelector {

	// https://minecraft.gamepedia.com/Commands#Target_selectors

	private static final Pattern OLD_PATTERN = Pattern.compile("@[arp]\\[-?[0-9]*]");

	private static final String PAIR = "\\s*[^=,]+\\s*=\\s*[^=,]+\\s*";

	private static final Pattern PATTERN = Pattern.compile("@[arp](\\[(" + PAIR + "(," + PAIR + ")*)?])?");

	private final CommandSender sender;
	private final char type;
	private final Map<String, String> options = new HashMap<>();

	public TargetSelector(@NonNull CommandSender sender, @NonNull String selector) {
		if (OLD_PATTERN.matcher(selector).matches()) {
			// Convert old RandomBox selector to the new format
			selector = selector.substring(0, 2) + "[limit=" + selector.substring(3, selector.length() - 1) + "]";
		} else if (!PATTERN.matcher(selector).matches()) {
			throw new IllegalArgumentException("Invalid selector format");
		}

		this.sender = sender;
		this.type = selector.charAt(1);

		if (selector.length() > 2) {
			for (String pair : selector.substring(3, selector.length() - 1).split(",")) {
				int i = pair.indexOf('=');

				this.options.put(pair.substring(0, i).trim(), pair.substring(i + 1).trim());
			}
		}
	}

	public List<Player> select() {
		switch (this.type) {
			case 'a':
				return selectAll();
			case 'r':
				return selectRandom();
			case 'p':
				return selectNearest();
			default:
				throw new IllegalArgumentException("Invalid selector type");
		}
	}

	private List<Player> selectAll() {
		return new ArrayList<>(Bukkit.getOnlinePlayers());
	}

	private List<Player> selectRandom() {
		List<Player> list = new ArrayList<>(Bukkit.getOnlinePlayers());

		Collections.shuffle(list);

		return limit(list);
	}

	private List<Player> selectNearest() {
		Location loc;

		if (this.sender instanceof Entity) {
			loc = ((Entity) this.sender).getLocation();
		} else if (this.sender instanceof BlockCommandSender) {
			loc = ((BlockCommandSender) this.sender).getBlock().getLocation();
		} else {
			loc = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
		}

		if (this.options.containsKey("world")) {
			String name = this.options.get("world");

			World world = Bukkit.getWorld(name);

			if (world == null) {
				throw new IllegalArgumentException("World " + name + " not found");
			}

			loc.setWorld(world);
		}

		parseCoord(loc, Location::setX, "x");
		parseCoord(loc, Location::setY, "y");
		parseCoord(loc, Location::setZ, "z");

		List<Player> list = loc.getWorld().getPlayers();

		list.sort(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(loc)));

		return limit(list);
	}

	private List<Player> limit(@NonNull List<Player> list) {
		if (list.isEmpty()) {
			return list;
		}

		int limit;

		try {
			limit = Integer.parseInt(this.options.getOrDefault("limit", "1"));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid limit");
		}

		if (limit < 1) {
			Collections.reverse(list);

			limit *= -1;
		}

		return list.subList(0, Math.max(1, Math.min(limit, list.size())));
	}

	private void parseCoord(@NonNull Location loc, @NonNull BiConsumer<Location, Double> setter, @NonNull String key) {
		if (this.options.containsKey(key)) {
			try {
				setter.accept(loc, Double.parseDouble(this.options.get(key)));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid " + key + " coordinate");
			}
		}
	}

}
