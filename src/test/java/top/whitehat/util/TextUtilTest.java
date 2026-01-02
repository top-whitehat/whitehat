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

import java.util.List;
import java.util.regex.Pattern;

import junit.framework.TestCase;


public class TextUtilTest extends TestCase {
	public void testGetPattern() {
		Pattern p = TextUtil.getPattern("/a[opq]*c/i");
		assertTrue(p.matcher("dAoppqcc").find());
	}

	public void testIsNumber() {
		assertTrue(TextUtil.isNumber("0.323"));
		assertTrue(TextUtil.isNumber(".323"));
		assertTrue(TextUtil.isNumber("+3.3"));
		assertTrue(TextUtil.isNumber("-3"));
		assertFalse(TextUtil.isNumber("-3a"));
		assertFalse(TextUtil.isNumber(""));
		assertFalse(TextUtil.isNumber(null));
	}

	public void testIsNumbers() {
		// Test valid number pairs
		assertTrue(TextUtil.isNumbers("123", "456"));
		assertTrue(TextUtil.isNumbers("12.34", "56.78"));
		assertTrue(TextUtil.isNumbers("0", "-456"));

		// Test invalid number pairs
		assertFalse(TextUtil.isNumbers("123", "abc"));
		assertFalse(TextUtil.isNumbers("abc", "456"));
		assertFalse(TextUtil.isNumbers("abc", "def"));
		assertFalse(TextUtil.isNumbers(null, "123"));
	}

	public void testCompareWithNumbers() {
		// Test numeric comparisons (these call compareNumber internally)
		assertTrue(TextUtil.compare("123", "=", "123"));
		assertTrue(TextUtil.compare("123", "==", "123"));
		assertTrue(TextUtil.compare("12.34", "=", "12.34"));
		assertTrue(TextUtil.compare("12.34", "==", "12.34"));

		// Test numeric inequality
		assertTrue(TextUtil.compare("123", "!=", "456"));
		assertTrue(TextUtil.compare("123", "<>", "456"));
		assertTrue(TextUtil.compare("12.34", "!=", "56.78"));
		assertTrue(TextUtil.compare("12.34", "<>", "56.78"));

		// Test numeric greater than
		assertTrue(TextUtil.compare("456", ">", "123"));
		assertTrue(TextUtil.compare("56.78", ">", "12.34"));

		// Test numeric less than
		assertTrue(TextUtil.compare("123", "<", "456"));
		assertTrue(TextUtil.compare("12.34", "<", "56.78"));

		// Test numeric greater than or equal
		assertTrue(TextUtil.compare("456", ">=", "123"));
		assertTrue(TextUtil.compare("123", ">=", "123"));
		assertTrue(TextUtil.compare("56.78", ">=", "12.34"));
		assertTrue(TextUtil.compare("12.34", ">=", "12.34"));

		// Test numeric less than or equal
		assertTrue(TextUtil.compare("123", "<=", "456"));
		assertTrue(TextUtil.compare("123", "<=", "123"));
		assertTrue(TextUtil.compare("12.34", "<=", "56.78"));
		assertTrue(TextUtil.compare("12.34", "<=", "12.34"));
	}

	public void testCompare() {
		assertTrue(TextUtil.compare("0.323", "<", "1.42"));
		assertFalse(TextUtil.compare("abc", "=", null));
		assertTrue(TextUtil.compare("abc", "=", "abc"));
		assertTrue(TextUtil.compare("abc", "<", "cbd"));
		assertTrue(TextUtil.compare("abc", "<=", "abc"));
		assertTrue(TextUtil.compare("bbc", ">", "abb"));
		assertTrue(TextUtil.compare("bbc", ">=", "bbc"));
	}

	public void testMatch() {
		assertTrue(TextUtil.match("ABCDF", "BCD"));
		assertFalse(TextUtil.match("ABCDF", "bcd"));
		assertTrue(TextUtil.match("ABCDF", "/bcd/i"));
	}

	public void testSplit() {
		List<String> arr = TextUtil.split("  AB  \t CDF \t D  ", " \t");
		assertEquals(3, arr.size());

		arr = TextUtil.split("  AB  CD  F", 5, 10);
		assertEquals(3, arr.size());
	}

	// Additional tests for all TextUtil methods
	public void testIsInteger() {
		// Test valid integers
		assertTrue(TextUtil.isInteger("123"));
		assertTrue(TextUtil.isInteger("-456"));
		assertTrue(TextUtil.isInteger("0"));
		assertTrue(TextUtil.isInteger("+789"));

		// Test invalid integers
		assertFalse(TextUtil.isInteger("12.34"));
		assertFalse(TextUtil.isInteger(".323"));
		assertFalse(TextUtil.isInteger("abc"));
		assertFalse(TextUtil.isInteger(""));
		assertFalse(TextUtil.isInteger(null));
	}

	public void testPrintfFormatToRegex() {
		// Test string format
		assertEquals("([^\\r\\n]*?)", TextUtil.printfFormatToRegex("//%s//"));
		assertEquals("([^\\r\\n]{5})", TextUtil.printfFormatToRegex("//%5s//"));

		// Test float format
		assertEquals("([0-9+-.]+)", TextUtil.printfFormatToRegex("//%f//"));
		assertEquals("([ \\t0-9.+-]{8})", TextUtil.printfFormatToRegex("//%8.3f//"));

		// Test integer format
		assertEquals("([0-9+-]+)", TextUtil.printfFormatToRegex("//%d//"));
		assertEquals("([ \\t0-9+-]{5})", TextUtil.printfFormatToRegex("//%5d//"));

		// Test character format
		assertEquals("(.)", TextUtil.printfFormatToRegex("//%c//"));
		assertEquals("(.{3})", TextUtil.printfFormatToRegex("//%3c//"));

		// Test hex format
		assertEquals("([0-9A-Za-z+-]+)", TextUtil.printfFormatToRegex("//%x//"));
		assertEquals("([ \\t0-9A-Za-z+-]{4})", TextUtil.printfFormatToRegex("//%4x//"));

		// Test percent sign
		assertEquals("%", TextUtil.printfFormatToRegex("//%%//"));
	}

	public void testExtractWords() {
		// Test extracting words using regex
		String[] words = TextUtil.extractWords("I love china.", "//I love %5s.//");
		assertEquals(1, words.length);
		assertEquals("china", words[0]);

		// Test extracting multiple words
		String[] words2 = TextUtil.extractWords("   1   2.3    -.35", "//%4d%6f%8.2f//");
		assertEquals(3, words2.length);
		assertEquals("1", words2[0].trim());
		assertEquals("2.3", words2[1].trim());
		assertEquals("-.35", words2[2].trim());

		// Test no match
		String[] words3 = TextUtil.extractWords("no match", "//%d//");
		assertEquals(0, words3.length);
	}

	public void testMatchWithPattern() {
		// Test matching with pre-compiled pattern
		Pattern p = Pattern.compile(".*BCD.*");
		assertTrue(TextUtil.match("ABCDF", p));

		Pattern p2 = Pattern.compile(".*xyz.*");
		assertFalse(TextUtil.match("ABCDF", p2));
	}

	public void testClone() {
		// Test cloning a list
		List<String> original = TextUtil.split("a b c", " ");
		List<String> cloned = TextUtil.clone(original);

		assertEquals(original.size(), cloned.size());
		for (int i = 0; i < original.size(); i++) {
			assertEquals(original.get(i), cloned.get(i));
		}

		// Test cloning null
		assertNull(TextUtil.clone(null));
	}

	public void testSplitByIndexes() {
		// Test splitting by column indexes
		List<String> arr = TextUtil.split("  AB  CD  F", 5, 10);
		assertEquals(3, arr.size());
		assertEquals("AB", arr.get(0));
		assertEquals("CD", arr.get(1));
		assertEquals("F", arr.get(2));

		// Test with negative starting index
		List<String> arr2 = TextUtil.split("ABCD", -1, 2);
		assertEquals(2, arr2.size());
		assertEquals("AB", arr2.get(0));
		assertEquals("CD", arr2.get(1));
	}

	public void testSplitBySeparatorWithLimit() {
		// Test splitting by separator characters with limit
		List<String> arr = TextUtil.split("a b c d e", " ", 3);
		assertEquals(3, arr.size());
		assertEquals("a", arr.get(0));
		assertEquals("b", arr.get(1));
		assertEquals("c d e", arr.get(2));
	}
	
	public void testCut() {
		// Test cutting text between two markers
		assertEquals("test", TextUtil.cut("start test end", "start ", " end"));
		assertEquals("test", TextUtil.cut("start test", "start ", null));
		assertEquals("test", TextUtil.cut("test end", null, " end"));
		assertEquals("test", TextUtil.cut("test", null, null));

		// Test when markers are not found
		assertNull(TextUtil.cut("start test end", "missing ", " end"));
		assertNull(TextUtil.cut("start test end", "start ", " missing"));

		// Test when end marker is before start marker
		assertNull(TextUtil.cut("end test start", "start ", " end"));
	}

}
