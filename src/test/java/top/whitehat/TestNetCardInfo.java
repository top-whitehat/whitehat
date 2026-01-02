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
package top.whitehat;

import java.util.List;






public class TestNetCardInfo {


	/**
	 * Main method to retrieve and display network adapter information Demonstrates
	 * usage of Windows IP Helper API through JNA
	 */
	public static void main(String[] args) {
		List<NetCard> infos = NetCard.list();
		for (NetCard info : infos) {
			System.out.println(info);
		}
	}


}
