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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import top.whitehat.net.DatagramNetCard;




public class UdpServer1 extends UdpServer {

	static Map<String, String> answers = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("hello", "Hi, boys");
			put("who are you?", "I'm AI boy.");
		}
	};
	
	public UdpServer1(int port) {
		super(new DatagramNetCard());
		this.netCard.setPort(port);
	}

	protected void onReceive(InetSocketAddress sender, byte[] bytes) {
		String request = new String(bytes);
		String response = answers.getOrDefault(request.toLowerCase(), "I dont konw");
		
		System.out.println(
				sender.getAddress() + ":" + sender.getPort() + "  " + request);
		
		this.send(sender.getAddress(), sender.getPort(), response.getBytes());
	}

	public static void main(String[] args) throws IOException {
		int port = 5354;
		UdpServer1 server = new UdpServer1(port);
		System.out.println("starting server at port " + port);
		server.start();
	}
}
