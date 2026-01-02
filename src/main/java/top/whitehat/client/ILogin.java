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


/** A server that can login */
public interface ILogin {
		
	/** set the host */
	public void setHost(String host);
	
	/** login with specified user and password
	 * 
	 * @param username  The user name
	 * @param password  The password
	 * @return return true if login success, return false if failed.
	 */
	public boolean login(String username, String password);	
	
	
	/** logout
	 * @return return true if logout success, return false if failed.
	 */
	public boolean logout();
	
}
