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



import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class TemplateTest {
    
    @Test
    public void testRenderWithVariables() {
        String template = "Hello <%name%>!";
        Map<String, Object> data = new HashMap<>();
        data.put("name", "World");
        
        String result = Template.render(template, data);
        assertEquals("Hello World!", result);
    }
    
    @Test
    public void testRenderWithLoop() {
        String template = "<% for(it : items) { %><%item%> <% } %>";
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();
        
        Map<String, Object> item1 = new HashMap<>();
        item1.put("item", "A");
        items.add(item1);
        
        Map<String, Object> item2 = new HashMap<>();
        item2.put("item", "B");
        items.add(item2);
        
        data.put("items", items);
        
        String result = Template.render(template, data);
        assertEquals("A B ", result);
    }
    
    @Test
    public void testRenderWithNestedDataInLoop() {
        String template = "<% for(user : users) { %><%user.name%> is <%user.age%> years old. <% } %>";
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> users = new ArrayList<>();
        
        Map<String, Object> user1 = new HashMap<>();
        user1.put("name", "Alice");
        user1.put("age", "25");
        users.add(user1);
        
        Map<String, Object> user2 = new HashMap<>();
        user2.put("name", "Bob");
        user2.put("age", "30");
        users.add(user2);
        
        data.put("users", users);
        
        String result = Template.render(template, data);
        System.out.println(result);
        assertEquals("Alice is 25 years old. Bob is 30 years old. ", result);
    }
    
    @Test
    public void testRenderWithMissingVariable() {
        String template = "Hello <%name%>!";
        Map<String, Object> data = new HashMap<>();
        // Not setting name in data
        
        String result = Template.render(template, data);
        assertEquals("Hello !", result);
    }
    
    @Test
    public void testRenderWithMissingLoopVariable() {
        String template = "<% for(item : items) { %><%item%> <% } %>";
        Map<String, Object> data = new HashMap<>();
        // Not setting items in data
        
        String result = Template.render(template, data);
        assertEquals("", result);
    }
}