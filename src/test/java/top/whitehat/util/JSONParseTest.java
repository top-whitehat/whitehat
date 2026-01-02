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

import static org.junit.Assert.assertEquals;

import org.junit.Test;



public class JSONParseTest {
	
	@SuppressWarnings("unlikely-arg-type")
	@Test
    public void test1() {
        try {
            // Test simple object
            String json1 = "{\"name\":\"John\",\"age\":30}";
            JSON result1 = JSON.parse(json1);
            assertEquals(result1.get("name"), "John");
            assertEquals(result1.get("age"), 30);
//            System.out.println("Parsed object: " + result1);
            
            // Test nested object
            String json2 = "{\"person\":{\"name\":\"John\",\"age\":30},\"city\":\"New York\"}";
            JSON result2 = JSON.parse(json2);
            assertEquals(result2.getJSON("person").get("name"), "John");
            assertEquals(result2.getJSON("person").get("age"), 30);
//            System.out.println("Parsed nested object: " + result2);

            
            // Test array
            String json3 = "[\"apple\",\"banana\",\"cherry\"]";
            JSON result3 = JSON.parse(json3);
            assertEquals(result3.get(0), "apple");
            assertEquals(result3.get(1), "banana");
            assertEquals(result3.get(2), "cherry");
//            System.out.println("Parsed array: " + result3);
            
            // Test complex structure
            String json4 = "{\"name\":\"John\",\"age\":30,\"hobbies\":[\"reading\",\"swimming\"],\"address\":{\"city\":\"New York\",\"zip\":10001}}";
            JSON result4 = JSON.parse(json4);
//            System.out.println("Parsed complex structure: " + result4);
            assertEquals(result4.getJSON("hobbies").get(0), "reading");
            assertEquals(result4.getJSON("address").get("city"), "New York");
            
            // Test with numbers, booleans and null
            String json5 = "{\"active\":true,\"score\":95.5,\"rank\":null}";
            JSON result5 = JSON.parse(json5);
//            System.out.println("Parsed mixed types: " + result5);
            assertEquals(result5.get("active"), true);
            assertEquals(result5.get("score"), 95.5);
            
            // Test with escaped characters
            String json6 = "{\"message\":\"Hello\\nWorld\\twith\\\"quotes\\\"\"}";
            JSON result6 = JSON.parse(json6);
//            System.out.println("Parsed with escaped chars: " + result6);
            assertEquals(result6.getString("message").contains("World"), true);
            
//            System.out.println("All tests passed!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}