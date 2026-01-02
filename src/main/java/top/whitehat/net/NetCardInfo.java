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


import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Platform;

import top.whitehat.NetCard;
import top.whitehat.packet.MacAddress;
import top.whitehat.packet.Packet;
import top.whitehat.pcap.PcapDumper;

public class NetCardInfo extends NetCard {
	
	/** Retrieve DNS server */
	public static String getDnsServer() {
		List<String> list = getDnsServers();
		return list == null || list.size() == 0 ? null : list.get(0);
	}

	/** Retrieve DNS server list */
	public static List<String> getDnsServers() {
		if (Platform.isWindows()) {
			return NetCardInfoWin.getDnsServers();
			
		} else if ( Platform.isMac()) {
			return NetCardInfoMac.getDnsServers();
			
		} else if ( Platform.isAndroid()) {
			return NetCardInfoAndroid.getDnsServers();
			
		} else if (Platform.isLinux() ) {
			return NetCardInfoLinux.getDnsServers();
			
		} else { 
			return NetCardInfoLinux.getDnsServers();  //TODO
		}
	}
	
	/** NetCard list cache */
	protected static List<NetCard> _list = null;
	
	/** Retrieve information of all network interface cards */
	public static List<NetCard> list() {
		if (_list == null) {
			if (Platform.isWindows()) {
				_list = NetCardInfoWin.list(); 
				
			}  else if (Platform.isMac()) {
				_list =  NetCardInfoMac.list(); 
				
			}  else if (Platform.isAndroid()) {
				_list =  NetCardInfoAndroid.list(); 
				
			} else if (Platform.isLinux()) {
				_list =  NetCardInfoLinux.list(); 
				
			} else { 
				_list =  new ArrayList<NetCard>();   //TODO
			}
		}
		return _list;
	}
	
	/** get default Mac Address */
	public static MacAddress getDefaultMac() {
		List<NetCard> infos = list();
		for(NetCard info : infos) {
			if (info.mac() != null)
				return info.mac();
		}
		return null;
	}

	public NetCardInfo() {
		super();
	}

	@Override
	protected void doSendPacket(Packet p) {
		// nothing to do
	}

	@Override
	public void start() {
		// nothing to do
	}

	@Override
	public void stop() {
		// nothing to do
	}

	@Override
	public void readDump(String filename) {
		// nothing to do
	}

	@Override
	public PcapDumper openDump(String filename, boolean isAppend) {
		// TODO Auto-generated method stub
		return null;
	}

}
