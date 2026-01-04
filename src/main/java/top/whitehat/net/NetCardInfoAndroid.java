package top.whitehat.net;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.sun.jna.Platform;

import top.whitehat.LAN;
import top.whitehat.NetCard;
import top.whitehat.NetCardAddress;
import top.whitehat.NetUtil;
import top.whitehat.packet.MacAddress;
import top.whitehat.util.CommandLine;
import top.whitehat.util.FileUtil;
import top.whitehat.util.Text;

/**
* Read network information in Android (TODO: not finished and tested)
*/
public class NetCardInfoAndroid {

	/** Retrieve DNS servers */
	public static List<String> getDnsServers() {
		List<String> dnsServers = new ArrayList<>();

		try {
			// read information from the file: /etc/resolv.conf
			Text text = Text.readFile("/etc/resolv.conf").grep("nameserver").split(" \t");
			for (int i = 0; i < text.rows(); i++) {
				String ip = text.cell(i, 1);
				if (NetUtil.isIpV4(ip) || NetUtil.isIpV6(ip))
					dnsServers.add(ip);
			}
		} catch (Exception e) {

		}
		return dnsServers;
	}

	/** Retrieve information of all network interface cards */
	public static List<NetCard> list() {
		List<NetCard> ret = new ArrayList<NetCard>();

		// get gateway ip and its mac
		String gatewayIP = getGatewayIP();
		String macAddress = getMACAddress(gatewayIP);

		InetAddress gatewayAddress;
		MacAddress gatewayMac = MacAddress.getByName(macAddress);

		try {
			gatewayAddress = InetAddress.getByName(gatewayIP);
			LAN.putMac(gatewayAddress, gatewayMac);
		} catch (Exception e) {
		}

		// get info from NetworkInterface
		try {
			Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
			while (nifs.hasMoreElements()) {
				NetworkInterface ni = nifs.nextElement();

				try {
					if (ni.isUp() && !ni.isLoopback()) {
						NetCardInfo info = new NetCardInfo();
						info.name(ni.getName());
						info.displayName(ni.getDisplayName());
						MacAddress mac = null;
						// try get MacAdress
						if (ni.getHardwareAddress() != null) {
							mac = MacAddress.getByAddress(ni.getHardwareAddress());
						}
						
						// search ip address
						List<InterfaceAddress> addrs = ni.getInterfaceAddresses();
						for (InterfaceAddress addr : addrs) {
							NetCardAddress nAddr = new NetCardAddress(addr.getAddress(), addr.getNetworkPrefixLength());
							info.getNetcardAddresses().add(nAddr);
							
							// try get MacAdress
							if (mac == null && nAddr.getAddress() instanceof Inet4Address) {
								String macStr = getMACAddress(nAddr.getAddress().getHostAddress());
								if (macStr != null) {
									mac = MacAddress.getByName(macStr);
								}
							}
						}

						if (mac == null) {
							// drop the network card that has no MAC address
							// System.out.println("no mac address for " + info);
							
						} else {
							// accept the network card that has MAC address
							info.mac(mac);
							try {
								InetAddress addr = InetAddress.getByName(gatewayIP);
								info.putGateWay(addr);
							} catch (Exception e) {
							}
							ret.add(info);
						}
					}
				} catch (SocketException e) {
					// "Error getting interface info: " + e.getMessage());
				}
			}

		} catch (SocketException e) {
			// "Error listing network interfaces: " + e.getMessage());
		}

		return ret;
	}

	/**
	 * Gets the default gateway IP address by reading /proc/net/route file
	 * 
	 * @return Gateway IP address as string, or null if not found
	 */
	public static String getGatewayIP() {
		String str = "";
		if (Platform.isAndroid()) {
			str = CommandLine.run("route").getOutput();
			System.out.println("Run Command route");
			System.out.println(str);
			System.out.println("==========");
		} else {
			try {
				str = FileUtil.loadFromFile("d:\\route.txt");
			} catch (IOException e) {
			}
		}
		System.out.println("--- table ----");
		System.out.println( Text.of(str).delete("table").split(" \t"));
		String ret = Text.of(str).delete("table").split(" \t").cell(1, 1);
		System.out.println("==========");
		return ret == null ? ret : ret.trim();
	}

	/**
	 * Gets the MAC address for a given IP address by reading /proc/net/arp
	 * 
	 * @param ipAddress The IP address to find MAC for
	 * @return MAC address as string in XX:XX:XX:XX:XX:XX format, or null if not
	 *         found
	 */
	public static String getMACAddress(String ipAddress) {
//		ipAddress = "192.168.100.1";
		String str = "";
		if (Platform.isAndroid()) {
			str = CommandLine.run("arp", "-n", ipAddress).getOutput();
		} else {
			try {
				str = FileUtil.loadFromFile("d:\\arp.txt");
			} catch (IOException e) {
			}
		}
		Text t = Text.of(str).delete("no entry").filter(ipAddress).split(" \t");
		System.out.println(t);
		String ret = t.rows() == 0 ? null : t.cell(0, 2);
		if(ret == null && "0.0.0.0".equals(ipAddress)) ret = MacAddress.BROADCAST.toString();
		return ret;
	}

}
