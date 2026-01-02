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

import java.nio.ByteBuffer;

public class Util {

	public static void print(ByteBuffer b) {
		print(b.array(), 0, b.limit());
	}
	
	public static void print(byte[] bs) {
		print(bs, 0, bs.length);
	}
	
	public static void print(byte[] bs, int index, int len) {
		StringBuilder sb = new StringBuilder();
		for(int i=index; i<len; i++) {
			sb.append(String.format("%02x ", bs[i]));
		}
		System.out.print(sb.toString());
	}
	
	public static void print(Object ... args) {
		for(Object arg : args) {
			if (arg instanceof ByteBuffer) {
				print((ByteBuffer)arg);
			} else if (arg instanceof byte[]) {
				print((byte[])arg);
			} else {
				System.out.print(arg == null ? "null" : arg.toString());
			}
			System.out.print(" ");
		}
	}
	
	public static void println(Object ... args) {
		print(args);
		System.out.println();
	}
}
