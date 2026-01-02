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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** A Writer that supports both binary and text reading from a stream */
public class StreamWriter extends BufferedOutputStream {

	protected Charset charset;
	
	 /** Constructor: create from an OutputStream */
	public StreamWriter(OutputStream out) {
		this(out, null);
	}

	/** Constructor: create from an InputStream with specified charset*/
    public StreamWriter(OutputStream out, Charset charset) {
        super(new BufferedOutputStream(out));
        this.out = out;
        if (charset == null) charset =  StandardCharsets.UTF_8;
        this.charset = charset;
    }
    
    @Override
    public void write(byte b[], int off, int len) throws IOException {
    	this.out.write(b, off, len);
    }
    
    /** print a string */
    public void print(String str) throws IOException {    	
    	write(str.getBytes(charset));
    }
    
    /** print a line  */
    public void println(String str) throws IOException {
    	print(str);
    	print("\r\n");
    }
    
    
}
