package me.saharnooby.plugins.randombox.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import lombok.NonNull;
import me.saharnooby.plugins.randombox.nms.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public final class ItemUtil {

	public static ItemMeta getOrCreateMeta(@NonNull ItemStack item) {
		return item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
	}

	public static void setDisplayName(@NonNull ItemStack item, String name) {
		ItemMeta meta = getOrCreateMeta(item);
		meta.setDisplayName(name);
		item.setItemMeta(meta);
	}

	public static Enchantment parseEnchantment(@NonNull String id) {
		switch (id.toLowerCase()) {
			case "power":
				return Enchantment.ARROW_DAMAGE;
			case "flame":
				return Enchantment.ARROW_FIRE;
			case "infinity":
				return Enchantment.ARROW_INFINITE;
			case "punch":
				return Enchantment.ARROW_KNOCKBACK;
			case "sharpness":
				return Enchantment.DAMAGE_ALL;
			case "bane":
			case "baneofarthropods":
			case "bane_of_arthropods":
				return Enchantment.DAMAGE_ARTHROPODS;
			case "smite":
				return Enchantment.DAMAGE_UNDEAD;
			case "efficiency":
				return Enchantment.DIG_SPEED;
			case "unbreaking":
				return Enchantment.DURABILITY;
			case "fire":
			case "fireaspect":
			case "fire_aspect":
				return Enchantment.FIRE_ASPECT;
			case "knockback":
				return Enchantment.KNOCKBACK;
			case "fortune":
				return Enchantment.LOOT_BONUS_BLOCKS;
			case "looting":
				return Enchantment.LOOT_BONUS_MOBS;
			case "luck":
			case "luckofthesea":
			case "luck_of_the_sea":
				return Enchantment.LUCK;
			case "lure":
				return Enchantment.LURE;
			case "respiration":
				return Enchantment.OXYGEN;
			case "protection":
				return Enchantment.PROTECTION_ENVIRONMENTAL;
			case "blast":
			case "blastprotection":
			case "blast_protection":
				return Enchantment.PROTECTION_EXPLOSIONS;
			case "feather":
			case "featherfalling":
			case "feather_falling":
				return Enchantment.PROTECTION_FALL;
			case "fireprotection":
			case "fire_protection":
				return Enchantment.PROTECTION_FIRE;
			case "projectile":
			case "projectileprotection":
			case "projectile_protection":
				return Enchantment.PROTECTION_PROJECTILE;
			case "silk":
			case "silktouch":
			case "silk_touch":
				return Enchantment.SILK_TOUCH;
			case "thorns":
				return Enchantment.THORNS;
			case "aqua":
			case "aquaaffinity":
			case "aqua_affinity":
				return Enchantment.WATER_WORKER;
			default:
				return Enchantment.getByName(id.toUpperCase());
		}
	}

	public static void setBase64EncodedTextures(@NonNull ItemStack item, @NonNull String base64) {
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);

		PropertyMap propertyMap = profile.getProperties();

		if (propertyMap == null) {
			throw new IllegalStateException("Profile doesn't contain a property map");
		}

		propertyMap.put("textures", new Property("textures", base64));

		ItemMeta meta = getOrCreateMeta(item);

		try {
			Field field = meta.getClass().getDeclaredField("profile");
			field.setAccessible(true);
			field.set(meta, profile);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}

		item.setItemMeta(meta);
	}

	public static boolean isCustomModelDataSupported() {
		return NMSUtil.getMinorVersion() >= 14;
	}

	@SuppressWarnings("JavaReflectionMemberAccess")
	public static void setCustomModelData(@NonNull ItemStack item, int data) {
		if (!isCustomModelDataSupported()) {
			throw new IllegalStateException("CustomModelData is not supported on " + NMSUtil.getVersion());
		}

		ItemMeta meta = getOrCreateMeta(item);

		try {
			ItemMeta.class.getMethod("setCustomModelData", Integer.class).invoke(meta, data);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}

		item.setItemMeta(meta);
	}

}
