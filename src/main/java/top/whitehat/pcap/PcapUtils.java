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
package top.whitehat.pcap;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.nio.ByteOrder.BIG_ENDIAN;
import java.nio.ByteOrder;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import top.whitehat.pcap.PcapLib.in6_addr;
import top.whitehat.pcap.PcapLib.in_addr;

/** Provides utility methods for working with libpcap */
public class PcapUtils {
	
	/** convert string to Inet4Address */
	public static Inet4Address toIpv4(String ip) {
		 try {
			return (Inet4Address) InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e.getMessage() + " " + ip);
		}
	}

	/** Return a copy of an array */
	public static byte[] copy(byte[] array) {
		byte[] clone = new byte[array.length];
		System.arraycopy(array, 0, clone, 0, array.length);
		return clone;
	}

	/** used in hexStringToByteArray() */
	private static final Pattern HEX_STRING_PATTERN = Pattern.compile("\\A([0-9a-fA-F][0-9a-fA-F])+\\z");

	/** Convert hex string to byte array */
	public static byte[] hexStringToBytes(String hexString, String separator) {
		if (hexString == null || separator == null) {
			throw new NullPointerException();
		}

		if (hexString.startsWith("0x")) {
			hexString = hexString.substring(2);
		}

		String noSeparatorHexString;
		if (separator.length() == 0) {
			if (!HEX_STRING_PATTERN.matcher(hexString).matches()) {
				StringBuilder sb = new StringBuilder(100);
				sb.append("invalid hex string(").append(hexString).append("), not match pattern(")
						.append(HEX_STRING_PATTERN.pattern()).append(")");
				throw new IllegalArgumentException(sb.toString());
			}
			noSeparatorHexString = hexString;

		} else {
			StringBuilder patternSb = new StringBuilder(60);
			patternSb.append("\\A[0-9a-fA-F][0-9a-fA-F](").append(Pattern.quote(separator))
					.append("[0-9a-fA-F][0-9a-fA-F])*\\z");
			String patternString = patternSb.toString();

			Pattern pattern = Pattern.compile(patternString);
			if (!pattern.matcher(hexString).matches()) {
				StringBuilder sb = new StringBuilder(150);
				sb.append("invalid hex string(").append(hexString).append("), not match pattern(").append(patternString)
						.append(")");
				throw new IllegalArgumentException(sb.toString());
			}
			noSeparatorHexString = hexString.replaceAll(Pattern.quote(separator), "");
		}

		int arrayLength = noSeparatorHexString.length() / 2;
		byte[] array = new byte[arrayLength];
		for (int i = 0; i < arrayLength; i++) {
			array[i] = (byte) Integer.parseInt(noSeparatorHexString.substring(i * 2, i * 2 + 2), 16);
		}

		return array;
	}

	/** used in toHex() */
	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

	/**
	 * Convert byte array to HEX string
	 * 
	 * @param separator separator
	 * @param offset    index
	 * @param length    length
	 * @param columns   how many chars per row
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

	/** Convert byte array to HEX string */
	public static String toHex(byte[] array, String separator) {
		return toHex(array, separator, 0, -1, -1);
	}

	/**
	 * ​​Validate that the index and length parameters are within the bounds of the array.​
	 *
	 * @param array  byte array
	 * @param index  offset
	 * @param length length
	 * @throws throw exception when index, length is invalid
	 */
	public static void validateBounds(byte[] array, int index, int length) {
		if (array == null) {
			throw new NullPointerException("array must not be null.");
		}

		if (array.length == 0) {
			throw new IllegalArgumentException("array is empty.");
		}

		if (index < 0 || length < 0) {
			throw new IllegalArgumentException("index and length should not be negative.");
		}

		if (index + length > array.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
	}

	/** Read an integer from an array at a specified offset */
	public static int readInt(byte[] array, int offset) {
		return readInt(array, offset, ByteOrder.BIG_ENDIAN);
	}

	/** Read an integer from an array at a specified offset and in a specified byte order.​ */
	public static int readInt(byte[] array, int offset, ByteOrder order) {
		validateBounds(array, offset, 4);
		if (order == null)
			order = BIG_ENDIAN;
		if (order.equals(LITTLE_ENDIAN)) {
			return ((array[offset + 3]) << 24) //
					| ((0xFF & array[offset + 2]) << 16) //
					| ((0xFF & array[offset + 1]) << 8) //
					| ((0xFF & array[offset] << 0));
		} else {
			return ((array[offset]) << 24) //
					| ((0xFF & array[offset + 1]) << 16) //
					| ((0xFF & array[offset + 2]) << 8) //
					| ((0xFF & array[offset + 3]) << 0);
		}
	}

	/** ​Read the specified number of bytes from an array at a specified offset.​ */
	public static byte[] readBytes(byte[] array, int offset, int length) {
		validateBounds(array, offset, length);
		byte[] subArray = new byte[length];
		System.arraycopy(array, offset, subArray, 0, length);
		return subArray;
	}

	/**
	 * Reverse the order of the bytes in an array.​
	 *
	 * @param array array
	 * @return a new array containing specified array's elements in reverse order.
	 */
	public static byte[] reverse(byte[] array) {
		byte[] rarray = new byte[array.length];
		for (int i = 0; i < array.length; i++) {
			rarray[i] = array[array.length - i - 1];
		}
		return rarray;
	}

	/** convert in_addr to InetAddress */
	public static Inet4Address intoInetAddress(in_addr in) {
		if (in == null) {
			return null;
		}
		return intToInetAddress(in.s_addr);
	}

	/** convert integer to byte array */
	public static byte[] intToByteArray(int value, ByteOrder order) {
		if (order.equals(LITTLE_ENDIAN)) {
			return new byte[] { //
					(byte) (value >> 0), //
					(byte) (value >> 8), //
					(byte) (value >> 16), //
					(byte) (value >> 24), //
			};
		} else {
			return new byte[] { //
					(byte) (value >> 24), //
					(byte) (value >> 16), //
					(byte) (value >> 8), //
					(byte) (value >> 0) //
			};
		}
	}

	/** convert integer to InetAddress */
	public static Inet4Address intToInetAddress(int i) {
		try {
			byte[] bytes = intToByteArray(i, PcapLib.NATIVE_BYTE_ORDER);
			return (Inet4Address) Inet4Address.getByAddress(bytes);
		} catch (UnknownHostException e) {
			return null;
		}
	}

	/** convert in6_addr to Inet6Address */
	public static Inet6Address ntoInetAddress(in6_addr in6) {
		try {
			if (in6 != null) {
				return (Inet6Address) InetAddress.getByAddress(in6.s6_addr);
			}
		} catch (UnknownHostException e) {
		}
		return null;
	}

}
