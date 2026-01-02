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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;




import static org.junit.Assert.*;

public class TextRowTest {

    @Test
    public void testDefaultConstructor() {
        TextRow textRows = new TextRow();
        assertNotNull(textRows);
        assertTrue(textRows.isEmpty());
    }

    @Test
    public void testListConstructor() {
        List<String> inputList = Arrays.asList("Line 1", "Line 2", "Line 3");
        TextRow textRows = new TextRow(inputList);
        assertEquals(3, textRows.size());
        assertEquals("Line 1", textRows.get(0));
        assertEquals("Line 2", textRows.get(1));
        assertEquals("Line 3", textRows.get(2));
    }

    @Test
    public void testStringConstructor() {
        String multiLineString = "Line 1\nLine 2\nLine 3";
        TextRow textRows = new TextRow(multiLineString);
        assertEquals(3, textRows.size());
        assertEquals("Line 1", textRows.get(0));
        assertEquals("Line 2", textRows.get(1));
        assertEquals("Line 3", textRows.get(2));
    }

    @Test
    public void testStringArrayConstructor() {
        String[] lines = {"Line 1", "Line 2", "Line 3"};
        TextRow textRows = new TextRow(lines);
        assertEquals(3, textRows.size());
        assertEquals("Line 1", textRows.get(0));
        assertEquals("Line 2", textRows.get(1));
        assertEquals("Line 3", textRows.get(2));
    }

    @Test
    public void testStringArrayConstructorWithCarriageReturn() {
        String[] lines = {"Line 1\r", "Line 2\r\n", "Line 3"};
        TextRow textRows = new TextRow(lines);
        assertEquals(3, textRows.size());
        assertEquals("Line 1", textRows.get(0));
        assertEquals("Line 2", textRows.get(1));
        assertEquals("Line 3", textRows.get(2));
    }

    @Test
    public void testCut() {
        TextRow textRows = new TextRow(Arrays.asList("start-middle-end", "start2-middle2-end2", "no markers here"));
        TextRow result = textRows.cut("start-", "-end");
        assertEquals(1, result.size());
        assertEquals("middle", result.get(0));
    }

    @Test
    public void testCutWithNullBefore() {
        TextRow textRows = new TextRow(Arrays.asList("Hello-World", "Test-Case"));
        TextRow result = textRows.cut(null, "-");
        assertEquals(2, result.size());
        assertEquals("Hello", result.get(0));
        assertEquals("Test", result.get(1));
    }

    @Test
    public void testCutWithNullAfter() {
        TextRow textRows = new TextRow(Arrays.asList("Hello-World", "Test-Case"));
        TextRow result = textRows.cut("-", null);
        assertEquals(2, result.size());
        assertEquals("World", result.get(0));
        assertEquals("Case", result.get(1));
    }

    @Test
    public void testClone() {
        TextRow original = new TextRow(Arrays.asList("Line 1", "Line 2", "Line 3"));
        TextRow cloned = original.clone();
        assertEquals(original.size(), cloned.size());
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i), cloned.get(i));
        }
        // Ensure it's a different object
        assertNotSame(original, cloned);
    }

    @Test
    public void testFilter() {
        TextRow textRows = new TextRow(Arrays.asList("apple", "banana", "cherry", "date"));
        TextRow filtered = textRows.filter("a");
        assertEquals(3, filtered.size());
        assertTrue(filtered.contains("apple"));
        assertTrue(filtered.contains("banana"));
        assertTrue(filtered.contains("date"));
    }

    @Test
    public void testFilterWithRegex() {
        TextRow textRows = new TextRow(Arrays.asList("cat", "dog", "bird", "fish"));
        TextRow filtered = textRows.filter("/^[a-z]{3}$/"); // 3-letter words only
        assertEquals(2, filtered.size());
        assertTrue(filtered.contains("cat"));
        assertTrue(filtered.contains("dog"));
    }

    @Test
    public void testDelete() {
        TextRow textRows = new TextRow(Arrays.asList("apple", "banana", "cherry", "date"));
        TextRow deleted = textRows.delete("a");
        assertEquals(1, deleted.size());
        assertEquals("cherry", deleted.get(0));
    }

    @Test
    public void testSplitByColumnIndexes() {
        TextRow textRows = new TextRow(Arrays.asList("abcde", "fghij", "klmno"));
        TextTable table = textRows.split(2, 4);
        // This should split at indexes 2 and 4, creating 3 columns: [0-2), [2-4), [4-end]
        assertEquals(3, table.size());
        assertEquals(3, table.get(0).size()); // 3 columns
        assertEquals("ab", table.get(0).get(0));
        assertEquals("cd", table.get(0).get(1));
        assertEquals("e", table.get(0).get(2));
    }

    @Test
    public void testSplitBySeparator() {
        TextRow textRows = new TextRow(Arrays.asList("a,b,c", "d,e,f", "g,h,i"));
        TextTable table = textRows.split(",");
        assertEquals(3, table.size());
        assertEquals(3, table.get(0).size()); // 3 columns
        assertEquals("a", table.get(0).get(0));
        assertEquals("b", table.get(0).get(1));
        assertEquals("c", table.get(0).get(2));
    }

    @Test
    public void testToString() {
        TextRow textRows = new TextRow(Arrays.asList("Line 1", "Line 2", "Line 3"));
        String result = textRows.toString();
        assertTrue(result.contains("Line 1\r\n"));
        assertTrue(result.contains("Line 2\r\n"));
        assertTrue(result.contains("Line 3\r\n"));
    }

    @Test
    public void testSizeAndIndexOperations() {
        TextRow textRows = new TextRow(Arrays.asList("Line 1", "Line 2", "Line 3"));
        assertEquals(3, textRows.size());
        assertEquals("Line 1", textRows.get(0));
        assertEquals("Line 2", textRows.get(1));
        assertEquals("Line 3", textRows.get(2));
    }

    @Test
    public void testEmptyConstructorAndOperations() {
        TextRow textRows = new TextRow();
        assertTrue(textRows.isEmpty());
        textRows.add("New Line");
        assertEquals(1, textRows.size());
        assertEquals("New Line", textRows.get(0));
    }

    @Test
    public void testNullHandlingInListConstructor() {
        List<String> listWithNulls = Arrays.asList("Line 1", null, "Line 3");
        TextRow textRows = new TextRow(listWithNulls);
        assertEquals(2, textRows.size()); // null should be skipped
        assertEquals("Line 1", textRows.get(0));
        assertEquals("Line 3", textRows.get(1));
    }
}