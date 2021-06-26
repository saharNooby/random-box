package me.saharnooby.plugins.randombox.nms;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.RandomBox;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.logging.Level;

public final class NMSItemUtil {

	public static String saveToNBT(ItemStack item) throws ReflectiveOperationException {
		item = item == null ? new ItemStack(Material.AIR) : item;

		Object copy = asNMSCopy(item);

		if (NMSUtil.getMinorVersion() >= 17) {
			Object compound = NMSUtil.getNMSClass("nbt.NBTTagCompound").newInstance();
			copy.getClass().getMethod("save", compound.getClass()).invoke(copy, compound);
			return compound.toString();
		} else {
			Object compound = NMSUtil.getNMSClass("NBTTagCompound").newInstance();
			copy.getClass().getMethod("save", compound.getClass()).invoke(copy, compound);
			return compound.toString();
		}
	}

	public static void setNBT(@NonNull ItemStack item, @NonNull String tag) {
		try {
			Object copy = asNMSCopy(item);

			if (NMSUtil.getMinorVersion() >= 17) {
				Object parsed = NMSUtil.getNMSClass("nbt.MojangsonParser").getMethod("parse", String.class).invoke(null, tag);
				NMSUtil.getNMSClass("world.item.ItemStack").getMethod("setTag", NMSUtil.getNMSClass("nbt.NBTTagCompound")).invoke(copy, parsed);
				Object meta = NMSUtil.getCraftClass("inventory.CraftItemStack").getMethod("getItemMeta", NMSUtil.getNMSClass("world.item.ItemStack")).invoke(null, copy);
				item.setItemMeta((ItemMeta) meta);
			} else {
				Object parsed = NMSUtil.getNMSClass("MojangsonParser").getMethod("parse", String.class).invoke(null, tag);
				NMSUtil.getNMSClass("ItemStack").getMethod("setTag", NMSUtil.getNMSClass("NBTTagCompound")).invoke(copy, parsed);
				Object meta = NMSUtil.getCraftClass("inventory.CraftItemStack").getMethod("getItemMeta", NMSUtil.getNMSClass("ItemStack")).invoke(null, copy);
				item.setItemMeta((ItemMeta) meta);
			}
		} catch (ReflectiveOperationException e) {
			RandomBox.getInstance().getLogger().log(Level.WARNING, "Failed to set item NBT", e);
		}
	}

	private static Object asNMSCopy(@NonNull ItemStack item) throws ReflectiveOperationException {
		return NMSUtil.getCraftClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
	}

}
