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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * A text processor: convert string to table, and extract information in it.
 */
public class Text {

	/**
	 * create Text object from file
	 * 
	 * @throws IOException
	 */
	public static Text fromFile(String filename) throws IOException {
		return new Text(FileUtil.loadFromFile(filename));
	}

	/** create Text object from lines */
	public static Text of(String... lines) {
		if (lines.length == 1) {
			return new Text(lines[0]);
		} else {
			Text ret = new Text("");
			ret.rows = new TextRow(lines);
			return ret;
		}
	}

	/** create Text object from a List of line */
	public static Text of(List<String> items) {
		Text ret = new Text("");
		ret.rows = new TextRow(items);
		return ret;
	}

	/** Connect the objects to a string line */
	public static String line(Object... objs) {
		StringBuilder sb = new StringBuilder();
		for (Object obj : objs) {
			if (sb.length() == 0)
				sb.append(" ");
			sb.append(obj == null ? "null" : obj.toString());
		}
		return sb.toString();
	}

	// members of Text object

	public TextRow rows;

	public TextTable table;

	/** Constructor : create from specified string */
	public Text(String str) {
		rows = new TextRow(str);
	}

	/** extract word */
	public String extractWord(String after, String before) {
		String ret = "";
		for (String row : rows) {
			int beforeIndex = (before == null || before.length() == 0) ? row.length() : row.lastIndexOf(before);
			int afterIndex = (after == null || after.length() == 0) ? 0 : row.indexOf(after);
			if (beforeIndex >= 0 && afterIndex >= 0) {
				String word = row.substring(afterIndex + after.length(), beforeIndex);
				if (word.length() > 0) {
					if (ret.length() > 0)
						ret += "\r\n";
					ret += word;
				}
			}
		}
		return ret;
	}

	/** extract word */
	public String extractWord(String after) {
		return extractWord(after, null);
	}

	/** Colon and return a copy of this object */
	public Text colon() {
		Text ret = new Text("");
		ret.rows = ret.rows.clone();
		ret.table = ret.table.clone();
		return ret;
	}

	/**
	 * Filters rows that match the given expression
	 * 
	 * @param expr The expression to match against rows (can be plain text or regex)
	 * 
	 * @return A new TextRow containing only the matching rows
	 */
	public Text filter(String expr) {
		rows = rows.filter(expr, false);
		return this;
	}
	
	/** Delete empty rows */
	public Text deleteEmpty() {
		rows = rows.deleteEmpty();
		return this;
	}

	/**
	 * Filters rows that match the given expression
	 * 
	 * @param expr The expression to match against rows (can be plain text or regex)
	 * 
	 * @return A new TextRow containing only the matching rows
	 */
	public Text grep(String expr) {
		return filter(expr);
	}

	/**
	 * Delete rows that match a pattern
	 * 
	 * @param expr The expression to match against rows (can be plain text or regex)
	 * 
	 * @return A new TextRow containing only the matching rows
	 */
	public Text grepInvert(String expr) {
		return delete(expr);
	}

	/**
	 * Delete rows that match the given expression
	 * 
	 * @param expr The expression to match against rows (can be plain text or regex)
	 * @param
	 * @return A new TextRow containing only the matching rows
	 */
	public Text delete(String expr) {
		if (expr == null || expr.length() == 0)
			rows = rows.deleteEmpty();
		else
			rows = rows.filter(expr, true);
		return this;
	}

	/**
	 * Delete rows before the row that match the given expression
	 */
	public Text deleteBefore(String expr, boolean include) {
		rows = rows.deleteBefore(expr, true);
		return this;
	}

	/**
	 * Delete rows after the row that match the given expression
	 */
	public Text deleteAfter(String expr, boolean include) {
		rows = rows.deleteAfter(expr, true);
		return this;
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
	public Text cut(String before, String after) {
		rows = rows.cut(before, after);
		return this;
	}

	/** Merge multiple rows into a row */
	public Text merge(String startLineExpr, String endLineExpr) {
		rows = rows.merge(startLineExpr, endLineExpr);
		return this;
	}

	/** Create a TextTable by extract words by regex in rows */
	public Text match(String regexExpr) {
		table = rows.match(regexExpr);
		return this;
	}
	
	/** Create a TextTable by extract words by regex in rows */
	public Text match(Pattern firstPattern, Pattern nextPattern, int fixedColumns) {
		table = rows.match(firstPattern, nextPattern, fixedColumns);
		return this;
	}

	/** Create a TextTable by splitting each row in specified columns */
	public Text split(int... columnIndexes) {
		table = rows.split(columnIndexes);
		return this;
	}

	/** Create a TextTable by splitting each row in specified separator chars */
	public Text split(String separatorChars, int limit) {
		table = rows.split(separatorChars, limit);
		return this;
	}

	/** Create a TextTable by splitting each row in specified separator chars */
	public Text split(String separatorChars) {
		table = rows.split(separatorChars);
		return this;
	}

	/** get count of rows(records) */
	public int rows() {
		return table == null ? (rows == null ? 0 : rows.size()) : table.size();
	}

	/** get count of columns(fields) */
	public int columns() {
		return table == null ? (rows == null ? 0 : 1) : table.columns();
	}

	/** get field names */
	public String[] fieldNames() {
		if (table == null || table.fields == null)
			return new String[0];

		return table.fields.toArray();
	}

	/** set field names by the row at specified index */
	public Text fieldNames(int index) {
		table = table.setFieldNames(index);
		return this;
	}

	/**
	 * set field names to the table
	 * 
	 * @param names The names of the fields to add
	 * @return This TextTable for method chaining
	 */
	public Text fieldNames(String... names) {
		table = table.setFieldNames(names);
		return this;
	}

	/**
	 * Converts the TextTable to a List array, with each row becoming a Map object
	 * 
	 * @param fieldNames The field names needed in the JSON objects
	 * 
	 * @return A List of Map<String, Object>
	 */
	public List<Map<String, Object>> toList(String... fieldNames) {
		return table.toList(fieldNames);
	}

	/**
	 * Converts the TextTable to a Map
	 * 
	 * @param keyFieldName   The field name to use as keys
	 * @param valueFieldName The field name to use as values
	 * 
	 * @return A Map
	 */
	public Map<String, Object> toMap(String keyFieldName, String valueFieldName) {
		return table.toMap(keyFieldName, valueFieldName);
	}

	/**
	 * 
	 * select rows based on a condition applied to a specific field
	 * 
	 * The condition syntax is like "field >= value" or "field = value"
	 * 
	 * 
	 * 
	 * @param condition The condition string in the format "field operator value"
	 * 
	 * @return A new TextTable containing only the rows that match the condition
	 * 
	 */
	public Text where(String condition) {
		table = table.where(condition);
		return this;
	}

	/**
	 * Return specified row object
	 * 
	 * @param row The row index
	 * @return
	 */
	public TextRow row(int row) {
		return table.get(row);
	}

	/**
	 * Gets the value of a cell specified by row and column indices
	 * 
	 * @param row The row index
	 * @param col The column index
	 * @return The value of the specified cell. return null if the cell not exists.
	 */
	public String cell(int row, int col) {
		return table.cell(row, col);
	}
	
	/**
	 * Gets the value of a cell specified by field name and row index, using the
	 * field name to determine the column index. This method provides convenient
	 * access to table data by allowing retrieval of a cell value using the field
	 * name (column identifier) and row index instead of requiring numeric column
	 * indices. The method first looks up the column index by the field name using
	 * the internal fields collection, then accesses the cell at the specified row
	 * and the determined column. If the field name doesn't exist in the fields
	 * collection, the indexOf() method will return -1, which when used as an index
	 * will cause the TextRow.get() method to throw an exception. This approach
	 * makes the code more readable and less dependent on column ordering, as you
	 * can refer to columns by their semantic names rather than numeric positions.
	 * The returned value is the raw string content from the specified cell without
	 * any parsing or type conversion.
	 * 
	 * @param fieldName The name of the field (column) to retrieve the value from
	 * @param row       The row index of the cell to retrieve (0-based)
	 * @return The value of the specified cell as a string
	 * @throws RuntimeException or IndexOutOfBoundsException if the field name
	 *                          doesn't exist or if the row index is out of bounds
	 * 
	 *                          <h3>Usage Example</h3>
	 * 
	 *                          <pre>
	 *                          TextTable table = new TextTable();
	 *                          table.setFieldNames("Name", "Age", "City");
	 *                          table.add(new TextRow("John,30,New York"));
	 *                          String name = table.cell("Name", 0); // Returns "John" (row 0, Name column)
	 *                          String age = table.cell("Age", 0); // Returns "30" (row 0, Age column)
	 *                          </pre>
	 */
	public String cell(String fieldName, int row) {
		return table.cell(fieldName, row);
	}

	public String toString() {
		if (table == null) {
			return rows == null ? null : rows.toString();
		} else {
			return table.toString();
		}
	}

	public Text select(String str) {
		table.select(str);
		return this;
	}

	public String value() {
		return cell(0, 0);
	}
	
	public Text setFieldNames(int index) {
		table.setFieldNames(index);
		return this;
	}
	
	public Text setFieldNames(String... names) {
		table.setFieldNames(names);
		return this;
	}
}
