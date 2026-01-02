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
package top.whitehat.examples;

import java.net.UnknownHostException;

public class Examples {

	public static void main(String[] args) throws UnknownHostException {
		String sIndex = args.length > 0 ? args[0] : "";
		if (sIndex == null) return;
		switch (sIndex) {
		case "0":
			Example00_Hello.main(new String[0]);
			break;
		case "1":
			Example01_NetCard.main(new String[0]);
			break;
		case "2":
			Example02_Capture.main(new String[0]);
			break;
		case "3":
			Example03_Filter.main(new String[0]);
			break;
		case "4":
			Example04_Dump.main(new String[0]);
			break;
		case "5":
			Example05_ReadDump.main(new String[0]);
			break;
		case "6":
			Example06_ARP.main(new String[0]);
			break;
		case "7":
			Example07_ICMP.main(new String[0]);
			break;
		case "8":
			Example08_DNS.main(new String[0]);
			break;
		case "9":
			Example09_DHCP.main(new String[0]);
			break;
		case "10":
			Example10_TCP_SYN.main(new String[0]);
			break;
		case "11":
			Example11_ScanIp.main(new String[0]);
			break;
		case "12":
			Example12_ScanPort.main(new String[0]);
			break;
		default:
			break;
		}
	}
}
