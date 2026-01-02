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
package top.whitehat.util;

import static org.junit.Assert.assertEquals;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;






public class CompositeByteArrayTest {
	
	@Test
	public void testLimit() {
		ByteArray b1 = ByteArray.wrap("Hello\n".getBytes());
		ByteArray b2 = ByteArray.wrap("World.\n".getBytes());
		ByteArray b3 = ByteArray.wrap("I'm here\n".getBytes());
		CompositeByteArray bb = new CompositeByteArray(b1, b2, b3);
		
		assertEquals(bb.limit(), b1.limit() + b2.limit() + b3.limit());
	}
	
	@Test
	public void testGet() {
		ByteArray b1 = ByteArray.wrap("Hello".getBytes());
		ByteArray b2 = ByteArray.wrap("World.\n".getBytes());
		ByteArray b3 = ByteArray.wrap("I'm here\n".getBytes());
		CompositeByteArray bb = new CompositeByteArray(b1, b2, b3);
		
		byte[] dst = new byte[8];
		bb.get(dst);
		assertEquals(new String(dst), "HelloWor");
	}
	
	@Test
	public void testPut() {
		ByteArray b1 = ByteArray.wrap("Hello".getBytes());
		ByteArray b2 = ByteArray.wrap("World.\n".getBytes());
		ByteArray b3 = ByteArray.wrap("I'm here\n".getBytes());
		CompositeByteArray bb = new CompositeByteArray(b1, b2, b3);
		
		String str = "HELLOWORLD.\nABBCD";
		bb.writerIndex(0);
		bb.put(str.getBytes());
		
		byte[] dst = new byte[str.length()];
		bb.get(dst);
		assertEquals(new String(dst), str);
	}
	
	
	@SuppressWarnings("deprecation")
	@Test
	public void testStream() throws IOException {
		String[] words = {"Hello\n", "World\n", "Here\n"};
		ByteArray b1 = ByteArray.wrap(words[0].getBytes());
		ByteArray b2 = ByteArray.wrap(words[1].getBytes());
		ByteArray b3 = ByteArray.wrap(words[2].getBytes());
		
		CompositeByteArray bb = new CompositeByteArray(b1, b2, b3);
		
		InputStream st = StreamUtil.asInput(bb);
		DataInputStream ds = new DataInputStream(st);
		
		String s;
		int count = 0;
		while((s = ds.readLine()) != null) {
			assertEquals(words[count++], s + "\n");
		}
		
	}

}
