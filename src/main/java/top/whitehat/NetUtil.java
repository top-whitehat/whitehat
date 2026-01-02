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
package top.whitehat;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import top.whitehat.net.DatagramSocket;
import top.whitehat.packet.PacketUtil;
import top.whitehat.tcp.TcpPorts;
import top.whitehat.util.ArrayUtil;
import top.whitehat.util.Timer;

/** Provides utility methods for working with the network */
public class NetUtil {
	// ------------- Methods about IP ---------
	
	public static Inet4Address ANY_IP = (Inet4Address) toInetAddress("0.0.0.0");

	/** Convert string to InetAddress */
	public static InetAddress toInetAddress(String str, InetAddress defaultValue) {
		try {
			if (str == null)
				return defaultValue;
			return InetAddress.getByName(str);
		} catch (UnknownHostException e) {
			return defaultValue;
		}
	}

	/** Convert string to InetAddress */
	public static InetAddress toInetAddress(String str) {
		return toInetAddress(str, null);

	}

	public static List<InetAddress> strToInetAddresses(String ipAndMask) {
		List<InetAddress> result = new ArrayList<InetAddress>();
		NetCardAddress ipRange = NetCardAddress.getByName(ipAndMask);

		// prepare parameters
		int prefixLen = ipRange.getNetworkPrefixLength();
		long rangeIpNumber = NetUtil.ipToLong(ipRange.getAddress());
		long startIpNumber = rangeIpNumber & (0xFFFFFFFFL << (32 - prefixLen));
		long count = 0xFFFFFFFFL >> prefixLen; // ip count
		InetAddress dstAddress;

		// iterate all Ips
		for (int i = 0; i < count + 1; i++) {
			// get a destination address by ipNumber
			long ipNumber = startIpNumber + i;
			dstAddress = NetUtil.longToIp(ipNumber);
			result.add(dstAddress);
		}

		return result;
	}

	/** Convert InetAddress to an integer */
	public static int inetAddressToInt(InetAddress ip) {
		byte[] bytes = ip.getAddress(); // 获取IP地址的字节数组
		int result = 0;
		for (byte b : bytes) {
			// 将每个字节转换为无符号整数并与结果组合
			result = (result << 8) | (b & 0xFF);
		}
		return result;
	}

	/** Convert InetAddress to String */
	public static String ipString(InetAddress addr) {
		return addr == null ? "null" : addr.getHostAddress();
	}

	/** Check whether the specified string is an IPv4 address */
	public static boolean isIpV4(String s) {
		if (s != null && s.indexOf("/") > 0)
			s = s.substring(0, s.indexOf("/"));
		return PacketUtil.isIpV4(s);
	}

	/** Check whether the specified string is an IPv6 address */
	public static boolean isIpV6(String s) {
		return PacketUtil.isIpV6(s);
	}

	/** Check whether the specified address is an IPv4 address */
	public static boolean isIpV4(InetAddress addr) {
		return addr instanceof Inet4Address;
	}

	/** Check whether the specified address is an IPv4 address */
	public static boolean isIpV6(InetAddress addr) {
		return addr instanceof Inet6Address;
	}

	/** Check whether the specified address is an IPv4 address */
	public static boolean isIpV4(InetSocketAddress addr) {
		return isIpV4(addr.getAddress());
	}

	/** Check whether the specified address is an IPv4 address */
	public static boolean isIpV6(InetSocketAddress addr) {
		return isIpV6(addr.getAddress());
	}

	
	/** get IP version, return 4 or 6 */
	public static int ipVersion(InetAddress addr) {
		return isIpV4(addr) ? 4 : 6;
	}
	
	/**
	 * Check if two InetAddresses are in the same subnet with the specified prefix
	 * length
	 *
	 * @param ip1          First IP address
	 * @param ip2          Second IP address
	 * @param prefixLength Subnet mask length (e.g., 24 for IPv4, 64 for IPv6)
	 * @return true if both IPs are in the same subnet, otherwise false
	 * @throws UnknownHostException
	 * @throws IllegalArgumentException if IP versions are different or prefix
	 *                                  length is invalid
	 */
	public static boolean isInSameSubnet(InetAddress ip1, InetAddress ip2, int prefixLength) {
		// Check if both IP addresses are of the same version
		if ((ip1 instanceof Inet4Address && ip2 instanceof Inet6Address)
				|| (ip1 instanceof Inet6Address && ip2 instanceof Inet4Address)) {
			// Both IP addresses must be of the same version (IPv4 or IPv6)
			return false;
		}

		// Validate prefix length based on IP version
		int maxPrefixLength = (ip1 instanceof Inet4Address) ? 32 : 128;
		if (prefixLength < 0 || prefixLength > maxPrefixLength) {
			// Prefix length must be between 0 and 32 for IpV4 or 128 for IpV6
			return false;
		}

		// Calculate network addresses based on IP version
		if (ip1 instanceof Inet4Address) {
			return isInSameIPv4Subnet((Inet4Address) ip1, (Inet4Address) ip2, prefixLength);
		} else {
			return isInSameIPv6Subnet((Inet6Address) ip1, (Inet6Address) ip2, prefixLength);
		}
	}

	/**
	 * Check if two IPv4 addresses are in the same subnet
	 */
	public static boolean isInSameIPv4Subnet(Inet4Address ip1, Inet4Address ip2, int prefixLength) {
		long ip1Long = ipToLong(ip1);
		long ip2Long = ipToLong(ip2);

		long maskLong = getIpV4Mask(prefixLength);

		long network1 = ip1Long & maskLong;
		long network2 = ip2Long & maskLong;

		return network1 == network2;
	}

	/**
	 * Check if two IPv6 addresses are in the same subnet
	 */
	public static boolean isInSameIPv6Subnet(Inet6Address ip1, Inet6Address ip2, int prefixLength) {
		BigInteger ip1BigInt = ipv6ToBigInteger(ip1);
		BigInteger ip2BigInt = ipv6ToBigInteger(ip2);

		BigInteger maskBigInt = getIPv6Mask(prefixLength);

		BigInteger network1 = ip1BigInt.and(maskBigInt);
		BigInteger network2 = ip2BigInt.and(maskBigInt);

		return network1.equals(network2);
	}

	/**
	 * Convert IPv4 address to long value
	 */
	public static long ipToLong(InetAddress ip) {
		byte[] octets = ip.getAddress();
		long result = 0;
		for (byte octet : octets) {
			// Use AND operation with 0xFF to ensure byte is treated as unsigned
			result = (result << 8) | (octet & 0xFF);
		}
		return result;
	}

	/** Convert long value to IPv4 address */
	public static InetAddress longToIp(long n) {
		byte b0 = (byte) ((n & 0xFF000000L) >> 24);
		byte b1 = (byte) ((n & 0x00FF0000L) >> 16);
		byte b2 = (byte) ((n & 0x0000FF00L) >> 8);
		byte b3 = (byte) ((n & 0x000000FFL) >> 0);
		try {
			return InetAddress.getByAddress(new byte[]{b0, b1, b2, b3});
		} catch (UnknownHostException e) {
			return null;
		}
	}
	
	/**
	 * Convert IPv6 address to BigInteger
	 */
	public static BigInteger ipv6ToBigInteger(Inet6Address ip) {
		byte[] bytes = ip.getAddress();
		return new BigInteger(1, bytes); // Using 1 for positive number
	}

	/**
	 * Create IPv4 mask from prefix length
	 */
	public static long getIpV4Mask(int prefix) {
		if (prefix == 0) return 0L;
		return (0xFFFFFFFFL << (32 - prefix)) & 0xFFFFFFFFL;
	}

	/**
	 * Create IPv6 mask from prefix length
	 */
	public static BigInteger getIPv6Mask(int prefixLength) {
		if (prefixLength == 0)
			return BigInteger.ZERO;
		if (prefixLength == 128)
			return new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);

		// Create a mask with the first 'prefixLength' bits set to 1
		BigInteger mask = BigInteger.ZERO;
		for (int i = 0; i < prefixLength; i++) {
			mask = mask.shiftLeft(1).add(BigInteger.ONE);
		}
		mask = mask.shiftLeft(128 - prefixLength);

		return mask;
	}

	/** Whether current OS is MAC OS */
	public static boolean isMacOS() {
		return System.getProperty("os.name").toLowerCase().contains("mac");
	}

	/** Find the local IP address that is connected to the Internet.​ */
	public static InetAddress getInternetLocalAddress() {
		String[] hosts = { "8.8.8.8", "202.96.128.86", "1.1.1.1" }; // Internet address
		int port = 53; // DNS port, for connection detect
		try (DatagramSocket sock = new DatagramSocket()) {
			sock.setSoTimeout(2000); // timeout 2 seconds
			for (String h : hosts) {
				try {
					InetAddress server = InetAddress.getByName(h);
					sock.connect(server, port); // only connect, do not send packet
					InetAddress local = sock.getLocalAddress();
					if (local instanceof Inet4Address //
							&& !local.isLoopbackAddress() //
							&& !local.isAnyLocalAddress()) {
						return local;
					}
				} catch (Exception ignore) {

				}
			}
		} catch (IOException e) {
		}

		// if not found, try find by TCP
		return getInternetLocalAddressByTcp();
	}

	/** Find the local IP address that is connected to the Internet.​ */
	public static InetAddress getInternetLocalAddressByTcp() {
		String[] hosts = { "8.8.8.8", "1.1.1.1" }; // Internet address
		int port = 53; // DNS port, for connection detect
		for (String h : hosts) {
			Socket socket = null;
			try {
				// create TCP socket
				socket = new Socket();
				socket.connect(new InetSocketAddress(h, port), 1000); 
				InetAddress local = socket.getLocalAddress();
				if (local instanceof Inet4Address //
						&& !local.isLoopbackAddress() //
						&& !local.isAnyLocalAddress()) {
					return local;
				}
			} catch (IOException e) {

			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
					}
				}
			}
		}

		return null;
	}

	/**
	 * Calculates the network prefix length from a subnet mask InetAddress For IPv4:
	 * converts dotted-decimal mask to prefix length (e.g., 255.255.255.0 -> 24) For
	 * IPv6: calculates the number of leading 1 bits in the 128-bit mask
	 * 
	 * @param maskAddress The subnet mask as InetAddress
	 * @return The network prefix length (number of leading 1 bits)
	 * @throws IllegalArgumentException if the provided mask is invalid
	 */
	public static int calculatePrefixLength(InetAddress maskAddress) {
		byte[] addressBytes = maskAddress.getAddress();

		// Validate mask format
		if (!isValidSubnetMask(addressBytes)) {
			throw new IllegalArgumentException("Invalid subnet mask format");
		}

		return calculatePrefixFromBytes(addressBytes);
	}

	public static int calculatePrefixLength(String maskAddressStr) {
		try {
			return calculatePrefixLength(InetAddress.getByName(maskAddressStr));
		} catch (UnknownHostException e) {
			return -1;
		}
	}

	/**
	 * Validates if the byte array represents a valid subnet mask A valid mask
	 * should have contiguous 1 bits followed by contiguous 0 bits
	 * 
	 * @param addressBytes The byte array representing the mask
	 * @return true if valid subnet mask, false otherwise
	 */
	private static boolean isValidSubnetMask(byte[] addressBytes) {
		boolean foundZero = false;

		for (int i = 0; i < addressBytes.length; i++) {
			byte currentByte = addressBytes[i];

			// Check each bit in the byte
			for (int bitPos = 7; bitPos >= 0; bitPos--) {
				int bitValue = (currentByte >> bitPos) & 1;

				if (foundZero) {
					// If we found a zero already, all subsequent bits must be zero
					if (bitValue == 1) {
						return false; // Invalid mask: 1 after 0
					}
				} else {
					// Track when we transition from 1 to 0
					if (bitValue == 0) {
						foundZero = true;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Calculates the prefix length by counting leading 1 bits in the mask
	 * 
	 * @param addressBytes The byte array representing the mask
	 * @return The number of contiguous leading 1 bits (prefix length)
	 */
	private static int calculatePrefixFromBytes(byte[] addressBytes) {
		int prefixLength = 0;

		for (int i = 0; i < addressBytes.length; i++) {
			byte currentByte = addressBytes[i];

			// Handle the 0xFF case (all 1s)
			if ((currentByte & 0xFF) == 0xFF) {
				prefixLength += 8;
				continue;
			}

			// Count remaining 1 bits in the current byte
			int byteValue = currentByte & 0xFF;
			for (int bitPos = 7; bitPos >= 0; bitPos--) {
				if ((byteValue >> bitPos & 1) == 1) {
					prefixLength++;
				} else {
					// Found first zero, return the count
					return prefixLength;
				}
			}
		}

		return prefixLength;
	}

	/**
	 * Given an InetAddress and a prefix length (e.g., 24 for IPv4 or 64 for IPv6),
	 * calculate the "broadcast-like" address: - For IPv4: traditional broadcast
	 * address (host bits all 1) - For IPv6: address with host bits all set to 1
	 * (not a real broadcast, but similar in concept)
	 *
	 * @param address      The input IP address (IPv4 or IPv6)
	 * @param prefixLength The CIDR prefix length (e.g., 24, 64)
	 * @return The "broadcast-like" InetAddress, or null if failed
	 */
	public static InetAddress getBroadcast(InetAddress address, int prefixLength) {
		if (address == null) {
			return null;
		}

		try {
			byte[] bytes = address.getAddress();
			int byteCount = bytes.length; // 4 for IPv4, 16 for IPv6

			if (prefixLength < 0 || prefixLength > (byteCount * 8)) {
				throw new IllegalArgumentException("Prefix length must be between 0 and " + (byteCount * 8));
			}

			byte[] result = new byte[byteCount];

			// Make a copy of the original address
			System.arraycopy(bytes, 0, result, 0, byteCount);

			int hostBitsStart = prefixLength;
			int byteIdx = hostBitsStart / 8;
			int bitWithinByte = hostBitsStart % 8;

			if (byteIdx < byteCount) {
				// Preserve the network portion (prefix)
				// For the host portion (after prefix), set bits to 1

				// First, copy the unchanged prefix bytes
				for (int i = 0; i < byteIdx; i++) {
					result[i] = bytes[i];
				}

				if (byteIdx < byteCount) {
					// Handle the first byte of the host part
					if (bitWithinByte > 0) {
						// Keep the first (bitWithinByte) bits, set the rest to 1
						byte keepMask = (byte) (0xFF << (8 - bitWithinByte));
						byte setMask = (byte) (0xFF >>> bitWithinByte);
						result[byteIdx] = (byte) ((bytes[byteIdx] & keepMask) | setMask);
					} else {
						// If no bits to keep, set the whole byte to 1
						result[byteIdx] = (byte) 0xFF;
					}

					// Set all remaining bytes (host part) to 1
					for (int i = byteIdx + 1; i < byteCount; i++) {
						result[i] = (byte) 0xFF;
					}
				}
			}

			return InetAddress.getByAddress(result);

		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Check whether specified port is available
	 * 
	 * @param port The port to inspect
	 * @return
	 */
	public static boolean isPortAvailable(int port) {
		try (ServerSocket ignored = new ServerSocket(port)) {
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Check whether the given InetAddress is a public IP address (no loopback , no
	 * private)
	 *
	 * @param inetAddress The InetAddress object to inspect
	 * @return true if it's a local network address, false otherwise
	 */
	public static boolean isPublicIPAddress(InetAddress inetAddress) {
		return !inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()
				&& !inetAddress.isSiteLocalAddress()
				&& !(inetAddress instanceof Inet6Address && inetAddress.isAnyLocalAddress())
				&& !isLocalNetworkAddress(inetAddress);
	}

	/**
	 * Check whether the given InetAddress is a local (private) IP address.
	 *
	 * @param inetAddress The InetAddress object to inspect
	 * @return true if it's a local network address, false otherwise
	 */
	public static boolean isLocalNetworkAddress(InetAddress inetAddress) {
		if (inetAddress == null) {
			return false;
		}

		// 1. Check if it's a loopback address (e.g., 127.0.0.1)
		if (inetAddress.isLoopbackAddress()) {
			return true;
		}

		// 2. Check if it's a link-local address (e.g., 169.254.x.x)
		if (inetAddress.isLinkLocalAddress()) {
			return true;
		}

		// 3. For IPv4, manually check against private IP ranges
		byte[] addr = inetAddress.getAddress();
		if (addr == null || addr.length == 0) {
			return false;
		}

		// Handle IPv4
		if (inetAddress instanceof java.net.Inet4Address) {
			return isPrivateIPv4Address(addr);
		}

		// For IPv6, check site-local or link-local (optional, usually also considered
		// local)
		if (inetAddress.isSiteLocalAddress() || inetAddress.isLinkLocalAddress()) {
			return true;
		}

		return false;
	}

	/**
	 * Check if the given IPv4 address (as a byte array) is within private IP
	 * ranges.
	 */
	private static boolean isPrivateIPv4Address(byte[] addr) {
		if (addr.length != 4) {
			return false;
		}

		int firstOctet = addr[0] & 0xFF;
		int secondOctet = addr[1] & 0xFF;

		// 10.0.0.0/8
		if (firstOctet == 10) {
			return true;
		}

		// 172.16.0.0/12 → 172.16 to 172.31
		if (firstOctet == 172 && (secondOctet >= 16 && secondOctet <= 31)) {
			return true;
		}

		// 192.168.0.0/16
		if (firstOctet == 192 && secondOctet == 168) {
			return true;
		}

		return false;
	}

	/** Check whether the specified IP is reachable */
	public static boolean isReachable(String ip, int timeout) {
		try {
			InetAddress gateway = InetAddress.getByName(ip);
			return gateway.isReachable(timeout);
		} catch (Exception e) {
			return false;
		}
	}

	/** ​​Check whether the specified IP and port can be connected to.​ */
	public static boolean isConnectable(String ip, int port, int timeout) {
		try (Socket socket = new Socket()) {
			// try connect
			socket.connect(new InetSocketAddress(ip, port), timeout);
			return true; // if connected, the address is reachable
		} catch (IOException e) {
			return false; // if not connected, the network is not OK
		}
	}

	// ------------- Methods about port ---------

	/** Check whether the string is a port number */
	public static boolean isPort(String s) {
		try {
			int n = Integer.parseInt(s);
			return (n > 0) && (n < 65535);
		} catch (Exception e) {
			return false;
		}
	}

	/** collect integer from set, save to result list */
	private static void collectFromSet(Set<Integer> set, List<Integer> result) {
		Iterator<Integer> iterator = set.iterator();
		while (iterator.hasNext()) {
			result.add(iterator.next());
		}
	}

	/** Check whether the string is a port range description, such as: 80-99 */
	private static boolean isPortRange(String s) {
		return parsePortRange(s) != null;
	}

	/** Convert a port range description, such as: 80-99, to a port integer array */
	private static int[] parsePortRange(String s) {
		String words[] = s.split("-");
		if (words.length != 2)
			return null;

		try {
			int[] result = new int[2];
			result[0] = Integer.parseInt(words[0]);
			result[1] = Integer.parseInt(words[1]);
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	/** parse port description to a List of port integer */
	public static List<Integer> strToPorts(String portDescription) {
		List<Integer> result = new ArrayList<Integer>();

		String[] items = portDescription.split(",");
		for (int i = 0; i < items.length; i++) {
			String item = items[i].trim();

			switch (item.toLowerCase()) {
			case "common":
				collectFromSet(TcpPorts.value2Name.keySet(), result);
				break;
			case "fast":
				ArrayUtil.collectFromArray(TcpPorts.MOST_COMMON, result);
				break;
			default:
				if (isPortRange(item)) {
					int[] range = parsePortRange(item);
					for (int p = range[0]; p <= range[1]; p++) {
						result.add(p);
					}
				} else if (isPort(item)) {
					result.add(Integer.parseInt(item));
				} else {
					throw new IllegalArgumentException(item + " is not a port description");
				}
				break;
			}
		}

		return result;
	}

	// ------------- byte array <=> HEX ---------

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
		return PacketUtil.toHex(array, separator, offset, length, columns);
	}

	/** Convert bytes to HEX string */
	public static String toHex(byte[] array, String separator) {
		return PacketUtil.toHex(array, separator);
	}

	/** Convert bytes to HEX string */
	public static String toHex(byte[] array) {
		return toHex(array, " ");
	}

	/** Check whether the specified character is a hex digit.​ */
	private static boolean isHexChar(char c) {
		if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))
			return true;
		return false;
	}

	/** Convert hex string to byte array */
	public static byte[] toBytes(String hexStr) {
		List<String> numbers = new ArrayList<String>();
		int index = 0;
		String b = "";

		// convert hexStr to number string list
		while (index < hexStr.length()) {
			char c = hexStr.charAt(index++);
			if (b.length() == 0) {
				switch (c) {
				case '\t':
				case ' ':
				case '\r':
				case '\n':
				case ',':
				case ':':
				case '-':
					break;
				default:
					if (isHexChar(c)) {
						b += c;
					} else {
						throw new IllegalArgumentException("invalid hex char at " + index);
					}
				}
			} else if (b.length() == 1) {
				switch (c) {
				case '\t':
				case ' ':
				case '\r':
				case '\n':
				case ':':
				case '-':
					break;
				case 'x':
				case 'X':
					if ("0".equals(b)) {
						b = "";
						continue;
					} else {
						throw new IllegalArgumentException("invalid hex char at " + index);
					}
				default:
					if (isHexChar(c)) {
						b += c;
					} else {
						throw new IllegalArgumentException("invalid hex char at " + index);
					}
					break;
				}
				numbers.add(b);
				b = "";
			}
		}

		// process the last single char
		if (b.length() == 1 && isHexChar(b.charAt(0))) {
			numbers.add(b);
		}

		// convert list of numbers to byte[]
		byte[] bytes = new byte[numbers.size()];
		for (int i = 0; i < numbers.size(); i++) {
			bytes[i] = (byte) Integer.parseInt(numbers.get(i), 16);
		}
		return bytes;
	}

	// ------------- Others ---------

	/** print objects */
	public static void print(Object... args) {
		int count = 0;
		for (Object arg : args) {
			if (count++ > 0)
				System.out.print(" ");

			if (arg instanceof byte[]) {
				// print the byte array
				byte[] data = (byte[]) arg;
				for (int i = 0; i < data.length; i++) {
					print(String.format("%02X ", data[i]));
				}

			} else {
				System.out.print(arg == null ? "null" : arg.toString());
			}
		}
	}

	/** print objects and new line */
	public static void println(Object... args) {
		print(args);
		print("\r\n");
	}

	/** print byte array */
	public static void printBytes(String prompt, byte[] data, int offset) {
		if (prompt != null)
			print(prompt + "(" + data.length + "): ");

		for (int i = offset; i < data.length; i++) {
			print(String.format("%02X ", data[i]));
		}

		println("");
	}

	/** 判断 s 是否可以转换为 boolean */
	public static boolean isBoolean(String s) {
		try {
			Boolean.valueOf(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static int toInt(String s, int defaultValue) {
		try {
			return Integer.valueOf(s);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/** 判断 s 是否可以转换为 integer */
	public static boolean isInteger(String s) {
		try {
			Integer.valueOf(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/** 判断 s 是否可以转换为 double */
	public static boolean isDouble(String s) {
		try {
			Double.valueOf(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// ------------- JavaScript like ---------

	private static final Random RANDOM = new Random();

	/** Create a random integer in [minValue, maxValue] */
	public static int randomInt(int minValue, int maxValue) {
		return RANDOM.nextInt((maxValue - minValue) + 1) + minValue;
	}

	/** Create a random integer */
	public static int randomInt() {
		return RANDOM.nextInt();
	}

	/**
	 * Execute a function after a specified delay (in milliseconds). <br>
	 * This has the same functionality as the setTimeout() function in JavaScript.​
	 * 
	 * @param task              the runnable task
	 * @param delayMilliseconds delay time (in milliseconds)
	 * @return return an id which can used in clearTimeout()
	 */
	public static int setTimeout(Runnable task, long delayMilliseconds) {
		return Timer.setTimeout(task, delayMilliseconds);
	}
	
	/**
	 * Cancel a timeout task that previously established by calling setTimeout() or setInterval(). <br>
	 * This has the same functionality as the clearTimeout() function in JavaScript.​
	 * 
	 * @return return true if the id exists and removed.
	 */
	public static boolean clearTimeout(int id) {
		return Timer.clearTimeout(id);
	}
	
	/** sleep a while */
	public static void sleep(long milliseconds) {
		Timer.sleep(milliseconds);
	}
	
	/**
	 * Execute a function periodically . <br>
	 * 
	 * This has the same functionality as the setInterval() function in JavaScript.​
	 * 
	 * @param task              the runnable task
	 * @param delayMilliseconds delay time (in milliseconds)
	 * @return return an id which can used in clearTimeout()
	 */
	public static int setInterval(Runnable task, long delayMilliseconds) {
		return Timer.setInterval(task, delayMilliseconds);
	}
	
	/**
	 * Execute a function periodically . <br>
	 * 
	 * Submits a periodic action that becomes enabled first after the given initial delay, 
	 * and subsequently with the given period
	 *  
	 * 
	 * @param task              the runnable task
	 * @param initialDelay      the time to delay first execution
	 * @param period      		the period between successive executions (in milliseconds)
	 * @return return an id which can used in clearTimeout()
	 */
	public static int run(Runnable task, long initialDelay, long period) {
		return Timer.run(task, initialDelay, period);
	}
	
	/** Run a task in thread */
	public static void runThread(Runnable task) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(task);
		executor.shutdown();
	}

}
