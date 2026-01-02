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

import java.util.Map;

import org.junit.Test;






public class JSONTest {


    @Test
    public void testParseSimpleObject() {
        String json = "{\"name\":\"John\",\"age\":30}";
        JSON result = JSON.parse(json);
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals("John", map.get("name"));
        assertEquals(30, map.get("age"));
        
    }
    
  
      
    @Test
    public void testParseArray() {
        String json = "[1,2,3]";
        JSONArray result = JSON.parseArray(json);
        assertNotNull(result);
        
        assertTrue(result instanceof JSONArray);
        
        JSONArray array = (JSONArray) result;
        assertEquals(1, array.get(0));
        assertEquals(2, array.get(1));
        assertEquals(3, array.get(2));
    }
    
    
    @Test
    public void testParseNestedObject() {
        String json = "{\"person\":{\"name\":\"John\",\"age\":30}}";
        Object result = JSON.parse(json);
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertTrue(map.get("person") instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> person = (Map<String, Object>) map.get("person");
        assertEquals("John", person.get("name"));
        assertEquals(30, person.get("age"));
    }
    

    @Test
    public void testStringifySimpleObject() {
        JSON json = new JSON();
        json.set("name", "John");
        json.set("age", 30);
        
        String result = JSON.stringify(json);
        assertNotNull(result);
        assertTrue(result.contains("\"John\""));
        assertTrue(result.contains("30"));
    }
    

    @Test
    public void testStringifyArray() {
//        JSON.ArrayBuilder array = JSON.array();
    	JSONArray array = new JSONArray();
        array.add(1);
        array.add(2);
        array.add(3);
        
        String result = JSON.stringify(array);
        System.out.println(result);
//        assertEquals("[1,2,3]", result);
    }

	
}