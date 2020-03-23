package me.saharnooby.plugins.randombox;

import lombok.Getter;
import lombok.NonNull;
import me.saharnooby.plugins.randombox.block.RandomBlockStorage;
import me.saharnooby.plugins.randombox.box.BoxOpenTimestampStorage;
import me.saharnooby.plugins.randombox.box.BoxStorage;
import me.saharnooby.plugins.randombox.command.*;
import me.saharnooby.plugins.randombox.config.PluginConfig;
import me.saharnooby.plugins.randombox.economy.VaultEconomy;
import me.saharnooby.plugins.randombox.listener.BoxInteractListener;
import me.saharnooby.plugins.randombox.listener.FireworkDamageListener;
import me.saharnooby.plugins.randombox.listener.RandomBlockEditListener;
import me.saharnooby.plugins.randombox.listener.RandomBlockInteractListener;
import me.saharnooby.plugins.randombox.message.MessageKey;
import me.saharnooby.plugins.randombox.metrics.Metrics;
import me.saharnooby.plugins.randombox.nms.NMSUtil;
import me.saharnooby.plugins.randombox.util.IOUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Stream;

@Getter
public final class RandomBox extends JavaPlugin {

	@Getter
	private static RandomBox instance;

	private PluginConfig pluginConfig;
	private YamlConfiguration locale;

	private final BoxStorage boxStorage = new BoxStorage();
	private final RandomBlockStorage randomBlockStorage = new RandomBlockStorage();
	private final BoxOpenTimestampStorage timestampStorage = new BoxOpenTimestampStorage();

	private VaultEconomy economy;

	@Override
	public void onEnable() {
		instance = this;

		saveDefaultConfig();
		loadAll();

		Bukkit.getPluginManager().registerEvents(new BoxInteractListener(), this);
		Bukkit.getPluginManager().registerEvents(new FireworkDamageListener(), this);
		Bukkit.getPluginManager().registerEvents(new RandomBlockEditListener(), this);
		Bukkit.getPluginManager().registerEvents(new RandomBlockInteractListener(), this);

		getCommand("randombox").setExecutor(new RandomBoxCommand());
		getCommand("openbox").setExecutor(new OpenBoxCommand());
		getCommand("givebox").setExecutor(new GiveBoxCommand());
		getCommand("randomblock").setExecutor(new RandomBlockCommand());
		getCommand("forceopenbox").setExecutor(new ForceOpenBoxCommand());

		this.economy = new VaultEconomy();

		new Metrics(this, 6848);
	}

	private void loadAll() {
		this.pluginConfig = new PluginConfig(getConfig());
		loadLocale();
		loadBoxes();
		loadRandomBlocks();
		loadOpenTimestamps();
	}

	private void loadLocale() {
		String locale = this.pluginConfig.getLocale();

		InputStream check = RandomBox.class.getResourceAsStream("/locales/" + locale + ".yml");

		if (check == null) {
			getLogger().warning("Unknown locale " + locale + ", using en");

			locale = "en";
		} else {
			IOUtil.closeSilent(check);
		}

		try {
			saveDefaultLocale(locale);
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed to save default locale " + locale, e);
		}

		this.locale = new YamlConfiguration();

		try {
			this.locale.load(new File(getDataFolder(), "locales/" + locale + ".yml"));
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed to read locale file", e);
		}
	}

	private void saveDefaultLocale(@NonNull String name) throws Exception {
		File dir = new File(getDataFolder(), "locales");

		IOUtil.mkdirs(dir);

		File file = new File(dir, name + ".yml");

		if (file.exists()) {
			return;
		}

		Files.copy(RandomBox.class.getResourceAsStream("/locales/" + name + ".yml"), file.toPath());
	}

	private void loadBoxes() {
		try {
			saveDefaultBox();
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed to save default box", e);
		}

		this.boxStorage.load();
	}

	private void saveDefaultBox() throws IOException {
		File dir = new File(getDataFolder(), "boxes");

		IOUtil.mkdirs(dir);

		if (Stream.of(Objects.requireNonNull(dir.listFiles())).anyMatch(f -> f.getName().endsWith(".yml"))) {
			return;
		}

		String resName = "/boxes/9901" + (NMSUtil.getMinorVersion() < 13 ? "_legacy" : "") + ".yml";

		Files.copy(RandomBox.class.getResourceAsStream(resName), new File(dir, "9901.yml").toPath());
	}

	private void loadRandomBlocks() {
		try {
			this.randomBlockStorage.load();
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed to load random block storage", e);
		}
	}

	private void loadOpenTimestamps() {
		try {
			this.timestampStorage.load();
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed to load open timestamp storage", e);
		}
	}

	public void reloadPlugin() {
		saveDefaultConfig();
		reloadConfig();
		loadAll();
	}

	public static void info(String message) {
		instance.getLogger().log(Level.INFO, message);
	}

	public static void warn(String message) {
		instance.getLogger().log(Level.WARNING, message);
	}

	public static void send(@NonNull CommandSender sender, @NonNull String message) {
		sender.sendMessage(MessageKey.PREFIX + message);
	}

	public static void send(@NonNull CommandSender sender, @NonNull MessageKey key) {
		send(sender, key.toString());
	}
	
}