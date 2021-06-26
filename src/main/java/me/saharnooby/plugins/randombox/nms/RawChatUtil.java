package me.saharnooby.plugins.randombox.nms;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

public final class RawChatUtil {

	public static void broadcastRawMessage(@NonNull String json) throws ReflectiveOperationException {
		if (NMSUtil.getMinorVersion() >= 17) {
			broadcastRawMessageSpigotBungeeCordAPI(json);

			return;
		}

		Object packet = createChatPacket(json);

		for (Player player : Bukkit.getOnlinePlayers()) {
			sendPacket(player, packet);
		}
	}

	@SuppressWarnings("JavaReflectionInvocation")
	private static void broadcastRawMessageSpigotBungeeCordAPI(@NonNull String json) throws ReflectiveOperationException {
		Object component = Class.forName("net.md_5.bungee.chat.ComponentSerializer")
				.getMethod("parse", String.class)
				.invoke(null, json);

		component = Class.forName("net.md_5.bungee.api.chat.TextComponent")
				.getConstructor(Class.forName("[Lnet.md_5.bungee.api.chat.BaseComponent;"))
				.newInstance(component);

		Method sendMessage = Player.Spigot.class.getMethod("sendMessage", Class.forName("net.md_5.bungee.api.chat.BaseComponent"));

		for (Player player : Bukkit.getOnlinePlayers()) {
			sendMessage.invoke(player.spigot(), component);
		}
	}

	private static Object createChatPacket(@NonNull String json) throws ReflectiveOperationException {
		Method method = NMSUtil.getNMSClass("IChatBaseComponent$ChatSerializer").getMethod("a", String.class);

		Object component = method.invoke(null, json);

		Class<?> componentClass = NMSUtil.getNMSClass("IChatBaseComponent");

		Class<?> packetClass = NMSUtil.getNMSClass("PacketPlayOutChat");

		try {
			return packetClass.getConstructor(componentClass, byte.class).newInstance(component, (byte) 0);
		} catch (NoSuchMethodException e) {
			Class<?> typeClass = NMSUtil.getNMSClass("ChatMessageType");

			Object type = typeClass.getField("CHAT").get(null);

			if (NMSUtil.getMinorVersion() >= 16) {
				return packetClass.getConstructor(componentClass, typeClass, UUID.class).newInstance(component, type, new UUID(0, 0));
			} else {
				return packetClass.getConstructor(componentClass, typeClass).newInstance(component, type);
			}
		}
	}

	private static void sendPacket(@NonNull Player player, @NonNull Object packet) throws ReflectiveOperationException {
		Object connection = ReflectionUtil.getField(player.getClass().getMethod("getHandle").invoke(player), "playerConnection");

		connection.getClass().getMethod("sendPacket", NMSUtil.getNMSClass("Packet")).invoke(connection, packet);
	}

}
