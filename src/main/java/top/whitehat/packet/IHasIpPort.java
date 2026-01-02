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


/** ​​Classes that have srcPort() and dstPort() methods should implement this interface.​ */
public interface IHasIpPort extends IHasIp {

	/** get source port */
	public int srcPort();

	/** set source port */
	public IHasIpPort srcPort(int value);

	/** get destination port */
	public int dstPort();

	/** set destination port */
	public IHasIpPort dstPort(int value);

	/** Check whether srcPort or dstPort match specified port */
	public default boolean hasPort(int port) {
		return srcPort() == port || dstPort() == port;
	}
	
	/** Get "ip:port" string
	 * 
	 * @param isSrc  whether return source ip
	 * @return if isSrc is true, return source ip:port string, else return destination ip:port string 
	 */
	public default String getIpPort(boolean isSrc) {
		int port = isSrc ? srcPort() : dstPort();
		return getIpString(isSrc) + ":" + port;
	}
	
	public default String srcIpPortStr() {
		return getIpPort(true);
	}
	
	public default String dstIpPortStr() {
		return getIpPort(false);
	}
}
