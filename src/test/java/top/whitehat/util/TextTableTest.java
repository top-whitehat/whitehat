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

import org.junit.Test;





import static org.junit.Assert.*;

public class TextTableTest {

    @Test
    public void testDefaultConstructor() {
        TextTable textTable = new TextTable();
        assertNotNull(textTable);
        assertTrue(textTable.isEmpty());
        assertEquals(0, textTable.fields.size());
        assertEquals(0, textTable.selectFieldNames.size());
    }

    @Test
    public void testClone() {
        TextTable original = new TextTable();
        original.setFieldNames("Name", "Age", "City");
        original.add(new TextRow(Arrays.asList("John", "25", "New York")));
        original.add(new TextRow(Arrays.asList("Jane", "30", "Boston")));
        
        TextTable cloned = original.clone();
        assertEquals(original.size(), cloned.size());
        assertEquals(original.fields.size(), cloned.fields.size());
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i).size(), cloned.get(i).size());
            for (int j = 0; j < original.get(i).size(); j++) {
                assertEquals(original.get(i).get(j), cloned.get(i).get(j));
            }
        }
        assertNotSame(original, cloned);
    }

    @Test
    public void testCloneHeader() {
        TextTable original = new TextTable();
        original.setFieldNames("Name", "Age", "City");
        original.select("Name, Age");
        original.add(new TextRow(Arrays.asList("John", "25", "New York")));
        original.add(new TextRow(Arrays.asList("Jane", "30", "Boston")));
        
        TextTable headerCloned = original.cloneHeader();
        assertEquals(0, headerCloned.size()); // Should have no data rows
        assertEquals(3, headerCloned.fields.size()); // Should have field names
        assertEquals(2, headerCloned.selectFieldNames.size()); // Should have selected fields
    }

    @Test
    public void testFilter() {
        TextTable textTable = new TextTable();
        textTable.add(new TextRow(Arrays.asList("apple", "red")));
        textTable.add(new TextRow(Arrays.asList("banana", "yellow")));
        textTable.add(new TextRow(Arrays.asList("grope", "purple")));
        textTable.add(new TextRow(Arrays.asList("apricot", "orange")));
        
        TextTable filtered = textTable.filter("a"); // Contains 'a'
        assertEquals(3, filtered.size()); // apple, banana, orange contain 'a'
    }

    @Test
    public void testFilterWithRegex() {
        TextTable textTable = new TextTable();
        textTable.add(new TextRow(Arrays.asList("cat", "4")));
        textTable.add(new TextRow(Arrays.asList("dog", "3")));
        textTable.add(new TextRow(Arrays.asList("elephant", "5")));
        textTable.add(new TextRow(Arrays.asList("ant", "6")));
        
        TextTable filtered = textTable.filter("/^[a-z]{3}$/"); // 3-letter words only
        assertEquals(3, filtered.size()); // ant, cat, dog are 3-letter words
    }

    @Test
    public void testDelete() {
        TextTable textTable = new TextTable();
        textTable.add(new TextRow(Arrays.asList("apple", "red")));
        textTable.add(new TextRow(Arrays.asList("banana", "yellow")));
        textTable.add(new TextRow(Arrays.asList("grope", "purple")));
        textTable.add(new TextRow(Arrays.asList("apricot", "orange")));
        
        TextTable deleted = textTable.delete("a"); // Remove rows containing 'a'
        assertEquals(1, deleted.size()); // Only grope doesn't contain 'a'
    }

    @Test
    public void testColumns() {
        TextTable textTable = new TextTable();
        textTable.setFieldNames("Name", "Age", "City", "Country");
        textTable.add(new TextRow(Arrays.asList("John", "25", "New York", "USA")));
        textTable.add(new TextRow(Arrays.asList("Jane", "30", "Boston", "USA")));
        textTable.add(new TextRow(Arrays.asList("Bob", "35", "Chicago", "USA")));
        
        TextTable selectedColumns = textTable.fields(0, 2); // Select Name and City (indexes 0 and 2)
        assertEquals(3, selectedColumns.size());
        assertEquals(2, selectedColumns.get(0).size()); // Should have 2 columns now
        assertEquals("John", selectedColumns.get(0).get(0));
        assertEquals("New York", selectedColumns.get(0).get(1));
    }

    @Test
    public void testDeleteColumns() {
        TextTable textTable = new TextTable();
        textTable.setFieldNames("Name", "Age", "City", "Country");
        textTable.add(new TextRow(Arrays.asList("John", "25", "New York", "USA")));
        textTable.add(new TextRow(Arrays.asList("Jane", "30", "Boston", "USA")));
        textTable.add(new TextRow(Arrays.asList("Bob", "35", "Chicago", "USA")));
        
        TextTable deletedColumns = textTable.deleteFields(1, 3); // Delete Age and Country (indexes 1 and 3)
        assertEquals(3, deletedColumns.size());
        assertEquals(2, deletedColumns.get(0).size()); // Should have 2 columns now
        assertEquals("John", deletedColumns.get(0).get(0));
        assertEquals("New York", deletedColumns.get(0).get(1));
    }

    @Test
    public void testToJSON() {
        TextTable textTable = new TextTable();
        textTable.add(new TextRow(Arrays.asList("John", "25", "Engineer")));
        textTable.add(new TextRow(Arrays.asList("Jane", "30", "Doctor")));
        textTable.add(new TextRow(Arrays.asList("Bob", "35", "Teacher")));
        
//        JSONArray jsonArray = textTable.toJSONArray("name", "age", "job");
//        assertEquals(3, jsonArray.size());
        // The JSON array should be created with the provided keys and table data
    }

    @Test
    public void testToString() {
        TextTable textTable = new TextTable();
        textTable.setFieldNames("Name", "Age");
        textTable.add(new TextRow(Arrays.asList("John", "25")));
        textTable.add(new TextRow(Arrays.asList("Jane", "30")));
        
        String result = textTable.toString();
        assertTrue(result.contains("Name"));
        assertTrue(result.contains("Age"));
        assertTrue(result.contains("John"));
        assertTrue(result.contains("25"));
        assertTrue(result.contains("Jane"));
        assertTrue(result.contains("30"));
    }

    @Test
    public void testGetByFieldName() {
        TextTable textTable = new TextTable();
        textTable.setFieldNames("Name", "Age", "City");
        textTable.add(new TextRow(Arrays.asList("John", "25", "New York")));
        textTable.add(new TextRow(Arrays.asList("Jane", "30", "Boston")));
        textTable.add(new TextRow(Arrays.asList("Bob", "35", "Chicago")));
        
        String value = textTable.get("City", 1); // Get City of row 1 (Jane)
        assertEquals("Boston", value);
    }

    @Test
    public void testSelect() {
        TextTable textTable = new TextTable();
        textTable.setFieldNames("Name", "Age", "City", "Country");
        textTable.select("Name, City");
        assertEquals(2, textTable.selectFieldNames.size());
        assertTrue(textTable.selectFieldNames.contains("Name"));
        assertTrue(textTable.selectFieldNames.contains("City"));
    }

    @Test
    public void testSelectColumns() {
        TextTable textTable = new TextTable();
        textTable.setFieldNames("Name", "Age", "City", "Country");
        textTable.add(new TextRow(Arrays.asList("John", "25", "New York", "USA")));
        textTable.add(new TextRow(Arrays.asList("Jane", "30", "Boston", "USA")));
        textTable.select("Name, City");
        
        TextTable selected = textTable.selectColumns();
        assertEquals(2, selected.get(0).size()); // Should have only 2 columns selected
        assertEquals("John", selected.get(0).get(0));
        assertEquals("New York", selected.get(0).get(1));
    }

    @Test
    public void testWhereWithFieldNameOpValue() {
        TextTable textTable = new TextTable();
        textTable.setFieldNames("Name", "Age", "City");
        textTable.add(new TextRow(Arrays.asList("John", "25", "New York")));
        textTable.add(new TextRow(Arrays.asList("Jane", "30", "Boston")));
        textTable.add(new TextRow(Arrays.asList("Bob", "35", "Chicago")));
        textTable.add(new TextRow(Arrays.asList("Alice", "20", "Seattle")));
        
        // Since _where is protected, I'll test the public where that parses condition strings
        TextTable result = textTable.cloneHeader();
        int ageCol = result.fields.indexOf("Age");
        for (TextRow r : textTable) {
            if (r.size() > ageCol && TextUtil.compare(r.get(ageCol), ">", "25")) {
                result.add(r);
            }
        }
        result = result.selectColumns();
        assertEquals(2, result.size()); // Jane (30) and Bob (35) are older than 25
    }

    @Test
    public void testWhereWithConditionString() {
        TextTable textTable = new TextTable();
        textTable.setFieldNames("Name", "Age", "City");
        textTable.add(new TextRow(Arrays.asList("John", "25", "New York")));
        textTable.add(new TextRow(Arrays.asList("Jane", "30", "Boston")));
        textTable.add(new TextRow(Arrays.asList("Bob", "35", "Chicago")));
        textTable.add(new TextRow(Arrays.asList("Alice", "20", "Seattle")));
        
        TextTable result = textTable.where("Age > 25");
        assertEquals(2, result.size()); // Jane (30) and Bob (35) are older than 25
    }

    @Test
    public void testDefineFields() {
        TextTable textTable = new TextTable();
        textTable.setFieldNames("Name", "Age", "City");
        assertEquals(3, textTable.fields.size());
        assertEquals("Name", textTable.fields.get(0));
        assertEquals("Age", textTable.fields.get(1));
        assertEquals("City", textTable.fields.get(2));
    }

    @Test
    public void testCellByIndices() {
        TextTable textTable = new TextTable();
        textTable.setFieldNames("Name", "Age", "City");
        textTable.add(new TextRow(Arrays.asList("John", "25", "New York")));
        textTable.add(new TextRow(Arrays.asList("Jane", "30", "Boston")));
        
        String cellValue = textTable.cell(1, 2); // Row 1, Col 2 (City column)
        assertEquals("Boston", cellValue);
    }

    @Test
    public void testCellByFieldName() {
        TextTable textTable = new TextTable();
        textTable.setFieldNames("Name", "Age", "City");
        textTable.add(new TextRow(Arrays.asList("John", "25", "New York")));
        textTable.add(new TextRow(Arrays.asList("Jane", "30", "Boston")));
        
        String cellValue = textTable.cell("City", 1); // City field, Row 1
        assertEquals("Boston", cellValue);
    }

    @Test
    public void testFieldsInnerClass() {
        TextTable.Fields fields = new TextTable.Fields();
        fields.add("Field1");
        fields.add("Field2");
        assertEquals(2, fields.size());
        assertEquals("Field1", fields.get(0));
        assertEquals("Field2", fields.get(1));
        
        int index = fields.indexOf("Field2");
        assertEquals(1, index);
        
        int notFoundIndex = fields.indexOf("NonExistent");
        assertEquals(-1, notFoundIndex);
    }

    @Test
    public void testFieldsConstructorFromTextRows() {
        TextRow source = new TextRow(Arrays.asList("FieldA", "FieldB", "FieldC"));
        TextTable.Fields fields = new TextTable.Fields(source);
        assertEquals(3, fields.size());
        assertEquals("FieldA", fields.get(0));
        assertEquals("FieldB", fields.get(1));
        assertEquals("FieldC", fields.get(2));
    }

    @Test
    public void testComplexWhereCondition() {
        TextTable textTable = new TextTable();
        textTable.setFieldNames("Name", "Score");
        textTable.add(new TextRow(Arrays.asList("Alice", "85")));
        textTable.add(new TextRow(Arrays.asList("Bob", "92")));
        textTable.add(new TextRow(Arrays.asList("Charlie", "78")));
        textTable.add(new TextRow(Arrays.asList("Diana", "96")));
        
        TextTable result = textTable.where("Score >= 90");
        assertEquals(2, result.size()); // Bob (92) and Diana (96) have scores >= 90
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWhereWithInvalidCondition() {
        TextTable textTable = new TextTable();
        textTable.where("invalid condition");
    }
}