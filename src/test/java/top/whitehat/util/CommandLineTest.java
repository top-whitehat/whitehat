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
package top.whitehat.util;








public class CommandLineTest {
	static void parsePing() {
		CommandResult t = CommandLine.run("ping 8.8.8.8");
		TextRow rows = t.rows("=").delete("%").delete("average").delete("平均");
		TextTable table = rows.cut("=", null).split("= \t").deleteFields(1, 3);
		table.setFieldNames("bytes", "time", "TTL");
		System.out.println(table);
//		System.out.println(table.toJSONArray("bytes", "time", "TTL"));
	}

	static void parseArpA() {
		CommandResult ret = CommandLine.run("arp -a");
		TextRow rows = ret.rows("-").delete("--");
		TextTable table = rows.split(" \t").deleteFields(2).setFieldNames("ip", "mac");
		System.out.println(table);
	}
	
	static void parseIpConfig1() {
		CommandResult ret = CommandLine.run("ipconfig /all");
		TextRow rows = ret.rows();
		int r = 0;
		
		System.out.println(rows);

		TextTable table = new TextTable();
		table.setFieldNames("name", "IPv4", "IPv6", "DHCP 服务器", "物理地址", "描述", "子网掩码");
		table.closeEdit();
		
		int times = 0;
		while (r < rows.size()) {
			String line = rows.get(r++);
			if (line == "") {
				if (!table.isEditing()) {
					table.startEdit();
					times = 1;
				} else if (times == 1) {
					times++;
					
				} else {
					table.closeEdit();
				}
			} else {
				if (table.isEditing()) {
					if (!line.startsWith(" ")) {
						table.editField("name", line.trim());
					} else {
						for(String name : table.fields) {
							if (line.indexOf(name) >= 0) {
								table.editField(name, TextUtil.cut(line, ":", null));
							}
						}
					}
				}
			}
		}
		
//		System.out.println(table.toJSONArray());
	}

	static void parseIpConfig() {
		CommandResult ret = CommandLine.run("ipconfig /all");
		TextRow rows = ret.rows();
		int r = 0;
		
		System.out.println(rows);

		JSON json = null;
		int times = 0;
		while (r < rows.size()) {
			String line = rows.get(r++);
			if (line == "") {
				if (json == null) {
					json = new JSON();
					times = 1;
				} else if (times == 1) {
					times++;
					
				} else {
					System.out.println(json);
					json = null;
				}
			} else {
				if (json != null) {
					if (!line.startsWith(" ")) {
						json.put("name", line.trim());
					} else if (line.indexOf("IPv4") >= 0) {
						json.put("IPv4", TextUtil.cut(line, ":", null));
					} else if (line.indexOf("IPv6") >= 0) {
						json.put("IPv6", TextUtil.cut(line, ":", null));
					} else if (line.indexOf("物理地址") >= 0) {
						json.put("物理地址", TextUtil.cut(line, ":", null));
					} else if (line.indexOf("DHCP 服务器") >= 0) {
						json.put("DHCP", TextUtil.cut(line, ":", null));
					} else if (line.indexOf("描述") >= 0) {
						json.put("描述", TextUtil.cut(line, ":", null));
					} else if (line.indexOf("子网掩码") >= 0) {
						json.put("子网掩码", TextUtil.cut(line, ":", null));
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		parseIpConfig1();
	}

}
