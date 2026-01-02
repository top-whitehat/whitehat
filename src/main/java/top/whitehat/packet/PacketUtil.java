/*
 * Copyright 2026 The WhiteHat Project
 *
 * The WhiteHat Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package top.whitehat.packet;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.regex.Pattern;

import static java.nio.ByteOrder.BIG_ENDIAN;

/** Utilities */
public class PacketUtil {

	/** The byte order that is being used in this class, default is BIG_ENDIAN */
	public static ByteOrder byteOrder = BIG_ENDIAN;
	
	/** used in isIpV4() */
	private static final Pattern IPV4_PATTERN = Pattern
			.compile("^(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)\\." + "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)\\."
					+ "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)\\." + "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)$");

	/** Checks whether the specified string is a valid IPv4 address. */
	public static boolean isIpV4(String s) {
		if (s != null && IPV4_PATTERN.matcher(s).matches()) {
			try {
				Inet4Address.getByName(s);
				return true;
			} catch (UnknownHostException e) {
			}
		}
		return false;
	}

	/** Checks whether the specified string is a valid IPv6 address. */
	public static boolean isIpV6(String s) {
		try {
			Inet6Address.getByName(s);
			return true;
		} catch (UnknownHostException e) {
		}
		return false;
	}

	/** Checks whether the specified InetAddress is an IPv4 address. */
	public static boolean isIpV4(InetAddress addr) {
		return addr.getAddress().length == 4;
	}

	/** Checks whether the specified InetAddress is an IPv6 address. */
	public static boolean isIpV6(InetAddress addr) {
		return addr.getAddress().length == 16;
	}

	/** Convert number to InetAddress */
	public static InetAddress toInetAddress(int b1, int b2, int b3, int b4) {
		try {
			return InetAddress.getByAddress(new byte[] { (byte) b1, (byte) b2, (byte) b3, (byte) b4 });
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("invalid IP address");
		}
	}

	/** Convert the IP string into an InetAddress object */
	public static InetAddress toInetAddress(String ipStr) {
		InetAddress addr;
		try {
			addr = InetAddress.getByName(ipStr);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException(ipStr + " is not a IP");
		}
		return addr;
	}

	/** Convert the IP string into an Inet4Address object */
	public static Inet4Address toInet4Address(byte[] bytes) {
		try {
			return (Inet4Address) Inet4Address.getByAddress(bytes);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException();
		}
	}

	/** An Inet4Address object of 0.0.0.0 */
	public static Inet4Address anyInet4Address = toInet4Address(new byte[] { 0, 0, 0, 0 });

	/** An Inet4Address object of local host 127.0.0.1 */
	public static Inet4Address ipLocalHost = toInet4Address(new byte[] { 127, 0, 0, 1 });

	/** Read an uint16 integer from the byte array at specified offset */
	public static int readUInt16(byte[] array, int offset) {
		if (BIG_ENDIAN.equals(byteOrder))
			return ((array[offset + 0] & 0xFF) << 8) | (array[offset + 1] & 0xFF);
		else
			return ((array[offset + 1] & 0xFF) << 8) | (array[offset + 0] & 0xFF);
	}

	/** Write an uint16 integer to byte array at specified offset */
	public static void writeUInt16(byte[] array, int offset, int b) {
		if (BIG_ENDIAN.equals(byteOrder)) {
			array[offset + 1] = (byte) (0xFF & b);
			array[offset + 0] = (byte) ((0xFF00 & b) >> 8);
		} else {
			array[offset + 0] = (byte) (0xFF & b);
			array[offset + 1] = (byte) ((0xFF00 & b) >> 8);
		}
	}

	/** Read an uint32 integer from the byte array at specified offset */
	public static long readUInt32(byte[] array, int offset) {
		if (BIG_ENDIAN.equals(byteOrder)) {
			return (array[offset + 0] << 24) | ((0xFFL & array[offset + 1]) << 16) | ((0xFFL & array[offset + 2]) << 8)
					| ((0xFFL & array[offset + 3]) << 0);
		} else {
			return (array[offset + 3] << 24) | ((0xFFL & array[offset + 2]) << 16) | ((0xFFL & array[offset + 1]) << 8)
					| ((0xFFL & array[offset + 0]) << 0);
		}
	}

	/** Write an uint32 integer to the byte array at specified offset */
	public static void writeUInt32(byte[] array, int offset, long b) {
		if (BIG_ENDIAN.equals(byteOrder)) {
			array[offset + 3] = (byte) (0xFF & b);
			array[offset + 2] = (byte) ((0xFF00 & b) >> 8);
			array[offset + 1] = (byte) ((0xFF0000 & b) >> 16);
			array[offset + 0] = (byte) ((0xFF000000 & b) >> 24);
		} else {
			array[offset + 0] = (byte) (0xFF & b);
			array[offset + 1] = (byte) ((0xFF00 & b) >> 8);
			array[offset + 2] = (byte) ((0xFF0000 & b) >> 16);
			array[offset + 3] = (byte) ((0xFF000000 & b) >> 24);
		}
	}

	/** Read an int64 integer from the byte at specified offset */
	public static long readInt64(byte[] array, int index) {
		if (BIG_ENDIAN.equals(byteOrder)) {
			return (array[index + 7] << 56) | ((0xFFL & array[index + 6]) << 48) | ((0xFFL & array[index + 5]) << 40)
					| ((0xFFL & array[index + 4]) << 32) | ((0xFFL & array[index + 3]) << 24)
					| ((0xFFL & array[index + 2]) << 16) | ((0xFFL & array[index + 1]) << 8)
					| ((0xFFL & array[index + 0]) << 0);
		} else {
			return (array[index + 0] << 56) | ((0xFFL & array[index + 1]) << 48) | ((0xFFL & array[index + 2]) << 40)
					| ((0xFFL & array[index + 3]) << 32) | ((0xFFL & array[index + 4]) << 24)
					| ((0xFFL & array[index + 5]) << 16) | ((0xFFL & array[index + 6]) << 8)
					| ((0xFFL & array[index + 7]) << 0);
		}
	}

	/** Write an int64 integer to the byte array at specified offset */
	public static void writeInt64(byte[] array, int index, long b) {
		if (BIG_ENDIAN.equals(byteOrder)) {
			array[index + 7] = (byte) (0xFF & b);
			array[index + 6] = (byte) ((0xFF00L & b) >> 8);
			array[index + 5] = (byte) ((0xFF0000L & b) >> 16);
			array[index + 4] = (byte) ((0xFF000000L & b) >> 24);
			array[index + 3] = (byte) ((0xFF00000000L & b) >> 32);
			array[index + 2] = (byte) ((0xFF0000000000L & b) >> 40);
			array[index + 1] = (byte) ((0xFF000000000000L & b) >> 48);
			array[index + 0] = (byte) ((0xFF00000000000000L & b) >> 56);
		} else {
			array[index + 0] = (byte) (0xFF & b);
			array[index + 1] = (byte) ((0xFF00L & b) >> 8);
			array[index + 2] = (byte) ((0xFF0000L & b) >> 16);
			array[index + 3] = (byte) ((0xFF000000L & b) >> 24);
			array[index + 4] = (byte) ((0xFF00000000L & b) >> 32);
			array[index + 5] = (byte) ((0xFF0000000000L & b) >> 40);
			array[index + 6] = (byte) ((0xFF000000000000L & b) >> 48);
			array[index + 7] = (byte) ((0xFF00000000000000L & b) >> 56);
		}
	}

	/** XOR calculation on two byte array, return a new created byte array */
	public static byte[] xor(byte[] arr1, byte[] arr2) {
		if (arr1 == null) {
			throw new NullPointerException("arr1 must not be null.");
		}
		if (arr2 == null) {
			throw new NullPointerException("arr2 must not be null.");
		}
		if (arr1.length != arr2.length) {
			throw new IllegalArgumentException("arr1.length must equal to arr2.length.");
		}

		byte[] result = new byte[arr1.length];
		for (int i = 0; i < arr1.length; i++) {
			result[i] = (byte) (arr1[i] ^ arr2[i]);
		}

		return result;
	}

	/** Connect tow byte array, return a new created byte array */
	public static byte[] concatenate(byte[] arr1, byte[] arr2) {
		byte[] result = new byte[arr1.length + arr2.length];
		System.arraycopy(arr1, 0, result, 0, arr1.length);
		System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
		return result;
	}

	/**
	 * List the static fields of specified class, and stores the name–value pairs in
	 * the provided Maps
	 * 
	 * @param clazz         The class that has static fields.
	 * @param name2ValueMap The map that stores name-value pairs
	 * @param value2NameMap The map that stores value-name pairs map
	 */
	public static void listFields(Class<?> clazz, Map<String, Integer> name2ValueMap,
			Map<Integer, String> value2NameMap) {
		listFields(clazz, null, name2ValueMap, value2NameMap);
	}

	/**
	 * Lists the static fields of the specified class whose names match the given
	 * prefix, and stores the name–value pairs in the provided Maps
	 * 
	 * @param clazz         The class that has static fields.
	 * @param name2ValueMap The map that stores name-value pairs
	 * @param value2NameMap The map that stores value-name pairs map
	 */
	public static void listFields(Class<?> clazz, String prefix, Map<String, Integer> name2ValueMap,
			Map<Integer, String> value2NameMap) {

		Field[] fields = clazz.getDeclaredFields();

		// iterate each field
		for (Field field : fields) {
			int modifiers = field.getModifiers();
			if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
				String fieldName = field.getName();
				// if prefix is specified
				if (prefix != null && prefix.length() > 0) {
					if (!fieldName.startsWith(prefix))
						continue;
				}
				// if field data type match
				Class<?> fieldType = field.getType();
				if (fieldType.equals(int.class)) {
					try {
						int value = field.getInt(clazz);
						if (name2ValueMap != null)
							name2ValueMap.put(fieldName, value);
						if (value2NameMap != null)
							value2NameMap.put(value, fieldName);
					} catch (Exception e) {
					}
				}
			}
		}
	}

	/** used in toHex() */
	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

	/**
	 * Validates that the specified offset and length are within the array bounds.
	 *
	 * @param array  The byte array
	 * @param offset The offset
	 * @param len    The length
	 * @throws RunTimeException when out of bounds
	 */
	public static void validateBounds(byte[] array, int offset, int len) {
		if (array == null) {
			throw new NullPointerException("array must not be null.");
		}

		if (array.length == 0) {
			throw new IllegalArgumentException("array is empty.");
		}

		if (len == 0) {
			StringBuilder sb = new StringBuilder(100);
			sb.append("length is zero. offset: ").append(offset).append(", arr: ").append(toHex(array, ""));
			throw new RuntimeException(sb.toString());
		}

		if (offset < 0 || len < 0 || offset + len > array.length) {
			StringBuilder sb = new StringBuilder(100);
			sb.append("arr.length: ").append(array.length).append(", offset: ").append(offset).append(", len: ")
					.append(len).append(", arr: ").append(toHex(array, ""));
			throw new ArrayIndexOutOfBoundsException(sb.toString());
		}
	}

	/**
	 * Convert the byte array to HEX string
	 * 
	 * @param array     The byte array
	 * @param separator Separator chars between bytes
	 * @param offset    The start index of the first byte to convert
	 * @param length    The length of bytes to convert
	 * @param columns   Insert a line break when a line reaches the specified column
	 *                  width.
	 * 
	 * @return hex string
	 */
	public static String toHex(byte[] array, String separator, int offset, int length, int columns) {
		if (offset < 0)
			offset = array.length + offset;
		if (length == -1)
			length = array.length - offset;
		validateBounds(array, offset, length);
		StringBuilder sb = new StringBuilder();

		boolean hasSep = separator != null && separator.length() > 0;
		int col = 0;
		for (int i = 0; i < length; i++) {
			int v = array[offset + i] & 0xFF;
			sb.append(HEX_CHARS[v >>> 4]).append(HEX_CHARS[v & 0x0F]);
			if (hasSep && i < length - 1)
				sb.append(separator);
			col++;
			if (columns > 0) {
				if (col >= columns) {
					sb.append("\r\n");
					col = 0;
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Convert the byte array to HEX string
	 * 
	 * @param array     The byte array
	 * @param separator Separator chars between bytes
	 * @return hex string
	 */
	public static String toHex(byte[] array, String separator) {
		return toHex(array, separator, 0, -1, -1);
	}

	/**
	 * Pads the input string with space characters if its length is less than the
	 * specified length.
	 * 
	 * @param input     The string to pad
	 * @param length    The target length
	 * @param direction Padding direction: 1 means add chars at the right, -1 means
	 *                  at the left, 0 means at both side.
	 * @return The padded string
	 */
	public static String padding(String input, int length, int direction) {
		if (input.length() >= length) {
			return input;
		}

		int paddingSize = length - input.length();
		int prefix = 0;
		int suffix = 0;

		StringBuilder sb = new StringBuilder();
		if (direction == 0) {
			// padding both
			prefix = paddingSize / 2;
			suffix = paddingSize - prefix;
		} else if (direction > 0) {
			// padding right
			prefix = 0;
			suffix = paddingSize;

		} else {
			// padding left
			prefix = paddingSize;
			suffix = 0;
		}

		for (int i = 0; i < prefix; i++)
			sb.append(' ');
		sb.append(input);
		for (int i = 0; i < suffix; i++)
			sb.append(' ');

		return sb.toString();
	}

	/**
	 * Pads the input string with space characters if its length is less than the
	 * specified length.
	 * 
	 * @param input  The string to pad
	 * @param length The target length
	 * @return The padded string
	 */
	public static String padding(String input, int length) {
		return padding(input, length, 1);
	}

}
