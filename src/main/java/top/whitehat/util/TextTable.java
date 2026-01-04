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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * TextTable is a specialized ArrayList implementation that contains a list of
 * TextRow objects, designed to represent tabular data from text sources. It
 * provides convenient methods for filtering, transforming, and manipulating
 * tabular text data with field-based operations. The class maintains field
 * names and offers SQL-like operations on text-based data tables, making it
 * suitable for parsing and processing structured text files or CSV-like data.
 * 
 * <h3>Usage Examples</h3>
 * 
 * <pre>
 * // Create a new TextTable from TextRow data
 * TextTable table = new TextTable();
 * table.add(new TextRow("Name,Age,City"));
 * table.add(new TextRow("John,30,NYC"));
 * table.add(new TextRow("Jane,25,LA"));
 * 
 * // Set field names for column access
 * table.setFieldNames("Name", "Age", "City");
 * 
 * // Filter rows based on conditions
 * TextTable filtered = table.where("Age > 25");
 * 
 * // Extract specific fields
 * TextTable nameAge = table.fields(0, 1);
 * </pre>
 */
public class TextTable extends ArrayList<TextRow> {
	private static final long serialVersionUID = 3544506857133696089L;

	/**
	 * List of names of the table fields, providing access to column names for
	 * field-based operations
	 */
	public Fields fields = new Fields();

	/**
	 * List of selected field names for field selection operations, used when
	 * filtering specific columns
	 */
	public List<String> selectFieldNames = new ArrayList<String>();

	/**
	 * Index of the row that is currently being edited during interactive edit
	 * operations
	 */
	public int editingIndex = -1;

	/**
	 * Default constructor for TextTable that creates an empty table instance
	 * without any rows or fields
	 */
	public TextTable() {
	}

	/**
	 * Gets the total number of records (rows) in this TextTable. This method
	 * returns the current size of the internal ArrayList, which represents the
	 * total number of TextRow objects in the table. This count includes all rows,
	 * whether they contain data or are empty, and remains accurate even after
	 * filtering operations that return new TextTable instances since the original
	 * table size is unchanged.
	 * 
	 * @return The number of TextRow objects currently in this TextTable
	 * 
	 *         <h3>Usage Example</h3>
	 * 
	 *         <pre>
	 *         TextTable table = new TextTable();
	 *         table.add(new TextRow("Row 1"));
	 *         table.add(new TextRow("Row 2"));
	 *         int count = table.getRecordCount(); // Returns 2
	 *         </pre>
	 */
	public int getRecordCount() {
		return this.size();
	}

	/**
	 * Checks whether the table is currently in an editing state by examining the
	 * editingIndex property. This method indicates if an interactive editing
	 * session has been started using the startEdit() method and not yet completed
	 * with the closeEdit() method. During editing, new rows can be added and
	 * modified using the editField() method. This is useful for building tables
	 * incrementally when processing data that needs to be accumulated before being
	 * added to the table.
	 * 
	 * @return true if editingIndex is greater than or equal to 0 (editing in
	 *         progress), false otherwise
	 * 
	 *         <h3>Usage Example</h3>
	 * 
	 *         <pre>
	 *         TextTable table = new TextTable();
	 *         System.out.println(table.isEditing()); // Returns false
	 *         table.startEdit();
	 *         System.out.println(table.isEditing()); // Returns true
	 *         table.closeEdit();
	 *         System.out.println(table.isEditing()); // Returns false
	 *         </pre>
	 */
	public boolean isEditing() {
		return editingIndex >= 0;
	}

	/**
	 * Appends a new empty record (TextRow) to the table and starts an editing
	 * session for it. This method adds a new TextRow instance to the end of the
	 * table and sets the editingIndex to point to this newly added row, allowing
	 * subsequent editField() calls to modify this specific row. This is part of an
	 * interactive editing mechanism that allows building table rows incrementally
	 * before finalizing the edit. The method returns this TextTable instance to
	 * support method chaining in fluent API style operations. After completing
	 * edits, closeEdit() should be called to finish the editing session.
	 * 
	 * @return This TextTable instance for method chaining
	 * 
	 *         <h3>Usage Example</h3>
	 * 
	 *         <pre>
	 *         TextTable table = new TextTable();
	 *         table.setFieldNames("Name", "Age");
	 *         table.startEdit().editField("Name", "John").editField("Age", "30").closeEdit();
	 *         // Table now contains one row: ["John", "30"]
	 *         </pre>
	 */
	public TextTable startEdit() {
		add(new TextRow());
		this.editingIndex = this.size() - 1;
		return this;
	}

	/**
	 * Finishes the current editing session by resetting the editing index to -1.
	 * This method terminates an interactive editing session that was started with
	 * startEdit(), effectively marking the end of modifications to the currently
	 * edited row. After calling this method, any subsequent editField() calls will
	 * start a new edit session with a new row. This method returns this TextTable
	 * instance to support method chaining in fluent API style operations, allowing
	 * it to be combined with other operations in a single statement sequence.
	 * 
	 * @return This TextTable instance for method chaining
	 * 
	 *         <h3>Usage Example</h3>
	 * 
	 *         <pre>
	 *         TextTable table = new TextTable();
	 *         table.setFieldNames("Name", "Age");
	 *         table.startEdit().editField("Name", "John").editField("Age", "30");
	 *         table.closeEdit(); // Ends the editing session
	 *         System.out.println(table.isEditing()); // Returns false
	 *         </pre>
	 */
	public TextTable closeEdit() {
		this.editingIndex = -1;
		return this;
	}

	/**
	 * Changes the value of a specific field in the currently edited record by field
	 * name and value. This method modifies the cell at the intersection of the
	 * currently edited row (determined by editingIndex) and the column
	 * corresponding to the specified field name. If no editing session is currently
	 * active (editingIndex < 0), this method will automatically start a new edit
	 * session by adding a new empty row and setting the editing index to the last
	 * row. The field name is used to look up the corresponding column index in the
	 * fields collection, and the value is set at that position in the current row.
	 * If the field name does not exist in the fields collection, a RuntimeException
	 * will be thrown.
	 * 
	 * @param fieldName The name of the field (column) to modify in the currently
	 *                  edited row
	 * @param value     The value to set in the specified field of the currently
	 *                  edited row
	 * @throws RuntimeException If the specified field name does not exist in the
	 *                          fields collection
	 * 
	 *                          <h3>Usage Example</h3>
	 * 
	 *                          <pre>
	 *                          TextTable table = new TextTable();
	 *                          table.setFieldNames("Name", "Age", "City");
	 *                          table.startEdit().editField("Name", "John").editField("Age", "30").editField("City", "New York").closeEdit();
	 *                          // Table now contains one row: ["John", "30", "New York"]
	 *                          </pre>
	 */
	public void editField(String fieldName, String value) {
		if (this.editingIndex < 0) {
			this.add(new TextRow());
			this.editingIndex = this.size() - 1;
		}

		int col = fields.indexOf(fieldName);
		if (col >= 0) {
			this.get(this.editingIndex).set(col, value);
		} else {
			throw new RuntimeException("field " + fieldName + " not found");
		}

	}

	/**
	 * Creates a copy of this TextTable's header information (field names and
	 * selected field names) without copying the actual data rows. This method
	 * creates a new TextTable instance containing only the structural information -
	 * the field names and selected field names - but no data rows. This is useful
	 * when you need to create a new table with the same structure as the current
	 * one but for different data, or when you want to preserve the schema while
	 * creating a new table. The returned TextTable will have the same field
	 * definitions but will be empty of any data rows, allowing you to add new data
	 * while maintaining the same column structure and selection criteria.
	 * 
	 * @return A new TextTable with the same header information but no data rows
	 * 
	 *         <h3>Usage Example</h3>
	 * 
	 *         <pre>
	 *         TextTable original = new TextTable();
	 *         original.setFieldNames("Name", "Age", "City");
	 *         original.select("Name, City"); // Sets selected fields
	 *         TextTable headerCopy = original.cloneHeader();
	 *         // headerCopy has same fields and selection as original but no data rows
	 *         </pre>
	 */
	public TextTable cloneHeader() {
		TextTable ret = new TextTable();
		ret.fields = new Fields(this.fields);
		ret.selectFieldNames = TextUtil.clone(selectFieldNames);
		return ret;
	}

	/**
	 * Creates a complete deep copy of this TextTable including all data rows and
	 * header information. This method creates a new TextTable instance that
	 * contains copies of all the data rows in the current table, preserving both
	 * the field names and all the row data. Each TextRow in the original table is
	 * cloned using the TextRow.clone() method, ensuring that modifications to the
	 * cloned table will not affect the original table. This is useful when you need
	 * to perform operations on a table while preserving the original data, or when
	 * you need to split data processing into separate threads or operations. The
	 * method returns a completely independent copy of the table with the same
	 * structure and data as the original.
	 * 
	 * @return A new TextTable with the same data as this one
	 * 
	 *         <h3>Usage Example</h3>
	 * 
	 *         <pre>
	 *         TextTable original = new TextTable();
	 *         original.setFieldNames("Name", "Age");
	 *         original.add(new TextRow("John,30"));
	 *         original.add(new TextRow("Jane,25"));
	 *         TextTable cloned = original.clone();
	 *         // cloned is an independent copy with same data
	 *         cloned.get(0).set(0, "Bob"); // Does not affect original
	 *         </pre>
	 */
	public TextTable clone() {
		TextTable ret = new TextTable();
		ret.fields = new Fields(this.fields);
		for (TextRow row : this) {
			ret.add(row.clone());
		}
		return ret;
	}

	/**
	 * Filters rows that match the given expression by searching for the expression
	 * in all cells of each row. This method creates a new TextTable containing only
	 * the rows where at least one cell contains the specified expression. The
	 * expression can be either plain text or a regular expression, depending on the
	 * implementation of TextUtil.match(). The search is performed across all
	 * columns in each row, and any row containing a match will be included in the
	 * result. This method does not modify the original table but returns a new
	 * instance with the filtered data, preserving the original table structure and
	 * field definitions. The method is useful for extracting specific records from
	 * large datasets based on content criteria.
	 * 
	 * @param expr The expression to match against cells in the rows (can be plain
	 *             text or regex)
	 * @return A new TextTable containing only the rows that match the expression
	 * 
	 *         <h3>Usage Example</h3>
	 * 
	 *         <pre>
	 *         TextTable table = new TextTable();
	 *         table.setFieldNames("Name", "Age", "City");
	 *         table.add(new TextRow("John,30,New York"));
	 *         table.add(new TextRow("Jane,25,LA"));
	 *         table.add(new TextRow("Bob,35,New York"));
	 *         TextTable filtered = table.filter("New York");
	 *         // Returns a new table with only the rows containing "New York"
	 *         </pre>
	 */
	public TextTable filter(String expr) {
		return filterRow(false, expr);
	}

	/**
	 * Removes rows that match the given expression by excluding them from the
	 * result table. This method creates a new TextTable containing only the rows
	 * that do NOT match the specified expression. The expression can be either
	 * plain text or a regular expression, depending on the implementation of
	 * TextUtil.match(). The search is performed across all columns in each row, and
	 * any row containing a match will be excluded from the result. This method does
	 * not modify the original table but returns a new instance with the filtered
	 * data, preserving the original table structure and field definitions. This is
	 * the inverse operation of the filter() method and is useful for removing
	 * unwanted records from datasets based on content criteria.
	 * 
	 * @param expr The expression to match against cells in the rows for exclusion
	 *             (can be plain text or regex)
	 * @return A new TextTable containing only the rows that don't match the
	 *         expression
	 * 
	 *         <h3>Usage Example</h3>
	 * 
	 *         <pre>
	 *         TextTable table = new TextTable();
	 *         table.setFieldNames("Name", "Age", "City");
	 *         table.add(new TextRow("John,30,New York"));
	 *         table.add(new TextRow("Jane,25,LA"));
	 *         table.add(new TextRow("Bob,35,Chicago"));
	 *         TextTable filtered = table.delete("New York");
	 *         // Returns a new table with rows that don't contain "New York"
	 *         </pre>
	 */
	public TextTable delete(String expr) {
		return filterRow(true, expr);
	}

	/**
	 * Internal method to filter rows based on an expression
	 * 
	 * @param isDelete If true, removes rows that match; if false, keeps rows that
	 *                 match
	 * @param expr     The expression to match against cells in the rows (can be
	 *                 plain text or regex)
	 * @return A new TextTable with the filtered rows
	 */
	protected TextTable filterRow(boolean isDelete, String expr) {
		TextTable result = new TextTable();

		for (int r = 0; r < this.size(); r++) {
			TextRow row = this.get(r);
			boolean match = false;

			for (int i = 0; i < row.size(); i++) {
				String cell = row.get(i);
				if (TextUtil.match(cell, expr)) {
					match = true;
					break;
				}
			}

			if (match) {
				if (!isDelete)
					result.add(row);
			} else {
				if (isDelete)
					result.add(row);
			}
		}

		return result;
	}

	/**
	 * Selects specific fields (columns) by their indexes, creating a new TextTable
	 * with only the specified columns. This method creates a new TextTable that
	 * contains only the columns at the specified indexes while preserving all the
	 * rows. The field names corresponding to the selected columns are also
	 * preserved in the result table. This is useful for extracting a subset of
	 * columns from a table while maintaining the row structure and relationships
	 * between the selected data elements. The original table remains unchanged, and
	 * the returned table contains the same number of rows but only the selected
	 * columns. The order of the columns in the result follows the order of the
	 * indexes provided in the parameter list.
	 * 
	 * @param fieldIndexes The indexes of the fields (columns) to select
	 * @return A new TextTable containing only the specified fields (columns)
	 * 
	 *         <h3>Usage Example</h3>
	 * 
	 *         <pre>
	 *         TextTable table = new TextTable();
	 *         table.setFieldNames("Name", "Age", "City", "Country");
	 *         table.add(new TextRow("John,30,New York,USA"));
	 *         table.add(new TextRow("Jane,25,LA,USA"));
	 *         TextTable selected = table.fields(0, 2); // Select Name and City columns only
	 *         // Result contains only Name and City columns: [["John", "New York"], ["Jane", "LA"]]
	 *         </pre>
	 */
	public TextTable fields(int... fieldIndexes) {
		return filterFields(false, fieldIndexes);
	}

	/**
	 * Removes specific fields (columns) by their indexes, creating a new TextTable
	 * without the specified columns. This method creates a new TextTable that
	 * excludes the columns at the specified indexes while preserving all other
	 * columns and all rows. The field names corresponding to the non-deleted
	 * columns are also preserved in the result table. This is useful for removing
	 * unnecessary columns from a table while maintaining the data in other columns.
	 * The original table remains unchanged, and the returned table contains the
	 * same number of rows but with the specified columns removed. This operation is
	 * the inverse of the fields() method which selects only specified columns
	 * instead of removing them.
	 * 
	 * @param fieldIndexes The indexes of the fields (columns) to remove
	 * @return A new TextTable with the specified fields (columns) removed
	 * 
	 *         <h3>Usage Example</h3>
	 * 
	 *         <pre>
	 *         TextTable table = new TextTable();
	 *         table.setFieldNames("Name", "Age", "City", "Country");
	 *         table.add(new TextRow("John,30,New York,USA"));
	 *         table.add(new TextRow("Jane,25,LA,USA"));
	 *         TextTable withoutAge = table.deleteFields(1); // Remove Age column (index 1)
	 *         // Result contains Name, City, and Country columns: [["John", "New York", "USA"], ["Jane", "LA", "USA"]]
	 *         </pre>
	 */
	public TextTable deleteFields(int... fieldIndexes) {
		return filterFields(true, fieldIndexes);
	}

	/**
	 * Internal method to filter fields based on their indexes
	 * 
	 * @param isDelete If true, removes the specified fields; if false, keeps only
	 *                 the specified fields
	 * @param fields   The indexes of the fields to filter
	 * @return A new TextTable with the filtered fields
	 */
	protected TextTable filterFields(boolean isDelete, int fieldIndexes[]) {
		TextTable result = new TextTable();

		HashMap<Integer, Boolean> colMap = new HashMap<Integer, Boolean>();
		for (int i = 0; i < fieldIndexes.length; i++) {
			colMap.put(fieldIndexes[i], true);
		}

		// filter field name
		int col = 0;
		for (String name : fields) {
			if (colMap.containsKey(col++) == !isDelete) {
				result.fields.add(name);
			}
		}

		// filter row
		for (List<String> oldRow : this) {
			TextRow row = new TextRow();
			col = 0;
			for (String cell : oldRow) {
				if (colMap.containsKey(col) == !isDelete) {
					row.add(cell);
				}
				col++;
			}
			result.add(row);
		}

		return result;
	}

	/**
	 * Converts the TextTable to a List of Map objects, where each Map represents a
	 * row with field names as keys. This method transforms the tabular data into a
	 * more accessible format where each row becomes a Map<String, Object> with
	 * field names as keys and corresponding cell values as values. If no field
	 * names are specified in the parameter list, all available fields are used. If
	 * specific field names are provided, only those columns will be included in the
	 * resulting Maps. The values in the Maps are parsed using
	 * TextUtil.parseValue(), which may convert numeric strings to actual numeric
	 * types. This is particularly useful for converting tabular text data to a
	 * format that can be easily processed by other parts of an application or
	 * serialized to JSON or other formats.
	 * 
	 * @param fieldNames The specific field names to include in the resulting Maps;
	 *                   if no names are provided, all fields are included
	 * @return A List of Map objects, where each Map represents a row with field
	 *         names as keys and cell values as values
	 * @throws IllegalArgumentException If any of the specified field names don't
	 *                                  exist in the table
	 * 
	 *                                  <h3>Usage Example</h3>
	 * 
	 *                                  <pre>
	 *                                  TextTable table = new TextTable();
	 *                                  table.setFieldNames("Name", "Age", "City");
	 *                                  table.add(new TextRow("John,30,New York"));
	 *                                  table.add(new TextRow("Jane,25,LA"));
	 *                                  List<Map<String, Object>> result = table.toList("Name", "Age");
	 *                                  // Returns [{"Name": "John", "Age": 30}, {"Name": "Jane", "Age": 25}]
	 *                                  </pre>
	 */
	public List<Map<String, Object>> toList(String... fieldNames) {
		if (fieldNames.length == 0) {
			if (fields == null || fields.size() == 0) {
				fieldNames = null;
			} else {
				fieldNames = fields.toArray();
			}
		}

		int[] columns;

		if (fieldNames != null) {
			// convert fieldNames to column array
			columns = new int[fieldNames.length];
			for (int i = 0; i < fieldNames.length; i++) {
				int col = fields.indexOf(fieldNames[i]);
				if (col < 0)
					throw new IllegalArgumentException("field \"" + fieldNames[i] + "\" not found");
				columns[i] = col;
			}
		} else {
			// column array include all columns
			columns = new int[this.columns()];
			fieldNames = new String[this.columns()];
			for (int i = 0; i < fieldNames.length; i++) {
				columns[i] = i;
				fieldNames[i] = "" + i;
			}
		}

		List<Map<String, Object>> ret = new ArrayList<>();
		for (TextRow r : this) {
			Map<String, Object> obj = new HashMap<>();
			for (int i = 0; i < columns.length; i++) {
				int col = columns[i];
				if (col >= 0 && col < r.size()) {
					String key = fieldNames[i];
					String value = r.get(col);
					obj.put(key, TextUtil.parseValue(value));
				}
			}
			ret.add(obj);
		}
		return ret;
	}

	/**
	 * Converts the TextTable to a Map where one field serves as keys and another
	 * field serves as values. This method creates a Map<String, Object> by using
	 * values from the specified key field as Map keys and values from the specified
	 * value field as Map values. Each row in the table contributes one entry to the
	 * resulting Map. This is particularly useful for creating lookup tables or
	 * key-value mappings from tabular data, such as converting a table of
	 * configuration parameters or identifier-value pairs into a more accessible Map
	 * format. The values in the Map are stored as trimmed strings from the original
	 * table cells. If there are duplicate keys in the key field, later entries will
	 * overwrite earlier ones in the resulting Map, following standard Map behavior.
	 * 
	 * @param keyFieldName   The field name to use as keys in the resulting Map
	 * @param valueFieldName The field name to use as values in the resulting Map
	 * @return A Map object with keys from the keyFieldName column and values from
	 *         the valueFieldName column
	 * @throws IllegalArgumentException If either of the specified field names
	 *                                  doesn't exist in the table
	 * 
	 *                                  <h3>Usage Example</h3>
	 * 
	 *                                  <pre>
	 *                                  TextTable table = new TextTable();
	 *                                  table.setFieldNames("ID", "Name", "Age");
	 *                                  table.add(new TextRow("1,John,30"));
	 *                                  table.add(new TextRow("2,Jane,25"));
	 *                                  Map<String, Object> result = table.toMap("ID", "Name");
	 *                                  // Returns {"1": "John", "2": "Jane"}
	 *                                  </pre>
	 */
	public Map<String, Object> toMap(String keyFieldName, String valueFieldName) {
		int colKey = this.fields.indexOf(keyFieldName);
		if (colKey < 0)
			throw new IllegalArgumentException("field \"" + keyFieldName + "\" not found");

		int colValue = this.fields.indexOf(valueFieldName);
		if (colValue < 0)
			throw new IllegalArgumentException("field \"" + valueFieldName + "\" not found");

		Map<String, Object> ret = new HashMap<String, Object>();
		for (TextRow r : this) {
			ret.put(r.get(colKey).trim(), r.get(colValue).trim());
		}

		return ret;
	}

	/**
	 * Returns the number of fields (columns) in this TextTable by finding the
	 * maximum column count across all rows. This method iterates through all rows
	 * in the table and determines the maximum number of columns present in any
	 * single row. This is useful for understanding the table structure,
	 * particularly when rows might have varying numbers of columns. The result
	 * represents the width of the table if it were visualized as a grid, indicating
	 * how many columns are needed to accommodate the row with the most fields. This
	 * information is often used internally by other methods such as toString() for
	 * formatting and column-based operations that need to know the table width.
	 * 
	 * @return The maximum number of columns found in any row of the table
	 * 
	 *         <h3>Usage Example</h3>
	 * 
	 *         <pre>
	 *         TextTable table = new TextTable();
	 *         table.add(new TextRow("A,B,C")); // 3 columns
	 *         table.add(new TextRow("X,Y")); // 2 columns
	 *         table.add(new TextRow("1,2,3,4")); // 4 columns
	 *         int colCount = table.columns(); // Returns 4
	 *         </pre>
	 */
	public int columns() {
		int cols = 0;
		for (int r = 0; r < getRecordCount(); r++) {
			TextRow row = get(r);
			if (row.size() > cols)
				cols = row.size();
		}
		return cols;
	}

	/**
	 * Converts the TextTable to a string representation with rows separated by
	 * newlines and cells separated by tabs. This method creates a formatted string
	 * representation of the table data, with field names (if defined) appearing as
	 * the first row. The method calculates the appropriate width for each column
	 * based on the content, ensuring proper alignment of tabular data. Each cell is
	 * padded to match the width of the largest value in that column for better
	 * visual presentation. This string representation is useful for displaying the
	 * table content in console applications, saving to text files, or any situation
	 * where a textual view of the tabular data is needed. The string format is
	 * human-readable and maintains the tabular structure of the original data.
	 * 
	 * @return A string representation of the table data with field names as the
	 *         first row (if defined) and each subsequent row containing the data
	 *         values, with cells separated by tabs and rows by newlines
	 * 
	 *         <h3>Usage Example</h3>
	 * 
	 *         <pre>
	 *         TextTable table = new TextTable();
	 *         table.setFieldNames("Name", "Age");
	 *         table.add(new TextRow("John,30"));
	 *         table.add(new TextRow("Jane,25"));
	 *         String str = table.toString();
	 *         // Returns a string like:
	 *         // "Name    Age
	 *         //  John    30
	 *         //  Jane    25"
	 *         </pre>
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String CRLF = "\n";
		String delim = "\t";

		// calculation each column's width
		int cols = columns();
		int[] colWidths = new int[cols];
		for (int i = 0; i < cols; i++)
			colWidths[i] = 0;
		for (int r = 0; r < getRecordCount(); r++) {
			TextRow row = get(r);
			for (int c = 0; c < row.size(); c++) {
				String val = row.get(c);
				int w = val == null ? 0 : val.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
				if (w > colWidths[c])
					colWidths[c] = w;
			}
		}

		// append field names
		if (fields.size() > 0) {
			int count = 0;
			for (String name : fields) {
				if (count++ > 0)
					sb.append(delim);
				sb.append(TextUtil.toLength(name, colWidths[count - 1]));
			}
			sb.append(CRLF);
		}

		// append each row
		for (TextRow row : this) {
			int count = 0;
			for (String cell : row) {
				if (count++ > 0)
					sb.append(delim);
				String ss = TextUtil.toLength(cell, colWidths[count - 1]);
				sb.append(ss);
			}
			sb.append(CRLF);
		}

		return sb.toString();
	}

	/**
	 * Gets the value of a specific cell identified by field name and row index.
	 * This method provides convenient access to table data by allowing retrieval of
	 * a cell value using the field name (column identifier) and row index instead
	 * of requiring numeric column indices. The method first looks up the column
	 * index by the field name using the internal fields collection, then accesses
	 * the cell at the specified row and the determined column. This is particularly
	 * useful when you want to access specific data points in a table where you know
	 * the column name and row position, making the code more readable and less
	 * dependent on column ordering. The method returns the raw string value from
	 * the cell without any parsing or type conversion.
	 * 
	 * @param fieldName The name of the field (column) to retrieve the value from
	 * @param row       The index of the row containing the value to retrieve
	 * @return The string value of the cell at the specified field and row
	 * @throws IndexOutOfBoundsException If the row index is out of bounds
	 * @throws RuntimeException          If the specified field name is not found in
	 *                                   the fields collection
	 * 
	 *                                   <h3>Usage Example</h3>
	 * 
	 *                                   <pre>
	 *                                   TextTable table = new TextTable();
	 *                                   table.setFieldNames("Name", "Age", "City");
	 *                                   table.add(new TextRow("John,30,New York"));
	 *                                   table.add(new TextRow("Jane,25,LA"));
	 *                                   String name = table.get("Name", 0); // Returns "John"
	 *                                   String city = table.get("City", 1); // Returns "LA"
	 *                                   </pre>
	 */
	public String get(String fieldName, int row) {
		int col = fields.indexOf(fieldName);
		return get(row).get(col);
	}

	/**
	 * Sets the field names to be selected for subsequent operations by parsing a
	 * string of field names. This method takes a string containing field names
	 * separated by spaces, tabs, or commas and stores them in the selectFieldNames
	 * property. These selected field names are used by the selectColumns() method
	 * to determine which columns to include in the output. This provides a way to
	 * define which columns should be included in operations that respect the
	 * selection criteria. The method returns this TextTable instance to support
	 * method chaining in fluent API style operations, allowing it to be combined
	 * with other operations in a single statement sequence. This is particularly
	 * useful when you want to extract specific columns from a table based on their
	 * names rather than their positions.
	 * 
	 * @param fields A string containing field names separated by spaces, tabs, or
	 *               commas
	 * @return This TextTable for method chaining
	 * 
	 *         <h3>Usage Example</h3>
	 * 
	 *         <pre>
	 *         TextTable table = new TextTable();
	 *         table.setFieldNames("Name", "Age", "City", "Country");
	 *         table.add(new TextRow("John,30,New York,USA"));
	 *         table.select("Name City"); // Select Name and City fields for later operations
	 *         TextTable result = table.selectColumns(); // Gets only the selected columns
	 *         </pre>
	 */
	public TextTable select(String fields) {
		selectFieldNames = TextUtil.split(fields, " \t,");
		return this;
	}

	/**
	 * Selects only the columns specified by the selectFieldNames property, creating
	 * a new TextTable with the selected columns only. If no fields have been
	 * selected using the select() method, this method returns this table unchanged,
	 * preserving all columns. This method works in conjunction with the select()
	 * method to provide a two-step process for column selection: first specifying
	 * which columns to select using select(), then applying the selection with
	 * selectColumns(). The method looks up the column indexes corresponding to the
	 * selected field names and creates a new TextTable containing only those
	 * columns while preserving all rows. This is particularly useful for extracting
	 * specific named columns from a table when the exact column positions may vary
	 * or when working with tables that have many columns but only a subset is
	 * needed for further processing.
	 * 
	 * @return A new TextTable containing only the selected columns, or this table
	 *         if no fields are selected
	 * 
	 *         <h3>Usage Example</h3>
	 * 
	 *         <pre>
	 *         TextTable table = new TextTable();
	 *         table.setFieldNames("Name", "Age", "City", "Country");
	 *         table.add(new TextRow("John,30,New York,USA"));
	 *         table.add(new TextRow("Jane,25,LA,Canada"));
	 *         TextTable result = table.select("Name,City").selectColumns();
	 *         // Returns a new table with only Name and City columns: [["John", "New York"], ["Jane", "LA"]]
	 *         </pre>
	 */
	public TextTable selectColumns() {
		if (selectFieldNames.size() == 0)
			return this;

		int[] cols = new int[selectFieldNames.size()];
		for (int i = 0; i < selectFieldNames.size(); i++) {
			int col = fields.indexOf(selectFieldNames.get(i));
			cols[i] = col;
		}
		selectFieldNames.clear();

		return filterFields(false, cols);
	}

	/**
	 * Filters rows based on a condition applied to a specific field
	 * 
	 * @param fieldName The name of the field to apply the condition to
	 * @param op        The comparison operator (=, ==, >, <, >=, <=)
	 * @param value     The value to compare against
	 * @return A new TextTable containing only the rows that match the condition
	 */
	protected TextTable _where(String fieldName, String op, String value) {
		TextTable ret = this.cloneHeader();

		int col = fields.indexOf(fieldName);
		for (TextRow r : this) {
			if (r.size() <= col)
				continue;

			if (TextUtil.compare(r.get(col), op, value)) {
				ret.add(r);
			}
		}

		return ret.selectColumns();
	}

	private static String findOp(String s, String op) {
		int pos = s.indexOf(op);
		if (pos > 0)
			return op;
		return null;
	}

	private static String findOperator(String s) {
		@SuppressWarnings("unused")
		String op;
		if ((op = findOp(s, "==")) != null)
			return "==";
		if ((op = findOp(s, "!=")) != null)
			return "!=";
		if ((op = findOp(s, ">=")) != null)
			return ">=";
		if ((op = findOp(s, "<=")) != null)
			return "<=";
		if ((op = findOp(s, "<>")) != null)
			return "<>";
		if ((op = findOp(s, ">")) != null)
			return ">";
		if ((op = findOp(s, "<")) != null)
			return "<";
		if ((op = findOp(s, "=")) != null)
			return "=";
		return null;
	}

	/**
	 * Filters rows based on a condition applied to a specific field using SQL-like
	 * syntax. This method allows for complex row filtering using a condition string
	 * that follows the format "field operator value" where the operator can be any
	 * of: =, ==, >, <, >=, <=, !=, <>. The method parses the condition string to
	 * extract the field name, operator, and value, then evaluates each row to
	 * determine if it meets the specified condition. Only rows where the value in
	 * the specified field meets the condition are included in the returned
	 * TextTable. This provides SQL-like query functionality for filtering tabular
	 * text data. The method supports numeric comparisons where appropriate, and the
	 * TextUtil.compare() method handles the actual comparison logic. If the
	 * condition syntax is invalid, an IllegalArgumentException is thrown. This
	 * operation does not modify the original table but returns a new filtered
	 * instance.
	 * 
	 * @param condition The condition string in the format "field operator value",
	 *                  e.g. "Age >= 18" or "Name = John"
	 * @return A new TextTable containing only the rows that match the condition
	 * @throws IllegalArgumentException If the condition string has invalid syntax
	 *                                  or references non-existent fields
	 * 
	 *                                  <h3>Usage Example</h3>
	 * 
	 *                                  <pre>
	 *                                  TextTable table = new TextTable();
	 *                                  table.setFieldNames("Name", "Age", "City");
	 *                                  table.add(new TextRow("John,30,New York"));
	 *                                  table.add(new TextRow("Jane,25,LA"));
	 *                                  table.add(new TextRow("Bob,35,Chicago"));
	 *                                  TextTable result = table.where("Age > 28"); // Returns rows where Age is greater than 28
	 *                                  // Result contains: [["John", "30", "New York"], ["Bob", "35", "Chicago"]]
	 *                                  </pre>
	 */
	public TextTable where(String condition) {
		String op = findOperator(condition);
		if (op == null)
			throw new IllegalArgumentException("condition syntax error");

		int pos = condition.indexOf(op);
		String field = condition.substring(0, pos).trim();
		String value = condition.substring(pos + op.length()).trim();
		return _where(field, op, value);
	}

	/**
	 * Defines field names for the table by using the values from a specified row
	 * index as column headers. This method extracts the values from the row at the
	 * given index and uses them as field names for the table. This is particularly
	 * useful when processing text files where the first row contains column
	 * headers, allowing you to easily set up the field names based on the actual
	 * header row. The method retrieves the TextRow at the specified index and
	 * passes its values to the setFieldNames(String...) method to establish the
	 * field definitions. After calling this method, you can refer to columns by
	 * their names instead of numeric indices. This operation modifies the current
	 * table instance and returns it to support method chaining in fluent API style
	 * operations. Note that the row used for field names will remain in the table
	 * as data unless explicitly removed after calling this method.
	 * 
	 * @param index The index of the row to use as field names (headers)
	 * @return This TextTable for method chaining
	 * @throws IndexOutOfBoundsException If the index is out of bounds for the
	 *                                   current table
	 * 
	 *                                   <h3>Usage Example</h3>
	 * 
	 *                                   <pre>
	 *                                   TextTable table = new TextTable();
	 *                                   table.add(new TextRow("Name,Age,City")); // Row 0: headers
	 *                                   table.add(new TextRow("John,30,New York")); // Row 1: data
	 *                                   table.add(new TextRow("Jane,25,LA")); // Row 2: data
	 *                                   table.setFieldNames(0); // Use first row as field names
	 *                                   // Now fields are: ["Name", "Age", "City"]
	 *                                   </pre>
	 */
	public TextTable setFieldNames(int index) {
		TextRow row = get(index);
		return setFieldNames(row.toArray(new String[row.size()]));
	}

	/**
	 * Defines field names for the table by adding the specified names to the fields
	 * collection. This method accepts a variable number of string arguments
	 * representing the names of fields (columns) in the table. Setting field names
	 * enables the use of named column access throughout the TextTable API, allowing
	 * methods like get(String fieldName, int row) and where() conditions that
	 * reference fields by name rather than index. The order of the field names
	 * should correspond to the order of columns in your data. The method adds all
	 * provided names to the internal fields collection in the order they are given.
	 * This operation modifies the current table instance and returns it to support
	 * method chaining in fluent API style operations. Field names can contain any
	 * valid string content, but should be unique to avoid confusion when
	 * referencing columns by name.
	 * 
	 * @param names The names of the fields to add, in the order they correspond to
	 *              columns
	 * @return This TextTable for method chaining
	 * 
	 *         <h3>Usage Example</h3>
	 * 
	 *         <pre>
	 *         TextTable table = new TextTable();
	 *         table.setFieldNames("Name", "Age", "City"); // Set field names
	 *         table.add(new TextRow("John,30,New York"));
	 *         // Now can access by name: table.get("Name", 0) returns "John"
	 *         </pre>
	 */
	public TextTable setFieldNames(String... names) {
		for (String name : names) {
			fields.add(name);
		}
		return this;
	}
	
	/** Return row at specified index */
	public TextRow row(int index) {
		return this.get(index);
	}

	/**
	 * Gets the value of a cell specified by row and column indices, returning null
	 * if the cell doesn't exist. This method provides direct access to table data
	 * using numeric indices for both row and column positions. If either the row or
	 * column index is out of bounds (negative or beyond the table dimensions), the
	 * method safely returns null instead of throwing an exception. This safe
	 * behavior makes it convenient to access cells without having to check bounds
	 * first, though it requires null checks in the calling code. The method is
	 * particularly useful when working with dynamically sized tables or when
	 * implementing algorithms that may access cells near the boundaries of the
	 * table. The returned value is the raw string content from the specified cell
	 * without any parsing or type conversion.
	 * 
	 * @param row The row index of the cell to retrieve (0-based)
	 * @param col The column index of the cell to retrieve (0-based)
	 * @return The value of the specified cell as a string, or null if the cell
	 *         doesn't exist (indices out of bounds)
	 * 
	 *         <h3>Usage Example</h3>
	 * 
	 *         <pre>
	 *         TextTable table = new TextTable();
	 *         table.add(new TextRow("A,B,C"));
	 *         table.add(new TextRow("X,Y,Z"));
	 *         String value = table.cell(0, 1); // Returns "B" (row 0, column 1)
	 *         String missing = table.cell(5, 5); // Returns null (out of bounds)
	 *         </pre>
	 */
	public String cell(int row, int col) {
		if (row >= 0 && row < size()) {
			TextRow r = get(row);
			if (col >= 0 && col < r.size())
				return r.get(col);
		}
		return null;
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
		return get(row).get(fields.indexOf(fieldName));
	}

	public void toCSV(String filename) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
			for(TextRow row : this) {
				writer.write(row.csv());
				writer.newLine();
			}
		}
	}
	
	/** Fields of the table */
	public static class Fields extends TextRow {
		private static final long serialVersionUID = -661029933615125616L;

		public Fields() {
			super();
		}

		public Fields(TextRow f) {
			super(f);
		}

		/**
		 * Finds the index of a field by its name within the Fields collection,
		 * returning -1 if not found. This method searches through the Fields collection
		 * (which extends TextRow) to find the first occurrence of a field name that
		 * matches the specified string. The Fields collection stores the names of all
		 * fields (columns) in the parent TextTable in the order they were defined. This
		 * method is essential for name-based column access throughout the TextTable
		 * API, as it translates field names to the corresponding column indices used
		 * for data access. The method performs a linear search through the collection,
		 * comparing each field name using standard string equality. If multiple fields
		 * have the same name, only the index of the first occurrence is returned. If
		 * the specified field name is not found in the collection, the method returns
		 * -1, which is a common convention for "not found" in index lookup methods.
		 * 
		 * @param fieldName The name of the field to find in the Fields collection
		 * @return The index of the field in the collection if found, or -1 if the field
		 *         name does not exist
		 * 
		 *         <h3>Usage Example</h3>
		 * 
		 *         <pre>
		 *         TextTable table = new TextTable();
		 *         table.setFieldNames("Name", "Age", "City");
		 *         int nameIndex = table.fields.indexOf("Name"); // Returns 0
		 *         int ageIndex = table.fields.indexOf("Age"); // Returns 1
		 *         int missingIndex = table.fields.indexOf("Address"); // Returns -1 (not found)
		 *         </pre>
		 */
		public int indexOf(String fieldName) {
			return super.indexOf(fieldName);
		}

		/**
		 * Converts the Fields collection to a String array containing all field names.
		 * This method creates a new array and populates it with all the field names
		 * stored in this Fields collection, maintaining their original order.
		 * 
		 * @return A String array containing all field names in this collection
		 */
		public String[] toArray() {
			String[] names = new String[this.size()];
			int count = 0;
			for (String name : this) {
				names[count++] = name;
			}
			return names;
		}
	}

}
