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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** A Writer that supports both binary and text reading from a OutputStream */
public class TcpWriter extends OutputStream {

	private Charset charset;

	private OutputStream out;

	/** Constructor: create from an OutputStream */
	public TcpWriter(OutputStream out) {
		this(out, null);
	}

	/** Constructor: create from an InputStream with specified charset */
	public TcpWriter(OutputStream out, Charset charset) {
		super();
		this.out = out;
		this.charset = charset == null ? StandardCharsets.UTF_8 : charset;
	}
	
	/** get charset */
	public Charset getCharset() {
		return this.charset;
	}
	
	/** set charset */
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	/** Print a string */
	public void print(String str) throws IOException {
		out.write(str.getBytes(charset));
	}

	/** Print a line  */
	public void println(String str) throws IOException {
		print(str);
		print("\r\n");
	}
	
	/** Keep the last n bytes of the byte array and move them to the 
	 *  beginning of the array, discarding the other bytes. */
	public void remain(int nBytes)  throws IOException {
		if (nBytes > 0 && out instanceof TcpOutputStream) {
			TcpOutputStream stream = (TcpOutputStream)out;
			stream.remain(nBytes);
		} else {
			flush(); //TODO:
		}
	}

	public void flush() throws IOException {
		out.flush();
	}
	
	public void close() throws IOException {
		out.close();
    }

	/** Return count of available bytes*/
	public int available() {
		if (out instanceof TcpOutputStream) {
			return ((TcpOutputStream)out).available();
		} else {
			return 0;
		}
	}
	
	/** Convert to ByteBuffer */
	public ByteBuffer toByteBuffer() {
		if (out instanceof TcpOutputStream) {
			return ((TcpOutputStream)out).toByteBuffer();
		} else {
			return null;
		}
	}

}
