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
package top.whitehat.packet;

import java.net.InetAddress;

/** ​​Classes that have srcIp() and dstIp() methods should implement this interface.​ */
public interface IHasIp {

	/** Get source InetAddress */
	public InetAddress srcIp();
	
	/** Set source InetAddress */
	public IHasIp srcIp(InetAddress addr);
	
	/** Get destination InetAddress */
	public InetAddress dstIp();	
	
	/** Set destination InetAddress */
	public IHasIp dstIp(InetAddress addr);
	
	/** Check whether srcIp or dstIp match specified ip */
	public default boolean hasIp(InetAddress ip) {
		return ip.equals(srcIp()) || ip.equals(dstIp());
	}
	
	/** Get IP string
	 * 
	 * @param isSrc  whether return source ip
	 * @return if isSrc is true, return source ip string, else return destination ip string 
	 */
	public default String getIpString(boolean isSrc) {
		InetAddress addr = isSrc ? srcIp() : dstIp();
		return addr == null ? "null" : addr.getHostAddress();		
	}
}
