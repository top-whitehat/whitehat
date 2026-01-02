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
package top.whitehat;

import top.whitehat.util.CommandLine;
import top.whitehat.util.CommandResult;
import top.whitehat.util.Text;

public class TestWinARPCommand {

	public static void main(String[] args) {
		CommandResult ret = CommandLine.run("arp -a");
		Text t = ret.text().filter("-").delete("--").split(" \t").fieldNames("ip", "mac", "type");
		System.out.println(t);
		//System.out.println(t.toJSON("ip", "mac"));
		
		String value = t.select("mac").where("ip = 192.168.100.1").value();
		System.out.println(value);
	}
	
}