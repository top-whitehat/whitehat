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

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * TextRow is a specialized ArrayList<String> implementation designed for
 * handling multi-line text data efficiently. It provides convenient methods for
 * text manipulation, filtering, and parsing operations commonly needed when
 * processing text files or multi-line string data. The class extends
 * ArrayList<String> to maintain all standard list operations while adding
 * text-specific functionality such as file loading, pattern matching, and text
 * filtering.
 * 
 * <h3>Usage Examples</h3>
 * 
 * <pre>
 * // Create from a multi-line string
 * String text = "Line 1\nLine 2\nLine 3";
 * TextRow textRow = new TextRow(text);
 * 
 * // Load from file
 * TextRow fileText = TextRow.fromFile("example.txt");
 * 
 * // Filter rows containing specific text
 * TextRow filtered = textRow.filter("Line 2");
 * 
 * // Split into a table based on column positions
 * TextTable table = textRow.split(0, 10, 20);
 * </pre>
 */
public class TextRow extends ArrayList<String> {
	private static final long serialVersionUID = 8577775977975871948L;

	/** load from specified file */
	public static TextRow fromFile(String filename) {
		// Try-with-resources ensures the reader is closed automatically
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			StringBuilder content = new StringBuilder();

			// Read each line until the end of the file (null)
			while ((line = br.readLine()) != null) {
				content.append(line).append("\n"); // Append newline for line breaks
			}

			String fullText = content.toString();
			return new TextRow(fullText);

		} catch (Exception e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " " + e.getMessage());
		}
	}

	/** load from specified string */
	public static TextRow fromString(String text) {
		return new TextRow(text);
	}

	/** Default constructor for TextRow */
	public TextRow() {
	}

	/**
	 * Constructs a TextRow from a List of strings
	 * 
	 * @param rows The list of strings to initialize the TextRow with
	 */
	public TextRow(List<String> rows) {
		for (String r : rows) {
			if (r == null)
				continue;
			if (r.endsWith("\r\n"))
				r = r.substring(0, r.length() - 2);
			if (r.endsWith("\r"))
				r = r.substring(0, r.length() - 1);
			add(r);
		}
	}

	/**
	 * Constructs a TextRow from a multiple line string Each line in the string
	 * becomes a separate row in the TextRow
	 * 
	 * @param text The multiple line string to split into rows
	 */
	public TextRow(String text) {
		this(text.split("\n"));
	}

	public TextRow(TextRow rows) {
		this((List<String>) rows);
	}

	/**
	 * Constructs a TextRow from an array of strings Each string in the array
	 * becomes a separate row in the TextRow Carriage return characters at the end
	 * of lines are removed
	 * 
	 * @param lines The array of strings to initialize the TextRow with
	 */
	public TextRow(String[] lines) {
		for (int i = 0; i < lines.length; i++) {
			String r = lines[i];
			if (r == null)
				continue;
			if (r.endsWith("\r\n"))
				r = r.substring(0, r.length() - 2);
			if (r.endsWith("\r"))
				r = r.substring(0, r.length() - 1);
			add(r);
		}

	}

	public String getFrom(int index, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (int i = index; i < this.size(); i++) {
			if (i != index)
				sb.append(delimiter);
			sb.append(get(i));
		}
		return sb.toString();
	}

	/**
	 * Extracts substrings from each row between the specified 'before' and 'after'
	 * markers
	 * 
	 * Rows that don't contain both markers are removed
	 * 
	 * @param before The starting marker string (null means start of line)
	 * 
	 * @param after  The ending marker string (null means end of line)
	 * 
	 * @return This TextRow with modified rows containing only the extracted
	 *         substrings
	 */
	public TextRow cut(String before, String after) {
		int i = 0;
		while (i < size()) {
			String row = this.get(i);
			int startIndex = before == null ? 0 : row.indexOf(before);
			if (startIndex >= 0) {
				startIndex = startIndex + (before == null ? 0 : before.length());
				int endIndex = after == null ? row.length() : row.indexOf(after);
				if (endIndex >= 0 && endIndex >= startIndex) {
					row = row.substring(startIndex, endIndex);
					this.set(i, row);
					i++;
					continue;
				}
			}
			this.remove(i);
		}
		return this;
	}

	/** Duplicate this object */
	public TextRow clone() {
		return new TextRow(this);
	}

	/**
	 * Filters rows that match the given expression
	 * 
	 * @param expr The expression to match against rows (can be plain text or regex)
	 * 
	 * @return A new TextRow containing only the matching rows
	 */
	public TextRow filter(String expr) {
		return filter(expr, false);
	}
	
	/** Alias of filter()
	 * 
	 * @param expr
	 * @return
	 */
	public TextRow grep(String expr) {
		return filter(expr);
	}

	/**
	 * Delete rows that match the given expression
	 * 
	 * @param expr The expression to match against rows (can be plain text or regex)
	 * @param
	 * @return A new TextRow containing only the matching rows
	 */
	public TextRow delete(String expr) {
		return filter(expr, true);
	}

	/** delete empty rows */
	public TextRow deleteEmpty() {
		return delete(null);
	}

	/**
	 * Delete rows before the row that match the given expression
	 */
	public TextRow deleteBefore(String expr, boolean include) {
		TextRow result = new TextRow();
		boolean start = false;
		for (int i = 0; i < this.size(); i++) {
			String line = this.get(i);
			if (start) {
				result.add(line);
			} else {
				if (TextUtil.match(line, expr)) {
					if (include)
						result.add(line);
				}
			}
		}
		return result;
	}

	/**
	 * Delete rows after the row that match the given expression
	 */
	public TextRow deleteAfter(String expr, boolean include) {
		TextRow result = new TextRow();
		boolean start = false;
		for (int i = 0; i < this.size(); i++) {
			String line = this.get(i);
			if (!start) {
				if (TextUtil.match(line, expr)) {
					if (include)
						result.add(line);
				} else {
					result.add(line);
				}
			}
		}
		return result;
	}

	/**
	 * Filters items that match the given expression
	 * 
	 * @param expr     The expression to match against rows (can be plain text or
	 *                 regex)
	 * @param isDelete Indicates whether delete the row when the expression is match
	 * @return A new TextRow containing only the matching rows
	 */
	protected TextRow filter(String expr, boolean isDelete) {
		TextRow result = new TextRow();
		for (int i = 0; i < this.size(); i++) {
			String line = this.get(i);
			if (TextUtil.match(line, expr) == (!isDelete)) {
				result.add(line);
			}
		}
		return result;
	}

	/** merge multiple rows into a row */
	public TextRow merge(String startLineExpr, String endLineExpr) {
		TextRow result = new TextRow();

		int startRow = -1;
		String curLine = "";

		int i = 0;
		while (i < this.size()) {
			String line = this.get(i);
			if (startRow < 0) {
				// when not start
				if (TextUtil.match(line, startLineExpr)) {
					startRow = i;
					curLine = line;
				}
			} else {
				// when started
				if (TextUtil.match(line, startLineExpr)) {
					// when start line is met again
					result.add(curLine);
					startRow = i;
					curLine = line;
				} else if (endLineExpr != null && TextUtil.match(line, endLineExpr)) {
					// when end line is met
					startRow = -1;
					result.add(curLine);
					curLine = "";
				} else {
					// other lines
					curLine += "```" + line;
				}
			}
			// next line
			i++;
		}

		return result;
	}

	/** set row value */
	public String set(int index, String value) {
		if (index < 0 && index > 4096)
			throw new ArrayIndexOutOfBoundsException();
		while (size() <= index) {
			this.add("");
		}
		super.set(index, value);
		return value;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String line : this)
			sb.append(line).append("\r\n");
		return sb.toString();
	}

	/** Create a TextTable by splitting each row in specified columns */
	public TextTable split(int... columnIndexes) {
		TextTable t = new TextTable();
		for (String row : this) {
			List<String> r = TextUtil.split(row, columnIndexes);
			t.add(new TextRow(r));
		}
		return t;
	}

	/** Create a TextTable by splitting each row in specified separator chars */
	public TextTable split(String separatorChars, int limit) {
		TextTable t = new TextTable();
		for (String row : this) {
			List<String> r = TextUtil.split(row, separatorChars, limit);
			t.add(new TextRow(r));
		}
		return t;
	}

	/** Create a TextTable by splitting each row in specified separator chars */
	public TextTable split(String separatorChars) {
		return split(separatorChars, 0);
	}

	/** Create a TextTable by extract words by regex in rows */
	public TextTable match(String regexExpr) {
		Pattern p = TextUtil.getPattern(regexExpr);
		return match(p);
	}

	/** Create a TextTable by extract words by regex in rows */
	public TextTable match(Pattern p) {
		return match(p, null, 0);
	}

	/** Create a TextTable by extract words by regex in rows */
	public TextTable match(Pattern firstPattern, Pattern nextPattern, int fixedColumns) {
		TextTable table = new TextTable();
		
		for (String line : this) {
			Matcher m = firstPattern.matcher(line);
			if (m.find()) {
				// create a new row, add each match words 
				TextRow row = new TextRow();
				for (int n = 1; n <= m.groupCount(); n++) {
					String word = m.group(n);
					row.add(word);
				}
				table.add(row);
										
				// find next pattern if needed		
				boolean hasNext = true;
				while (nextPattern != null && hasNext ) {
					line = line.substring(m.end());  // get remain string after end of matcher
					// find next pattern
					m = nextPattern.matcher(line);
					if (m.find()) {
						hasNext = true;						
						// create a new row
						TextRow nextRow = new TextRow();
						// add fixed columns from the previous row
						if (fixedColumns > 0) {
							for(int k=0; k<fixedColumns; k++) nextRow.add(row.get(k));
						}
						// add each match words
						for (int n = 1; n <= m.groupCount(); n++) {
							String word = m.group(n);
							nextRow.add(word);
						}
						table.add(nextRow);
					} else {
						hasNext = false;
					}
				}
				
			}
		}
		
		return table;
	}

	
	/** find words after afterWord and before beforeWord */
	public TextRow between(String afterWord, String beforeWord) {
		List<String> words = new ArrayList<String>();
		boolean findAfter = false;
		boolean findBefore = false;
		int i =0;
		while(i < this.size()) {
			String word = this.get(i++);
			if (!findAfter) {
				findAfter = TextUtil.match(word, afterWord);
			} else {
				findBefore = TextUtil.match(word, beforeWord);
				if (findBefore) {
					break;
				} else {
					words.add(word);
				}
			}
		}
		if (!findBefore) words.clear();
		return new TextRow(words);
	}
	
	public String value(int index) {
		String ret = index >= 0 && index < size() ? get(index) : null;
		return ret == null ? ret : ret.trim();
	}
}
