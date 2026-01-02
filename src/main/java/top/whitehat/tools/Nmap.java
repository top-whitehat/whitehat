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
package top.whitehat.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import top.whitehat.NetUtil;
import top.whitehat.util.CommandLine;
import top.whitehat.util.CommandResult;
import top.whitehat.util.FileUtil;
import top.whitehat.util.JSON;
import top.whitehat.util.JSONArray;
import top.whitehat.util.Text;
import top.whitehat.util.TextUtil;


/**
 * Nmap command line
 * 
 * SEE: https://nmap.org/book/man.html
 * 
<pre>

SEE: https://blog.csdn.net/tabactivity/article/details/126265884

SEE:https://blog.csdn.net/qq_40368925/article/details/143487794

常用脚本参数
nmap --script=vuln ip：漏洞扫描

nmap --script=brute ip：漏洞扫描。并进行暴力破解，如：数据库、smb、snmp等

nmap --script=ftp-brute.nse ip：对ftp协议密码爆破

nmap --script=external 域名：利用第三方资源进行漏洞扫描，如whios解析

nmap --script=auth ip：主机弱口令扫描

nmap --script=realvnc-auth-bypass ip：常见服务扫描，如：vnc、mysql、telnet、rsync等

nmap --script=broadcast ip：广播收集信息；

nmap --script=default：默认脚本，等同于-sC

nmap --script=discovery：发现主机与所开服务脚本

nmap --script=dos：拒绝服务攻击脚本

nmap --script=exploit：漏洞利用脚本

nmap --script=fuzzer：模糊测试脚本

nmap --script=intrusive：入侵脚本

nmap --script=malware：恶意软件检查脚本

nmap --script=safe：安全脚本

nmap --script=version：高级系统脚本
</pre>
*/
public class Nmap {

	/** option: Ping Scan */
	public static final String SCAN_PING = "-sP";

	/** option: ARP Scan */
	public static final String SCAN_ARP = "-PR";

	/** option: UDP Scan */
	public static final String SCAN_UDP = "-sU";

	/** option: do not scan port */
	private static final String NO_PORT_SCAN = "-sn";

	/** executable file name */
	private static String executable = "nmap";

	

	// script directory
	protected static String scriptPath = null;

	// list of script name
	private static List<String> scriptList = null;

	/** get name list of nmap scripts */
	public static List<String> scripts() {
		if (scriptList == null) {
			scriptList = new ArrayList<String>();
			String nmap = FileUtil.findFileInPath(executable, true);
			if (nmap != null) {
				String nmapPath = FileUtil.getFilePath(nmap);
				String scriptPath = FileUtil.joinPath(nmapPath, "scripts");
				if (FileUtil.dirExists(scriptPath)) {
					Nmap.scriptPath = scriptPath;
					System.out.println(scriptPath);
					List<String> filenames = FileUtil.listFiles(scriptPath, "*.nse");
					for (String filename : filenames) {
//						String fullFilename = FileUtil.joinPath(scriptPath, filename);
						String name = FileUtil.getPureFilename(filename);
						scriptList.add(name);
					}
				}
			}
		}
		return scriptList;
	}

	/** Convert ports List to description string */
	public static String toPorts(List<Integer> portArray) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < portArray.size(); i++) {
			if (i > 0)
				sb.append(",");
			sb.append(i);
		}
		return sb.toString();
	}

	/** Convert ports array to port description string */
	public static String toPorts(int[] portArray) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < portArray.length; i++) {
			if (i > 0)
				sb.append(",");
			sb.append(i);
		}
		return sb.toString();
	}

	/** Convert ports description string to command line parameter of nmap */
	private static String toPortParam(String portDescription) {
		return (portDescription == null || portDescription.length() == 0) ? "-PS"
				: "-p " + TextUtil.removeSpace(portDescription);
	}

	/** Convert a TextTable to JSON */
	protected static JSONArray tableToJSON(Text t, String... names) {
		JSONArray ret = new JSONArray();
		for (int r = 0; r < t.rows(); r++) {
			JSON obj = new JSON();
			for (String name : names) {
				obj.put(name, t.cell(name, r));
			}
			ret.add(obj);
		}
		return ret;
	}
	
	/** Check whether nmap is installed on current OS */
	public static boolean exists() {
		CommandResult ret = CommandLine.run(executable, "-v");
		return ret.getReturnValue() == 0;
	}

	/** Get version of nmap */
	public static String version() {
		CommandResult ret = CommandLine.run(executable, "-v");
		if (ret.getReturnValue() == 0)
			return ret.rows("Starting").split(" ").cell(0, 2);
		else
			return null;
	}

	/**
	 * Compose a command line to scan operation system information of specified IP
	 * address
	 * 
	 * @param host the host to scan
	 * 
	 * @return return CommandLine object. <br>
	 *         use CommandLine.exec() method to run the command line.<br>
	 *         use CommdLine.json() method to run the command line and get result in
	 *         JSON format which is a JsonArray, each item is a item of port =>
	 *         service status.
	 */
	public static CommandLine scanOs(String host) {
		return new CommandLine().command(executable, "-O", host).parser(ret -> {
			if (ret.getReturnValue() != 0) {
				return JSON.of("error", ret.getReturnValue(), "output", ret.getOutput());

			} else {
				// Get port information form the returned text that looks like:
				// PORT STATE SERVICE
				// 80/tcp open http
				// 443/tcp open https
				Text portTable = ret.text().filter("/tcp").split(" \t");
				JSON jports = JSON.of();
				for (int r = 0; r < portTable.rows(); r++) {
//					TextRow row = portTable.get(r);
					String sPort = portTable.cell(r, 0); // row.get(0);
					int pos = sPort.indexOf("/");
					if (pos >= 0) {
						sPort = sPort.substring(0, pos);
					}
					if (NetUtil.isPort(sPort)) {
						jports.put(sPort, portTable.cell(r, 2));
					}
				}

				// get OS information form the returned text that looks like:
				// Device type: general purpose|firewall|router
				// Running (JUST GUESSING): Linux 4.X|5.X|2.6.X|3.X (97%), IPFire 2.X (90%),
				// MikroTik RouterOS 7.X (89%)
				// OS CPE: cpe:/o:linux:linux_kernel:4 cpe:/o:linux:linux_kernel:5
				// cpe:/o:linux:linux_kernel:2.6.32 cpe:/o:linux:linux_kernel:3.10
				// cpe:/o:ipfire:ipfire:2.27 cpe:/o:linux:linux_kernel:6.1
				// cpe:/o:mikrotik:routeros:7 cpe:/o:linux:linux_kernel:5.6.3
				// Aggressive OS guesses: Linux 4.19 - 5.15 (97%), Linux 4.15 - 5.19 (91%),
				// Linux 4.19 (91%), Linux 5.0 - 5.14 (91%), OpenWrt 21.02 (Linux 5.4) (91%),
				// Linux 2.6.32 (90%), Linux 2.6.32 or 3.10 (90%), Linux 4.0 - 4.4 (90%), Linux
				// 4.15 (90%), IPFire 2.27 (Linux 5.15 - 6.1) (90%)
				// No exact OS matches for host (test conditions non-ideal).
				Text propTable = ret.text().filter(":") //
						.delete("Starting").delete("Nmap") //
						.delete("detection performed").delete("closed ports") //
						.split(":", 2); //
				JSON jprops = JSON.of();
				for (int r = 0; r < propTable.rows(); r++) {
//					TextRow row = propTable.get(r);
//					jprops.put(row.get(0), row.get(1).trim());
					jprops.put(propTable.cell(r, 0), propTable.cell(r, 1).trim());
				}

				jprops.put("ports", jports);
				return jprops;
			}
		});
	}

	/**
	 * Compose a CommandLine that get service version information about specified IP
	 * address on specified ports
	 * 
	 * @param host  The host or ip address to scan
	 * @param ports The ports to scan, such as: "80,443,90-100"
	 *
	 * @return return CommandLine object. <br>
	 *         use CommandLine.exec() method to run the command line.<br>
	 *         use CommdLine.json() method to run the command line and get result in
	 *         JSON format which is a JsonArray, each item is a item of port =>
	 *         service status.
	 */
	public static CommandLine scanService(String host, String ports) {
		return new CommandLine().command(executable, toPortParam(ports), "-sV", host).parser(ret -> {
			if (ret.getReturnValue() != 0) {
				return JSON.of("error", ret.getReturnValue(), "output", ret.getOutput());

			} else {
				// Get port information form the returned text that looks like:
				// PORT STATE SERVICE VERSION
				// 21/tcp open ftp Synology DiskStation NAS ftpd
				// 80/tcp open http nginx (reverse proxy)
				Text portTable = ret.text().filter("/tcp").split(" \t");
				JSON jports = JSON.of();
				for (int r = 0; r < portTable.rows(); r++) {
//					TextRow row = portTable.get(r);
					String sPort = portTable.cell(r, 0); // row.get(0);
					int pos = sPort.indexOf("/");
					if (pos >= 0) {
						sPort = sPort.substring(0, pos);
					}
					if (NetUtil.isPort(sPort)) {
//						jports.put(sPort, JSON.array(row.get(2).trim(), row.getFrom(3, " ")));
						jports.put(sPort, JSON.array(portTable.cell(r,  2).trim(), portTable.row(r).getFrom(3, " ")));
					}
				}

				return jports;
			}
		});
	}

	/**
	 * Scan ports of specified targets, find out the opening ports
	 * 
	 * @param targets The targets to scan, such as an IP range like "192.168.0.1/24"
	 * @param ports   The ports to scan, such as: "80,443,90-100"
	 * 
	 * @return return CommandLine object. <br>
	 *         use CommandLine.exec() method to run the command line.<br>
	 *         use CommdLine.json() method to run the command line, get result in
	 *         JSON format which is a JsonArray, each item contains an ip-port
	 */
	public static CommandLine scanPort(String targets, String ports) {
		String option = toPortParam(ports);

		return new CommandLine().command(executable, option, targets).parser(ret -> {
			if (ret.getReturnValue() != 0) {
				return JSON.of("error", ret.getReturnValue(), "output", ret.getOutput());

			} else {
				// merge multiple scan report rows of one ip into one row
				Text t = ret.text().merge("Nmap scan report", "Nmap done:");

				// compose pattern to extract information fields
				String regexItem = "([0-9.]*)\\/([\\w]*)[ \\t]+([\\w]*)[ \\t]+(.*?)```";
				String regex = "report for (.*?)```";
				regex += ".*PORT.*SERVICE```";
				regex += regexItem;
				Pattern firstItemPattern = Pattern.compile(regex);
				Pattern nextItemPattern = Pattern.compile(regexItem);

				// extract information from rows //TODO:AAAA
//				TextTable table = rows.match(firstItemPattern, nextItemPattern, 1);
				t.match(firstItemPattern, nextItemPattern, 1);
				t.table.setFieldNames("ip", "port", "protocol", "type", "service");

				// convert table to JSONArray
				return tableToJSON(t, "ip", "port", "type", "service");
			}
		});
	}

	/**
	 * Compose a CommandLine that scan specified IP targets, find out the existing
	 * IP
	 * 
	 * @param targets The targets to scan, such as an IP range like "192.168.0.1/24"
	 * @param options The options, such as: Nmap.SCAN_PING, Nmap.SCAN_ARP,
	 *                Nmap.SCAN_UDP
	 * 
	 * @return return CommandLine object. <br>
	 *         use CommandLine.exec() method to run the command line.<br>
	 *         use CommdLine.json() method to run the command line and get result in
	 *         JSON format which is a JsonArray, each item contains an ip->exists
	 *         status.
	 */
	public static CommandLine scanIp(String targets, String options) {
		if (options == null || options.length() == 0) {
			options = NetUtil.isIpV4(targets) ? SCAN_ARP : SCAN_PING;
		}

		return new CommandLine().command(executable, options, NO_PORT_SCAN, targets).parser(ret -> {
			if (ret.getReturnValue() != 0) {
				return JSON.of("error", ret.getReturnValue(), "output", ret.getOutput());

			} else {
				// merge multiple scan report rows of one ip into one row
				Text t = ret.text().merge("Nmap scan report", "Nmap done:");

				// compose a regex to extract information fields
				String fmt = "//report for %s```Host is %s.//";
				t.match(fmt);
				t.table.setFieldNames("ip", "exist");

				// convert table to JSONArray
				JSONArray arr = tableToJSON(t, "ip", "exist");
				for (int i = 0; i < arr.size(); i++) {
					JSON obj = (JSON) arr.get(i);
					boolean value = obj.get("exist").toString().contains("up") ? true : false;
					obj.set("exist", value);
				}

				return arr;
			}
		});
	}
	
	/**
	 * Compose a command line to run nmap script
	 * 
	 * @param scriptName   The script name
	 * @param argsLine     The script arguments in a line
	 * 
	 * @return return CommandLine object. <br>
	 *         use CommandLine.exec() method to run the command line.<br>
	 */	
	public static CommandLine script(String scriptName, String argsLine) {
		String[] args = CommandLine.lineToWord(argsLine);
		return script(scriptName, args);
	}
	
	/**
	 * Compose a command line to run nmap script
	 * 
	 * @param scriptName   The script name
	 * @param argsLine     The script arguments
	 * 
	 * @return return CommandLine object. <br>
	 *         use CommandLine.exec() method to run the command line.<br>
	 */	
	public static CommandLine script(String scriptName, String[] args) {
		CommandLine cmd = new CommandLine().command(executable, "--script", scriptName);
		if (args != null && args.length > 0) cmd.command(args);
		return cmd;
	}

}
