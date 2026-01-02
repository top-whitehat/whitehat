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
package top.whitehat.udp;

import java.net.InetAddress;

import top.whitehat.NetCard;
import top.whitehat.packet.Packet;
import top.whitehat.packet.UdpPacket;

public class UdpServer extends PacketServer {
	
	/**A listener that can receive incoming packet.​ */
	public interface UdpPacketListener {	
		
		/** The onPacket() method is called when a packet is incoming */
		void onPacket(UdpPacket pkt); 
	}
	
	private UdpPacketListener _packetlistener;
	
	public UdpServer(NetCard netCard) {
		super(netCard);
	}
	
 	public boolean filter(Packet packet) {
		if (packet instanceof UdpPacket) {
			return true;
			
		} else if (packet.parent() instanceof UdpPacket) {
			return true;
			
		} else {
			return false;
		}
	}
	
	
	
	public UdpServer onPacket(UdpPacketListener listener) {
		this._packetlistener = listener;
		return this;
	}

	
	@Override
	protected void receivePacket(Packet packet) {
		if (_packetlistener != null) {
			UdpPacket request = packet.getPacket(UdpPacket.class);
			if (request != null)
				_packetlistener.onPacket(request);
		}
	}
	
	public void reply(UdpPacket req, byte[] data) {
		if (req == null) throw new RuntimeException("cannot create UDP reply to a non-UDP request");
		UdpPacket response = UdpPacket.reply(req, data);
		netCard.sendPacket(response);
	}
	

	public void send(InetAddress dstIp,  int dstPort, byte[] data) {
		UdpPacket response = UdpPacket.create(
				netCard.ip(), getPort(), dstIp, dstPort, data);
		netCard.sendPacket(response);
	}


}
