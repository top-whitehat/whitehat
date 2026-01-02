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

import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;

import top.whitehat.client.Whois;
import top.whitehat.packet.MacAddress;
import top.whitehat.util.HttpUtil;
import top.whitehat.util.JSON;

public class Info {

	public static Locale locale = null;

	/**
	 * Get IP information from ip-api.com
	 * 
	 * @throws IOException
	 */
	public static JSON getIpInfo(String ipString)  {
		String url = "http://ip-api.com/json/" + ipString;
		if (locale != null)
			url += "?lang=" + locale.toLanguageTag();
		String ret = HttpUtil.getUrl(url);
		return JSON.parse(ret);

	}

	/** Get information of specified domain */
	public static JSON getDomainInfo(String domain) {
		return Whois.of(domain);
	}

	/** Get IP information from ip-api.com */
	public static JSON getIpInfo(InetAddress address) {
		return getIpInfo(address.getHostAddress());
	}

	protected static String officialOUI = "http://standards-oui.ieee.org/oui/oui.txt";

	private static final String API_URL = "https://api.macvendors.com/";

	/** get vendor name of specified MAC address */
	public static String getMacVendor(String mac) {
		return getMacVendor(MacAddress.getByName(mac));
	}

	/** get vendor name of specified MAC address */
	public static String getMacVendor(MacAddress mac) {
		String url = API_URL + mac.toString();
		return HttpUtil.getUrl(url);
	}

	private static String IFCONFIG = "https://ifconfig.me/ip";

	private static String MYEXTERNALIP = "https://myexternalip.com/raw";

	/** Get the public Internet ip of this machine */
	public static String myIp() {
		String ret;
		try {
			ret = HttpUtil.get(MYEXTERNALIP);
		} catch (Exception e) {
			ret = HttpUtil.getUrl(IFCONFIG);
		}
		if (ret.indexOf(" ") > 0)
			ret = ret.substring(0, ret.indexOf(" "));
		
		if (ret != null) ret = ret.trim();
		return ret;
	}

}
