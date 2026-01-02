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

import java.util.ArrayList;
import java.util.List;

/**
 *  Read/write ByteArray objects as one ByteArray
 */
public class CompositeByteArray extends ByteArray {

	/** A List to hold ByteArray objects */
	protected List<ByteArray> buffers = new ArrayList<ByteArray>();

	/** Constructor */
	public CompositeByteArray(ByteArray... bs) {
		for (ByteArray b : bs) {
			b.rewind();
			buffers.add(b);
		}
		buffersChanged(true);
	}
	
	public List<ByteArray> getBuffers() {
		return buffers;
	}

	/** Append a ByteArray object */
	public CompositeByteArray append(ByteArray b) {
		boolean updateWriterIndex = writerIndex() == limit();
		b.rewind();
		buffers.add(b);
		buffersChanged(updateWriterIndex);
		return this;
	}
	
	/** Remove a ByteArray object */
	public CompositeByteArray remove(ByteArray b) {
		boolean updateWriterIndex = writerIndex() == limit();
		for(int i=0; i<buffers.size(); i++) {
			if (buffers.get(i).equals(b)) {
				buffers.remove(i);
				break;
			}
		}
		buffersChanged(updateWriterIndex);
		return this;
	}

	/** return the last ByteArray */
 	public ByteArray lastArray() {
		return buffers.size() == 0 ? null : buffers.get(buffers.size() - 1);
	}

	/**
	 * Find index
	 * 
	 * @param index          the index
	 * @param throwException Indicate whether throw exception when index is invalid
	 * @return return -1 if not found. return positive long value if found. The
	 *         first 32-bit of return value is the index of ByteArray object in the
	 *         buffers. The second 32-bit of return value is the offset in the
	 *         ByteArray object.
	 */
	protected long findIndex(int index, boolean throwException) {
		if (index >= 0) {
			for (int i = 0; i < buffers.size(); i++) {
				ByteArray b = buffers.get(i);
				int bytes = b.limit();
				if (index < bytes) {
					long ret =  ((long)i)  << 32 | (long)index;
					return ret;
				} else {
					index -= bytes;
				}
			}
		}

		if (throwException)
			throw new IndexOutOfBoundsException();
		return -1;
	}

	/** find index */
	protected long findIndex(int index) {
		return findIndex(index, true);
	}

	/** called when buffers changed */
	protected void buffersChanged(boolean updateWriterIndex) {
    	if (updateWriterIndex) writerIndex(limit());
	}
	
	@Override
	public int limit() {
		int count = 0;
		for (ByteArray buf : buffers) {
			count += buf.limit();
		}
		return count;
	}

	@Override
	public int capacity() {
		ByteArray last = lastArray();
		return limit() + (last == null ? 0 : last.writableBytes());
	}

	/** Set capacity */
	public CompositeByteArray capacity(int cap) {
		boolean updateWriterIndex = writerIndex() == limit();
		
		if (cap < 0)
			throw new IllegalArgumentException("capacity should not be negative");

		int delta = cap - capacity();
		if (delta == 0)
			return this;
		else if (delta < 0)
			throw new IllegalAccessError("cannot reduce capacity");

		ByteArray lastArr = lastArray();
		if (lastArr == null) {
			buffers.add(new ByteArray(delta));
		} else {
			lastArr.capacity(lastArr.capacity() + delta);
		}

		buffersChanged(updateWriterIndex);
		onArrayChange();
		return this;
	}

	@Override
	public byte getByte(int index) {
		long location = findIndex(index);
		int bufferIndex = (int) (location >> 32);
		int i = (int) (location & 0xFFFFFFFFL);
		return buffers.get(bufferIndex).getByte(i);
	}

	@Override
	public void putByte(int index, byte b) {
		long location = findIndex(index);
		int bufferIndex = (int) (location >> 32);
		int i = (int) (location & 0xFFFFFFFFL);
		buffers.get(bufferIndex).putByte(i, b);
	}
	
	/** Transfers bytes from this ByteArray into the given destination array */
	@Override
	protected void getArray(int index, byte[] dst, int offsetDst, int length) {
//		arraycopy(array(), index + arrayOffset(), dst, offsetDst, length);
		if (length == 0) return;
		checkFromIndexSize(index, length, limit());
		checkFromIndexSize(offsetDst, length, dst.length);
		
		long location = findIndex(index);
		int bufferIndex = (int) (location >> 32);
		int bufferOffset = (int) (location & 0xFFFFFFFFL);
		
		int count = 0;
		while (length > 0) {
			ByteArray buf = buffers.get(bufferIndex);
			int readableBytes = buf.limit() - bufferOffset;
			int copyLength = readableBytes > length ? length : readableBytes;
			arraycopy(buf.array(), bufferOffset, dst, offsetDst + count, copyLength);
			count += copyLength;
			length -= copyLength;
			if (copyLength == readableBytes) {
				bufferIndex++;
				bufferOffset = 0;
			}
		}
	}
	
	/** Transfers bytes from the given destination array into this ByteArray */
	@Override
	protected void putArray(int index, byte[] src, int offsetSrc, int length) {
		// arraycopy(src, offsetSrc, array(), index + arrayOffset(), length);
		checkFromIndexSize(offsetSrc, length, src.length);
		
		long location = findIndex(index);
		int bufferIndex = (int) (location >> 32);
		int bufferOffset = (int) (location & 0xFFFFFFFFL);
		
		ensureCapacity(index + length);
		int count = 0;
		while (length > 0) {
			ByteArray buf = buffers.get(bufferIndex);
			int writableBytes = buf.limit() - bufferOffset;
			int copyLength = writableBytes > length ? length : writableBytes;
			arraycopy(src, offsetSrc + count, buf.array(), bufferOffset, copyLength);
			count += copyLength;
			length -= copyLength;
			if (copyLength == writableBytes) {
				bufferIndex++;
				bufferOffset = 0;
			}
		}
		
		limitMinimum(index + length);
		writerIndexMinimum(index + length);
	}


}
