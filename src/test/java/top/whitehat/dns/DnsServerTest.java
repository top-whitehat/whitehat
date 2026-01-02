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

import java.io.IOException;
import java.net.InetAddress;

import top.whitehat.NetCard;
import top.whitehat.dns.DnsServer.DnsFilters;
import top.whitehat.net.DatagramNetCard;

/**
 * Simple DnsServer
 */
public class DnsServerTest {

	public static void main(String[] args) throws IOException {
		test2();
	}

	static void test2() {

	}

	static void test1() {
		NetCard card;
		card = new DatagramNetCard(DNS.PORT);
		card = NetCard.inet();

		InetAddress cardIp = card.ip();

		// create DnsServer
		DnsServer server = new DnsServer(card);

		server.setPort(DNS.PORT);
		server.setLocalOnly(false);

		server.setDebug(2); // DEBUG level

		// set upper DNS server
		server.setUpperDnsServer("8.8.8.8");

		// set name, address of this server
		server.setNsName("hello.world", "127.0.0.1");

		// add a filter
		server.addFilter("porn.com", DnsFilters.STOP);

		// add a record
		server.addRecord("hello", "100.100.100.100");

		server.onStart(e -> {
			System.out.println("start " + cardIp + " " + server.getPort());
		});

		// start server
		server.start();
	}
}
