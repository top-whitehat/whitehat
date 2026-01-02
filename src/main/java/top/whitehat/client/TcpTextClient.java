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

import top.whitehat.tcp.TcpClient;

/** A base class for TCP client that use text command line */
public class TcpTextClient extends TcpClient {

	/** Indicates whether the response is ended by two CRLF */
	private boolean isDoubleCRLF = false;

	/** Indicates whether print out the command and response */
	private boolean isEcho = false;

	/** Constructor: create a client of TCP */
	public TcpTextClient() {
		super();
	}

	/** get the echo status */
	public boolean isEcho() {
		return this.isEcho;
	}

	/** set the echo status */
	public void setEcho(boolean value) {
		this.isEcho = value;
	}

	/** set whether the ending mark of the response is two CRLF */
	protected void setDoubleCRLF(boolean value) {
		this.isDoubleCRLF = value;
	}

	/** Parses the status code from the line. */
	protected int parseStatusCode(String line) {
		if (line == null)
			return -1;

		try {
			// read digits
			int i = 0;
			String number = "";
			while (i < line.length()) {
				char c = line.charAt(i++);
				if (c >= '0' && c <= '9')
					number += c;
				else
					break;
			}
			// parse to int
			return Integer.parseInt(number);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	/**
	 * Check whether the status code value of the response is in the range of
	 * [minCode, maxCode]
	 */
	protected boolean checkStatusCode(String response, int minCode, int maxCode) {
		int code = parseStatusCode(response);
		if (code >= minCode && code <= maxCode) {
			return true;
		} else {
			return false;
		}
	}

	/** Echo print */
	protected void echoPrint(String str) {
		if (isEcho())
			System.out.println(str);
	}

	/** Receives a response from the server. */
	protected String recieveResponse() throws IOException {
		String response = "";

		if (!isDoubleCRLF) {
			// read one line
			response = reader.readLine();
		} else {
			// read multiple lines until double CRLF is met
			StringBuilder sb = new StringBuilder();
			String line;
			do {
				line = reader.readLine();
				sb.append(line);
			} while (line != null && line.length() != 0);

			response = sb.toString();
		}

		if (isEcho)
			echoPrint(response);
		return response;
	}

	/** Sends a command and wait for the response with timeout */
	protected String sendCommand(String cmd) throws IOException {
		if (isEcho())
			echoPrint(cmd);
		send(cmd + "\r\n");
		// Read response
		return recieveResponse();
	}

	public boolean login(String username, String password) {
		try {
			connect();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean logout() {
		disconnect();
		return true;
	}

}
