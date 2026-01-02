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




public class TcpServerTest extends TcpServer {

	public TcpServerTest(int port) {
		super(port);
	}

	protected void service(TcpConnection session) {
		try {
			
			String line = session.getReader().readLine();
			while (line != null && line.length() > 0) {
				System.out.println(line);
				line = session.getReader().readLine();
			}
			System.out.println();
			
			session.getWriter().println("HTTP/1.1 200 OK");
			session.getWriter().println("");
			session.getWriter().println("<body>Hello, Boy</body>");
			session.setKeepAlive(false);
			System.out.println("response length =" + session.getWriter().available());
		} catch (Exception e) {
		}
	}

	public static void main(String[] args) {
		int port = 5354;
		TcpServerTest server = new TcpServerTest(port);
		server.serviceType("socket");
		
		server.onStart(e->{
			System.out.println("starting server at port " + server.getPort());
		});
		
		server.onConnect(connection->{
			System.out.println("connect from: " + connection.srcIp() + ":" + connection.srcPort());
		});
		
		server.start();		
	}
}
