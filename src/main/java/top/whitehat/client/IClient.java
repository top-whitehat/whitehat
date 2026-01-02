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
package top.whitehat.client;

import java.io.IOException;

/** A client that connect to specified host and port */
public interface IClient {

	/** get the host */
	public String getHost();
	
	/** set the host */
	public void setHost(String host);
	
	/** get the port */
	public int getPort();
	
	/** set the port */
	public void setPort(int port);
	
	/** Open connection */
	public boolean connect() throws IOException;
	
	/** Open connection */
	public default boolean connect(String host, int port) throws IOException {
		setHost(host);
		setPort(port);
		return connect();
	}
	
	/** Close connection */
	public void disconnect();
}
