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
package top.whitehat.tcp;

import java.io.IOException;


/** Service of the TCP server */
public abstract class TcpService {
		
	/** create TcpService object */
	public static TcpService of(String name, TcpServer server) throws IOException {
		switch (name.toLowerCase()) {
		case "nio":
			return new TcpNIOService().init(server);
		case "socket":
			return new TcpSocketService().init(server);
		default:
			return null;
		}
				
	}
	
	/** Constructor */
	TcpService() {}
	
	/** Initialization */
	public abstract TcpService init(TcpServer server) throws IOException;
	
	/** Start service */
	public abstract void start();
	
	/** Close service */
	public abstract void close();
	
}
