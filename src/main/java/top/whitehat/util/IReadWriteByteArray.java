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

/**
 * ​​IReadWrite can read from and write to a ByteArray object.​
 * 
 * @author JoStudio
 *
 */
public interface IReadWriteByteArray {
	
	/**  
	 * Read from the specified byte array at the current position and the specified bit offset, and increase the position after the read.​
	 * 
	 * @param b           The ByteArray object
	 * @param bitOffset   Bit offsets in a byte range from 0 to 7, where 0 represents the first bit (bit 0).​
	 * @return  Return the number of bits that have been read
	 */
	public int readByteArray(ByteArray b, int bitOffset);
	
	/** Write to the specified byte array at the current position and the specified bit offset, and increase the position after the write.​
	 * 
	 * @param b           The ByteArray object
	 * @param bitOffset   Bit offsets in a byte range from 0 to 7, where 0 represents the first bit (bit 0).​
	 * @return  Return the number of bits that have been written
	 */
	public int writeByteArray(ByteArray b, int bitOffset);
	
	/** Read from the specified byte array at the current position, and increase the position after the read.​
	 * 
	 * @param b           The ByteArray object
	 * @return  The number of bits that have been read
	 */
	public default int readByteArray(ByteArray b) {
		return readByteArray(b, 0);
	}

	/**  Write to the specified byte array at the current position, and increase the position after the write.​
	 * 
	 * @param b           The ByteArray object
	 * @return  The number of bits that have been written
	 */
	public default int writeByteArray(ByteArray b) {
		return writeByteArray(b, 0);
	}
	
}
