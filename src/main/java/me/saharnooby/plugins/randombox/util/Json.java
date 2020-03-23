package me.saharnooby.plugins.randombox.util;

import lombok.NonNull;

import java.util.List;
import java.util.Map;

public final class Json {

	public static String toJson(@NonNull Map map) {
		StringBuilder out = new StringBuilder();
		toJson(map, out);
		return out.toString();
	}

	private static void toJson(Object object, @NonNull StringBuilder out) {
		if (object instanceof Map) {
			out.append('{');

			Map map = (Map) object;

			int index = 0;

			for (Object key : map.keySet()) {
				if (!(key instanceof String)) {
					throw new IllegalArgumentException("Map keys must be strings");
				}

				out.append('"');
				escapeJsonString((String) key, out);
				out.append('"');
				out.append(':');

				toJson(map.get(key), out);

				if (++index != map.size()) {
					out.append(',');
				}
			}

			out.append('}');
		} else if (object instanceof List) {
			out.append('[');

			List list = (List) object;

			int index = 0;

			for (Object item : list) {
				toJson(item, out);

				if (++index != list.size()) {
					out.append(',');
				}
			}

			out.append(']');
		} else if (object instanceof String) {
			out.append('"');
			escapeJsonString((String) object, out);
			out.append('"');
		} else if (object instanceof Number) {
			Number number = (Number) object;

			double value = number.doubleValue();

			if (!Double.isFinite(value)) {
				throw new IllegalArgumentException("JSON can't contain NaN or Infinity");
			}

			if (number.doubleValue() == number.longValue()) {
				out.append(number.longValue());
			} else {
				out.append(number.doubleValue());
			}
		} else if (object instanceof Boolean || object == null) {
			out.append(object);
		} else {
			throw new IllegalArgumentException("JSON can't contain an element of type " + object.getClass());
		}
	}

	private static void escapeJsonString(@NonNull String string, @NonNull StringBuilder out) {
		if (string.isEmpty()) {
			return;
		}

		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);

			switch (c) {
				case '\"':
					out.append('\\').append('\"');
					break;
				case '\\':
					out.append('\\').append('\\');
					break;
				case '\b':
					out.append('\\').append('b');
					break;
				case '\t':
					out.append('\\').append('t');
					break;
				case '\n':
					out.append('\\').append('n');
					break;
				case '\f':
					out.append('\\').append('f');
					break;
				case '\r':
					out.append('\\').append('r');
					break;
				default:
					if (c > 0xFFF) {
						out.append("\\u").append(hex(c));
					} else if (c > 0xFF) {
						out.append("\\u0").append(hex(c));
					} else if (c > 0x7F) {
						out.append("\\u00").append(hex(c));
					} else if (c < 32) {
						if (c > 0xF) {
							out.append("\\u00").append(hex(c));
						} else {
							out.append("\\u000").append(hex(c));
						}
					} else {
						out.append(c);
					}

					break;
			}
		}
	}

	private static String hex(char c) {
		return Integer.toHexString(c);
	}

}
