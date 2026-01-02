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

import static org.junit.Assert.*;

import org.junit.Test;



public class ByteArrayTest {

    @Test
    public void testAllocate() {
        ByteArray array = ByteArray.allocate(10);
        assertNotNull(array);
        assertEquals(10, array.capacity());
        assertEquals(0, array.remaining());
        assertEquals(10, array.writableBytes());
    }

    @Test
    public void testWrap() {
        byte[] buf = new byte[]{1, 2, 3, 4, 5};
        ByteArray array = new ByteArray(buf);
        assertNotNull(array);
        assertEquals(0, array.readerIndex());
        assertEquals(5, array.writerIndex());
        assertEquals(5, array.limit());
    }

    @Test
    public void testGetAndPutByte() {
        ByteArray array = ByteArray.allocate(10);
        array.putByte((byte) 5);
        assertEquals((byte) 5, array.getByte(0));
    }

    @Test
    public void testGetAndPutShort() {
        ByteArray array = ByteArray.allocate(10);
        array.putShort((short) 1000);
        assertEquals((short) 1000, array.getShort(0));
    }

    @Test
    public void testGetAndPutInt() {
        ByteArray array = ByteArray.allocate(10);
        array.putInt(100000);
        assertEquals(100000, array.getInt(0));
    }

    @Test
    public void testGetAndPutLong() {
        ByteArray array = ByteArray.allocate(10);
        array.putLong(10000000000L);
        assertEquals(10000000000L, array.getLong(0));
    }

    @Test
    public void testGetAndPutFloat() {
        ByteArray array = ByteArray.allocate(10);
        array.putFloat(3.14f);
        assertEquals(3.14f, array.getFloat(0), 0.01f);
    }

    @Test
    public void testGetAndPutDouble() {
        ByteArray array = ByteArray.allocate(10);
        array.putDouble(3.14159);
        assertEquals(3.14159, array.getDouble(0), 0.0001);
    }

    @Test
    public void testGetAndPutString() {
        ByteArray array = ByteArray.allocate(20);
        String testString = "Hello";
        array.putString(testString);
        array.readerIndex(0);
        assertEquals(testString, array.getString());
    }
    
    @Test
    public void testGetPutBytes() {
    	String testString = "Hello";
        ByteArray array = ByteArray.allocate(20);
        byte[] b1 = testString.getBytes();
        byte[] b2 = new byte[b1.length];
        array.put(b1);
        array.readerIndex(0);
        array.get(b2);
        assertEquals(testString, new String(b2));
    }

    @Test
    public void testOrder() {
        ByteArray array = ByteArray.allocate(10);
        array.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        assertEquals(java.nio.ByteOrder.LITTLE_ENDIAN, array.order());
        array.order(java.nio.ByteOrder.BIG_ENDIAN);
        assertEquals(java.nio.ByteOrder.BIG_ENDIAN, array.order());
    }

//    @Test
//    public void testPositionAndLimit() {
//        ByteArray array = ByteArray.allocate(10);
//        array.readerIndex(5);
//        assertEquals(5, array.array());
//        array.limit(8);
//        assertEquals(8, array.limit());
//        assertTrue(array.hasRemaining());
//        assertEquals(3, array.remaining());
//    }

//    @Test
//    public void testFlip() {
//        ByteArray array = ByteArray.allocate(10);
//        array.readerIndex(5);
////        array.flip();
//        assertEquals(0, array.readerIndex());
//        assertEquals(5, array.limit());
//    }
    
//    @Test
//    public void testRewind() {
//        ByteArray array = ByteArray.allocate(10);
//        array.position(5);
//        array.rewind();
//        assertEquals(0, array.position());
//    }

    @Test
    public void testClear() {
        ByteArray array = ByteArray.allocate(10);
        array.writerIndex(10);
        array.readerIndex(5);
        array.clear();
        assertEquals(0, array.readerIndex());
        assertEquals(0, array.writerIndex());
    }

}