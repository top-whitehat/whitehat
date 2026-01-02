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

import java.util.*;




public class TemplateDirectTest {
    public static void main(String[] args) {
        // Test the failing case: testRenderWithNestedDataInLoop
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
        System.out.println("Result: '" + result + "'");
        System.out.println("Expected: 'Alice is 25 years old. Bob is 30 years old. '");
        System.out.println("Match: " + result.equals("Alice is 25 years old. Bob is 30 years old. "));
    }
}