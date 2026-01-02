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

import top.whitehat.util.CommandResult;

public class TelnetTest {

	static String  host = Config.local.host;
	static String  user = Config.local.user;
	static String  password = Config.local.password;
	
	public static void main(String[] args) {
		Telnet t = new Telnet();
		t.setHost(host);
		if (t.login(user, password)) {
			System.out.println(t.getWelcomeMsg());
			
			// Execute demonstration commands
			String[] commands = { "pwd", "whoami", "uname -a", "ls -la", "date" };

			System.out.println("\n=== Command Execution Results ===\n");

			for (String cmd : commands) {
				try {
					System.out.println("Command: " + cmd);
					CommandResult ret = t.run(cmd);
					System.out.println(ret);
					System.out.println();
					Thread.sleep(500); // pause between commands
				} catch (Exception e) {
					System.err.println("Error executing command '" + cmd + "': " + e.getMessage());
				}
			}
		} else {
			t.getLastException().printStackTrace();
		}
	}
}
