package me.saharnooby.plugins.randombox.block;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.box.Box;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Optional;
import java.util.logging.Level;

public final class RandomBlockParser {

	public static Optional<RandomBlock> tryParse(@NonNull String str) {
		String[] split = str.split(":");

		if (split.length != 6) {
			RandomBox.warn("Invalid random block '" + str + "'");
			return Optional.empty();
		}

		try {
			World world = Bukkit.getWorld(split[0]);

			if (world == null) {
				RandomBox.warn("Invalid random block '" + str + "': world " + split[0] + " not found");
				return Optional.empty();
			}

			double x = Double.valueOf(split[1]);
			double y = Double.valueOf(split[2]);
			double z = Double.valueOf(split[3]);
			Location loc = new Location(world, x, y, z);

			String id = split[4];

			Box box = RandomBox.getInstance().getBoxStorage().getBox(Integer.parseInt(id)).orElse(null);

			if (box == null) {
				RandomBox.warn("Invalid random block '" + str + "': box " + id + " not found");
				return Optional.empty();
			}

			double price = Double.valueOf(split[5]);

			return Optional.of(new RandomBlock(loc.getBlock(), box, price));
		} catch (Exception e) {
			RandomBox.getInstance().getLogger().log(Level.WARNING, "Invalid random block '" + str + "'", e);
		}

		return Optional.empty();
	}

}
