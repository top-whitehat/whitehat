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
package top.whitehat.dns;

import java.net.InetAddress;

import top.whitehat.dns.DnsServer.DnsFilters;

/** DNS response */
public class DnsResponse {

	/** the parent session */
	private DnsSession session;

	/** Constructor with parent session */
	public DnsResponse(DnsSession session) {
		this.session = session;
	}

	/** get the parent DnsServer object */
	public DnsServer getServer() {
		return session.getServer();
	}

	/**get the Request object */
	public DnsRequest getRequest() {
		return session.getRequest();
	}

	/** send response data to the client */
	public void send(byte[] bytes) {
		getServer().send(getRequest().srcIp(), getRequest().srcPort(), bytes);
	}

	/** send response data to the client  */
	public void send(DnsPacket msg) {
		send(msg.getBytes());
	}

	/** send error to the client
	 * 
	 * @param errorCode error code, see: DnsError
	 */
	public void sendError(DnsError errorCode) {
		DnsPacket msg = getRequest().getDnsPacket().responseError(errorCode.getValue());
		send(msg.getBytes());
	}

	/** Send error to the client */
	public void sendError() {
		sendError(DnsError.REFUSED);
	}

	/** sent filtered response to the client
	 * 
	 * @param filterMode filter mode
	 */
	public void sendFilterResult(int filterMode) {
		DnsRequest request = getRequest();
		DnsPacket msg;
		InetAddress addr;
		
		switch (filterMode) {
		case DnsFilters.WARNING:
			addr = getServer().getFilters().warningServer.getAddressByDefault();
			msg = request.getDnsPacket().responseA(request.getDnsPacket().queryName(), addr, 0);
			send(msg.getBytes());
			break;
		case DnsFilters.STOP:
			addr = getServer().getFilters().stopServer.getAddressByDefault();
			msg = request.getDnsPacket().responseA(request.getDnsPacket().queryName(), addr, 0);
			send(msg.getBytes());
			break;
		default:
			msg = request.getDnsPacket().responseError(DnsError.REFUSED.getValue());
			send(msg.getBytes());
			break;
		}
	}

}
