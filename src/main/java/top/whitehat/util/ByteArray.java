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

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * ByteArray is a utility class that provides comprehensive byte array read and write operations
 * with support for buffer-like functionality including reader and writer indexes, limits,
 * capacity management, and byte order handling. It serves as a wrapper around byte arrays
 * to provide a more convenient API for handling binary data with similar functionality
 * to Java's ByteBuffer but with additional features specific to buffer management.
 * 
 * <h3>Usage Example</h3>
 * <pre>
 * // Allocate a new ByteArray with specific capacity
 * ByteArray buffer = ByteArray.allocate(1024);
 * 
 * // Write data to the buffer
 * buffer.putByte((byte) 0x41); // Write 'A'
 * buffer.putInt(12345);
 * 
 * // Read data from the buffer
 * buffer.rewind(); // Reset reader index to beginning
 * byte b = buffer.getByte(); // Read the first byte
 * int i = buffer.getInt();   // Read the integer
 * </pre>
 * 
 * @version 0.91
 */
public class ByteArray {

	public final static int LOW_BITS = 3;

	public final static int HIGH_BITS = 7;

	/** A value means unknown */
	protected final static int UNKNOWN = -1;

	/**
	 * Allocates a new ByteArray object with the specified capacity. The new ByteArray's
	 * limit will be set to its capacity. This method creates a heap-based ByteArray
	 * using a standard byte array as backing storage.
	 * 
	 * @param capacity The initial capacity of the ByteArray in bytes
	 * @return A new ByteArray with the specified capacity
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(1024); // Creates a 1KB buffer
	 * </pre>
	 */
	public static ByteArray allocate(int capacity) {
		return new ByteArray(capacity);
	}

	/**
	 * Allocates a new direct ByteArray object with the specified capacity. The new
	 * ByteArray's limit will be set to its capacity. Direct ByteArrays are typically
	 * allocated outside the Java heap and may provide better performance for I/O
	 * operations, but they may also have higher allocation costs.
	 * 
	 * @param capacity The initial capacity of the direct ByteArray in bytes
	 * @return A new direct ByteArray with the specified capacity
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray directBuffer = ByteArray.allocateDirect(1024); // Creates a 1KB direct buffer
	 * </pre>
	 */
	public static ByteArray allocateDirect(int capacity) {
		ByteBuffer b = ByteBuffer.allocateDirect(capacity);
		return new ByteArray(b);
	}

	/**
	 * Wraps a byte array with specified offset and length to create a new ByteArray
	 * instance. The new ByteArray will be backed by the given byte array; that is,
	 * modifications to the ByteArray will cause the array to be modified and vice versa.
	 * This is useful when you need to work with a subset of an existing byte array
	 * without copying the data.
	 * <p>
	 * The new ByteArray's capacity will be {@code array.length}, its position will be
	 * {@code offset}, its limit will be {@code offset + length}, its mark will be
	 * undefined, and its byte order will be {@link ByteOrder#BIG_ENDIAN BIG_ENDIAN}.
	 * 
	 * @param buf    The array that will back the new buffer
	 * @param offset The offset of the subarray to be used. If negative, it means count
	 *               from the end of the buffer (e.g., -1 means the last element)
	 * @param length The length of the subarray to be used; use UNKNOWN (-1) for the
	 *               remaining length from the offset
	 * @return The new ByteArray object backed by the specified array
	 *
	 * @throws IndexOutOfBoundsException If the preconditions on the {@code offset}
	 *                                   and {@code length} parameters do not hold
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05};
	 * ByteArray buffer = ByteArray.wrap(data, 1, 3); // Wraps bytes 0x02, 0x03, 0x04
	 * </pre>
	 */
	public static ByteArray wrap(byte[] buf, int offset, int length) {
		if (offset < 0)
			offset = buf.length + offset;
		int len = length == UNKNOWN ? buf.length  - offset : length;
		ByteArray b = new ByteArray(buf, 0, len);
		b.maxCapacity(UNKNOWN);
		b.readerIndex(offset);
		b.writerIndex(offset + len);
		return b;
	}

	/**
	 * Wraps an entire byte array to create a new ByteArray instance. The new
	 * ByteArray will be backed by the given byte array; that is, modifications to
	 * the buffer will cause the array to be modified and vice versa. The new
	 * ByteArray capacity will be {@code array.length}, its position will be 0,
	 * its limit will be the full length, its mark will be undefined, and its
	 * byte order will be {@link ByteOrder#BIG_ENDIAN BIG_ENDIAN}.
	 * 
	 * @param buf The array that will back this ByteArray
	 * @return The new buffer backed by the specified array
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * byte[] data = {0x01, 0x02, 0x03, 0x04};
	 * ByteArray buffer = ByteArray.wrap(data); // Wraps the entire array
	 * </pre>
	 */
	public static ByteArray wrap(byte[] buf) {
		return wrap(buf, 0, UNKNOWN);
	}

	/**
	 * Compares two byte arrays lexicographically within the specified ranges. This
	 * method compares up to the specified length of bytes starting from the given
	 * offsets in each array. The comparison is performed byte by byte from left to
	 * right, returning as soon as a difference is found. This is useful for
	 * comparing sections of binary data or implementing ordered collections of
	 * byte arrays.
	 * 
	 * @param arr1    The first byte array to compare
	 * @param offset1 The start offset in the first byte array (must be non-negative)
	 * @param arr2    The second byte array to compare
	 * @param offset2 The start offset in the second byte array (must be non-negative)
	 * @param length  The number of bytes to compare from each array (must be non-negative)
	 * @return 0 if the specified ranges of both arrays are equal, 1 if the first differing
	 *         byte in arr1 is greater than the corresponding byte in arr2, -1 if the
	 *         first differing byte in arr1 is less than the corresponding byte in arr2
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * byte[] array1 = {0x01, 0x02, 0x03};
	 * byte[] array2 = {0x01, 0x02, 0x04};
	 * int result = ByteArray.compare(array1, 0, array2, 0, 3); // Returns -1
	 * </pre>
	 */
	public static int compare(byte[] arr1, int offset1, byte[] arr2, int offset2, int length) {
		if (arr1.length < offset1 + length)
			return -1;

		if (arr2.length < offset2 + length)
			return 1;

		for (int i = 0; i < length; i++) {
			if (arr1[offset1 + i] != arr2[offset2 + i]) {
				return arr1[offset1 + i] < arr2[offset2 + i] ? -1 : 1;
			}
		}

		return 0;
	}

	/**
	 * Compares two entire byte arrays lexicographically. This method compares the
	 * entire content of both arrays, starting from index 0. It's a convenience
	 * method that compares the full length of the first array, which is equivalent
	 * to calling compare(arr1, 0, arr2, 0, arr1.length). This is useful for
	 * comparing complete binary data values or implementing equality checks for
	 * byte array containers.
	 * 
	 * @param arr1 The first byte array to compare
	 * @param arr2 The second byte array to compare
	 * @return 0 if both arrays are equal, 1 if the first differing byte in arr1
	 *         is greater than the corresponding byte in arr2, -1 if the first
	 *         differing byte in arr1 is less than the corresponding byte in arr2
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * byte[] array1 = {0x01, 0x02, 0x03};
	 * byte[] array2 = {0x01, 0x02, 0x03};
	 * int result = ByteArray.compare(array1, array2); // Returns 0 (equal)
	 * </pre>
	 */
	public static int compare(byte[] arr1, byte[] arr2) {
		return compare(arr1, 0, arr2, 0, arr1.length);
	}

	/**
	 * Finds the first mismatch between two byte arrays within the specified ranges.
	 * This method compares up to the specified length of bytes starting from the
	 * given offsets in each array and returns the position of the first differing
	 * byte. If no mismatch is found within the specified range, it returns 0.
	 * This is useful for identifying where two byte sequences start to differ.
	 * 
	 * @param arr1    The first byte array to compare
	 * @param offset1 The start offset in the first byte array (must be non-negative)
	 * @param arr2    The second byte array to compare
	 * @param offset2 The start offset in the second byte array (must be non-negative)
	 * @param length  The number of bytes to compare from each array (must be non-negative)
	 * @return 0 if the specified ranges of both arrays match completely, otherwise
	 *         returns the absolute index of the first mismatch in arr1
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * byte[] array1 = {0x01, 0x02, 0x03};
	 * byte[] array2 = {0x01, 0x05, 0x03};
	 * int mismatchPos = ByteArray.mismatchArray(array1, 0, array2, 0, 3); // Returns 1
	 * </pre>
	 */
	protected static int mismatchArray(byte[] arr1, int offset1, byte[] arr2, int offset2, int length) {
		if (arr1.length < offset1 + length)
			return offset1 + length;

		if (arr2.length < offset2 + length)
			return offset2 + length;

		for (int i = 0; i < length; i++) {
			if (arr1[offset1 + i] != arr2[offset2 + i]) {
				return offset1 + i;
			}
		}

		return 0;
	}

	/**
	 * Transfers bytes from this ByteArray into the given destination array. This
	 * method copies the specified number of bytes from this ByteArray starting at
	 * the given index to the destination array at the specified offset. This is
	 * useful for extracting data from the buffer to a standard byte array for
	 * processing or storage. The method uses System.arraycopy for efficient copying.
	 * 
	 * @param index The starting index in this ByteArray from which to copy bytes
	 * @param dst The destination array where bytes will be copied
	 * @param offsetDst The starting offset in the destination array
	 * @param length The number of bytes to copy
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(10);
	 * byte[] source = {0x01, 0x02, 0x03, 0x04};
	 * buffer.putArray(0, source, 0, source.length); // Copy source to buffer
	 * byte[] dest = new byte[4];
	 * buffer.getArray(0, dest, 0, 4); // Copy from buffer to dest
	 * </pre>
	 */
	protected void getArray(int index, byte[] dst, int offsetDst, int length) {
		arraycopy(array(), index + arrayOffset(), dst, offsetDst, length);
	}

	/**
	 * Transfers bytes from the given source array into this ByteArray. This method
	 * copies the specified number of bytes from the source array starting at the
	 * specified offset to this ByteArray at the given index. If necessary, the
	 * capacity of this ByteArray is expanded to accommodate the data. The write
	 * index and limit are updated accordingly to reflect the new data. This is
	 * useful for importing data from standard byte arrays into the buffer.
	 * 
	 * @param index The starting index in this ByteArray where bytes will be copied
	 * @param src The source array containing bytes to be copied
	 * @param offsetSrc The starting offset in the source array
	 * @param length The number of bytes to copy
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(10);
	 * byte[] data = {0x01, 0x02, 0x03, 0x04};
	 * buffer.putArray(0, data, 0, data.length); // Copy data to buffer at index 0
	 * </pre>
	 */
	protected void putArray(int index, byte[] src, int offsetSrc, int length) {
		ensureCapacity(index + length);
		arraycopy(src, offsetSrc, array(), index + arrayOffset(), length);
		limitMinimum(index + length);
		writerIndexMinimum(index + length);
	}

	/**
	 * Checks that the specified range (fromIndex, size) is within the bounds of an array
	 * with the given length. This method validates that fromIndex is non-negative,
	 * size is non-negative, and that the range [fromIndex, fromIndex + size) is
	 * contained within [0, length). If the check fails, an ArrayIndexOutOfBoundsException
	 * is thrown. This is a utility method used internally to validate array access
	 * operations and prevent buffer overflows.
	 * 
	 * @param fromIndex The starting index of the range to check (inclusive)
	 * @param size The size of the range to check
	 * @param length The total length of the array being checked against
	 * @throws ArrayIndexOutOfBoundsException if the range is invalid
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * // This method is typically called internally by other methods
	 * // to validate ranges before array operations
	 * try {
	 *     checkFromIndexSize(5, 3, 10); // Valid: [5, 8) is within [0, 10)
	 *     checkFromIndexSize(8, 5, 10); // Invalid: [8, 13) extends beyond [0, 10)
	 * } catch (ArrayIndexOutOfBoundsException e) {
	 *     System.out.println("Invalid range");
	 * }
	 * </pre>
	 */
	protected void checkFromIndexSize(int fromIndex, int size, int length) {
		if (fromIndex >= 0 && size >= 0 && fromIndex <= length) {
			if (fromIndex + size <= length)
				return;
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	/**
	 * Validates that the specified index is within the valid range [0, capacity]
	 * for this ByteArray. This method checks that the index is non-negative and
	 * does not exceed the current capacity of the buffer. If the validation fails,
	 * an ArrayIndexOutOfBoundsException is thrown. This is used internally to
	 * prevent invalid array access operations and maintain buffer integrity.
	 * 
	 * @param index The index to validate against the buffer's capacity
	 * @throws ArrayIndexOutOfBoundsException if the index is out of bounds
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * // This method is typically called internally by other methods
	 * // to validate index access before operations
	 * ByteArray buffer = ByteArray.allocate(10);
	 * try {
	 *     validateIndex(5);  // Valid: 5 is within [0, 10]
	 *     validateIndex(15); // Invalid: 15 exceeds capacity of 10
	 * } catch (ArrayIndexOutOfBoundsException e) {
	 *     System.out.println("Index out of bounds");
	 * }
	 * </pre>
	 */
	protected void validateIndex(int index) {
		if (index < 0 || index > capacity())
			throw new ArrayIndexOutOfBoundsException(index);
	}

	/** the backing array */
	private byte[] _array = null;

	/** The offset within the backing array of the first element of the ByteArray */
	private int _arrayOffset = 0;

//	/** Current position */
//	private int _position = 0;

	/** index for reader */
	private int _readerIndex;

	/** index for writer */
	private int _writerIndex;

//	/** whether current mode is writing */
//	private boolean writingMode = true;

	/** mark reader index */
	private int _markReader = UNKNOWN;

	/** mark writer index */
	private int _markWriter = UNKNOWN;

	/** Max data length */
	private int _limit = 0;

//	/** mark position */
//	private int _mark = UNKNOWN;

	/** capacity */
	private int _capacity = 0;

	/** Max capacity */
	private int _max_capacity = UNKNOWN;

	/** the parent */
	protected ByteArray _parent = null;

	/** Indicate that the byte order is BIG_ENDIAN.​ */
	private boolean _bigEndian = true;

	/** Indicate that this ByteArray is read only​ */
	private boolean _readOnly = false;

	/** Indicate that lowercase hex characters are used in toHex() */
	private boolean _lowerCase = false;
	
	private boolean _direct = false;

	/** constructor */
	public ByteArray() {
	}

	/** constructor */
	public ByteArray(int size) {
		this(new byte[size]);
		readerIndex(0);
		writerIndex(0);
		limit(0);
	}

	/** constructor */
	public ByteArray(byte[] buf) {
		this();
		array(buf);
	}

	/** constructor */
	public ByteArray(byte[] buf, int index) {
		this();
		array(buf, index);
	}

	/** constructor */
	public ByteArray(byte[] buf, int index, int length) {
		this();
		array(buf, index, length);
	}
	
	public ByteArray(ByteBuffer b) {
		this();
		_direct = b.isDirect();
		array(b.array(), b.arrayOffset(), b.capacity() - b.arrayOffset());
	}

	// ---- Methods that access the backing array ----

	/**
	 * Gets the current capacity of this ByteArray. The capacity represents the
	 * total number of bytes that can be stored in the buffer. This value never
	 * changes unless explicitly modified via the capacity(int) method or when
	 * the underlying array is changed. The capacity defines the upper limit for
	 * valid indices in the buffer, meaning that valid positions range from 0
	 * to capacity()-1. The actual amount of data that can be read or written
	 * may be further restricted by the limit, reader index, and writer index.
	 * This method is equivalent to the capacity() method in Java's ByteBuffer class.
	 * 
	 * @return The current capacity of this ByteArray in bytes, representing the
	 *         total size of the underlying storage array
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(1024); // Allocate 1KB buffer
	 * int cap = buffer.capacity(); // Returns 1024
	 * ByteArray directBuffer = ByteArray.allocateDirect(512); // Allocate 512B direct buffer
	 * int directCap = directBuffer.capacity(); // Returns 512
	 * </pre>
	 */
	public int capacity() {
		return _capacity;
	}

	/**
	 * update _capacity when array , maxCapacity, arrayOffset change
	 */
	protected void updateCapacity() {
		if (array() == null) {
			_capacity = 0;
		} else {
			if (maxCapacity() < 0) {
				_capacity = arrayLength() - arrayOffset();
			} else {
				_capacity = maxCapacity();
			}
		}
	}

	/**
	 * Gets a single byte from this ByteArray at the specified index. This method
	 * accesses the byte at the given index by adding the array offset to the index
	 * and retrieving the value from the backing array. This is a direct, efficient
	 * way to read a byte from a specific location in the buffer without affecting
	 * the reader index. This is useful when you need to access bytes at specific
	 * positions without advancing the buffer's read position.
	 * 
	 * @param index The index in the buffer from which to read the byte (relative
	 *              to the buffer's start, not the backing array)
	 * @return The byte value at the specified index
	 * @throws IndexOutOfBoundsException if the index is negative or greater than
	 *                                   or equal to the buffer's capacity
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(10);
	 * buffer.putByte(0, (byte) 0x41); // Write 'A' at index 0
	 * byte b = buffer.getByte(0);    // Read 'A' from index 0
	 * </pre>
	 */
	public byte getByte(int index) {
		return _array[index + _arrayOffset];
	}

	/**
	 * Puts a single byte into this ByteArray at the specified index. This method
	 * stores the given byte value at the specified index by adding the array offset
	 * to the index and writing the value to the backing array. This is a direct,
	 * efficient way to write a byte to a specific location in the buffer without
	 * affecting the writer index. This is useful when you need to update bytes at
	 * specific positions without advancing the buffer's write position.
	 * 
	 * @param index The index in the buffer where the byte should be written (relative
	 *              to the buffer's start, not the backing array)
	 * @param b The byte value to write to the buffer
	 * @throws IndexOutOfBoundsException if the index is negative or greater than
	 *                                   or equal to the buffer's capacity
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(10);
	 * buffer.putByte(0, (byte) 0x41); // Write 'A' at index 0
	 * buffer.putByte(1, (byte) 0x42); // Write 'B' at index 1
	 * </pre>
	 */
	public void putByte(int index, byte b) {
		_array[index + _arrayOffset] = b;
//		return this;
	}

	/**
	 * Gets a single byte from this ByteArray at the current reader index and
	 * increments the reader index by one. This method provides sequential reading
	 * from the buffer, automatically advancing the read position to the next byte.
	 * This is useful when processing data in order from the buffer. After calling
	 * this method, subsequent calls will read from the next position in sequence.
	 * 
	 * @return The byte value at the current reader index
	 * @throws IndexOutOfBoundsException if the reader index is greater than or
	 *                                   equal to the buffer's limit
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(10);
	 * buffer.putByte((byte) 0x41); // Write 'A'
	 * buffer.putByte((byte) 0x42); // Write 'B' at next position
	 * buffer.rewind();             // Reset to beginning for reading
	 * byte b1 = buffer.getByte();  // Returns 0x41, readerIndex moves to 1
	 * byte b2 = buffer.getByte();  // Returns 0x42, readerIndex moves to 2
	 * </pre>
	 */
	public byte getByte() {
		byte b = getByte(readerIndex());
		readerIndex(readerIndex() + 1);
		return b;
	}

	/**
	 * Puts a single byte into this ByteArray at the current writer index and
	 * increments the writer index by one. This method provides sequential writing
	 * to the buffer, automatically advancing the write position to the next byte.
	 * This is useful when adding data in order to the buffer. After calling this
	 * method, subsequent writes will occur at the next position in sequence.
	 * The method returns this ByteArray instance to enable method chaining.
	 * 
	 * @param b The byte value to write to the buffer
	 * @return This ByteArray instance for method chaining
	 * @throws IndexOutOfBoundsException if the writer index is greater than or
	 *                                   equal to the buffer's capacity
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(10);
	 * buffer.putByte((byte) 0x41)      // Write 'A', writerIndex moves to 1
	 *        .putByte((byte) 0x42);    // Write 'B', writerIndex moves to 2
	 * </pre>
	 */
	public ByteArray putByte(byte b) {
		putByte(writerIndex(), b);
		writerIndex(writerIndex() + 1);
		return this;
	}

	/**
	 * Gets the backing byte array that stores the actual data for this ByteArray.
	 * This method returns the internal byte array used to store the data. This
	 * allows direct access to the underlying storage, which can be useful for
	 * efficient bulk operations or for interfacing with other APIs that expect
	 * a standard byte array. Note that modifications to the returned array will
	 * directly affect the contents of this ByteArray.
	 * 
	 * @return The backing byte array, or null if this ByteArray has no backing array
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(10);
	 * buffer.putByte(0, (byte) 0x41);
	 * byte[] internalArray = buffer.array(); // Get direct access to internal storage
	 * </pre>
	 */
	public byte[] array() {
		return _array;
	}

	/**
	 * Sets the backing byte array that stores the actual data for this ByteArray
	 * and updates the capacity accordingly. This method is typically called
	 * internally when creating or modifying ByteArray instances. When this
	 * ByteArray has a parent (is a slice), the new array is also set on the
	 * parent to maintain consistency. This ensures that all related ByteArray
	 * instances share the same underlying data storage when appropriate.
	 * 
	 * @param buf The byte array to use as the new backing array for this ByteArray
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * // This method is typically used internally when creating ByteArray instances
	 * byte[] data = new byte[100];
	 * ByteArray buffer = new ByteArray();
	 * buffer.setArray(data); // Set the internal array (usually done via constructor)
	 * </pre>
	 */
	protected void setArray(byte[] buf) {
		_array = buf;
		updateCapacity();
		if (parent() != null) {
			parent().setArray(buf);
		}
	}

	/**
	 * Copies array data from source to destination using System.arraycopy().
	 * This method serves as a wrapper around System.arraycopy() and can be
	 * overridden by subclasses to provide custom array copying behavior if needed.
	 * This is useful for implementing alternative copy mechanisms or adding
	 * additional processing during the copy operation. By default, it performs
	 * a standard array copy operation that is optimized by the JVM.
	 * 
	 * @param src The source array from which to copy data
	 * @param srcPos The starting position in the source array
	 * @param dest The destination array to which data is copied
	 * @param destPos The starting position in the destination array
	 * @param length The number of elements to copy
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * // This method is typically called internally by other methods
	 * byte[] src = {0x01, 0x02, 0x03};
	 * byte[] dst = new byte[5];
	 * arraycopy(src, 0, dst, 0, 3); // Copy first 3 bytes from src to dst
	 * </pre>
	 */
	protected void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
		System.arraycopy(src, srcPos, dest, destPos, length);
	}

	// ---- Methods that are the same as those in ByteBuffer.​ ----

	/** get the first element's offset within the backing array. */
	public int arrayOffset() {
		if (array() == null)
			throw new NullPointerException();
		return _arrayOffset;
	}

	/**
	 * Clears this ByteArray. The position is set to zero, the limit is set to the
	 * capacity, and the mark is discarded. <br>
	 * 
	 * the buffer is changed to writing mode.
	 * 
	 * @return this
	 */
	public ByteArray clear() {
		clearMarks();
		readerIndex(0);
		writerIndex(0);
		limit(capacity());
		return this;
	}

	/**
	 * Compacts this ByteArray (optional operation).
	 * 
	 * The bytes between the buffer's current position and its limit, if any, are
	 * copied to the beginning of the buffer. That is, the byte at index p =
	 * position() is copied to index zero, the byte at index p + 1 is copied to
	 * index one, and so forth until the byte at index limit() - 1 is copied to
	 * index n = limit() - 1 - p. The buffer's position is then set to n+1 and its
	 * limit is set to its capacity. The mark, if defined, is discarded.
	 *
	 * <br>
	 * the buffer is changed to reading mode.
	 * 
	 * @return this
	 */
	public ByteArray compact() {
		clearMarks();
		int start = readerIndex();
		for (int i = start; i < writerIndex(); i++) {
			putByte(i - start, getByte(i));
		}
		writerIndex(writerIndex() - start);
		limit(limit() - start);
		return this;
	}

	/**
	 * Compares this ByteArray to another.
	 *
	 * <p>
	 * Two ByteArray are compared by comparing their sequences of remaining elements
	 * lexicographically, without regard to the starting position of each sequence
	 * within its corresponding buffer.
	 *
	 * @return A negative integer, zero, or a positive integer as this ByteArray is
	 *         less than, equal to, or greater than the given buffer
	 */
	public int compareTo(Object obj) {
		if (obj instanceof ByteArray) {
			ByteArray that = (ByteArray) obj;
			return compare(this.array(), 0, that.array(), 0, limit());

		} else if (obj instanceof byte[]) {
			byte[] that = (byte[]) obj;
			return compare(this.array(), 0, that, 0, limit());

		}

		throw new IllegalArgumentException("cannot compare to " + obj.getClass().getName());
	}

	/**
	 * Creates a new ByteArray that shares this ByteArray's content.
	 *
	 * <p>
	 * The content of the new ByteArray will be that of this ByteArray. Changes to
	 * this ByteArray's content will be visible in the new ByteArray, and vice
	 * versa; the two buffers' position, limit, and mark values will be independent.
	 *
	 * <p>
	 * The new ByteArray's capacity, limit, position, and mark values will be
	 * identical to those of this ByteArray, and its byte order will be
	 * {@link ByteOrder#BIG_ENDIAN BIG_ENDIAN}.
	 * 
	 * 
	 * The new ByteArray will be direct if, and only if, this ByteArray is direct,
	 * and it will be read-only if, and only if, this ByteArray is read-only.
	 * </p>
	 *
	 * @return The new buffer
	 */
	public ByteArray duplicate() {
		ByteArray ret = new ByteArray(array(), 0, capacity());
		ret.capacity(this.capacity());
		ret.maxCapacity(this.maxCapacity());
		ret.limit(this.limit());
		ret.readerIndex(this.readerIndex());
		ret.writerIndex(this.writerIndex());
		ret.readOnly(this.isReadOnly());
		return ret;
	}

	@Override
	public boolean equals(Object ob) {
		if (this == ob)
			return true;

		if (!(ob instanceof ByteArray))
			return false;

		ByteArray that = (ByteArray) ob;
		int thisPos = this.readerIndex();
		int thatPos = that.readerIndex();
		int thisRem = this.limit() - thisPos;
		int thatRem = that.limit() - thatPos;
		if (thisRem < 0 || thisRem != thatRem)
			return false;

		return compare(this.array(), thisPos, that.array(), thatPos, thisRem) != 0;
	}

//	/**
//	 * Flips this ByteArray. The limit is set to the current position and then the
//	 * position is set to zero. If the mark is defined then it is discarded. <br>
//	 * 
//	 * the buffer is changed to reading mode.
//	 *
//	 */
//	public ByteArray flip() {
//		clearMark();
//		limitToPosition();
//		position(0);
//		writingMode = false;
//		return this;
//	}

	/** Returns the current hash code of this ByteArray. */
	@Override
	public int hashCode() {
		int h = 1;
		int p = readerIndex();
		for (int i = limit() - 1; i >= p; i--)
			h = 31 * h + (int) getByte(i);
		return h;
	}

	/**
	 * Returns the number of elements between the current position and the limit.
	 */
	public int remaining() {
		int rem = limit() - readerIndex();
		return rem > 0 ? rem : 0;
	}
	
	/**
	 * Tells whether there are any elements between the current position and the
	 * limit.
	 */
	public boolean hasRemaining() {
		return remaining() > 0;
	}

	/**
	 * Check whether or not this ByteArray is backed by an accessible byte array.
	 */
	public boolean hasArray() {
		return array() != null;
	}

	/** Check whether or not this ByteArray is direct */
	public boolean isDirect() {
		return _direct;
	}

	/** Check whether this ByteArray read only */
	public boolean isReadOnly() {
		return _readOnly;
	}

	/**
	 * Gets the current limit of this ByteArray. The limit represents the index
	 * one past the last accessible byte in the buffer. No bytes beyond this point
	 * can be read or written. The limit is always less than or equal to the
	 * capacity of the buffer and greater than or equal to the reader index.
	 * This method is equivalent to the limit() method in Java's ByteBuffer class.
	 * 
	 * @return The current limit of this ByteArray, representing the maximum index
	 *         (exclusive) that can be accessed in the buffer
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(100);
	 * int currentLimit = buffer.limit(); // Returns 100
	 * buffer.limit(50); // Set new limit to 50
	 * int newLimit = buffer.limit(); // Returns 50
	 * </pre>
	 */
	public int limit() {
		return _limit;
	}

	/**
	 * Sets the limit of this ByteArray to the specified value. The limit defines
	 * the maximum index (exclusive) that can be accessed in the buffer. The new
	 * limit must be greater than or equal to the current reader index and less than
	 * or equal to the current capacity. This method validates that the new limit
	 * does not violate these constraints and throws appropriate exceptions if it does.
	 * After setting a new limit, the limit of any parent ByteArray is also updated
	 * to maintain consistency in buffer hierarchies. This method is equivalent to
	 * the limit(int) method in Java's ByteBuffer class.
	 * 
	 * @param index The new limit value for this ByteArray (must be &ge; readerIndex and &le; capacity)
	 * @return This ByteArray instance for method chaining
	 * @throws IndexOutOfBoundsException if the specified index is negative or greater than capacity
	 * @throws IllegalArgumentException if the specified limit is smaller than the current writer index
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(100);
	 * buffer.writerIndex(80); // Set writer index to 80
	 * buffer.readerIndex(20); // Set reader index to 20
	 * buffer.limit(75);       // Set limit to 75 (must be >= readerIndex and <= capacity)
	 * </pre>
	 */
	public ByteArray limit(int index) {
		validateIndex(index);
		if (index < writerIndex())
			throw new IllegalArgumentException("limit is smaller than writerIndex");

		if (index > capacity())
			throw new IllegalArgumentException("limit should smaller than capcity");

		if (maxCapacity() > 0 && index > maxCapacity())
			throw new IllegalArgumentException("limit should smaller than max capcity");

		_limit = index;

		updateParentLimit(arrayOffset() + index);
		return this;
	}

	/**
	 * Gets the current byte order of this ByteArray. The byte order determines how
	 * multi-byte values (such as integers, shorts, and longs) are stored and read
	 * from the buffer. The ByteArray supports both BIG_ENDIAN (most significant
	 * byte first) and LITTLE_ENDIAN (least significant byte first) byte orders.
	 * This method is equivalent to the order() method in Java's ByteBuffer class.
	 * 
	 * @return The current ByteOrder of this ByteArray, either BIG_ENDIAN or
	 *         LITTLE_ENDIAN depending on the internal configuration
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(10);
	 * ByteOrder currentOrder = buffer.order(); // Returns BIG_ENDIAN by default
	 * buffer.order(ByteOrder.LITTLE_ENDIAN);  // Change to little endian
	 * ByteOrder newOrder = buffer.order();    // Returns LITTLE_ENDIAN
	 * </pre>
	 */
	public ByteOrder order() {
		return _bigEndian ? BIG_ENDIAN : LITTLE_ENDIAN;
	}

	/**
	 * Sets the byte order of this ByteArray. The byte order determines how
	 * multi-byte values (such as integers, shorts, and longs) are stored and read
	 * from the buffer. After changing the byte order, all subsequent multi-byte
	 * read and write operations will use the new byte order. This method is
	 * equivalent to the order(ByteOrder) method in Java's ByteBuffer class.
	 * 
	 * @param value The new ByteOrder to be used for this ByteArray (BIG_ENDIAN or LITTLE_ENDIAN)
	 * @return This ByteArray instance for method chaining
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(10);
	 * buffer.order(ByteOrder.LITTLE_ENDIAN); // Set to little endian order
	 * buffer.putInt(0x12345678);             // Write int in little endian format
	 * buffer.order(ByteOrder.BIG_ENDIAN);    // Change to big endian order
	 * </pre>
	 */
	public ByteArray order(ByteOrder value) {
		_bigEndian = value == BIG_ENDIAN;
		return this;
	}

//	/** Get current position */
//	public int position() {
//		if (writingMode)
//			return writerIndex();
//		else
//			return readerIndex();
////		return _position;
//	}

//	/** Set current position */
//	public ByteArray position(int index) {
//		validateIndex(index);
//
////		_position = index;
//		if (writingMode)
//			writerIndex(index);
//		else
//			readerIndex(index);
//
//		if (limit() < position())
//			limitToPosition();
//		return this;
//	}

	/**
	 * Returns the current {@code readerIndex} of this buffer. The reader index
	 * represents the position from which the next byte will be read when using
	 * relative read operations (like get(), getByte(), etc.). The reader index
	 * must always be less than or equal to the writer index, and both must be
	 * within the bounds of the buffer's capacity. This method is commonly used
	 * when you need to know the current read position or to save the current
	 * position for later restoration.
	 * 
	 * @return The current reader index of this ByteArray, indicating the position
	 *         from which the next relative read operation will begin
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(100);
	 * buffer.putByte((byte) 0x01); // Write at position 0, index moves to 1
	 * buffer.putByte((byte) 0x02); // Write at position 1, index moves to 2
	 * int currentIndex = buffer.readerIndex(); // Returns 0 (start position)
	 * buffer.rewind(); // Reset reader index to 0
	 * int newIndex = buffer.readerIndex(); // Returns 0
	 * byte b = buffer.getByte(); // Read byte at index 0, readerIndex moves to 1
	 * </pre>
	 */
	public int readerIndex() {
		return _readerIndex;
	}

	/**
	 * Sets the {@code readerIndex} of this buffer to the specified value. This
	 * method allows you to reposition the read pointer to any valid position
	 * within the buffer. The new reader index must be between 0 and the current
	 * writer index (inclusive). Setting the reader index beyond the writer index
	 * would allow reading of uninitialized data, which is not permitted. This
	 * method is equivalent to the position(int) method in Java's ByteBuffer when
	 * in read mode, and is essential for navigating through buffer contents
	 * during processing operations.
	 * 
	 * @param index The new reader index for this ByteArray (must be &ge; 0 and &le; writerIndex)
	 * @return This ByteArray instance for method chaining
	 * @throws IndexOutOfBoundsException if the specified {@code readerIndex} is less than {@code 0} or greater than {@code this.writerIndex}
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(100);
	 * buffer.putByte((byte) 0x01).putByte((byte) 0x02); // Write at positions 0 and 1
	 * buffer.writerIndex(2); // Ensure writer index is at 2
	 * buffer.readerIndex(1); // Set reader index to position 1
	 * byte b = buffer.getByte(); // Read byte at index 1 (0x02)
	 * </pre>
	 */
	public ByteArray readerIndex(int index) {
		int length = writerIndex();
		checkFromIndexSize(index, 0, length);
		this._readerIndex = index;
		return this;
	}

	/**
	 * Marks the current {@code readerIndex} in this buffer. You can reposition the
	 * current {@code readerIndex} to the marked {@code readerIndex} by calling
	 * {@link #resetReaderIndex()}. The initial value of the marked
	 * {@code readerIndex} is {@code 0}.
	 */
	public ByteArray markReaderIndex() {
		_markReader = readerIndex();
		return this;
	}

	/**
	 * Repositions the current {@code readerIndex} to the marked {@code readerIndex}
	 * in this buffer.
	 *
	 * @throws IndexOutOfBoundsException if the current {@code writerIndex} is less
	 *                                   than the marked {@code readerIndex}
	 */
	public ByteArray resetReaderIndex() {
		if (_markReader >= 0)
			readerIndex(_markReader);
		_markReader = UNKNOWN;
		return this;
	}

	/**
	 * Marks the current {@code writerIndex} in this buffer. You can reposition the
	 * current {@code writerIndex} to the marked {@code writerIndex} by calling
	 * {@link #resetWriterIndex()}. The initial value of the marked
	 * {@code writerIndex} is {@code 0}.
	 */
	public ByteArray markWriterIndex() {
		_markWriter = writerIndex();
		return this;
	}

	/**
	 * Repositions the current {@code writerIndex} to the marked {@code writerIndex}
	 * in this buffer.
	 *
	 * @throws IndexOutOfBoundsException if the current {@code readerIndex} is
	 *                                   greater than the marked {@code writerIndex}
	 */
	public ByteArray resetWriterIndex() {
		if (_markWriter >= 0)
			writerIndex(_markWriter);
		_markWriter = UNKNOWN;
		return this;
	}

	/**
	 * Returns the current {@code writerIndex} of this buffer. The writer index
	 * represents the position at which the next byte will be written when using
	 * relative write operations (like put(), putByte(), etc.). The writer index
	 * must always be greater than or equal to the reader index, and both must be
	 * within the bounds of the buffer's capacity. This method is commonly used
	 * when you need to know the current write position or to track how much data
	 * has been written to the buffer. The writer index also determines the
	 * effective size of the data in the buffer for read operations.
	 * 
	 * @return The current writer index of this ByteArray, indicating the position
	 *         at which the next relative write operation will occur
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(100);
	 * int initialIndex = buffer.writerIndex(); // Returns 0 initially
	 * buffer.putByte((byte) 0x01); // Write at position 0, index moves to 1
	 * buffer.putByte((byte) 0x02); // Write at position 1, index moves to 2
	 * int currentIndex = buffer.writerIndex(); // Returns 2
	 * buffer.writerIndex(5); // Jump writer index to position 5
	 * int newIndex = buffer.writerIndex(); // Returns 5
	 * </pre>
	 */
	public int writerIndex() {
		return _writerIndex;
	}

	/**
	 * Sets the {@code writerIndex} of this buffer to the specified value. This
	 * method allows you to reposition the write pointer to any valid position
	 * within the buffer. The new writer index must be between 0 and the buffer's
	 * capacity (inclusive). Setting the writer index beyond the buffer's capacity
	 * is not permitted. When the new writer index is greater than the current
	 * limit, the limit is automatically increased to match the writer index to
	 * maintain consistency. This method is equivalent to the position(int)
	 * method in Java's ByteBuffer when in write mode, and is essential for
	 * managing where data is written in the buffer or for truncating data.
	 * 
	 * @param writerIndex The new writer index for this ByteArray (must be &ge; 0 and &le; capacity)
	 * @return This ByteArray instance for method chaining
	 * @throws IndexOutOfBoundsException if the specified {@code writerIndex} is less than {@code 0} or greater than {@code this.capacity}
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(100);
	 * buffer.putByte((byte) 0x01).putByte((byte) 0x02); // Write at positions 0 and 1
	 * buffer.writerIndex(10); // Set writer index to position 10 (skipping positions 2-9)
	 * int newIndex = buffer.writerIndex(); // Returns 10
	 * buffer.putByte((byte) 0x03); // Write at position 10, writerIndex moves to 11
	 * </pre>
	 */
	public ByteArray writerIndex(int writerIndex) {
		checkFromIndexSize(writerIndex, 0, capacity() + 1);
		_writerIndex = writerIndex;
		if (limit() < writerIndex())
			limitToWriterIndex();
		return this;
	}

	/**
	 * Sets the {@code readerIndex} and {@code writerIndex} of this buffer in one
	 * shot. This method is useful when you have to worry about the invocation order
	 * of {@link #readerIndex(int)} and {@link #writerIndex(int)} methods. For
	 * example, the following code will fail:
	 *
	 * <pre>
	 * // Create a buffer whose readerIndex, writerIndex and capacity are
	 * // 0, 0 and 8 respectively.
	 * {@link ByteBuf} buf = {@link Unpooled}.buffer(8);
	 *
	 * // IndexOutOfBoundsException is thrown because the specified
	 * // readerIndex (2) cannot be greater than the current writerIndex (0).
	 * buf.readerIndex(2);
	 * buf.writerIndex(4);
	 * </pre>
	 *
	 * The following code will also fail:
	 *
	 * <pre>
	 * // Create a buffer whose readerIndex, writerIndex and capacity are
	 * // 0, 8 and 8 respectively.
	 * {@link ByteBuf} buf = {@link Unpooled}.wrappedBuffer(new byte[8]);
	 *
	 * // readerIndex becomes 8.
	 * buf.readLong();
	 *
	 * // IndexOutOfBoundsException is thrown because the specified
	 * // writerIndex (4) cannot be less than the current readerIndex (8).
	 * buf.writerIndex(4);
	 * buf.readerIndex(2);
	 * </pre>
	 *
	 * By contrast, this method guarantees that it never throws an
	 * {@link IndexOutOfBoundsException} as long as the specified indexes meet basic
	 * constraints, regardless what the current index values of the buffer are:
	 *
	 * <pre>
	 * // No matter what the current state of the buffer is, the following
	 * // call always succeeds as long as the capacity of the buffer is not
	 * // less than 4.
	 * buf.setIndex(2, 4);
	 * </pre>
	 *
	 * @throws IndexOutOfBoundsException if the specified {@code readerIndex} is
	 *                                   less than 0, if the specified
	 *                                   {@code writerIndex} is less than the
	 *                                   specified {@code readerIndex} or if the
	 *                                   specified {@code writerIndex} is greater
	 *                                   than {@code this.capacity}
	 */
	public ByteArray setIndex(int readerIndex, int writerIndex) {
		readerIndex(readerIndex);
		writerIndex(writerIndex);
		return this;
	}

	/**
	 * Discards the bytes between the 0th index and {@code readerIndex}. It moves
	 * the bytes between {@code readerIndex} and {@code writerIndex} to the 0th
	 * index, and sets {@code readerIndex} and {@code writerIndex} to {@code 0} and
	 * {@code oldWriterIndex - oldReaderIndex} respectively.
	 * <p>
	 * Please refer to the class documentation for more detailed explanation.
	 */
	public ByteArray discardReadBytes() {
		int len = limit() - readerIndex();
		int base = readerIndex();
		for (int i = 0; i < len; i++) {
			put(i, getByte(i + base));
		}
		readerIndex(0);
		writerIndex(len);
		limit(len);
		return this;
	}

	/**
	 * Similar to {@link ByteBuf#discardReadBytes()} except that this method might
	 * discard some, all, or none of read bytes depending on its internal
	 * implementation to reduce overall memory bandwidth consumption at the cost of
	 * potentially additional memory consumption.
	 */
	public ByteArray discardSomeReadBytes() {
		discardReadBytes(); // TODO:
		return this;
	}

	/**
	 * Expands the buffer {@link #capacity()} to make sure the number of
	 * {@linkplain #writableBytes() writable bytes} is equal to or greater than the
	 * specified value. If there are enough writable bytes in this buffer, this
	 * method returns with no side effect.
	 *
	 * @param minWritableBytes the expected minimum number of writable bytes
	 * @throws IndexOutOfBoundsException if {@link #writerIndex()} +
	 *                                   {@code minWritableBytes} &gt;
	 *                                   {@link #maxCapacity()}.
	 * @see #capacity(int)
	 */
	public ByteArray ensureWritable(int minWritableBytes) {
		int size = writerIndex() + minWritableBytes;
		ensureCapacity(size);
		return this;
	}

	/**
	 * Expands the buffer {@link #capacity()} to make sure the number of
	 * {@linkplain #writableBytes() writable bytes} is equal to or greater than the
	 * specified value. Unlike {@link #ensureWritable(int)}, this method returns a
	 * status code.
	 *
	 * @param minWritableBytes the expected minimum number of writable bytes
	 * @param force            When {@link #writerIndex()} +
	 *                         {@code minWritableBytes} &gt; {@link #maxCapacity()}:
	 *                         <ul>
	 *                         <li>{@code true} - the capacity of the buffer is
	 *                         expanded to {@link #maxCapacity()}</li>
	 *                         <li>{@code false} - the capacity of the buffer is
	 *                         unchanged</li>
	 *                         </ul>
	 * @return {@code 0} if the buffer has enough writable bytes, and its capacity
	 *         is unchanged. {@code 1} if the buffer does not have enough bytes, and
	 *         its capacity is unchanged. {@code 2} if the buffer has enough
	 *         writable bytes, and its capacity has been increased. {@code 3} if the
	 *         buffer does not have enough bytes, but its capacity has been
	 *         increased to its maximum.
	 */
	public int ensureWritable(int minWritableBytes, boolean force) {
		ensureWritable(minWritableBytes); // TODO
		return 0;
	}

	/**
	 * Increases the current {@code readerIndex} by the specified {@code length} in
	 * this buffer.
	 *
	 * @throws IndexOutOfBoundsException if {@code length} is greater than
	 *                                   {@code this.readableBytes}
	 */
	public ByteArray skipBytes(int length) {
		readerIndex(readerIndex() + length);
		return this;
	}

	/**
	 * Locates the first occurrence of the specified {@code value} in this buffer.
	 * The search takes place from the specified {@code fromIndex} (inclusive) to
	 * the specified {@code toIndex} (exclusive).
	 * <p>
	 * If {@code fromIndex} is greater than {@code toIndex}, the search is performed
	 * in a reversed order from {@code fromIndex} (exclusive) down to
	 * {@code toIndex} (inclusive).
	 * <p>
	 * Note that the lower index is always included and higher always excluded.
	 * <p>
	 * This method does not modify {@code readerIndex} or {@code writerIndex} of
	 * this buffer.
	 *
	 * @return the absolute index of the first occurrence if found. {@code -1}
	 *         otherwise.
	 */
	public int indexOf(int fromIndex, int toIndex, byte value) {
		if (fromIndex > toIndex) {
			int temp = fromIndex;
			fromIndex = toIndex;
			toIndex = temp;
		}

		int i = fromIndex;
		while (i < toIndex) {
			byte b = getByte(i);
			if (b == value)
				return i;
		}
		return -1;
	}

	/**
	 * Returns {@code true} if and only if
	 * {@code (this.writerIndex - this.readerIndex)} is greater than {@code 0}.
	 */
	public boolean isReadable() {
		return writerIndex() - readerIndex() > 0;
	}

	/**
	 * Returns {@code true} if and only if this buffer contains equal to or more
	 * than the specified number of elements.
	 */
	public boolean isReadable(int size) {
		return writerIndex() - readerIndex() > size;
	}

	/**
	 * Returns {@code true} if and only if
	 * {@code (this.capacity - this.writerIndex)} is greater than {@code 0}.
	 */
	public boolean isWritable() {
		return capacity() - writerIndex() > 0;
	}

	/**
	 * Returns {@code true} if and only if this buffer has enough room to allow
	 * writing the specified number of elements.
	 */
	public boolean isWritable(int size) {
		return capacity() - writerIndex() > size;
	}

	/**
	 * Returns the number of writable bytes which is equal to
	 * {@code (this.capacity - this.writerIndex)}.
	 */
	public int writableBytes() {
		return capacity() - writerIndex();
	}

	/**
	 * Returns the number of readable bytes which is equal to
	 * {@code (this.writerIndex - this.readerIndex)}.
	 */
	public int readableBytes() {
		return writerIndex() - readerIndex();
	}

	/**
	 * Rewinds this ByteArray. The position is set to zero and the mark is
	 * discarded.
	 */
	public ByteArray rewind() {
		clearMarks();
		readerIndex(0);
//		writerIndex(0);
		return this;
	}

	/**
	 * Creates a new ByteArray whose content is a shared subsequence of this
	 * ByteArray's content.
	 *
	 * <p>
	 * The content of the new buffer will start at this ByteArray's current
	 * position. Changes to this ByteArray's content will be visible in the new
	 * buffer, and vice versa; the two buffers' position, limit, and mark values
	 * will be independent.
	 *
	 * <p>
	 * The new buffer's position will be zero, its capacity and its limit will be
	 * the number of bytes remaining in this ByteArray, its mark will be undefined,
	 * and its byte order will be
	 * 
	 * {@link ByteOrder#BIG_ENDIAN BIG_ENDIAN}.
	 * 
	 * 
	 * 
	 * The new buffer will be direct if, and only if, this ByteArray is direct, and
	 * it will be read-only if, and only if, this ByteArray is read-only.
	 * </p>
	 *
	 * @return The new buffer
	 *
	 * 
	 * @see #alignedSlice(int)
	 * 
	 */
	public ByteArray slice() {
		return slice(readerIndex(), writerIndex() - readerIndex());
	}
	

	/**
	 * Creates a new ByteArray whose content is a shared subsequence of this
	 * ByteArray's content.
	 *
	 * <p>
	 * The content of the new buffer will start at position {@code index} in this
	 * buffer, and will contain {@code length} elements. Changes to this ByteArray's
	 * content will be visible in the new buffer, and vice versa; the two buffers'
	 * position, limit, and mark values will be independent.
	 *
	 * <p>
	 * The new buffer's position will be zero, its capacity and its limit will be
	 * {@code length}, its mark will be undefined, and its byte order will be
	 * 
	 * {@link ByteOrder#BIG_ENDIAN BIG_ENDIAN}.
	 * 
	 * 
	 * 
	 * The new buffer will be direct if, and only if, this ByteArray is direct, and
	 * it will be read-only if, and only if, this ByteArray is read-only.
	 * </p>
	 *
	 * @param index  The position in this ByteArray at which the content of the new
	 *               buffer will start; must be non-negative and no larger than
	 *               {@link #limit() limit()}
	 *
	 * @param length The number of elements the new buffer will contain; must be
	 *               non-negative and no larger than {@code limit() - index}
	 *
	 * @return The new buffer
	 *
	 * @throws IndexOutOfBoundsException If {@code index} is negative or greater
	 *                                   than {@code limit()}, {@code length} is
	 *                                   negative, or
	 *                                   {@code length > limit() - index}
	 *
	 * @since 13
	 */
	public ByteArray slice(int index, int length) {
		return new ByteArray(array(), index, length);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName());
		sb.append("[readerIndex=");
		sb.append(readerIndex());
		sb.append(" writerIndex=");
		sb.append(writerIndex());
		sb.append(" lim=");
		sb.append(limit());
		sb.append(" cap=");
		sb.append(capacity());
		if (maxCapacity() >= 0) {
			sb.append(" max_cap=");
			sb.append(maxCapacity());
		}
		sb.append("]");
		return sb.toString();
	}

//	/** Check whether the buffer is in writing mode */
//	public boolean isWriting() {
//		return writingMode;
//	}

	// -------- getter and putter Methods that are the same as those in ByteBuffer.
	// ------------

	/** get one byte at current position, and then increments the position */
	public byte get() {
		byte b = get(readerIndex());
		readerIndex(readerIndex() + 1);
		return b;
	}

	/** get one byte at the given index */
	public byte get(int index) {
		return getByte(index);
	}

	/** put the byte at the given index */
	public ByteArray put(byte value) {
		put(writerIndex(), value);
		writerIndex(writerIndex() + 1);
		if (limit() < writerIndex())
			limitToWriterIndex();
		return this;
	}

	/** put the byte at specified index */
	public ByteArray put(int index, byte value) {
		ensureCapacity(index + 1);
		putByte(index, value);
		writerIndexMinimum(index + 1);
		return this;
	}
	
	/**
	 * Set limit to specified number when the limit is smaller than specified number
	 */
	protected void writerIndexMinimum(int n) {
		if (writerIndex() < n) {
			writerIndex(n);
		}
	}

	/**
	 * Relative bulk <i>get</i> method.
	 *
	 * <p>
	 * This method transfers bytes from this ByteArray into the given destination
	 * array. An invocation of this method of the form {@code src.get(a)} behaves in
	 * exactly the same way as the invocation
	 *
	 * <pre>
	 * src.get(a, 0, a.length)
	 * </pre>
	 *
	 * @param dst The destination array
	 *
	 * @return this ByteArray
	 *
	 * @throws BufferUnderflowException If there are fewer than {@code length} bytes
	 *                                  remaining in this ByteArray
	 */
	public ByteArray get(byte[] dst) {
		return get(dst, 0, dst.length);
	}

	/**
	 * Relative bulk <i>get</i> method. Transfers bytes from this ByteArray into the
	 * given array.
	 * 
	 * @param index  The index in this ByteArray from which the first byte will be
	 *               read; must be non-negative and less than {@code limit()}
	 *
	 * @param dst    The destination array
	 *
	 * @param offset The offset within the array of the first byte to be written;
	 *               must be non-negative and less than {@code dst.length}
	 *
	 * @param length The number of bytes to be written to the given array; must be
	 *               non-negative and no larger than the smaller of
	 *               {@code limit() - index} and {@code dst.length - offset}
	 *
	 * @return this ByteArray
	 */
	public ByteArray get(byte[] dst, int offset, int length) {
		checkFromIndexSize(offset, length, dst.length); // TODO
		int pos = readerIndex();
		if (length > limit() - pos)
			throw new BufferUnderflowException();

		getArray(pos, dst, offset, length);
		readerIndex(pos + length);
		return this;
	}

	/**
	 * Absolute bulk <i>get</i> method.
	 *
	 * <p>
	 * This method transfers {@code length} bytes from this ByteArray into the given
	 * array, starting at the given index in this ByteArray and at the given offset
	 * in the array. The position of this ByteArray is unchanged.
	 *
	 * <p>
	 * An invocation of this method of the form
	 * <code>src.get(index,&nbsp;dst,&nbsp;offset,&nbsp;length)</code> has exactly
	 * the same effect as the following loop except that it first checks the
	 * consistency of the supplied parameters and it is potentially much more
	 * efficient:
	 *
	 * <pre>{@code
	 * for (int i = offset, j = index; i < offset + length; i++, j++)
	 * 	dst[i] = src.get(j);
	 * }</pre>
	 *
	 * @param index  The index in this ByteArray from which the first byte will be
	 *               read; must be non-negative and less than {@code limit()}
	 *
	 * @param dst    The destination array
	 *
	 * @param offset The offset within the array of the first byte to be written;
	 *               must be non-negative and less than {@code dst.length}
	 *
	 * @param length The number of bytes to be written to the given array; must be
	 *               non-negative and no larger than the smaller of
	 *               {@code limit() - index} and {@code dst.length - offset}
	 *
	 * @return this ByteArray
	 *
	 * @throws IndexOutOfBoundsException If the preconditions on the {@code index},
	 *                                   {@code offset}, and {@code length}
	 *                                   parameters do not hold
	 *
	 * @since 13
	 */
	public ByteArray get(int index, byte[] dst, int offset, int length) {
		checkFromIndexSize(index, length, limit());
		checkFromIndexSize(offset, length, dst.length);

		getArray(index, dst, offset, length);
		return this;
	}

	/**
	 * Absolute bulk <i>get</i> method.
	 * 
	 * <p>
	 * This method transfers bytes from this ByteArray into the given destination
	 * array. The position of this ByteArray is unchanged. An invocation of this
	 * method of the form <code>src.get(index,&nbsp;dst)</code> behaves in exactly
	 * the same way as the invocation:
	 *
	 * <pre>
	 * src.get(index, dst, 0, dst.length)
	 * </pre>
	 *
	 * @param index The index in this ByteArray from which the first byte will be
	 *              read; must be non-negative and less than {@code limit()}
	 *
	 * @param dst   The destination array
	 *
	 * @return this ByteArray
	 *
	 * @throws IndexOutOfBoundsException If {@code index} is negative, not smaller
	 *                                   than {@code limit()}, or
	 *                                   {@code limit() - index < dst.length}
	 *
	 */
	public ByteArray get(int index, byte[] dst) {
		return get(index, dst, 0, dst.length);
	}

	/**
	 * Relative bulk <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p>
	 * This method transfers bytes into this ByteArray from the given source array.
	 * If there are more bytes to be copied from the array than remain in this
	 * ByteArray, that is, if
	 * {@code length}&nbsp;{@code >}&nbsp;{@code remaining()}, then no bytes are
	 * transferred and a {@link BufferOverflowException} is thrown.
	 *
	 * <p>
	 * Otherwise, this method copies {@code length} bytes from the given array into
	 * this ByteArray, starting at the given offset in the array and at the current
	 * position of this ByteArray. The position of this ByteArray is then
	 * incremented by {@code length}.
	 *
	 * <p>
	 * In other words, an invocation of this method of the form
	 * <code>dst.put(src,&nbsp;off,&nbsp;len)</code> has exactly the same effect as
	 * the loop
	 *
	 * <pre>{@code
	 * for (int i = off; i < off + len; i++)
	 * 	dst.put(src[i]);
	 * }</pre>
	 *
	 * except that it first checks that there is sufficient space in this ByteArray
	 * and it is potentially much more efficient.
	 *
	 * @param src    The array from which bytes are to be read
	 *
	 * @param offset The offset within the array of the first byte to be read; must
	 *               be non-negative and no larger than {@code src.length}
	 *
	 * @param length The number of bytes to be read from the given array; must be
	 *               non-negative and no larger than {@code src.length - offset}
	 *
	 * @return this ByteArray
	 *
	 * @throws BufferOverflowException   If there is insufficient space in this
	 *                                   buffer
	 *
	 * @throws IndexOutOfBoundsException If the preconditions on the {@code offset}
	 *                                   and {@code length} parameters do not hold
	 */
	public ByteArray put(byte[] src, int offset, int length) {
		checkFromIndexSize(offset, length, src.length);
		int pos = writerIndex();
		if (length > capacity() - pos)
			throw new BufferOverflowException();

		putArray(pos, src, offset, length);
		writerIndex(pos + length);
		this.limitToWriterIndex();
		return this;
	}

	/**
	 * Relative bulk <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p>
	 * This method transfers the entire content of the given source byte array into
	 * this ByteArray. An invocation of this method of the form {@code dst.put(a)}
	 * behaves in exactly the same way as the invocation
	 *
	 * <pre>
	 * dst.put(a, 0, a.length)
	 * </pre>
	 *
	 * @param src The source array
	 *
	 * @return this ByteArray
	 *
	 * @throws BufferOverflowException If there is insufficient space in this
	 *                                 ByteArray
	 *
	 * @throws ReadOnlyBufferException If this ByteArray is read-only
	 */
	public ByteArray put(byte[] src) {
		return put(src, 0, src.length);
	}

	/**
	 * Absolute bulk <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p>
	 * This method transfers {@code length} bytes from the given array, starting at
	 * the given offset in the array and at the given index in this ByteArray. The
	 * position of this ByteArray is unchanged.
	 *
	 * <p>
	 * An invocation of this method of the form
	 * <code>dst.put(index,&nbsp;src,&nbsp;offset,&nbsp;length)</code> has exactly
	 * the same effect as the following loop except that it first checks the
	 * consistency of the supplied parameters and it is potentially much more
	 * efficient:
	 *
	 * <pre>{@code
	 * for (int i = offset, j = index; i < offset + length; i++, j++)
	 * 	dst.put(j, src[i]);
	 * }</pre>
	 *
	 * @param index     The index in this ByteArray at which the first byte will be
	 *                  written; must be non-negative and less than {@code limit()}
	 *
	 * @param src       The array from which bytes are to be read
	 *
	 * @param srcOffset The offset within the array of the first byte to be read;
	 *                  must be non-negative and less than {@code src.length}
	 *
	 * @param length    The number of bytes to be read from the given array; must be
	 *                  non-negative and no larger than the smaller of
	 *                  {@code limit() - index} and {@code src.length - offset}
	 *
	 * @return this ByteArray
	 *
	 * @throws IndexOutOfBoundsException If the preconditions on the {@code index},
	 *                                   {@code offset}, and {@code length}
	 *                                   parameters do not hold
	 *
	 */
	public ByteArray put(int index, byte[] src, int srcOffset, int length) {
		checkFromIndexSize(index, length, capacity());
		checkFromIndexSize(srcOffset, length, src.length);

		putArray(index, src, srcOffset, length);
		return this;
	}

	/**
	 * Absolute bulk <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p>
	 * This method copies bytes into this ByteArray from the given source array. The
	 * position of this ByteArray is unchanged. An invocation of this method of the
	 * form <code>dst.put(index,&nbsp;src)</code> behaves in exactly the same way as
	 * the invocation:
	 *
	 * <pre>
	 * dst.put(index, src, 0, src.length);
	 * </pre>
	 *
	 * @param index The index in this ByteArray at which the first byte will be
	 *              written; must be non-negative and less than {@code limit()}
	 *
	 * @param src   The array from which bytes are to be read
	 *
	 * @return this ByteArray
	 *
	 * @throws IndexOutOfBoundsException If {@code index} is negative, not smaller
	 *                                   than {@code limit()}, or
	 *                                   {@code limit() - index < src.length}
	 */
	public ByteArray put(int index, byte[] src) {
		return put(index, src, 0, src.length);
	}

	/** Read a short at the current position and increment the position.​ */
	public short getShort() {
		short n = getShort(readerIndex());
		readerIndex(readerIndex() + 2);
		return n;
	}

	/** Read a short at the given index */
	public short getShort(int index) {
		if (_bigEndian) {
			return (short) ((getByte(index + 0) & 0xFF) << 8 | (getByte(index + 1) & 0xFF));
		} else {
			return (short) ((getByte(index + 1) & 0xFF) << 8 | (getByte(index + 0) & 0xFF));
		}
	}

	/** Write a short at the current position and increment the position */
	public ByteArray putShort(short n) {
		putShort(writerIndex(), n);
		writerIndex(writerIndex() + 2);
		return this;
	}

	/** Write a short at the given index */
	public ByteArray putShort(int index, short value) {
		ensureCapacity(index + 2);
		if (_bigEndian) {
			putByte(index, (byte) (value >> 8));
			putByte(index + 1, (byte) (value & 0xFF));
		} else {
			putByte(index + 1, (byte) (value >> 8));
			putByte(index, (byte) (value & 0xFF));
		}
		return this;
	}

	/** Read an integer at the current position and increment the position */
	public int getInt() {
		int n = getInt(readerIndex());
		readerIndex(readerIndex() + 4);
		return n;
	}

	/** Read an integer at the given index */
	public int getInt(int index) {
		if (_bigEndian) {
			return ((getByte(index + 0) & 0xFF) << 24 | //
					(getByte(index + 1) & 0xFF) << 16 | //
					(getByte(index + 2) & 0xFF) << 8 | //
					(getByte(index + 3) & 0xFF) << 0 //
			);
		} else {
			return ((getByte(index + 3) & 0xFF) << 24 | //
					(getByte(index + 2) & 0xFF) << 16 | //
					(getByte(index + 1) & 0xFF) << 8 | //
					(getByte(index + 0) & 0xFF) << 0 //
			);
		}
	}

	/** Write an integer at the current position and increment the position */
	public ByteArray putInt(int n) {
		putInt(writerIndex(), n);
		writerIndex(writerIndex() + 4);
		return this;
	}

	/** Write an integer at the given index */
	public ByteArray putInt(int index, int value) {
		ensureCapacity(index + 4);
		if (_bigEndian) {
			putByte(index + 0, (byte) (value >> 24));
			putByte(index + 1, (byte) (value >> 16));
			putByte(index + 2, (byte) (value >> 8));
			putByte(index + 3, (byte) value);
		} else {
			putByte(index + 3, (byte) (value >> 24));
			putByte(index + 2, (byte) (value >> 16));
			putByte(index + 1, (byte) (value >> 8));
			putByte(index + 0, (byte) value);
		}
		return this;
	}

	/** Read a long at the current position and increment the position */
	public long getLong() {
		long n = getLong(readerIndex());
		readerIndex(readerIndex() + 8);
		return n;
	}

	/** Read a long at the given index */
	public long getLong(int index) {
		if (_bigEndian) {
			return ((getByte(index + 0) & 0xFFL) << 56 | //
					(getByte(index + 1) & 0xFFL) << 48 | //
					(getByte(index + 2) & 0xFFL) << 40 | //
					(getByte(index + 3) & 0xFFL) << 32 | //
					(getByte(index + 4) & 0xFFL) << 24 | //
					(getByte(index + 5) & 0xFFL) << 16 | //
					(getByte(index + 6) & 0xFFL) << 8 | //
					(getByte(index + 7) & 0xFFL) << 0);
		} else {
			return ((getByte(index + 7) & 0xFFL) << 56 | //
					(getByte(index + 6) & 0xFFL) << 48 | //
					(getByte(index + 5) & 0xFFL) << 40 | //
					(getByte(index + 4) & 0xFFL) << 32 | //
					(getByte(index + 3) & 0xFFL) << 24 | //
					(getByte(index + 2) & 0xFFL) << 16 | //
					(getByte(index + 1) & 0xFFL) << 8 | //
					(getByte(index + 0) & 0xFFL) << 0);
		}
	}

	/** Write a long at the current position and increment the position */
	public ByteArray putLong(long n) {
		putLong(writerIndex(), n);
		writerIndex(writerIndex() + 8);
		return this;
	}

	/** Write a long at the given index */
	public ByteArray putLong(int index, long value) {
		ensureCapacity(index + 8);
		if (_bigEndian) {
			putByte(index + 0, (byte) (value >> 56));
			putByte(index + 1, (byte) (value >> 48));
			putByte(index + 2, (byte) (value >> 40));
			putByte(index + 3, (byte) (value >> 32));
			putByte(index + 4, (byte) (value >> 24));
			putByte(index + 5, (byte) (value >> 16));
			putByte(index + 6, (byte) (value >> 8));
			putByte(index + 7, (byte) value);
		} else {
			putByte(index + 7, (byte) (value >> 56));
			putByte(index + 6, (byte) (value >> 48));
			putByte(index + 5, (byte) (value >> 40));
			putByte(index + 4, (byte) (value >> 32));
			putByte(index + 3, (byte) (value >> 24));
			putByte(index + 2, (byte) (value >> 16));
			putByte(index + 1, (byte) (value >> 8));
			putByte(index + 0, (byte) value);
		}
		return this;
	}

	/** Read a float at the given index */
	public float getFloat(int index) {
		int intBits;
		if (_bigEndian) {
			intBits = ((getByte(index) & 0xFF) << 24) | ((getByte(index + 1) & 0xFF) << 16)
					| ((getByte(index + 2) & 0xFF) << 8) | (getByte(index + 3) & 0xFF);
		} else {
			intBits = ((getByte(index + 3) & 0xFF) << 24) | ((getByte(index + 2) & 0xFF) << 16)
					| ((getByte(index + 1) & 0xFF) << 8) | (getByte(index + 0) & 0xFF);
		}
		return Float.intBitsToFloat(intBits);
	}

	/** Read a float at the current position and increment the position */
	public float getFloat() {
		float ret = getFloat(readerIndex());
		readerIndex(readerIndex() + 4);
		return ret;
	}

	/** Write a float at the current position and increment the position */
	public ByteArray putFloat(float value) {
		putFloat(writerIndex(), value);
		writerIndex(writerIndex() + 4);
		return this;
	}

	/** Write a float at the given index */
	public ByteArray putFloat(int index, float value) {
		ensureCapacity(index + 4);
		int intBits = Float.floatToIntBits(value);
		if (_bigEndian) {
			putByte(index + 0, (byte) (intBits >> 24));
			putByte(index + 1, (byte) (intBits >> 16));
			putByte(index + 2, (byte) (intBits >> 8));
			putByte(index + 3, (byte) (intBits >> 0));
		} else {
			putByte(index + 3, (byte) (intBits >> 24));
			putByte(index + 2, (byte) (intBits >> 16));
			putByte(index + 1, (byte) (intBits >> 8));
			putByte(index + 0, (byte) (intBits >> 0));
		}
		return this;
	}

	/** Read a double at the given index */
	public double getDouble(int offset) {
		long longBits;
		if (_bigEndian) {
			longBits = ((long) getByte(offset) << 56) | ((long) (getByte(offset + 1) & 0xFF) << 48)
					| ((long) (getByte(offset + 2) & 0xFF) << 40) | ((long) (getByte(offset + 3) & 0xFF) << 32)
					| ((long) (getByte(offset + 4) & 0xFF) << 24) | ((long) (getByte(offset + 5) & 0xFF) << 16)
					| ((long) (getByte(offset + 6) & 0xFF) << 8) | ((long) (getByte(offset + 7) & 0xFF));
		} else {
			longBits = ((long) getByte(offset + 7) << 56) | ((long) (getByte(offset + 6) & 0xFF) << 48)
					| ((long) (getByte(offset + 5) & 0xFF) << 40) | ((long) (getByte(offset + 4) & 0xFF) << 32)
					| ((long) (getByte(offset + 3) & 0xFF) << 24) | ((long) (getByte(offset + 2) & 0xFF) << 16)
					| ((long) (getByte(offset + 1) & 0xFF) << 8) | ((long) (getByte(offset + 0) & 0xFF));

		}
		return Double.longBitsToDouble(longBits);
	}

	/** Read a double at the current position and increment the position */
	public double getDouble() {
		double ret = getDouble(readerIndex());
		readerIndex(readerIndex() + 8);
		return ret;
	}

	/** Write a double at the current position and increment the position */
	public ByteArray putDouble(double value) {
		putDouble(writerIndex(), value);
		writerIndex(writerIndex() + 8);
		return this;
	}

	/** Write a double at the given index */
	public ByteArray putDouble(int index, double value) {
		ensureCapacity(index + 8);
		long longBits = Double.doubleToLongBits(value);
		if (_bigEndian) {
			putByte(index + 0, (byte) (longBits >> 56));
			putByte(index + 1, (byte) (longBits >> 48));
			putByte(index + 2, (byte) (longBits >> 40));
			putByte(index + 3, (byte) (longBits >> 32));
			putByte(index + 4, (byte) (longBits >> 24));
			putByte(index + 5, (byte) (longBits >> 16));
			putByte(index + 6, (byte) (longBits >> 8));
			putByte(index + 7, (byte) (longBits >> 0));
		} else {
			putByte(index + 7, (byte) (longBits >> 56));
			putByte(index + 6, (byte) (longBits >> 48));
			putByte(index + 5, (byte) (longBits >> 40));
			putByte(index + 4, (byte) (longBits >> 32));
			putByte(index + 3, (byte) (longBits >> 24));
			putByte(index + 2, (byte) (longBits >> 16));
			putByte(index + 1, (byte) (longBits >> 8));
			putByte(index + 0, (byte) (longBits >> 0));
		}
		return this;
	}

	// ----Methods which are not included in the ByteBuffer class----

	/**
	 * get 1-bit unsigned int at the specified bit offset of the byte at current
	 * position
	 * 
	 * @param index     the index of the byte to read
	 * @param bitOffset position of the bit in one byte, value in range of [0, 7]
	 * @return return true if the bit is 1, return false if the bit is 0
	 */
	public int getUInt1(int bitOffset) {
		int ret = getUInt1(readerIndex(), bitOffset);
		if (bitOffset == 0)
			readerIndex(readerIndex() + 1);
		return ret;
	}

	/**
	 * get 1-bit unsigned int at the specified bit offset of the byte at specified
	 * index
	 * 
	 * @param index     the index of the byte to read
	 * @param bitOffset offset of the bit in one byte, value in range of [0, 7]
	 * @return return true if the bit is 1, return false if the bit is 0
	 */
	public int getUInt1(int index, int bitOffset) {
		byte b = getByte(index);
		byte mask = (byte) (1 << bitOffset);
		return ((byte) (b & mask)) != 0 ? 1 : 0;
	}

	/**
	 * put 1-bit unsigned int at the specified bit offset of the byte at current
	 * position
	 * 
	 * @param index     the index of the byte to write
	 * @param bitOffset offset of the bit in one byte, value in range of [0, 7]
	 * @param value     value of the bit(1 or 0)
	 * @return this
	 */
	public ByteArray putUInt1(int bitOffset, int value) {
		putUInt1(writerIndex(), bitOffset, value);
		if (bitOffset == 0)
			writerIndex(writerIndex() + 1);
		return this;
	}

	/**
	 * put 1-bit unsigned int at the specified bit position of the byte at specified
	 * index
	 * 
	 * @param index       the index of the byte to write
	 * @param bitPosition position of the bit in one byte, value in range of [0, 7]
	 * @param value       value of the bit(1 or 0)
	 * @return this
	 */
	public ByteArray putUInt1(int index, int bitPosition, int value) {
		ensureCapacity(index);
		byte b = getByte(index);
		byte mask = (byte) (1 << bitPosition);
		int v = (value != 0) ? (1 << bitPosition) : 0;
		b = (byte) ((b & ~mask) | v);
		putByte(index, b);
		return this;
	}

	/** Read a bool at the current position and increment the position */
	public boolean getBool() {
		byte b = getByte();
		return b != 0;
	}

	/** Read a bool at the given index */
	public boolean getBool(int index) {
		byte b = getByte(index);
		return b != 0;
	}

	/** Write a bool at the current position and increment the position */
	public ByteArray putBool(boolean value) {
		byte b = (byte) (value ? 1 : 0);
		return putByte(b);
	}

	/** Write a bool at the given index */
	public ByteArray putBool(int index, boolean value) {
		ensureCapacity(index + 1);
		byte b = (byte) (value ? 1 : 0);
		putByte(index, b);
		return this;
	}

	/**
	 * Read the 1-bit value at the specified bit offset of the byte at the current
	 * position.​ If this bit is the 0th bit of the byte, increase the current
	 * position.​
	 * 
	 * @param index     The index of the byte to read
	 * @param bitOffset Bit offsets in a byte range from 0 to 7, where 0 represents
	 *                  the first bit (bit 0).​
	 * @return return true if the bit is 1, return false if the bit is 0
	 */
	public boolean getBit(int bitOffset) {
		boolean ret = getBit(readerIndex(), bitOffset);
		if (bitOffset == 0)
			readerIndex(readerIndex() + 1);
		return ret;
	}

	/**
	 * Read the 1-bit value at the specified bit offset of the byte at specified
	 * index
	 * 
	 * @param index     The index of the byte to read
	 * @param bitOffset Bit offsets in a byte range from 0 to 7, where 0 represents
	 *                  the first bit (bit 0).​
	 * @return return true if the bit is 1, return false if the bit is 0
	 */
	public boolean getBit(int index, int bitOffset) {
		byte b = getByte(index);
		byte mask = (byte) (1 << bitOffset);
		return ((byte) (b & mask)) != 0;
	}

	/**
	 * Write the 1-bit value at the specified bit offset of the byte at current
	 * position. If this bit is the 0th bit of the byte, increase the current
	 * position.
	 * 
	 * @param index     The index of the byte to read
	 * @param bitOffset Bit offsets in a byte range from 0 to 7, where 0 represents
	 *                  the first bit (bit 0).​
	 * @param value     A bit value of 1 corresponds to true, and a bit value of 0
	 *                  corresponds to false.​
	 * @return this
	 */
	public ByteArray putBit(int bitOffset, boolean value) {
		putBit(writerIndex(), bitOffset, value);
		if (bitOffset == 0)
			writerIndex(writerIndex() + 1);
		return this;
	}

	/**
	 * Write the 1-bit value at the specified bit offset of the byte at specified
	 * index
	 * 
	 * @param index     The index of the byte to read
	 * @param bitOffset Bit offsets in a byte range from 0 to 7, where 0 represents
	 *                  the first bit (bit 0).​
	 * @param value     A bit value of 1 corresponds to true, and a bit value of 0
	 *                  corresponds to false.​
	 * @return this
	 */
	public ByteArray putBit(int index, int bitOffset, boolean value) {
		ensureCapacity(index);
		byte b = getByte(index);
		byte mask = (byte) (1 << bitOffset);
		int v = value ? (1 << bitOffset) : 0;
		b = (byte) ((b & ~mask) | v);
		putByte(index, b);
		return this;
	}

	/**
	 * Read a 2-bit unsigned integer at the specified bit offset in the byte at
	 * current position. If this bit offset is 1, increase the current position.
	 * 
	 * @param index     The index of the byte to read
	 * @param bitOffset Bit offsets in a byte range from 1 to 7, where 1 represents
	 *                  the second bit (bit 1).​
	 * @return integer
	 */
	public int getUInt2(int bitOffset) {
		int ret = getUInt2(readerIndex(), bitOffset);
		if (bitOffset == 1)
			readerIndex(readerIndex() + 1);
		return ret;
	}

	/**
	 * Read a 2-bit unsigned integer at the specified bit offset in the byte at
	 * specified index
	 * 
	 * @param index     The index of the byte to read
	 * @param bitOffset Bit offsets in a byte range from 1 to 7, where 1 represents
	 *                  the second bit (bit 1).​
	 * @return integer
	 */
	public int getUInt2(int index, int bitOffset) {
		int shift = bitOffset - 1;
		byte mask = (byte) (0x03 << shift);

		byte b = getByte(index);
		return ((b & 0xFF) & mask) >> shift;
	}

	/**
	 * Write a 2-bit unsigned integer at the specified bit offset in the byte at
	 * current position
	 * 
	 * @param index     The index of the byte to read
	 * @param bitOffset Bit offsets in a byte range from 1 to 7, where 1 represents
	 *                  the second bit (bit 1).​
	 * @param value     the value to write
	 * @return this
	 */
	public ByteArray putUInt2(int bitOffset, int value) {
		int index = writerIndex();
		putUInt2(index, bitOffset, value);
		if (bitOffset == 1)
			writerIndex(writerIndex() + 1);
		return this;
	}

	/**
	 * Write a 2-bit unsigned integer at the specified bit offset in the byte at
	 * specified index
	 * 
	 * @param index     The index of the byte to read
	 * @param bitOffset Bit offsets in a byte range from 1 to 7, where 1 represents
	 *                  the second bit (bit 1).​
	 * @param value     The value to write
	 * @return this
	 */
	public ByteArray putUInt2(int index, int bitOffset, int value) {
		ensureCapacity(index);
		int shift = bitOffset - 1;
		byte mask = (byte) (0x03 << shift);

		if (bitOffset < 1)
			throw new RuntimeException("bit offset for unit2 should larger than 0");
		byte b = getByte(index);
		b = (byte) ((b & 0xFF) & (~mask)); // set old bits to zero
		byte b2 = (byte) ((value & 0x03) << shift); // new bits
		b = (byte) ((b & 0xFF) | (b2 & 0xFF)); // combine
		putByte(index, b);
		return this;
	}

	/**
	 * Read a 4-bit unsigned integer at the specified bit offset in the byte at
	 * current position.
	 * 
	 * @param index     The index of the byte to read
	 * @param offsetBit Bit offsets in a byte range from 3 to 7, where 3 represents
	 *                  the 4th bit (bit 3).​
	 * @return integer
	 */
	public int getUInt4(int offsetBit) {
		int ret = getUInt4(readerIndex(), offsetBit);
		if (offsetBit == 3)
			readerIndex(readerIndex() + 1);
		return ret;
	}

	/**
	 * Read a 4-bit unsigned integer at the specified bit offset in the byte at the
	 * given index
	 * 
	 * @param index     The index of the byte to read
	 * @param offsetBit Bit offsets in a byte range from 3 to 7, where 3 represents
	 *                  the 4th bit (bit 3).​
	 * @return integer
	 */
	public int getUInt4(int index, int offsetBit) {
		if (offsetBit == 0)
			offsetBit = 7;
		if (offsetBit <= 2)
			throw new RuntimeException("unit4 offset bit must greater than 2");
		byte b = getByte(index);
		int mask = 0xF << (offsetBit - 3);
		return (b & mask) >> (offsetBit - 3);
	}

	/**
	 * Write a 4-bit unsigned integer at the specified bit offset in the byte at
	 * current position
	 * 
	 * @param offsetBit Bit offsets in a byte range from 3 to 7, where 3 represents
	 *                  the 4th bit (bit 3).​
	 * @param value     The value to write
	 * @return this
	 */
	public ByteArray putUInt4(int offsetBit, int value) {
		putUInt4(writerIndex(), offsetBit, value);
		if (offsetBit == 3)
			writerIndex(writerIndex() + 1);
		return this;
	}

	/**
	 * Write a 4-bit unsigned integer at the specified bit offset in the byte at the
	 * given index
	 * 
	 * @param index     The index of the byte to write
	 * @param offsetBit Bit offsets in a byte range from 3 to 7, where 3 represents
	 *                  the 4th bit (bit 3).​
	 * @param value     The value to write
	 * @return this
	 */
	public ByteArray putUInt4(int index, int offsetBit, int value) {
		ensureCapacity(index);
		if (offsetBit == 0)
			offsetBit = 7;
		if (offsetBit <= 2)
			throw new RuntimeException("unit4 offset bit must greater than 2");

		byte b = getByte(index);
		int mask = 0xF << (offsetBit - 3);

		b = (byte) (b & (~mask));
		byte bv = (byte) (value & 0xF);
		bv = (byte) (bv << (offsetBit - 3));
		b = (byte) (b | bv);

		putByte(index, b);
		return this;
	}

	/** Read a 8-bit unsigned integer at the given index */
	public int getUInt8(int index) {
		return ((0xFF & getByte(index)));
	}

	/**
	 * Read a 8-bit unsigned integer at the current position and increment the
	 * position
	 */
	public int getUInt8() {
		int ret = getUInt8(readerIndex());
		readerIndex(readerIndex() + 1);
		return ret;
	}

	/** Write a 8-bit unsigned integer at the given index */
	public ByteArray putUInt8(int index, int value) {
		ensureCapacity(index);
		putByte(index, (byte) (value & 0xFF));
		return this;
	}

	/**
	 * Write a 8-bit unsigned integer at current position and increment the position
	 */
	public ByteArray putUInt8(int value) {
		putUInt8(writerIndex(), value);
		writerIndex(writerIndex() + 1);
		return this;
	}

	/**
	 * Read a 12-bit unsigned integer at the specified bit offset in the byte at the
	 * given index
	 * 
	 * @param index     The index of the byte to read
	 * @param offsetBit ​​Bit offset values can be either 3 or 7. The value 3
	 *                  represents the 4th bit (bit 3).​
	 * @return integer
	 */
	public int getUInt12(int index, int offsetBit) {
		boolean isLowBits;
		if (offsetBit == 3) {
			isLowBits = true;
		} else if (offsetBit == 7 || offsetBit == 0) {
			offsetBit = 7;
			isLowBits = false;
		} else {
			throw new RuntimeException("offset bit of uint12 should be 7 or 3");
		}

		int ret, b1, b2;
		if (isLowBits) {
			b1 = getUInt4(index, offsetBit);
			b2 = getUInt8(index + 1);
			if (_bigEndian) {
				ret = ((b1 & 0x0F) << 8) | (b2 & 0xFF);
			} else {
				b1 = ((b1 & 0x0F) << 4) | ((b2 & 0xF0) >> 4);
				b2 = b2 & 0x0F;
				ret = ((b2 & 0xFF) << 8) | (b1 & 0xFF);
			}
		} else {
			b1 = getUInt8(index);
			b2 = getUInt4(index + 1, HIGH_BITS);
			if (_bigEndian) {
				ret = ((b1 & 0xFF) << 4) | (b2 & 0x0F);
			} else {
				ret = ((b2 & 0x0F) << 8) | (b1 & 0xFF);
			}
		}
		return ret;
	}

	/**
	 * Read a 12-bit unsigned integer at the specified bit offset in the byte at
	 * current position and increment the position
	 * 
	 * @param offsetBit ​​Bit offset values can be either 3 or 7. The value 3
	 *                  represents the 4th bit (bit 3).​
	 * @return integer
	 */
	public int getUInt12(int offsetBit) {
		int index = readerIndex();
		int ret = getUInt12(index, offsetBit);
		readerIndex(index + (offsetBit == 3 ? 2 : 1));
		return ret;
	}

	/**
	 * Write a 12-bit unsigned integer at the specified bit offset in the byte at
	 * the given index
	 * 
	 * @param index     The index of the byte to read
	 * @param offsetBit ​​Bit offset values can be either 3 or 7. The value 3
	 *                  represents the 4th bit (bit 3).​
	 * @param value     The value to write
	 * @return this
	 */
	public ByteArray putUInt12(int index, int offsetBit, int b) {
		boolean isLowBits;
		if (offsetBit == 3) {
			isLowBits = true;
		} else if (offsetBit == 7 || offsetBit == 0) {
			offsetBit = 7;
			isLowBits = false;
		} else {
			throw new RuntimeException("offset bit of uint12 should be 7 or 3");
		}

		ensureCapacity(index + (isLowBits ? 2 : 1));

		int b1, b2;
		if (_bigEndian) {
			if (isLowBits) {
				b1 = (b & 0xF00) >> 8;
				b2 = (b & 0xFF);
				putUInt4(index, offsetBit, b1);
				putUInt8(index + 1, b2);
				writerIndex(writerIndex() + 2);
//				position(position()+2);
			} else {
				b1 = (b & 0xFF0) >> 4;
				b2 = (b & 0xF);
				putUInt8(index, b1);
				putUInt4(index + 1, HIGH_BITS, b2);
				writerIndex(writerIndex() + 1);
//				position(position()+1);
			}
		} else {
			if (isLowBits) {
				b1 = (b & 0x0F0) >> 4;
				b2 = ((b & 0x0F) << 4) | ((b & 0xF00) >> 8);
				putUInt4(index, offsetBit, b1);
				putUInt8(index + 1, b2);
				writerIndex(writerIndex() + 2);
//				position(position()+2);
			} else {
				b1 = (b & 0xFF);
				b2 = (b & 0xF00) >> 8;
				putUInt8(index, b1);
				putUInt4(index + 1, HIGH_BITS, b2);
				writerIndex(writerIndex() + 1);
//				position(position()+1);
			}
		}
		return this;
	}

	/**
	 * Write a 12-bit unsigned integer at the specified bit offset in the byte at
	 * the current position and increment the position
	 * 
	 * @param index     The index of the byte to read
	 * @param offsetBit ​​Bit offset values can be either 3 or 7. The value 3
	 *                  represents the 4th bit (bit 3).​
	 * @param value     The value to write
	 * @return this
	 */
	public ByteArray putUInt12(int offsetBit, int b) {
		int index = writerIndex();
		putUInt12(index, offsetBit, b);
		writerIndex(writerIndex() + (offsetBit == 3 ? 2 : 1));
		return this;
	}

	/** Read a 16-bit unsigned integer at the given index */
	public int getUInt16(int index) {
		if (_bigEndian) {
			return ((0xFF & getByte(index + 0)) << 8) | ((0xFF & getByte(index + 1)));
		} else {
			return (getByte(index + 1) << 8) | getByte(index + 0);
		}
	}

	/**
	 * Read a 16-bit unsigned integer at the current position and increment the
	 * position
	 */
	public int getUInt16() {
		int ret = getUInt16(readerIndex());
		readerIndex(readerIndex() + 2);
		return ret;
	}

	/** Write a 16-bit unsigned integer at the given index */
	public ByteArray putUInt16(int index, int value) {
		ensureCapacity(index + 2);
		if (_bigEndian) {
			putByte(index + 1, (byte) (0xFF & value));
			putByte(index + 0, (byte) ((0xFF00 & value) >> 8));
		} else {
			putByte(index + 0, (byte) (0xFF & value));
			putByte(index + 1, (byte) ((0xFF00 & value) >> 8));
		}
		return this;
	}

	/**
	 * Write a 16-bit unsigned integer at current position and increment the
	 * position
	 */
	public ByteArray putUInt16(int value) {
		putUInt16(writerIndex(), value);
		writerIndex(writerIndex() + 2);
		return this;
	}

	/** Read a 32-bit unsigned integer at the given index */
	public long getUInt32(int index) {
		long ret;
		if (!_bigEndian) {
			ret = (getByte(index + 3) << 24) | ((0xFFL & getByte(index + 2)) << 16)
					| ((0xFFL & getByte(index + 1)) << 8) | ((0xFFL & getByte(index + 0)));
		} else {
			ret = ((getByte(index + 0)) << 24) | ((0xFFL & getByte(index + 1)) << 16)
					| ((0xFFL & getByte(index + 2)) << 8) | ((0xFFL & getByte(index + 3)));
		}
		ret = ret & 0xFFFFFFFFL;
		return ret;
	}

	/**
	 * Read a 32-bit unsigned integer at the current position and increment the
	 * position
	 */
	public long getUInt32() {
		long ret = getUInt32(readerIndex());
		readerIndex(readerIndex() + 4);
		return ret;
	}

	/** Write a 32-bit unsigned integer at the given index */
	public void putUInt32(int index, long value) {
		ensureCapacity(index + 4);
		if (_bigEndian) {
			putByte(index + 3, (byte) (0xFF & value));
			putByte(index + 2, (byte) ((0xFF00 & value) >> 8));
			putByte(index + 1, (byte) ((0xFF0000 & value) >> 16));
			putByte(index + 0, (byte) ((0xFF000000 & value) >> 24));
		} else {
			putByte(index + 0, (byte) (0xFF & value));
			putByte(index + 1, (byte) ((0xFF00 & value) >> 8));
			putByte(index + 2, (byte) ((0xFF0000 & value) >> 16));
			putByte(index + 3, (byte) ((0xFF000000 & value) >> 24));
		}
	}

	/**
	 * Write a 32-bit unsigned integer at at current position, and increment the
	 * position
	 */
	public ByteArray putUInt32(long value) {
		putUInt32(writerIndex(), value);
		writerIndex(writerIndex() + 4);
		return this;
	}

	/** Read a 64-bit unsigned integer at the given index */
	public long getUInt64(int index) {
		long ret = 0;
		if (_bigEndian) {
			ret = ( //
			((getByte(index + 0) & 0xFFL) << 56) | //
					((getByte(index + 1) & 0xFFL) << 48) | //
					((getByte(index + 2) & 0xFFL) << 40) | //
					((getByte(index + 3) & 0xFFL) << 32) | //
					((getByte(index + 4) & 0xFFL) << 24) | //
					((getByte(index + 5) & 0xFFL) << 16) | //
					((getByte(index + 6) & 0xFFL) << 8) | //
					((getByte(index + 7) & 0xFFL) << 0));//
		} else {
			ret = ( //
			((getByte(index + 7) & 0xFFL) << 56) | //
					((getByte(index + 6) & 0xFFL) << 48) | //
					((getByte(index + 5) & 0xFFL) << 40) | //
					((getByte(index + 4) & 0xFFL) << 32) | //
					((getByte(index + 3) & 0xFFL) << 24) | //
					((getByte(index + 2) & 0xFFL) << 16) | //
					((getByte(index + 1) & 0xFFL) << 8) | //
					((getByte(index + 0) & 0xFFL) << 0));//
		}
		return ret & 0xFFFFFFFFFFFFFFFFL;
	}

	/**
	 * Read a 64-bit unsigned integer at the current position and increment the
	 * position
	 */
	public long getUInt64() {
		long ret = getUInt64(readerIndex());
		readerIndex(readerIndex() + 8);
		return ret;
	}

	/** Write a 64-bit unsigned integer at the given index */
	public void putUInt64(int index, long n) {
		ensureCapacity(index + 8);
		if (_bigEndian) {
			putByte(index + 7, (byte) (0xFFL & n));
			putByte(index + 6, (byte) ((0xFF00L & n) >> 8));
			putByte(index + 5, (byte) ((0xFF0000L & n) >> 16));
			putByte(index + 4, (byte) ((0xFF000000L & n) >> 24));
			putByte(index + 3, (byte) ((0xFF00000000L & n) >> 32));
			putByte(index + 2, (byte) ((0xFF0000000000L & n) >> 40));
			putByte(index + 1, (byte) ((0xFF000000000000L & n) >> 48));
			putByte(index + 0, (byte) ((0xFF00000000000000L & n) >> 56));
		} else {
			putByte(index + 0, (byte) (0xFFL & n));
			putByte(index + 1, (byte) ((0xFF00L & n) >> 8));
			putByte(index + 2, (byte) ((0xFF0000L & n) >> 16));
			putByte(index + 3, (byte) ((0xFF000000L & n) >> 24));
			putByte(index + 4, (byte) ((0xFF00000000L & n) >> 32));
			putByte(index + 5, (byte) ((0xFF0000000000L & n) >> 40));
			putByte(index + 6, (byte) ((0xFF000000000000L & n) >> 48));
			putByte(index + 7, (byte) ((0xFF00000000000000L & n) >> 56));
		}
	}

	/**
	 * Write a 64-bit unsigned integer at at current position, and increment the
	 * position
	 */
	public ByteArray putUInt64(long value) {
		putUInt64(writerIndex(), value & 0xFFFFFFFFFFFFFFFFL);
		writerIndex(writerIndex() + 8);
		return this;
	}

	/** Determine how many bytes the utf8 character occupies */
	private static int getUtf8CharLength(byte b) {
		int firstByte = b & 0xFF;

		if (firstByte == 0)
			return 0; // ending zero char
		if ((firstByte & 0b10000000) == 0)
			return 1; // ASCII
		if ((firstByte & 0b11100000) == 0b11000000)
			return 2;
		if ((firstByte & 0b11110000) == 0b11100000)
			return 3;
		if ((firstByte & 0b11111000) == 0b11110000)
			return 4;
		throw new IllegalArgumentException("Invalid UTF-8 start byte");
	}

	/** Read a UTF8 char at the given index */
	public char getUtf8Char(int index) {
		int charLength = getUtf8CharLength(getByte(index));
		if (charLength == 0)
			return (char) 0;

		byte[] bytes = new byte[charLength];
		this.get(index, bytes);

		String s = new String(bytes, StandardCharsets.UTF_8);

		// Handle surrogate pairs (characters outside BMP)
		if (s.length() == 2 && Character.isSurrogatePair(s.charAt(0), s.charAt(1))) {
//	        return s.substring(0, 2); // Return both surrogate chars
			return s.substring(0, 2).charAt(1); // ???
		}

		return s.charAt(0);// Return single char
	}

	/** Read a UTF8 char at current position, and increment the position */
	public char getUtf8Char() {
		byte firstByte = getByte(readerIndex());
		int charLength = getUtf8CharLength(firstByte);
		if (charLength == 0) {
			readerIndex(readerIndex() + 1);
			return (char) 0;
		} else {
			char c = getUtf8Char(readerIndex());
			readerIndex(readerIndex() + charLength);
			return c;
		}
	}

	/** Write a UTF8 char at the given index, Return the written length.​ */
	public int putUtf8Char(int index, char c) {
		byte[] bytes = Character.toString(c).getBytes(StandardCharsets.UTF_8);
		put(index, bytes);
		return bytes.length;
	}

	/**
	 * Write a UTF8 char at current position, and increment the position, Return the
	 * written length.​
	 */
	public int putUtf8Char(char c) {
		int len = putUtf8Char(writerIndex(), c);
		writerIndex(writerIndex() + len);
		return len;
	}

	/** Read a string at current position, and increment the position */
	public String getString(Charset charset) {
		int len = 0;
		int max = writerIndex();
		int index = readerIndex();

		// calculate length of string bytes
		while (getByte(index + len) != 0 && (index + len) < max)
			len++;

		byte[] bs = new byte[len];
		get(bs);
		readerIndex(len + 1);

		return new String(bs, charset);
	}

	/** Read a string of the specified charset at the given index.​ */
	public String getString(int index, Charset charset) {
		int len = 0;
		int max = writerIndex();

		// calculate length of string bytes
		while (getByte(index + len) != 0 && (index + len) < max)
			len++;

		byte[] bs = new byte[len];
		get(index, bs);

		return new String(bs, charset);
	}

	/**
	 * Write a string at current position, and increment the position, return the
	 * number of bytes written.​
	 */
	public int putString(String s, Charset charset) {
		if (s == null)
			throw new NullPointerException("put string is null");

		byte[] bs = s.getBytes(charset);
		put(bs);
		int count = bs.length;

		// write ending zero
		putByte((byte) 0);
		return count;
	}

	/** Write a string at the given index, return the number of bytes written.​ */
	public int putString(int index, String s, Charset charset) {
		if (s == null)
			throw new NullPointerException("put string is null");

		byte[] bs = s.getBytes(charset);
		put(index, bs);
		int count = bs.length;

		// write ending zero
		ensureCapacity(index);
		putByte(index + count, (byte) 0);
		count += 1;

		return count;
	}

	/** Read a string at current position, and increment the position */
	public String getString() {
		return getString(Charset.defaultCharset());
	}

	/** Read a string at the given index. */
	public String getString(int index) {
		return getString(index, Charset.defaultCharset());
	}

	/**
	 * Write a string at current position, and increment the position, return the
	 * number of bytes written
	 */
	public int putString(String s) {
		return putString(s, Charset.defaultCharset());
	}

	/** Write a string at the given index, return the number of bytes written.​ */
	public int putString(int index, String s) {
		return putString(index, s, Charset.defaultCharset());
	}

	/**
	 * Read a string of specified length at current position, and increment the
	 * position
	 */
	public String getStringLen(int length) {
		byte[] bs = new byte[length];
		get(bs);
		return new String(bs, Charset.defaultCharset());
	}

	/** ​​Read a string of the specified length at the given index */
	public String getStringLen(int index, int length) {
		byte[] bs = new byte[length];
		get(index, bs);
		return new String(bs, Charset.defaultCharset());
	}

	/**
	 * ​​Write a string at the current position (without zero padding), return the
	 * number of bytes written
	 */
	public int putStringLen(String s) {
		byte[] bs = s.getBytes();
		put(bs);
		return bs.length;
	}

	/**
	 * ​​Write a string at the given index (without zero padding), return the number
	 * of bytes written
	 */
	public int putStringLen(int index, String s) {
		byte[] bs = s.getBytes();
		put(index, bs);
		return bs.length;
	}

	/**
	 * ​​Read a byte array of the specified length at the given index
	 * 
	 * @param index  Start offset, negative number means counting from the end.
	 * @param length The length of bytes, -1 means all remaining bytes.
	 * @return
	 */
	public byte[] getBytes(int index, int length) {
		int cap = capacity();

		if (index < 0)
			index = cap + index;

		if (index < 0 || index > cap)
			throw new ArrayIndexOutOfBoundsException(index);

		if (length == UNKNOWN )
			length = limit() - index;
		
		if (length < 0)
			throw new ArrayIndexOutOfBoundsException(length);

		byte[] ret = new byte[length];
		this.get(index, ret);
		return ret;
	}

	/** Read a byte array of the specified length at the current position */
	public byte[] getBytes(int length) {
		byte[] ret = getBytes(readerIndex(), length);
		readerIndex(readerIndex() + length);
		return ret;
	}

	/** Read bytes from the beginning to the end */
	public byte[] getBytes() {
		return getBytes(0, -1);
	}

	/**
	 * Write bytes at the given index.​
	 * 
	 * @param index     start index, negative number means counting from the end.
	 * @param src       source byte array, data to write
	 * @param srcOffset start offset of source byte array
	 * @param length    length of bytes
	 * @return this
	 */
	protected ByteArray putBytes(int index, byte[] src, int srcOffset, int length) {
		if (array() == null)
			throw new NullPointerException("packet byte array is null");
		int cap = capacity();
		if (index < 0)
			index = cap + index;
		if (index < 0 || index > cap)
			throw new ArrayIndexOutOfBoundsException(index);
		this.put(index, src, srcOffset, length);
		return this;
	}

	/**
	 * Write bytes at the current position and increase the position
	 * 
	 * @param index start offset, negative number means counting from the end.
	 * @param src   data to write
	 */
	public ByteArray putBytes(byte[] src) {
		putBytes(writerIndex(), src, 0, src.length);
		writerIndex(writerIndex() + src.length);
		return this;
	}

	/**
	 * Write bytes at the given index.​
	 * 
	 * @param index start offset, negative number means counting from the end.
	 * @param src   data to write
	 */
	public ByteArray putBytes(int index, byte[] src) {
		return putBytes(index, src, 0, src.length);
	}

	/**
	 * Read an object at current position and increase the position
	 * 
	 * @param obj The object to receive value
	 * 
	 * @return the number of bits read
	 */
	public int getObject(IReadWriteByteArray obj) {
		return obj.readByteArray(this, 0);
	}

	/**
	 * Read an object at the specified bit offset in the byte at current position
	 * and increase the position
	 * 
	 * @param obj       The object to receive value
	 * @param bitOffset Bit offsets in a byte range from 0 to 7, where 0 represents
	 *                  the first bit (bit 0).​
	 * @return the number of bits read
	 */
	public int getObject(IReadWriteByteArray obj, int bitOffset) {
		return obj.readByteArray(this, bitOffset);
	}

	/**
	 * Write an object at current position and increase the position
	 * 
	 * @param obj The object to write
	 * @return the number of bits written
	 */
	public int putObject(IReadWriteByteArray obj) {
		return obj.writeByteArray(this);
	}

	/**
	 * Write an object at the specified bit offset in the byte at current position
	 * and increase the position
	 * 
	 * @param obj       The object to write
	 * @param bitOffset Bit offsets in a byte range from 0 to 7, where 0 represents
	 *                  the first bit (bit 0).​
	 * @return the number of bits written
	 */
	public int putObject(IReadWriteByteArray obj, int bitOffset) {
		return obj.writeByteArray(this, bitOffset);
	}

	/** Read TLV at current position, and increment position */
	public TLV getTLV() {
		TLV ret = new TLV();
		ret.tag = getUInt8();
		int len = getUInt8();
		ret.value = new byte[len];
		get(ret.value);
		return ret;
	}

	/** Read TLV at specified position */
	public TLV getTLV(int index) {
		TLV ret = new TLV();
		ret.tag = getUInt8(index);
		int len = getUInt8(index + 1);
		ret.value = new byte[len];
		get(index + 2, ret.value);
		return ret;
	}

	/** Write TLV at current position, and increment position */
	public ByteArray putTLV(int tag, byte[] data) {
		putUInt8(tag);
		if (data != null) {
			putUInt8(data.length);
			put(data);
		} else {
			putUInt8(0);
		}
		return this;
	}

	/** put TLV at specified position */
	public ByteArray putTLV(int index, int tag, byte[] data) {
		putUInt8(index, tag);
		if (data != null) {
			putUInt8(index + 1, data.length);
			put(index + 2, data);
		} else {
			putUInt8(index + 1, 0);
		}
		return this;
	}

	// ---- Methods which are not included in the ByteBuffer classr ----

	/**
	 * Allocated a new byte array of specified length, set the backing array to the
	 * new byte array
	 */
	public void array(int length) {
		array(new byte[length]);
		return;
	}

	/** Set the backing array to specified byte array */
	public void array(byte[] buf) {
		array(buf, 0, UNKNOWN);
	}

	/** Set the backing array to specified byte array with specified offset */
	public void array(byte[] buf, int offset) {
		array(buf, offset, UNKNOWN);
	}

	/**
	 * Set the backing array to specified byte array with specified offset and
	 * length.
	 * 
	 * @param offset offset of the first element of data
	 * @param length length of data
	 * 
	 *               <p>
	 *               The capacity will be {@code length}, <br>
	 *               the arrayOffset will be{@code offset}, <br>
	 *               the position will be 0, <br>
	 *               the limit will be {@code offset + length},<br>
	 *               the its mark will be undefined.
	 *               </p>
	 * 
	 */
	public void array(byte[] buf, int offset, int length) {
		// validate parameters
		if (buf == null) {
			// when buf is null, offset should be 0, length should be non-position
			if (offset != 0)
				throw new NullPointerException();

			if (length > 0)
				throw new NullPointerException();
		} else {
			if (offset < 0)
				offset = buf.length + offset;

			if (offset < 0 || offset > buf.length)
				throw new ArrayIndexOutOfBoundsException();

//			int index = length >= 0 ? offset + length : buf.length - offset;			
//			if (index > buf.length)
//				throw new ArrayIndexOutOfBoundsException();
		}

		// change array
		clearMarks();
		setArray(buf);
		int len = length >= 0 ? length : buf.length - offset;		
		_limit = len;
		_arrayOffset = offset;
		_writerIndex = len;
		_readerIndex = 0;
		_max_capacity = length >= 0 ? length : UNKNOWN;
		updateCapacity();
		onArrayChange();
	}

	/** Return the length of the backing array */
	protected int arrayLength() {
		return array() == null ? 0 : array().length;
	}

	/** Set the first element's offset in the backing array */
	public ByteArray arrayOffset(int offset) {
		if (array() == null)
			throw new NullPointerException("current buffer is null");

		if (offset < 0 || offset > arrayLength())
			throw new ArrayIndexOutOfBoundsException(offset);

		int delta = offset - arrayOffset();
		if (delta == 0)
			return this;

		this._arrayOffset = offset;
		this.readerIndex(readerIndex() - delta);
		this.writerIndex(writerIndex() - delta);
//		this._position = this._position - delta;
//		this._limit = this._limit - delta;
		this.updateCapacity();
		return this;
	}

	/**
	 * Create a child byte array from the specified index and length.​
	 * 
	 * @param index  child's data start index
	 * @param length child's data length
	 *               <p>
	 *               The child array will be backed by the byte array of its parent;
	 *               that is, modifications to the child will cause the parent to be
	 *               modified.
	 */
	public <T extends ByteArray> T child(Class<T> clazz, int index, int length) {
		try {
			T p = clazz.getDeclaredConstructor().newInstance();
			p.array(array(), arrayOffset() + index, length);
			p.parent(this);
			return p;
		} catch (Exception e) {
			throw new RuntimeException("error create child: " + e.getClass().getSimpleName() + " " + e.getMessage());
		}
	}

	/**
	 * Create a child byte array from current position​
	 * 
	 * <p>
	 * The child array will be backed by the byte array of its parent; that is,
	 * modifications to the child will cause the parent to be modified.
	 */
	public <T extends ByteArray> T child(Class<T> clazz) {
		return child(clazz, 0, UNKNOWN);
	}

	/**
	 * Sets the capacity of this ByteArray to the specified value. This method
	 * expands the underlying storage array to accommodate the requested capacity
	 * if it is larger than the current capacity. The capacity cannot be reduced
	 * (this method throws an exception if the requested capacity is smaller than
	 * the current capacity) and cannot exceed the maximum capacity if one has
	 * been set. When expanding the capacity, the existing data is preserved by
	 * copying it to the new array. This method is useful when you need to ensure
	 * that the buffer has sufficient space for upcoming operations. This method
	 * is equivalent to the capacity(int) method in Java's ByteBuffer class but
	 * with the restriction that capacity can only be increased, not decreased.
	 * 
	 * @param cap The new capacity for this ByteArray (must be &ge; current capacity and &ge; 0)
	 * @return This ByteArray instance for method chaining
	 * @throws IllegalArgumentException if the specified capacity is negative
	 * @throws IllegalAccessError if the specified capacity is smaller than the current capacity
	 * @throws IllegalAccessError if a maximum capacity has been set and the requested capacity exceeds it
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(100);
	 * buffer.capacity(200); // Increase capacity to 200 bytes
	 * // buffer.capacity(50); // This would throw IllegalAccessError as capacity cannot be reduced
	 * </pre>
	 */
	public ByteArray capacity(int cap) {
		if (cap < 0)
			throw new IllegalArgumentException("capacity should not be negative");

		int delta = cap - capacity();
		if (delta == 0)
			return this;
		else if (delta < 0)
			throw new IllegalAccessError("cannot reduce capacity");

		if (_max_capacity > 0)
			throw new IllegalAccessError("cannot change capacity because max capacity is specified");

		// old buffer
		byte[] oldBuf = array();

		// create new buffer
		byte[] buf = new byte[arrayLength() + delta];

		// change buffer
		setArray(buf);

		// copy data from old buffer
		if (oldBuf != null) {
			int len = Math.min(oldBuf.length, arrayLength());
			arraycopy(oldBuf, 0, array(), 0, len);
		}

		onArrayChange();
		return this;
	}

//	/** Clear the mark */
//	public ByteArray clearMark() {
//		_mark = UNKNOWN;
//		return this;
//	}

	/**
	 * Clears the reader and writer marks of this ByteArray. This method resets
	 * both the marked reader index and the marked writer index to their undefined
	 * state (UNKNOWN). After calling this method, any attempt to reset the reader
	 * or writer index to the marked position will have no effect. This is
	 * equivalent to calling both resetReaderIndex() and resetWriterIndex() but
	 * without throwing exceptions if the marks are not set. This method is useful
	 * when you want to ensure that marked positions do not interfere with buffer
	 * operations or when cleaning up buffer state between operations.
	 * 
	 * @return This ByteArray instance for method chaining
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(100);
	 * buffer.readerIndex(10).writerIndex(50);
	 * buffer.markReaderIndex(); // Mark current reader position
	 * buffer.markWriterIndex(); // Mark current writer position
	 * // ... do some operations ...
	 * buffer.clearMarks(); // Clear both marks
	 * // buffer.resetReaderIndex(); // This would have no effect now since mark was cleared
	 * </pre>
	 */
	public ByteArray clearMarks() {
//		_mark = UNKNOWN;
		_markReader = UNKNOWN;
		_markWriter = UNKNOWN;
		return this;
	}

	/**
	 * Clears this ByteArray and fills all positions with the specified byte value.
	 * This method performs the same function as the clear() method by setting the
	 * reader index to zero, the writer index to zero, and the limit to the
	 * capacity, but then additionally fills all bytes in the buffer with the
	 * specified value. This is useful when you need to reset the buffer state
	 * while also initializing the content to a specific value. The mark is
	 * discarded as part of the clear operation. This provides a convenient way
	 * to reset the buffer's position and content in a single operation.
	 * 
	 * @param value The byte value to fill all positions in the buffer with
	 * @return This ByteArray instance for method chaining
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(10);
	 * buffer.putByte((byte) 0x01).putByte((byte) 0x02).putByte((byte) 0x03);
	 * buffer.clear((byte) 0x00); // Clear buffer state and fill with zeros
	 * byte b = buffer.getByte(0); // Returns 0x00 (the fill value)
	 * </pre>
	 */
	public ByteArray clear(byte value) {
		clear();
		// set bytes to value
		for (int i = 0; i < capacity(); i++) {
			putByte(i, value);
		}
		return this;
	}

	/**
	 * Ensures that this ByteArray has at least the specified capacity. If the
	 * current capacity is smaller than the specified value, this method will
	 * increase the capacity to accommodate the requirement. The capacity is
	 * increased in steps of 32 bytes to avoid frequent reallocations when
	 * capacity needs to be expanded incrementally. This method is particularly
	 * useful before performing operations that require a specific amount of
	 * space, ensuring that the buffer can accommodate the required data without
	 * throwing capacity-related exceptions. The method rounds up the requested
	 * size to the next multiple of the step size (32) to provide some buffer
	 * for future growth.
	 * 
	 * @param value The minimum capacity that this ByteArray should have
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(100);
	 * buffer.ensureCapacity(150); // Ensures capacity is at least 150 bytes
	 * // If current capacity was 100, it will be increased to at least 160 (next multiple of 32)
	 * int newCapacity = buffer.capacity(); // Returns the new capacity (e.g., 160)
	 * </pre>
	 */
	public void ensureCapacity(int value) {
		if (capacity() >= value)
			return;

		int step = 32;
		value = (Math.round(value / step) + 1) * step;
		capacity(value);
		return;
	}

	/** Get class name */
	protected String getClassName() {
		String ret = this.getClass().getSimpleName();
		if (ret.endsWith("Packet") && ret != "Packet")
			ret = ret.substring(0, ret.length() - 6);
		return ret;
	}
	
	/** Set limit to writer index */
	protected void limitToWriterIndex() {
		int delta = writerIndex() - limit();
		if (delta > 0) limit(limit() + delta);
	}
	
	/**
	 * Set limit to specified number when the limit is smaller than specified number
	 */
	protected void limitMinimum(int n) {
		if (limit() < n) {
			limit(n);
		}
	}

	/**
	 * Returns the maximum allowed capacity of this buffer. This value provides an
	 * upper bound on {@link #capacity()}. The max capacity restricts how much the
	 * buffer can grow when using operations that automatically expand the buffer
	 * size. If the max capacity is set to a negative value (UNKNOWN), it indicates
	 * that there is no upper limit on the buffer's capacity. This is useful when
	 * you want to prevent a buffer from growing beyond a certain size to avoid
	 * excessive memory consumption.
	 * 
	 * @return The maximum allowed capacity of this ByteArray, or a negative value
	 *         (UNKNOWN) if there is no upper limit on capacity
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(10);
	 * int maxCap = buffer.maxCapacity(); // Returns -1 (UNKNOWN) by default
	 * buffer.maxCapacity(100);           // Set max capacity to 100
	 * int newMaxCap = buffer.maxCapacity(); // Returns 100
	 * </pre>
	 */
	public int maxCapacity() {
		return _max_capacity;
	}

	/**
	 * Sets the maximum allowed capacity of this buffer. This value provides an
	 * upper bound on {@link #capacity()}. If the specified value is greater than
	 * the current capacity, the capacity will be expanded to match the new max
	 * capacity. The max capacity restricts how much the buffer can grow when using
	 * operations that automatically expand the buffer size. If set to a negative
	 * value, it removes any capacity restrictions. This method is protected and
	 * primarily intended for internal use or by subclasses that need to manage
	 * buffer capacity constraints.
	 * 
	 * @param value The new maximum capacity for this ByteArray (negative for no limit)
	 * @return This ByteArray instance for method chaining
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(10);
	 * buffer.maxCapacity(100); // Set maximum capacity to 100 bytes
	 * // Now buffer cannot grow beyond 100 bytes
	 * </pre>
	 */
	protected ByteArray maxCapacity(int value) {
		if (value > capacity()) {
			capacity(value);
		}
		_max_capacity = value;
		updateCapacity();
		return this;
	}

	/**
	 * Finds and returns the relative index of the first mismatch between this
	 * buffer and a given buffer or byte array. The index is relative to the
	 * beginning of each buffer and will be in the range of 0 (inclusive) up to the
	 * smaller of the limit elements in each buffer (exclusive).
	 * This method compares the content of this buffer with another buffer or
	 * a byte array, returning the index of the first position where the bytes
	 * differ. If the buffers have identical content up to the compared length,
	 * the method returns 0. This is useful for comparing buffer contents to
	 * detect differences or to implement equality checks. The comparison is
	 * performed on the actual buffer data, taking into account the buffer's
	 * limit but not the reader/writer indexes.
	 *
	 * @param obj The buffer (ByteArray or byte[]) to be tested for a mismatch with this ByteArray
	 *
	 * @return The relative index of the first mismatch between this and the given
	 *         buffer, otherwise 0 if the compared contents are identical.
	 * @throws IllegalArgumentException if the object is not a ByteArray or byte[]
	 *
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer1 = ByteArray.allocate(10);
	 * ByteArray buffer2 = ByteArray.allocate(10);
	 * buffer1.putByte((byte) 0x01).putByte((byte) 0x02);
	 * buffer2.putByte((byte) 0x01).putByte((byte) 0x03); // Different value at position 1
	 * int mismatchIndex = buffer1.mismatch(buffer2); // Returns 1 (first difference at index 1)
	 * </pre>
	 */
	public int mismatch(Object obj) {
		if (obj instanceof ByteArray) {
			ByteArray that = (ByteArray) obj;
			return mismatchArray(this.array(), 0, that.array(), 0, this.limit());
		} else if (obj instanceof byte[]) {
			byte[] that = (byte[]) obj;
			return mismatchArray(this.array(), 0, that, 0, this.limit());
		}

		throw new IllegalArgumentException("cannot compare to " + obj.getClass().getName());
	}

	/**
	 * Callback method invoked when the underlying byte array of this ByteArray
	 * is changed. This protected method serves as a hook that allows subclasses
	 * to perform custom actions when the backing array is modified, such as
	 * when a new array is set via the array() method or when the capacity is
	 * changed. The default implementation does nothing, but subclasses can
	 * override this method to add custom behavior like updating internal state,
	 * notifying listeners, or performing validation after array changes. This
	 * method is called automatically by operations that modify the underlying
	 * array, ensuring that subclasses can maintain consistency when the buffer's
	 * storage changes. This is particularly useful for implementing custom
	 * buffer types that need to maintain additional metadata or perform
	 * specific actions when the buffer storage is reallocated.
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * public class CustomByteArray extends ByteArray {
	 *     private int checksum = 0;
	 *     
	 *     {@literal @}Override
	 *     protected void onArrayChange() {
	 *         super.onArrayChange(); // Always call super implementation
	 *         // Recalculate checksum or update other internal state
	 *         this.checksum = calculateChecksum();
	 *     }
	 * }
	 * </pre>
	 */
	protected void onArrayChange() {
		// child class could implement this method to catch the event when array changed
	}

	/**
	 * Gets the parent ByteArray of this instance. In ByteArray hierarchies, a
	 * buffer can have a parent buffer when it's created as a slice or child
	 * of another buffer. The child buffer shares the same underlying byte array
	 * with its parent, so modifications to one will affect the other. This method
	 * returns the immediate parent buffer in the hierarchy, or null if this
	 * buffer has no parent. This is useful for navigating buffer relationships
	 * and understanding the buffer's position in a hierarchy of related buffers.
	 * 
	 * @return The parent ByteArray of this instance, or null if this buffer has no parent
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray parentBuffer = ByteArray.allocate(100);
	 * ByteArray childBuffer = parentBuffer.slice(10, 20); // Create child buffer from parent
	 * ByteArray parent = childBuffer.parent(); // Returns the original parentBuffer
	 * boolean hasParent = (parent != null); // Returns true
	 * </pre>
	 */
	public ByteArray parent() {
		return _parent;
	}

	/**
	 * Sets the parent ByteArray of this instance. This method establishes a
	 * parent-child relationship between buffers, which is used internally when
	 * creating slices or child buffers. When a parent is set, operations that
	 * affect the buffer's structure or capacity may propagate to the parent to
	 * maintain consistency. This method is primarily used internally by methods
	 * like slice() and child(), but can also be used by subclasses that need
	 * to establish buffer hierarchies. Setting a parent enables shared state
	 * management between related buffers.
	 * 
	 * @param p The parent ByteArray to set for this instance (can be null to remove parent)
	 * @return This ByteArray instance for method chaining
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray parent = ByteArray.allocate(50);
	 * ByteArray child = ByteArray.allocate(10);
	 * child.parent(parent); // Establish parent-child relationship
	 * ByteArray retrievedParent = child.parent(); // Returns the parent buffer
	 * </pre>
	 */
	public ByteArray parent(ByteArray p) {
		_parent = p;
		return this;
	}

	/**
	 * Sets whether this ByteArray is read-only. When a buffer is read-only,
	 * write operations will throw exceptions rather than modifying the buffer.
	 * This provides a way to protect buffer contents from modification while
	 * still allowing read operations. This is useful when you need to share
	 * buffer access with code that should only read the data, or when
	 * implementing APIs that need to provide read-only views of buffer data.
	 * The read-only status affects all write operations on the buffer and
	 * its associated views or slices. Note that making a buffer read-only
	 * does not prevent modification through the underlying array if accessed
	 * directly.
	 * 
	 * @param isReadOnly true to make this buffer read-only, false to allow write operations
	 * @return This ByteArray instance for method chaining
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray buffer = ByteArray.allocate(10);
	 * buffer.putByte((byte) 0x41); // This works
	 * buffer.readOnly(true);       // Make buffer read-only
	 * // buffer.putByte((byte) 0x42); // This would throw an exception
	 * byte b = buffer.getByte(0);  // This still works
	 * </pre>
	 */
	public ByteArray readOnly(boolean isReadOnly) {
		_readOnly = isReadOnly;
		return this;
	}

	/**
	 * Returns the top-level parent in the buffer hierarchy. This method traverses
	 * the parent chain to find the root buffer that has no parent itself. If this
	 * buffer has no parent, it returns itself. This is useful when you need to
	 * access the original buffer in a hierarchy of related buffers created through
	 * slicing or other operations. The root buffer typically represents the
	 * original data source and provides access to the complete underlying byte
	 * array. This method is particularly useful when implementing buffer
	 * management or when you need to perform operations on the entire data set
	 * that a hierarchy of buffers represents.
	 * 
	 * @return The root ByteArray in the hierarchy (this buffer or its topmost parent)
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * ByteArray rootBuffer = ByteArray.allocate(100);
	 * ByteArray child1 = rootBuffer.slice(10, 20);
	 * ByteArray child2 = child1.slice(5, 10);
	 * ByteArray root = child2.root(); // Returns the original rootBuffer
	 * </pre>
	 */
	public ByteArray root() {
		ByteArray ret = this;
		while (ret.parent() != null)
			ret = ret.parent();
		return ret;
	}

	/** used in toHex() */
	private static final char[] UPPER_HEX_CHARS = "0123456789ABCDEF".toCharArray();

	/** used in toHex() */
	private static final char[] LOWER_HEX_CHARS = "0123456789abcdef".toCharArray();

	/**
	 * convert to HEX string
	 * 
	 * @param separator separator
	 * @param index     index
	 * @param length    length
	 * @param columns   how many chars per row
	 * 
	 * @return hex string
	 */
	public String toHex(String separator, int index, int length, int columns) {
		if (array() == null)
			throw new NullPointerException();

		if (length < 0)
			length = array().length - arrayOffset() - index;
		
		if (arrayOffset() + index + length >= array().length) 
			length = array().length - arrayOffset() - index;

		char[] HEX_CHARS = _lowerCase ? LOWER_HEX_CHARS : UPPER_HEX_CHARS;
		boolean hasSep = separator != null && separator.length() > 0;
		StringBuilder sb = new StringBuilder();
		int col = 0;

		for (int i = 0; i < length; i++) {
			int v = 0;
			try {
				v = getByte(index + i) & 0xFF;
			} catch (Exception e) {
				v = 0;
			}
			sb.append(HEX_CHARS[v >>> 4]).append(HEX_CHARS[v & 0x0F]);
			if (hasSep && i < length - 1)
				sb.append(separator);
			col++;
			if (columns > 0) {
				if (col >= columns) {
					sb.append("\r\n");
					col = 0;
				}
			}
		}
		return sb.toString();
	}

	/** convert to HEX string */
	public String toHex() {
		return toHex(" ", 0, writerIndex(), UNKNOWN);
	}

	/** update parent's limit */
	protected void updateParentLimit(int offset) {
		ByteArray p = _parent;
		if (p != null) {
			if (p.arrayOffset() + p.writerIndex() < offset) {
				p.writerIndex(offset - p.arrayOffset());
			}
			p.updateParentLimit(offset);
		}
	}

	

	

}
