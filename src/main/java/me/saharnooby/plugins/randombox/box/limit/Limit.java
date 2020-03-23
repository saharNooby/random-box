package me.saharnooby.plugins.randombox.box.limit;

import lombok.Getter;
import lombok.NonNull;
import me.saharnooby.plugins.randombox.message.MessageKey;
import me.saharnooby.plugins.randombox.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Limit {

	private static final Pattern PLACEHOLDER = Pattern.compile("%remains\\[([^,]+),([^,]+),([^,]+),([^,]+)]%");

	@Getter
	private final long interval;
	private final String message;
	private final String checkPermission;
	private final String bypassPermission;

	public Limit(@NonNull ConfigurationSection section) {
		this.interval = parseInterval(section.getString("interval"));
		this.message = ConfigUtil.getColoredString(section, "message");
		this.checkPermission = section.getString("permissions.check");
		this.bypassPermission = section.getString("permissions.bypass");

		ConfigUtil.assertFalse(this.message == null, "Message not specified");
	}

	public Limit(long interval) {
		this.interval = interval;
		this.message = "" + MessageKey.PREFIX + MessageKey.BOX_OPEN_ERROR + ": " + MessageKey.OPEN_ONLY_AFTER.toString().replace("%d", "%remains[s.,m.,h.,day(s)]%");
		this.checkPermission = null;
		this.bypassPermission = null;
	}

	private static long parseInterval(String str) {
		ConfigUtil.assertFalse(str == null, "Interval not specified");
		ConfigUtil.assertFalse(!str.matches("\\d{1,9}[smhd]"), "Interval must consist of a number and a time unit like s, m, h, d");
		long value = Long.parseLong(str.substring(0, str.length() - 1));
		ConfigUtil.assertFalse(value == 0, "Zero interval");

		TimeUnit unit;

		switch (str.charAt(str.length() - 1)) {
			case 's':
				unit = TimeUnit.SECONDS;
				break;
			case 'm':
				unit = TimeUnit.MINUTES;
				break;
			case 'h':
				unit = TimeUnit.HOURS;
				break;
			case 'd':
				unit = TimeUnit.DAYS;
				break;
			default:
				throw new RuntimeException();
		}

		return unit.toMillis(value);
	}

	public boolean mustCheck(@NonNull Player player) {
		if (this.checkPermission != null && !player.hasPermission(this.checkPermission)) {
			return false;
		}

		return this.bypassPermission == null || !player.hasPermission(this.bypassPermission);
	}

	public String formatMessage(long remains) {
		Matcher matcher = PLACEHOLDER.matcher(this.message);

		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			matcher.appendReplacement(sb, formatTime(matcher, remains));
		}

		matcher.appendTail(sb);

		return sb.toString();
	}

	private static String formatTime(@NonNull Matcher units, long millis) {
		StringBuilder sb = new StringBuilder();

		int unitIndex = 4;

		for (TimeUnit unit : new TimeUnit[] {TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS}) {
			long size = unit.toMillis(1);

			if (millis > size || (sb.length() == 0 && unit == TimeUnit.SECONDS)) {
				sb.append(millis / size).append(' ').append(units.group(unitIndex)).append(' ');

				millis %= size;
			}

			unitIndex--;
		}

		sb.setLength(sb.length() - 1);

		return sb.toString();
	}

}
