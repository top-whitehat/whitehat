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




public class ArrayUtilTest {

    @Test
    public void testIsArrayClass() {
        assertTrue(ArrayUtil.isArrayClass(int[].class));
        assertTrue(ArrayUtil.isArrayClass(String[].class));
        assertTrue(ArrayUtil.isArrayClass(int[][].class));
        assertFalse(ArrayUtil.isArrayClass(String.class));
        assertFalse(ArrayUtil.isArrayClass(null));
    }

    @Test
    public void testGetElementClass() {
        assertEquals(int.class, ArrayUtil.getElementClass(int[].class));
        assertEquals(String.class, ArrayUtil.getElementClass(String[].class));
        assertEquals(int[].class, ArrayUtil.getElementClass(int[][].class));
        assertEquals(null, ArrayUtil.getElementClass(String.class));
        assertEquals(null, ArrayUtil.getElementClass(null));
    }

    @Test
    public void testGetArrayClass() {
        Class<?> arrayClass = ArrayUtil.getArrayClass(String.class, 0);
        assertTrue(ArrayUtil.isArrayClass(arrayClass));
        assertEquals(String[].class, arrayClass);
    }

    @Test
    public void testGetMultiDimensionArrayClass() {
        Class<?> arrayClass = ArrayUtil.getMultiDimensionArrayClass(int.class, 2);
        assertTrue(ArrayUtil.isArrayClass(arrayClass));
        assertEquals(int[][].class, arrayClass);
    }

    @Test
    public void testGetPrimitiveTypeCode() {
        assertEquals("I", ArrayUtil.getArrayClass(int.class, 0).getName().substring(1, 2));
    }

    @Test
    public void testIsArrayObject() {
        assertTrue(ArrayUtil.isArrayObject(new int[5]));
        assertTrue(ArrayUtil.isArrayObject(new String[3]));
        assertFalse(ArrayUtil.isArrayObject("not an array"));
        assertFalse(ArrayUtil.isArrayObject(null));
    }

    @Test
    public void testGetLength() {
        int[] arr = {1, 2, 3, 4, 5};
        assertEquals(5, ArrayUtil.getLength(arr));
        assertEquals(-1, ArrayUtil.getLength(null));
    }

    @Test
    public void testGetAndSet() {
        int[] arr = {1, 2, 3, 4, 5};
        assertEquals(3, ArrayUtil.get(arr, 2));
        ArrayUtil.set(arr, 2, 10);
        assertEquals(10, arr[2]);
    }

    @Test
    public void testNewInstance() {
        Object arr = ArrayUtil.newInstance(String.class, 10);
        assertTrue(arr instanceof String[]);
        assertEquals(10, ((String[]) arr).length);
    }
}