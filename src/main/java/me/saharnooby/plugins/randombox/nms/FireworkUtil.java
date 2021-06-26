package me.saharnooby.plugins.randombox.nms;

import lombok.NonNull;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public final class FireworkUtil {

	public static void explodeInstantly(@NonNull Firework firework, @NonNull FireworkMeta meta) throws ReflectiveOperationException {
		ReflectionUtil.setField(meta, "power", -1);
		firework.setFireworkMeta(meta);

		boolean v17 = NMSUtil.getMinorVersion() >= 17;

		Object nms = ReflectionUtil.getField(firework, "entity");
		ReflectionUtil.setField(nms, v17 ? "e" : "ticksFlown", 2);
		ReflectionUtil.setField(nms, v17 ? "f" : "expectedLifespan", 0);
	}

}