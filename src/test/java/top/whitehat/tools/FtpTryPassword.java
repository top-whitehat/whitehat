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
package top.whitehat.tools;

import java.util.ArrayList;
import java.util.List;

import top.whitehat.client.Config;





public class FtpTryPassword {

	static String  host = Config.local.host;
	static String  user = Config.local.user;
	static String  password = Config.local.password;
	
	public static void main(String[] args) throws InstantiationException {		
		List<String> passwords = new ArrayList<String>();
		passwords.add("12345");
		passwords.add("1234567");
		passwords.add(password);
		
//		PasswordTester tester = new PasswordTester(FtpClient.class, host);
//				
//		System.out.println(tester.tryPasswords(user, passwords));
	}
}
