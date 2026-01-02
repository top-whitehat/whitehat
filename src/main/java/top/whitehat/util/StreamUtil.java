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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtil {
	
	/** Convert a ByteBuffer object to InputStream object
	 * 
	 * @param obj
	 * @return
	 */
	public static InputStream asInput(ByteArray obj) {
		return new ByteArrayInputStream(obj);
	}
	
	/** Convert a ByteBuffer object to InputStream object
	 * 
	 * @param obj
	 * @return
	 */
	public static OutputStream asOutput(ByteArray obj) {
		ByteArrayOutputStream ret = new ByteArrayOutputStream(obj);
		//TODO
		return ret;
	}
	
	/**
     * Reads all bytes from an InputStream (Java 8 compatible version)
     * @param inputStream the input stream to read from
     * @return a byte array containing all the bytes read from the input stream
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the input stream is null
     */
    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        
        try (java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream()) {
            byte[] data = new byte[4096]; // buffer
            int bytesRead;
            
            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            
            buffer.flush();
            return buffer.toByteArray();
        }
    }
}
