package me.saharnooby.plugins.randombox.nms;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public final class RawChatUtil {

	public static void broadcastRawMessage(@NonNull String json) throws ReflectiveOperationException {
		Object packet = createChatPacket(json);

		for (Player player : Bukkit.getOnlinePlayers()) {
			sendPacket(player, packet);
		}
	}

	private static Object createChatPacket(@NonNull String json) throws ReflectiveOperationException {
		Method method = NMSUtil.getNMSClass("IChatBaseComponent$ChatSerializer").getMethod("a", String.class);

		Object component = method.invoke(null, json);

		try {
			return NMSUtil.getNMSClass("PacketPlayOutChat")
					.getConstructor(NMSUtil.getNMSClass("IChatBaseComponent"), byte.class)
					.newInstance(component, (byte) 0);
		} catch (NoSuchMethodException e) {
			Class<?> typeClass = NMSUtil.getNMSClass("ChatMessageType");

			Object type = typeClass.getField("CHAT").get(null);

			return NMSUtil.getNMSClass("PacketPlayOutChat")
					.getConstructor(NMSUtil.getNMSClass("IChatBaseComponent"), typeClass)
					.newInstance(component, type);
		}
	}

	private static void sendPacket(@NonNull Player player, @NonNull Object packet) throws ReflectiveOperationException {
		Object connection = ReflectionUtil.getField(player.getClass().getMethod("getHandle").invoke(player), "playerConnection");

		connection.getClass().getMethod("sendPacket", NMSUtil.getNMSClass("Packet")).invoke(connection, packet);
	}

}
