package me.saharnooby.plugins.randombox.box;

import lombok.Data;
import lombok.NonNull;
import me.saharnooby.plugins.randombox.RandomBox;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.logging.Level;

public final class BoxOpenTimestampStorage {

	@Data
	private static final class Key {

		private final String playerName;
		private final int boxId;

	}

	private final Map<Key, Long> timestamps = new HashMap<>();

	public void load() throws IOException {
		this.timestamps.clear();

		File file = getFile();

		if (!file.exists()) {
			return;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.isEmpty()) {
					String[] split = line.split(",");

					this.timestamps.put(new Key(split[0], Integer.parseInt(split[1])), Long.parseLong(split[2]));
				}
			}
		}
	}

	public OptionalLong get(@NonNull Player player, @NonNull Box box) {
		Long ts = this.timestamps.get(new Key(player.getName(), box.getId()));

		return ts == null ? OptionalLong.empty() : OptionalLong.of(ts);
	}

	public void onOpen(@NonNull Player player, @NonNull Box box) {
		this.timestamps.put(new Key(player.getName(), box.getId()), System.currentTimeMillis());
		saveAsync();
	}

	private void saveAsync() {
		Map<Key, Long> map = new HashMap<>(this.timestamps);

		Bukkit.getScheduler().runTaskAsynchronously(RandomBox.getInstance(), () -> {
			File file = getFile();

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				for (Map.Entry<Key, Long> e : map.entrySet()) {
					Key key = e.getKey();
					writer.write(key.getPlayerName());
					writer.write(",");
					writer.write(String.valueOf(key.getBoxId()));
					writer.write(",");
					writer.write(String.valueOf(e.getValue()));
					writer.newLine();
				}
			} catch (Exception e) {
				RandomBox.getInstance().getLogger().log(Level.WARNING, "Failed to save open timestamps to " + file.getAbsolutePath(), e);
			}
		});
	}

	private File getFile() {
		return new File(RandomBox.getInstance().getDataFolder(), "open-timestamps.dat");
	}

}
