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
import java.net.InetAddress;

import top.whitehat.NetUtil;




public class UdpClientTest {
	
	static int port = 6777;
	static  InetAddress host = NetUtil.toInetAddress("127.0.0.1");
	
	static String ask(String question) throws IOException {
		System.out.println("CLIENT: " + question);
		byte[] response = UdpClient.request(host, port, question.getBytes());
		String answer = response == null ? null : new String(response);
		System.out.println("SERVER: " + answer);
		return answer;
	}
	
	public static void main(String[] args) throws IOException {
		ask("hello");
		ask("who are you?");
		
	}
}
