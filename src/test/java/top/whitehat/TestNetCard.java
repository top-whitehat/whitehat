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



public class TestNetCard {

	static int count = 0;
	
	public static void main(String[] args) {
		NetCard card = NetCard.inet();
		System.out.println(card);
		
		card.onPacket(pkt->{
			System.out.println(pkt);
			if (count++ > 10) {
				card.stop();
			}
		});
				 
		card.start();
		
	}
}
