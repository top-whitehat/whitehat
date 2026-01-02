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

/** A simple FTP client that only implement FTP login, logout */
public class FtpClient extends TcpTextClient implements ILogin {

	public static final int FTP_PORT = 21;
	
	public FtpClient() {			
	}
		
	/** get default port */
	@Override
	public int getDefaultPort() {
		return FTP_PORT;
	}
		
	/** Ensures the FTP response status code indicates success (2xx). 
	 * @throws IOException */
	public void checkError(String response) throws IOException {
		if (!checkStatusCode(response, 100, 399)) {
			throwIOException("FTP ERROR: " + response);
		}
	}
		
	@Override
	public boolean login(String username, String password) {
		try {
			connect();
			// read welcome message 
			checkError(recieveResponse());
			// send USER, PASS to login
			checkError(sendCommand("USER " + username));
			checkError(sendCommand("PASS " + password));
			return true;
		} catch (IOException e) {
			setLastException(e);
			return false;
		}		
	}

	@Override
	public boolean logout() {
		try {
			sendCommand("QUIT");
			return true;
		} catch (IOException ignore) {
			return false;
		} finally {
			disconnect();
		}		
	}

}
