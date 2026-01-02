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
import java.nio.ByteBuffer;



public class TestStream {

	public static void main(String[] args) {
		testStream();
	}
	
	
	static void testStream() {
		ByteBuffer b1 = ByteBuffer.allocate(4);
		
		b1.putInt(0x12345678);
		b1.flip();
		Util.println("b1 =", b1);
//		Util.println(b1.remaining());
//		System.out.println(b1);

		try (TcpInputStream stream = new TcpInputStream()) {
			stream.append(b1);
			stream.append(b1);
			Util.println(stream.getBuffer());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		Util.println(b0);
//		b0.put(b1);
		
//		b0.putShort((short)0xABCD);
//		System.out.println(b0);
//		b0 = appendBuffer(b0, b1);
//		System.out.println(b0);
//		Util.println("b0 =", b0);		
//		Util.println(b0);
//		Util.println(b0);
	}
	
}
