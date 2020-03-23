package me.saharnooby.plugins.randombox.box.effect;

import lombok.NonNull;
import me.saharnooby.plugins.randombox.RandomBox;
import me.saharnooby.plugins.randombox.listener.FireworkDamageListener;
import me.saharnooby.plugins.randombox.nms.ReflectionUtil;
import me.saharnooby.plugins.randombox.util.ConfigUtil;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;

public final class BoxFirework {

	private final FireworkEffect effect;
	private final int power;

	public BoxFirework(@NonNull ConfigurationSection section) {
		this.effect = ConfigUtil.parseFireworkEffect(section);
		this.power = section.getInt("power", 0);

		ConfigUtil.assertFalse(this.power < 0, "Firework power must be >= 0");
	}

	public void launch(@NonNull Location loc) {
		Firework firework = loc.getWorld().spawn(loc, Firework.class);

		firework.setMetadata(FireworkDamageListener.META_KEY, new FixedMetadataValue(RandomBox.getInstance(), true));

		FireworkMeta meta = firework.getFireworkMeta();
		meta.clearEffects();
		meta.addEffect(this.effect);

		if (this.power == 0) {
			try {
				ReflectionUtil.setField(meta, "power", -1);
				firework.setFireworkMeta(meta);

				Object nms = ReflectionUtil.getField(firework, "entity");
				ReflectionUtil.setField(nms, "ticksFlown", 2);
				ReflectionUtil.setField(nms, "expectedLifespan", 0);
			} catch (Exception e) {
				RandomBox.warn("Failed to launch a firework: " + e);
			}
		} else {
			meta.setPower(this.power);
			firework.setFireworkMeta(meta);
		}
	}

}
