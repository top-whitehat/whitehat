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
import java.util.regex.*;

/** <h3>Template Engine</h3>
 * 
 *  <h3>Syntax & Usage Guide:</h3>
 *  
 *  <h4>1.Variable Output</h4>
 	<code> <%variableName%> </code>​
    
    <h4>2. ​​Looping Over a List (For Loop)</h4>
    <pre> 
  <% for(item : listVariable) { %>
    ... template content using <%item.field%> ...
  <% } %>
    </pre>
 *  
 */
public class Template {

    /** Pattern which match variable, such as: <% varName %> */
    private static Pattern VAR_PATTERN;
    
    /** Pattern which match loop, such as: <% for(item : list) { %> ... body ... <% } %> */
    private static Pattern LOOP_PATTERN;
    
    /** set start tag and end tag */
    public static void setTags(String startTag, String endTag) {
    	if (startTag == null) startTag = "<%";
    	if (endTag == null) endTag = "%>";
    	
    	// variable Syntax: <% varName %>
    	VAR_PATTERN = Pattern.compile(startTag + "\\s*([\\w\\.]+)\\s*" + endTag);
    	
    	// Loop Syntax: <% for(item : list) { %> ... 内容 ... <% } %>
    	LOOP_PATTERN = Pattern.compile(
    			startTag + "\\s*for\\s*\\(\\s*(\\w+)\\s*:\\s*(\\w+)\\s*\\)\\s*\\{\\s*"+endTag //
    			+"(.*?)"+startTag+"\\s*}\\s*" + endTag,   //
    	        Pattern.DOTALL | Pattern.MULTILINE
    	    );
    }
    
    // init
    static {
    	setTags("<%", "%>");
    }

    /** render template
    *
    * @param template  template text
    * @param data      data, Map<key, value>, value could be String, number , List<Map<String, String>>
    * 
    * @return rendered text
    */
    public static String render(String template, Map<String, Object> data) {
    	Template t = new Template(template);
    	return t.render(data);
    }
    
    private String template;
    
    /** constructor */
    public Template(String template) {
    	this.template = template;
    }
    
    /** render template
     *
     * @param template  template text
     * @param data      data, Map<key, value>, value could be String, number , List<Map<String, String>>
     * 
     * @return rendered text
     */
    public String render(Map<String, Object> data) {
        if (template == null) return "";

        // firstly, process loops
        String result = processLoops(template, data);
        
        // secondly, process variables
        result = processVariables(result, data);
        
        return result;
    }
    
    /** trim leading CRLF */
    protected static String trimLeadingCRLF(String str) {
    	if (str.startsWith("\r\n"))
    		return str.substring(2);
    	else if (str.startsWith("\n"))
    		return str.substring(1);
    	else
    		return str;
    }
    
    /** trim ending CRLF */
    protected static String trimEndingCRLF(String str) {
    	if (str.endsWith("\r\n"))
    		return str.substring(0, str.length()-2);
    	else if (str.startsWith("\n"))
    		return str.substring(0, str.length()-1);
    	else
    		return str;
    }
    
    /** trim left-size blanks */
    protected static String trimLeft(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        int i = 0;
        while (i < str.length() && Character.isWhitespace(str.charAt(i))) {
            i++;
        }
        return str.substring(i);
    }
    
    /** process loops <% for(item : list) { %> ... <% } %>
     */
    private String processLoops(String template, Map<String, Object> data) {
    	 /** match loop syntax, such as: <% for(item : list) { %> ... body ... <% } %> */
        Matcher loopMatcher = LOOP_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();

        while (loopMatcher.find()) {
            String itemName = loopMatcher.group(1).trim();     // loop var, such as: user
            String listName = loopMatcher.group(2).trim();     // loop list, such as:users
            String loopBody = trimLeadingCRLF(loopMatcher.group(3)); // loop body content

            Object listObj = data.getOrDefault(listName, null); 
            if (!(listObj instanceof List)) {
            	// it is not a list, replace with blank
                loopMatcher.appendReplacement(result, Matcher.quoteReplacement(""));
                continue;
            }

            List<?> list = (List<?>) listObj;
            if (list.isEmpty() || !(list.get(0) instanceof Map)) {
            	// it is not a variable and not a list, replace with blanks
                loopMatcher.appendReplacement(result, Matcher.quoteReplacement(""));
                continue;
            }

            // process body: process each item            StringBuilder loopResult = new StringBuilder();            for (Object itemObj : list) {                if (!(itemObj instanceof Map)) continue;
                @SuppressWarnings("unchecked")                Map<String, Object> item = (Map<String, Object>) itemObj;
                // construct loop data:  data + item                Map<String, Object> loopData = new HashMap<>(data); // process outer layer                loopData.putAll(item); // put current item
                // Also add the item itself with its name so we can access it as itemName.property                loopData.put(itemName, item);
                String renderedBody = processVariables(loopBody, loopData);
                loopResult.append(renderedBody);
            }

            loopMatcher.appendReplacement(result, Matcher.quoteReplacement(loopResult.toString()));
        }
        
        loopMatcher.appendTail(result);
        return result.toString();
    }

    /** process variable, such as <% key %>, get value from data, replace it with value
     */
    private String processVariables(String template, Map<String, Object> data) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = VAR_PATTERN.matcher(template);

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            Object value = getValueFromData(key, data);
            matcher.appendReplacement(result, Matcher.quoteReplacement(value != null ? value.toString() : ""));
        }
        matcher.appendTail(result);
        return result.toString();
    }
    
    /** get value from data, support nested access like user.name
     */
    private Object getValueFromData(String key, Map<String, Object> data) {
        // Check if it's a nested key (contains dot)
        if (key.contains(".")) {
            String[] parts = key.split("\\.", 2);
            String parentKey = parts[0];
            String childKey = parts[1];
            
            Object parentValue = data.getOrDefault(parentKey, null);
            if (parentValue instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> parentMap = (Map<String, Object>) parentValue;
                return parentMap.getOrDefault(childKey, "");
            } else {
                return ""; // Parent key doesn't exist or is not a map
            }
        } else {
            // Simple key access
            return data.getOrDefault(key, "");
        }
    }
}
