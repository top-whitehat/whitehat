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

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;


/**
 * This is the JNA interface for the packet.dll (in Windows) 
 */
public class PacketDll {

	static final String PACKET_LIB_NAME = "Packet";

	static int PACKET_OID_DATA_SIZE;

	/** instance of packet.dll */
	static NativeLibrary packetDllInstance = null;
	
	/** whether packet.dll in current OS environment */
	public static boolean isExists() {
		// if not in Windows, exit
		if (!Platform.isWindows()) return false;

		try {			
			// try load library
			if (packetDllInstance == null) {
				packetDllInstance = NativeLibrary.getInstance(PACKET_LIB_NAME);
			}			
		} catch (UnsatisfiedLinkError | RuntimeException e) {		
			packetDllInstance = null;
		}
		
		return packetDllInstance != null;
	}
	
	/** initialize this class */
	static void init() {
		// If possible, use Npcap as the preferred option
		// In windows, if JNA library path is undefined, and NPCAP_DIR exists
		if (Platform.isWindows() && System.getProperty("jna.library.path") == null) {
			// add NPCAP_DIR to library searching path
			if (PcapLib.NPCAP_DIR.exists()) {
				NativeLibrary.addSearchPath("wpcap", PcapLib.NPCAP_DIR.getAbsolutePath());
			}
		}
				
		// if libpcap exists
		if (isExists()) {
			// Maps all methods within this class to native libraries via the JNA.
			Native.register(PacketDll.class, NativeLibrary.getInstance(PACKET_LIB_NAME));
			PACKET_OID_DATA_SIZE = new PACKET_OID_DATA().size();
		}			
	}
	
	static {
		init();
	}

	// LPADAPTER PacketOpenAdapter(PCHAR AdapterNameWA)
	static native Pointer PacketOpenAdapter(String AdapterNameWA);

	// BOOLEAN PacketRequest(LPADAPTER  AdapterObject,BOOLEAN Set,PPACKET_OID_DATA  OidData)
	static native int PacketRequest(Pointer AdapterObject, int Set, PACKET_OID_DATA OidData);

	// VOID PacketCloseAdapter(LPADAPTER lpAdapter)
	static native void PacketCloseAdapter(Pointer lpAdapter);

	private PacketDll() {
	}
	
	/** get bytes of MAC address */
	public static byte[] getMacAddress(String nifName) {
		if (!PacketDll.isExists()) return null;
		
		// open adapter
		Pointer lpAdapter = PacketDll.PacketOpenAdapter(nifName);
		if (lpAdapter == null) return null;
		
//		// get file handle
//		long hFile = (Native.POINTER_SIZE == 4) ?  lpAdapter.getInt(0) : lpAdapter.getLong(0);		
//		if (hFile == -1L) return null;
		
		// create an PACKET_OID_DATA Structure  
		Memory m = new Memory(PacketDll.PACKET_OID_DATA_SIZE);
		m.clear();
		PACKET_OID_DATA oidData = new PACKET_OID_DATA(m);
		
		// request for OID data of MAC address
		long OID_802_3_CURRENT_ADDRESS = 0x01010102L;  // MAC Address
		oidData.Length = new NativeLong(6L);		
		oidData.Oid = new NativeLong(OID_802_3_CURRENT_ADDRESS);
		int status = PacketDll.PacketRequest(lpAdapter, 0, oidData);
		PacketDll.PacketCloseAdapter(lpAdapter);

		// return result if success
		if (status != 0 && oidData.Data != null) {
			return oidData.Data.clone();
		}
		
		// failed, return null
		return null;
	}

	// struct _PACKET_OID_DATA {
	//     ULONG Oid;       ///< OID code. See the Microsoft DDK documentation or the file ntddndis.h
	//                      ///< for a complete list of valid codes.
	//     ULONG Length;    ///< Length of the data field
	//     UCHAR Data[1];   ///< variable-lenght field that contains the information passed to or received
	//                      ///< from the adapter.
	// };
	public static class PACKET_OID_DATA extends Structure {

		public NativeLong Oid; // ULONG
		public NativeLong Length; // ULONG
		public byte[] Data = new byte[6]; // UCHAR

		public PACKET_OID_DATA() {
		}

		public PACKET_OID_DATA(Pointer p) {
			super(p);
			read();
		}

		public static class ByReference extends PACKET_OID_DATA implements Structure.ByReference {
		}

		@Override
		protected List<String> getFieldOrder() {
			List<String> list = new ArrayList<String>();
			list.add("Oid");
			list.add("Length");
			list.add("Data");
			return list;
		}

	}

}
