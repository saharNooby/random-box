package me.saharnooby.plugins.randombox.block;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.RandomBox;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public final class RandomBlockStorage {

	private final Map<Block, RandomBlock> blocks = new LinkedHashMap<>();
	
	public void load() throws IOException {
		this.blocks.clear();
		
		File file = getFile();
		
		if (!file.exists()) {
			return;
		}
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.isEmpty() && !line.startsWith("#")) {
					RandomBlockParser.tryParse(line).ifPresent(block -> this.blocks.put(block.getBlock(), block));
				}
			}
		}
		
		RandomBox.info(this.blocks.size() + " blocks have been loaded");
	}

	public void addBlock(@NonNull RandomBlock b) {
		this.blocks.put(b.getBlock(), b);
		saveAsync();
	}
	
	public void removeBlock(@NonNull RandomBlock b) {
		this.blocks.remove(b.getBlock());
		saveAsync();
	}
	
	public Optional<RandomBlock> findBlock(@NonNull Block b) {
		return Optional.ofNullable(this.blocks.get(b));
	}

	private void saveAsync() {
		List<RandomBlock> blocks = new ArrayList<>(this.blocks.values());

		Bukkit.getScheduler().runTaskAsynchronously(RandomBox.getInstance(), () -> {
			File file = getFile();

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				writer.write("# Format: world:x:y:z:box_id:price");
				writer.newLine();
				writer.write("# Do not touch this file.");
				writer.newLine();

				for (RandomBlock block : blocks) {
					writer.write(block.toString());
					writer.newLine();
				}
			} catch (Exception e) {
				RandomBox.getInstance().getLogger().log(Level.WARNING, "Failed to save RandomBlocks to " + file.getAbsolutePath(), e);
			}
		});
	}

	private File getFile() {
		return new File(RandomBox.getInstance().getDataFolder(), "blocks.dat");
	}
	
}
