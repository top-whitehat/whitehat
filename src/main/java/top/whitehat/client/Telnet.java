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
import java.nio.charset.StandardCharsets;

import top.whitehat.util.CommandResult;

/** A telnet client */
public class Telnet extends TcpTextClient implements ILogin {

	/** Executes command on remote host, return the result of the command */
	public static CommandResult execute(String userAtHost, String password, String command) throws IOException {
		// split userAtHost string
		int pos = userAtHost.indexOf("@");
		if (pos <= 0) {
			throw new IOException("first parameter should like username@some.com");
		}
		
		String user = userAtHost.substring(0, pos);
		String host = userAtHost.substring(pos + 1);
		
		Telnet client = null;
		try {
			client = new Telnet();
			client.setHost(host);
			if (client.login(user, password))
				return client.run(command);
			else
				throw new IOException(client.getLastException());
		} finally {
			if (client != null)
				client.logout();
		}
	}

	
	/** default port of telnet */
	public static final int TELNET_PORT = 23;

	// telnet protocol constants: command
	private static final int IAC = 255; // Interpret As Command
	private static final int WILL = 251; // Will option
	private static final int WONT = 252; // Won't option
	private static final int DO = 253; // Do option
	private static final int DONT = 254; // Don't option
	private static final int SB = 250; // Sub-negotiation Begin
	private static final int SE = 240; // Sub-negotiation End

	// telnet options
	private static final int ECHO = 1; // Echo option
	private static final int RECONNECT = 3; // re-connect
	private static final int STATUS = 5; // status query
	private static final int TERMINAL_TYPE = 24; // Terminal type
	private static final int NAWS = 31; // Negotiate About Window Size
	private static final int TERMINAL_SPEED = 32; // Terminal Speed
	private static final int REMOTE_FLOW_CONTROL = 33; // Remote Flow Control
	private static final int X_DISPLAY_LOCATION = 35; // X Display Location
	private static final int NEW_ENVIRONMENT = 39; // New Environment

	/** get command name string */
	protected static String commandName(int cmd) {
		switch (cmd) {
		case IAC:
			return "IAC";
		case WILL:
			return "WILL";
		case WONT:
			return "WONT";
		case DO:
			return "DO";
		case DONT:
			return "DONT";
		case SB:
			return "SB";
		case SE:
			return "SE";
		default:
			return "" + cmd;
		}
	}

	/** get option name string */
	private static String optionName(int option) {
		switch (option) {
		case ECHO:
			return "ECHO";
		case RECONNECT:
			return "RECONNECT";
		case STATUS:
			return "STATUS";
		case TERMINAL_TYPE:
			return "TERMINAL_TYPE";
		case NAWS:
			return "NAWS";
		case TERMINAL_SPEED:
			return "TERMINAL_SPEED";
		case REMOTE_FLOW_CONTROL:
			return "REMOTE_FLOW_CONTROL";
		case X_DISPLAY_LOCATION:
			return "X_DISPLAY_LOCATION";
		case NEW_ENVIRONMENT:
			return "NEW_ENVIRONMENT";
		default:
			return "" + option;
		}
	}

	/** CRLF */
	private String CRLF = "\r\n";

	/** command prompt */
	private String prompt = "$";

	/** terminal type */
	private String terminalType = "VT100";

	/** welcome message */
	private String welcomeMsg = "";
	
	
	public Telnet() {
		super();
	}

	/** log for debug */
	protected void debug(String str) {
//		if (isEcho()) System.out.println(str);
	}

	/** get welcome message */
	public String getWelcomeMsg() {
		return welcomeMsg;
	}

	/** get prompt char */
	public String getPromptChar() {
		return prompt;
	}
	
	/** set prompt char */
	public void setPromptChar(String s) {
		prompt = s;
	}
	
	@Override
	public int getDefaultPort() {
		return TELNET_PORT;
	}

	/** perform telnet negotiation */
	private void telnetNegotiation() throws IOException {
		while (reader.peek() == IAC) {
			reader.readByte();
			int command = reader.readByte();
			int option = reader.readByte();
			debug("SERVER: " + commandName(command) + " " + optionName(option));

			switch (command) {
			case WILL:
				handleWillOption(option);
				break;
			case DO:
				handleDoOption(option);
				break;
			case WONT:
				handleWontOption(option);
				break;
			case DONT:
				handleDontOption(option);
				break;
			case SB:
				telnetSubnegotiation(option);
				break;
			}

			sleep(50);
		}
	}

	/** perform telnet sub negotiation */
	private void telnetSubnegotiation(int option) throws IOException {
		// SERVER: IAC, SB, 24(option), 1(Send), IAC, SE
		if (option == TERMINAL_TYPE) {
			int b = reader.readByte();
			if (b != 1) {
				throwIOException(new IllegalArgumentException("the byte after option should be 1"));
				return;
			}
				
		}

		if (reader.readByte() == IAC) {
			int command = reader.readByte();
			if (command == SE)
				return;
		}

		throwIOException(new IllegalArgumentException());
	}

	/** send telnet command */
	private void sendTelnetCommand(int command, int option) throws IOException {
		debug("CLIENT: " + commandName(command) + " option=" + optionName(option));
		byte[] cmd = { (byte) IAC, (byte) command, (byte) option };
		writer.write(cmd);
		writer.flush();
	}

	/** send terminal type */
	private void sendTerminalType() throws IOException {
		byte[] terminalTypeBytes = terminalType.getBytes(StandardCharsets.US_ASCII);
		byte[] response = new byte[terminalTypeBytes.length + 6];

		response[0] = (byte) IAC;
		response[1] = (byte) SB;
		response[2] = TERMINAL_TYPE;
		response[3] = 0; // IS command
		System.arraycopy(terminalTypeBytes, 0, response, 4, terminalTypeBytes.length);
		response[response.length - 2] = (byte) IAC;
		response[response.length - 1] = (byte) SE;

		debug("CLIENT: TerminalType " + terminalType);
		writer.write(response);
		writer.flush();
	}

	/** handle will option from server */
	private void handleWillOption(int option) throws IOException {
		switch (option & 0xFF) {
		case ECHO:
			sendTelnetCommand(DO, ECHO);
			break;
		default:
			sendTelnetCommand(DONT, option);
			break;
		}
	}

	/** handle do option from server */
	private void handleDoOption(int option) throws IOException {
		switch (option & 0xFF) {
		case TERMINAL_TYPE:
			sendTelnetCommand(WILL, TERMINAL_TYPE);
			sendTerminalType();
			break;
		case ECHO:
			sendTelnetCommand(WILL, ECHO);
			break;
		default:
			sendTelnetCommand(WONT, option);
			break;
		}
	}

	/** handle wont option from server */
	private void handleWontOption(int option) {
		// when server refuses an option - no action is needed
		debug("Server refused option: " + option);
	}

	/** handle dont option from server */
	private void handleDontOption(int option) throws IOException {
		sendTelnetCommand(WONT, option);
	}

	/** Wait for specific pattern in server response */
	public String waitFor(String pattern, int timeoutMs) throws IOException {
		StringBuilder response = new StringBuilder();
		byte[] buffer = new byte[2048];
		long startTime = System.currentTimeMillis();

		while (System.currentTimeMillis() - startTime < timeoutMs) {
			if (reader.hasData()) {
				int bytesRead = reader.read(buffer);
				if (bytesRead > 0) {
					String chunk = new String(buffer, 0, bytesRead, reader.getCharset());
					echoPrint(chunk);
					response.append(chunk);
					String ss = response.toString();
					if (ss.contains(pattern)) {
						return response.toString();
					}
				}
			} else {
				sleep(50);
			}

		}

		throwIOException(null, "Timeout waiting for pattern: ");
		return pattern;
	}

	/** Wait for command prompt */
	private String waitForPrompt(int timeoutMs) throws IOException {
		return waitFor(prompt + " ", timeoutMs);
	}

	/** cut leading command line and trailing prompt line from the response */
	private String cutPromptLine(String response, String command) {
		int len = response.length();
		int start = 0;
		if (command != null && response.startsWith(command)) {
			start = command.length();
			if (start < len && response.charAt(start) == '\r') 
				start++;
			if (start < len && response.charAt(start) == '\n') 
				start++;
		}
			
		// position of the last char
		int i = len - 1;
		
		// skip last blank
		if (i >=start  && response.charAt(i) == ' ')  i--;
		
		// skip prompt char 
		if (i >=start && !("" + response.charAt(i)).equals(prompt)) {
			return response;
		} else {
			i--;
		}
		
		while(i >= start) {
			char c = response.charAt(i--);
			
			if (c == '\n') return response;
			
			if (c == '[') {
				if (i >=start && response.charAt(i) == '\n') i--;
				if (i >=start && response.charAt(i) == '\4') i--;
				return response.substring(start, i);
			}
		}
		
		return start > 0 ? response.substring(start) : response;
	}

	/** Sends a command and wait for the response */
	public CommandResult run(String cmd) throws IOException {
		if (isEcho())
			echoPrint(cmd);
		send(cmd + CRLF);

		String stdout = "";
		int exitStatus = 0;
		try {
			stdout = waitForPrompt(getTimeout());
			if (stdout.endsWith(prompt) || stdout.endsWith(prompt + " "))
				stdout = cutPromptLine(stdout, cmd);
		} catch (Exception e) {
			exitStatus = -1;
		}
		return new CommandResult(exitStatus, stdout);
	}

	@Override
	public boolean login(String username, String password) {
		try {			
			if (connect()) {
				sleep(500);
				telnetNegotiation();

				// Wait for login prompt
				String loginResponse = waitFor("login:", getTimeout());
				if (!loginResponse.contains("login:")) {
					throwIOException("Login prompt not received");
					return false;
				}

				// Send user name
				send(username + CRLF);

				// Wait for password prompt
				String passwordResponse = waitFor("Password:", getTimeout());
				if (!passwordResponse.contains("Password:")) {
					throwIOException("Password prompt not received");
					return false;
				}

				// Send password
				send(password + CRLF);

				// Set prompt based on user type
				this.prompt = "root".equals(username) ? "#" : "$";

				// Wait for command prompt
				welcomeMsg = cutPromptLine(waitForPrompt(getTimeout()), null);

				return true;
			}
		} catch (IOException e) {
			setLastException(e);
		}

		return false;
	}

}
