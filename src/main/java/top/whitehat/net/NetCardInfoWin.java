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
package top.whitehat.net;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

import top.whitehat.NetCard;
import top.whitehat.NetCardAddress;
import top.whitehat.NetUtil;
import top.whitehat.packet.DataLinkType;
import top.whitehat.packet.MacAddress;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Retrieve network interface card information on Windows system */
public class NetCardInfoWin {

	/**
	 * Utility class for retrieving network adapter information on Windows systems
	 * Uses JNA (Java Native Access) to call Windows IP Helper API functions
	 * Provides details about network adapters including MAC addresses, IP
	 * addresses, and gateway information
	 * 
	 */

	// Define IP_ADDRESS_STRING structure (corrected version)
	protected static class IP_ADDRESS_STRING extends Structure {
		public byte[] String = new byte[16]; // Character array storing the actual IP address

		public IP_ADDRESS_STRING() {
			super();
		}

		public IP_ADDRESS_STRING(Pointer p) {
			super(p);
			read();
		}

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("String");
		}

		/**
		 * Get the IP address as a formatted string
		 * 
		 * @return Trimmed IP address string
		 */
		public String getIpAddressString() {
			return new String(this.String).trim();
		}
	}

	// Define IP_MASK_STRING structure (same as IP_ADDRESS_STRING)
	protected static class IP_MASK_STRING extends Structure {
		public byte[] String = new byte[16];

		public IP_MASK_STRING() {
			super();
		}

		public IP_MASK_STRING(Pointer p) {
			super(p);
			read();
		}

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("String");
		}

		public String getIpAddressString() {
			return new String(this.String).trim();
		}
	}

	// IP_ADDR_STRING structure definition
	protected static class IP_ADDR_STRING extends Structure {
		public Pointer Next; // Pointer to the next IP_ADDR_STRING node
		public IP_ADDRESS_STRING IpAddress; // IP address structure (corrected: uses structure instead of byte[])
		public IP_MASK_STRING IpMask; // Subnet mask structure (corrected: uses structure instead of byte[])
		public int Context; // Context identifier

		public IP_ADDR_STRING() {
			super();
		}

		public IP_ADDR_STRING(Pointer p) {
			super(p);
			read();
		}

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("Next", "IpAddress", "IpMask", "Context");
		}

		/**
		 * Get the IP address as a formatted string
		 * 
		 * @return IP address string from the nested structure
		 */
		public String getIpAddressString() {
			return this.IpAddress.getIpAddressString();
		}

		/**
		 * Get the IP address as a formatted string
		 * 
		 * @return IP address string from the nested structure
		 */
		public String getMaskAddressString() {
			return this.IpMask.getIpAddressString();
		}
	}

	// FIXED_INFO structure definition
	protected static class FIXED_INFO extends Structure {
        public byte[] hostName = new byte[132];
        public byte[] domainName = new byte[132];
        public Pointer currentDnsServer;
        public IP_ADDR_STRING dnsServerList;
        public int nodeType;
        public byte[] scopeId = new byte[260];
        public int enableRouting;
        public int enableProxy;
        public int enableDns;
        
        public FIXED_INFO() {
			super();
		}

		public FIXED_INFO(Pointer p) {
			super(p);
			read();
		}
		
        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList (
                "hostName", "domainName", "currentDnsServer", 
                "dnsServerList", "nodeType", "scopeId", 
                "enableRouting", "enableProxy", "enableDns"
            );
        }
    }

	// IP_ADAPTER_INFO structure definition
	protected static class IP_ADAPTER_INFO extends Structure {
		public Pointer Next; // Pointer to the next adapter in the linked list
		public int ComboIndex; // Combination index
		public byte[] AdapterName = new byte[260]; // Adapter name
		public byte[] Description = new byte[132]; // Adapter description
		public int AddressLength; // Length of the MAC address
		public byte[] Address = new byte[6]; // MAC address (physical address)
		public int Index; // Adapter index
		public int Type; // Adapter type
		public int DhcpEnabled; // DHCP enabled flag
		public Pointer CurrentIpAddress; // Reserved field
		public IP_ADDR_STRING IpAddressList; // Linked list of IP addresses (corrected)
		public IP_ADDR_STRING GatewayList; // Linked list of gateway addresses (corrected)
		public IP_ADDR_STRING DhcpServer; // DHCP server address
		public int HaveWins; // WINS server flag
		public IP_ADDR_STRING PrimaryWinsServer; // Primary WINS server
		public IP_ADDR_STRING SecondaryWinsServer; // Secondary WINS server
		public long LeaseObtained; // Lease obtained timestamp
		public long LeaseExpires; // Lease expiration timestamp

		public IP_ADAPTER_INFO() {
			super();
		}

		public IP_ADAPTER_INFO(Pointer p) {
			super(p);
			read();
		}

		@Override
		protected List<String> getFieldOrder() {
			List<String> fieldOrder = new ArrayList<>();
			fieldOrder.add("Next");
			fieldOrder.add("ComboIndex");
			fieldOrder.add("AdapterName");
			fieldOrder.add("Description");
			fieldOrder.add("AddressLength");
			fieldOrder.add("Address");
			fieldOrder.add("Index");
			fieldOrder.add("Type");
			fieldOrder.add("DhcpEnabled");
			fieldOrder.add("CurrentIpAddress");
			fieldOrder.add("IpAddressList");
			fieldOrder.add("GatewayList");
			fieldOrder.add("DhcpServer");
			fieldOrder.add("HaveWins");
			fieldOrder.add("PrimaryWinsServer");
			fieldOrder.add("SecondaryWinsServer");
			fieldOrder.add("LeaseObtained");
			fieldOrder.add("LeaseExpires");
			return fieldOrder;
		}
	}

	// Define IP Helper API interface
	protected interface IPHelperAPI extends StdCallLibrary {
		/**
		 * Retrieve adapter information function
		 * 
		 * @param pAdapterInfo Output parameter, pointer to IP_ADAPTER_INFO structure
		 *                     buffer
		 * @param pOutBufLen   Input-output parameter, specifies buffer size on input,
		 *                     returns actual required size on output
		 * @return Error code: 0 indicates success
		 */
		int GetAdaptersInfo(Pointer pAdapterInfo, IntByReference pOutBufLen);

        int GetNetworkParams(Pointer pFixedInfo, IntByReference pOutBufLen);
        
		/**
	     * Sends an ARP request to resolve an IP address to a physical address
	     * @param destIP Destination IP address (32-bit integer in network byte order)
	     * @param srcIP Source IP address, typically set to 0
	     * @param pMacAddr Pointer to buffer for receiving MAC address
	     * @param pPhyAddrLen Pointer to value specifying MAC address buffer length
	     * @return NO_ERROR (0) if successful, error code otherwise
	     */
	    int SendARP(int destIP, int srcIP, Pointer pMacAddr, IntByReference pPhyAddrLen);
	    
		// Error code constants
		int ERROR_SUCCESS = 0;
		int ERROR_NO_DATA = 232;
		int ERROR_BUFFER_OVERFLOW = 111;
		int ERROR_NOT_SUPPORTED = 50;
	}

	/** filename of iphlpapi.dll */
	protected static String LIB_NAME = "iphlpapi";

	/** instance of iphlpapi.dll */
	protected static IPHelperAPI INSTANCE;

	/** Check if iphlpapi.dll exists on this Windows system. */
	public static boolean isExists() {
		if (!Platform.isWindows())
			return false;

		// try load library
		try {
			INSTANCE = Native.load(LIB_NAME, IPHelperAPI.class);
		} catch (UnsatisfiedLinkError | RuntimeException e) {
			INSTANCE = null;
		}

		return INSTANCE != null;
	}
	
	/** Retrieve DNS servers */
	public static List<String> getDnsServers() {
		List<String> dnsServers = new ArrayList<>();
		if (!isExists())
			return dnsServers;
		
		try {
            IntByReference bufferSize = new IntByReference(0);
            
            // First call to get required buffer size
            int result = INSTANCE.GetNetworkParams(null, bufferSize);
            
            if (result == IPHelperAPI.ERROR_BUFFER_OVERFLOW) {
                Memory buffer = new Memory(bufferSize.getValue());
                result = INSTANCE.GetNetworkParams(buffer, bufferSize);
                
                if (result == 0) { // NO_ERROR
                    FIXED_INFO fixedInfo = new FIXED_INFO(buffer);
                    
                    // Get DNS servers from the linked list
                    IP_ADDR_STRING dnsServer = fixedInfo.dnsServerList;
                    
                    while (dnsServer != null) {
                        String ipAddress = dnsServer.getIpAddressString(); // Native.toString(dnsServer.ipAddress);
                        if (!ipAddress.isEmpty() && !ipAddress.equals("0.0.0.0")) {
                            dnsServers.add(ipAddress);
                        }
                        
                        // Move to next DNS server in the list
                        // Move to next IP address in the list
						if (dnsServer.Next != null && !dnsServer.Next.equals(Pointer.NULL)) {
							dnsServer = new IP_ADDR_STRING(dnsServer.Next);
						} else {
							dnsServer = null;
						}
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting DNS servers: " + e.getMessage());
        }
		
		return dnsServers;
	}

	/** Retrieve information of all network interface cards */
	public static List<NetCard> list() {
		if (!isExists())
			return null;

		List<NetCard> infos = new ArrayList<NetCard>();

		try {
			IntByReference bufferSizeRef = new IntByReference(0);
			int result;

			// First call: Get required buffer size
			result = INSTANCE.GetAdaptersInfo(null, bufferSizeRef);

			if (result == IPHelperAPI.ERROR_BUFFER_OVERFLOW) {
				// Allocate sufficient memory, buffer size = bufferSizeRef.getValue() bytes
				Memory buffer = new Memory(bufferSizeRef.getValue());
				buffer.clear(); // Clear memory to avoid dirty data

				// Second call: Actually retrieve adapter information
				result = INSTANCE.GetAdaptersInfo(buffer, bufferSizeRef);

				if (result == IPHelperAPI.ERROR_SUCCESS) {
					// Successfully retrieved adapter information

					// Traverse the adapter linked list
					IP_ADAPTER_INFO currentAdapter = new IP_ADAPTER_INFO(buffer);
					while (currentAdapter != null) {
						// create one NetCardInfo
						NetCardInfo info = new NetCardInfo();
						infos.add(info);

						// Get adapter description and name
						info.displayName(new String(currentAdapter.Description).trim());
						info.name(new String(currentAdapter.AdapterName).trim());
						
						// Get MAC address
						if (currentAdapter.Address.length == 6) {
							MacAddress m = new MacAddress(currentAdapter.Address);
							info.mac(m);
						}

						// Get IP address list
						InetAddress addr = null;
						IP_ADDR_STRING ipAddr = currentAdapter.IpAddressList;
						while (ipAddr != null) {
							String ipAddress = ipAddr.getIpAddressString();
							if (!ipAddress.isEmpty() && !ipAddress.equals("0.0.0.0")) {
								try {
									addr = InetAddress.getByName(ipAddress);									
									int maskLength = NetUtil.calculatePrefixLength(ipAddr.getMaskAddressString());
									NetCardAddress nAddr = new NetCardAddress(addr, maskLength);
									info.getNetcardAddresses().add(nAddr);
								} catch (Exception e) {
								}
							}
							// Move to next IP address in the list
							if (ipAddr.Next != null && !ipAddr.Next.equals(Pointer.NULL)) {
								ipAddr = new IP_ADDR_STRING(ipAddr.Next);
							} else {
								ipAddr = null;
							}
						}

						// Get gateway addresses
						IP_ADDR_STRING gateway = currentAdapter.GatewayList;
						while (gateway != null) {
							String gatewayIP = gateway.getIpAddressString();
							if (!gatewayIP.isEmpty() && !gatewayIP.equals("0.0.0.0")) {
								try {
									addr = InetAddress.getByName(gatewayIP);
									info.putGateWay(addr);
								} catch (Exception e) {
								}
							}

							// Move to next gateway in the list
							if (gateway.Next != null && !gateway.Next.equals(Pointer.NULL)) {
								gateway = new IP_ADDR_STRING(gateway.Next);
							} else {
								gateway = null;
							}
						}

						// Display additional information
						info.dataLinkType(toDataLinkType(currentAdapter.Type));
						info.dhcpEnabled(currentAdapter.DhcpEnabled == 0 ? true : false);

						// Display DHCP server if DHCP is enabled
						if (currentAdapter.DhcpEnabled != 0 && currentAdapter.DhcpServer != null) {
							String dhcpServer = currentAdapter.DhcpServer.getIpAddressString();
							if (!dhcpServer.isEmpty() && !dhcpServer.equals("0.0.0.0")) {
								try {
									addr = InetAddress.getByName(dhcpServer);
									info.dhcpServer(addr);
								} catch (Exception e) {
								}

							}
						}

						// Move to next adapter in the linked list
						if (currentAdapter.Next != null && !currentAdapter.Next.equals(Pointer.NULL)) {
							currentAdapter = new IP_ADAPTER_INFO(currentAdapter.Next);
						} else {
							currentAdapter = null;
						}
					}

				} else {
//					System.err.println("GetAdaptersInfo call failed, error code: " + result);
//					getErrorMessage(result);
				}
			} else {
//				System.err.println("Failed to get buffer size, error code: " + result);
//				getErrorMessage(result);
			}

		} catch (Exception e) {

		}

		return infos;
	}

	/**
	 * Print descriptive error message based on error code
	 * 
	 * @param errorCode The Windows API error code
	 */
	public static String getErrorMessage(int errorCode) {
		switch (errorCode) {
		case IPHelperAPI.ERROR_NO_DATA:
			return ("Error Description: No network adapters installed in the system");
		case IPHelperAPI.ERROR_NOT_SUPPORTED:
			return ("Error Description: This function is not supported on this system");
		case 5: // ERROR_ACCESS_DENIED
			return ("Error Description: Access denied, please run as administrator");
		case 8: // ERROR_NOT_ENOUGH_MEMORY
			return ("Errorr Description: Insufficient memory");
		case 87: // ERROR_INVALID_PARAMETER
			return ("Errorr Description: Invalid parameter");
		default:
			return ("Errorr Description: Unknown error");
		}
	}

	/** convert Windows type to DataLinkType */
	protected static int toDataLinkType(int type) {
		switch (type) {
		case 1:
			return DataLinkType.OTHERS; // "Others";
		case 6:
			return DataLinkType.ETHERNET; // "Ethernet";
		case 9:
			return DataLinkType.TOKEN_RING; // "Token Ring";
		case 15:
			return DataLinkType.FDDI; // "FDDI";
		case 23:
			return DataLinkType.PPP; // "PPP";
		case 24:
			return DataLinkType.LOOPBACK; // "Loopback";
		case 28:
			return DataLinkType.SLIP; // "SLIP";
		default:
			return DataLinkType.NULL; // "Unknown (" + type + ")";
		}
	}

	/** Retrieves the MAC address for the specified IP address using ARP protocol */
	protected static MacAddress getMacAddress(InetAddress addr) {
		if (!isExists()) return null;
		
		// Prepare buffer for MAC address (6 bytes for Ethernet MAC)
        Pointer macAddrPtr = new Memory(6);
        // Initialize length reference with expected MAC address length
        IntByReference macAddrLen = new IntByReference(6);

        int destIP = NetUtil.inetAddressToInt(addr);
        // Execute SendARP function to resolve IP to MAC address
        int result = INSTANCE.SendARP(destIP, 0, macAddrPtr, macAddrLen);

        // Process the result of SendARP operation
        if (result == 0) { // NO_ERROR - successful resolution
            // Extract MAC address bytes from buffer
            byte[] macBytes = macAddrPtr.getByteArray(0, 6);
            // Format bytes into human-readable MAC address string
            return new MacAddress(macBytes);
        } else {
            // Handle different error scenarios with descriptive messages
//            switch (result) {
//                case 31: // ERROR_GEN_FAILURE
//                    System.err.println("Error: ARP request failed. Target device (" + ipAddress + ") may be unreachable or on different subnet.");
//                    break;
//                case 87: // ERROR_INVALID_PARAMETER
//                    System.err.println("Error: Invalid parameters passed to SendARP function.");
//                    break;
//                case 1114: // ERROR_NO_SUCH_DEVICE
//                    System.err.println("Error: Required network components for ARP request are unavailable.");
//                    break;
//                default:
//                    System.err.println("SendARP call failed with error code: " + result);
//            }
            return null;
        }
	}
}
