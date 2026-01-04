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
import java.util.function.Consumer;

/**
 * A lightweight JSON object that constructs a JSON-like structure using
 * Map<String, Object> and List<Object>.
 * 
 * <h3>Usage example</h3>
 <pre>
  // convert json string to JSON object
  JSON jsonObj = JSON.parse("{name: 'peter', age: 18}");
  
  // JSON object is a Map&lt;String, Object&gt; object, get value by key
  String name = (String)jsonObject.get("name");
  int age = (Integer)jsonObject.get("age");
 
  // convert JSON object to json string
  String jsonString = JSON.stringify(jsonObj);
 </pre>
 */
public class JSON extends LinkedHashMap<String, Object> {

	private static final long serialVersionUID = 6615119570530620095L;

	/** create JSON object from parameters */
	private static JSON fromParams(Object... objs) {
		JSON ret = new JSON();
		int i = 0;
		while (i < objs.length) {
			String key;
			try {
				key = (String) objs[i];
			} catch (Exception e) {
				String cls = objs[i] == null ? "null" : objs[i].getClass().getSimpleName();
				String msg = "parameter at position " + i + " should be String (as key name), but " + cls + " is found";
				throw new IllegalArgumentException(msg);
			}
			Object value = i < objs.length - 1 ? objs[i + 1] : null;
			ret.put(key, value);
			i += 2;
		}
		return ret;
	}

	/** create a JSON array */
	public static JSONArray array(Object... objs) {
		JSONArray ret = new JSONArray();
		int i = 0;
		while (i < objs.length) {
			ret.add(objs[i++]);
		}
		return ret;
	}

	/** create a JSON object */
	public static JSON of(Object... objs) {
		JSON ret = fromParams(objs);
		return ret.build();
	}

	/** create a JSON String */
	public static String string(Object... objs) {
		JSON ret = fromParams(objs);
		return ret.toString();
	}

	// map holds key-values
	private JSON map; // = new HashMap<>();

	// constructor
	public JSON() {
		map = this;
	}

	@SuppressWarnings("unchecked")
	public JSON set(String key, Object value) {
		if (value instanceof JSON) {
			putJSON(key, (JSON)value);
		} else if (value instanceof List<?>) {
			putArray(key, (List<Object>)value);
		} else {
			put(key, value);
		}
		return this;
	}
	
	public JSON getJSON(String key) {
		return (JSON)get(key);
	}
	
	public String getString(String key) {
		return (String)get(key);
	}
	
	public int getInt(String key) {
		return (Integer)get(key);
	}
	
	public double getDouble(String key) {
		Object obj = get(key);
		if (obj instanceof Integer)
			return (Integer)obj;
		else if (obj instanceof Long)
			return (Long)obj;
		else if (obj instanceof Short)
			return (Short)obj;
		else if (obj instanceof Double)
			return (Double)obj;
		else
			throw new RuntimeException("cannot convert " + obj.getClass().getSimpleName() + " to double");
	}
	
	public boolean getBoolean(String key) {
		return (Boolean)get(key);
	}
	
	public boolean has(String key) {
		return containsKey(key);
	}
	
	/**
	 * Adds a key-value pair to the JSON object.
	 *
	 * @param key   The key (must be a valid JSON string key).
	 * @param value The value (must be a supported type: String, Number, Boolean,
	 *              null, Map, or List).
	 * @return This builder instance for chaining.
	 */
	public JSON put(String key, Object value) {
		if (value instanceof JSON) {
			return putJSON(key, (JSON) value);
		} else {
			super.put(key, value);
		}
		return this;
	}

	/**
	 * Adds a nested JSON object under the specified key.
	 *
	 * @param key           The key for the nested object.
	 * @param objectBuilder A consumer that receives a new JsonBuilder to define the
	 *                      nested object.
	 * @return This builder instance for chaining.
	 */
	public JSON putObject(String key, Consumer<JSON> objectBuilder) {
		JSON builder = new JSON();
		objectBuilder.accept(builder);
		super.put(key, builder.build());
		return this;
	}

	/** Adds a nested JSON object under the specified key. */
	public JSON putJSON(String key, JSON obj) {
		super.put(key, obj);
		return this;
	}
	
	

	/**
	 * Adds a JSON array (List<Object>) under the specified key.
	 *
	 * @param key          The key for the array.
	 * @param arrayBuilder A consumer that receives an ArrayBuilder to define the
	 *                     array contents.
	 * @return This builder instance for chaining.
	 */
	public JSON putArray(String key, Consumer<ArrayBuilder> arrayBuilder) {
		ArrayBuilder builder = new ArrayBuilder();
		arrayBuilder.accept(builder);
		super.put(key, builder.build());
		return this;
	}

	/** Adds a JSON array (List<Object>) under the specified key. */
	public JSON putArray(String key, List<Object> list) {
		super.put(key, (JSONArray) list);
		return this;
	}

	/**
	 * Converts the built JSON structure into a pretty-printed JSON string.
	 *
	 * @return A formatted JSON string.
	 */
	@Override
	public String toString() {
		return toJson(map, 0);
	}

	/**
	 * Recursively converts a Java object into its JSON string representation with
	 * proper indentation.
	 *
	 * @param value       The object to serialize.
	 * @param indentLevel Current depth for indentation.
	 * @return JSON string representation of the value.
	 * @throws IllegalArgumentException If the value is of an unsupported type.
	 */
	@SuppressWarnings({ "unchecked" })
	private static String toJson(Object value, int indentLevel) {
		StringBuilder indent = new StringBuilder();
		for (int i = 0; i < indentLevel; i++)
			indent.append("  "); // 2 spaces per indent level

		if (value == null) {
			return "null";
		} else if (value instanceof String) {
			return "\"" + escapeJsonString((String) value) + "\"";
		} else if (value instanceof Number) {
			return value.toString();
		} else if (value instanceof Boolean) {
			return value.toString();
		} else if (value instanceof JSONArray) {
			return jsonArrayToJson((JSONArray)value, indentLevel);
		} else if (value instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) value;
			return mapToJson(map, indentLevel);
		} else {
			throw new IllegalArgumentException("Unsupported type for JSON: " + value.getClass().getName()
					+ ". Only String, Number, Boolean, null, Map<String, Object>, and List<Object> are allowed.");
		}
	}

	/**
	 * Escapes special characters in a string to make it valid JSON.
	 *
	 * @param input The raw string.
	 * @return Escaped JSON string.
	 */
	private static String escapeJsonString(String input) {
		if (input == null)
			return "";
		return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\b", "\\b").replace("\f", "\\f")
				.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
	}

	/**
	 * Converts a Map<String, Object> into a pretty-printed JSON object string.
	 *
	 * @param map         The map representing the JSON object.
	 * @param indentLevel Current indentation level.
	 * @return Formatted JSON object string.
	 */
	private static String mapToJson(Map<String, Object> map, int indentLevel) {
		if (map.isEmpty())
			return "{}";

		StringBuilder sb = new StringBuilder();
		String indent = repeat("  ", indentLevel);
		String nextIndent = repeat("  ", indentLevel + 1);

		sb.append("{\n");
		boolean first = true;
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (!first)
				sb.append(",\n");
			first = false;

			sb.append(nextIndent).append("\"").append(escapeJsonString(entry.getKey())).append("\": ")
					.append(toJson(entry.getValue(), indentLevel + 1));
		}
		sb.append("\n").append(indent).append("}");
		return sb.toString();
	}

	/** repeat String */
	private static String repeat(String s, int times) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < times; i++) {
			sb.append(s);
		}
		return sb.toString();
	}

	
	/** Converts a JSONArray object into a pretty-printed JSON array string.
	 *
	 * @param list        The list representing the JSON array.
	 * @param indentLevel Current indentation level.
	 * @return Formatted JSON array string.
	 */
	private static String jsonArrayToJson(JSONArray list, int indentLevel) {
		if (list.isEmpty())
			return "[]";

		StringBuilder sb = new StringBuilder();
		String indent = repeat("  ", indentLevel);
		String nextIndent = repeat("  ", indentLevel + 1);

		sb.append("[\n");
		boolean first = true;
		for (Object item : list.getList()) {
			if (!first)
				sb.append(",\n");
			first = false;

			sb.append(nextIndent).append(toJson(item, indentLevel + 1));
		}
		sb.append("\n").append(indent).append("]");
		return sb.toString();
	}
	
	/**
	 * Converts a List<Object> into a pretty-printed JSON array string.
	 *
	 * @param list        The list representing the JSON array.
	 * @param indentLevel Current indentation level.
	 * @return Formatted JSON array string.
	 */
	protected static String listToJson(List<Object> list, int indentLevel) {
		if (list.isEmpty())
			return "[]";

		StringBuilder sb = new StringBuilder();
		String indent = repeat("  ", indentLevel);
		String nextIndent = repeat("  ", indentLevel + 1);

		sb.append("[\n");
		boolean first = true;
		for (Object item : list) {
			if (!first)
				sb.append(",\n");
			first = false;

			sb.append(nextIndent).append(toJson(item, indentLevel + 1));
		}
		sb.append("\n").append(indent).append("]");
		return sb.toString();
	}

	/**
	 * Builder for constructing JSON arrays (List<Object>).
	 */
	public static class ArrayBuilder {
		private final JSONArray list = new JSONArray();

		public ArrayBuilder add(Object value) {
			list.add(value);
			return this;
		}

		public ArrayBuilder addObject(Consumer<JSON> objectBuilder) {
			JSON builder = new JSON();
			objectBuilder.accept(builder);
			list.add(builder.build());
			return this;
		}

		public ArrayBuilder addArray(Consumer<ArrayBuilder> nestedArrayBuilder) {
			ArrayBuilder builder = new ArrayBuilder();
			nestedArrayBuilder.accept(builder);
			list.add(builder.build());
			return this;
		}

		public JSONArray build() {
			return list;
		}
	}
	
	public boolean isArray() {
		return this instanceof JSONArray;
	}

	/**
	 * Returns the underlying Map structure representing the JSON object.
	 *
	 * @return The built Map<String, Object>.
	 */
	public JSON build() {
		return map;
	}

	
	/** Convert an object to JSON string */
	public static String stringify(Object obj) {
		if (obj instanceof Map) {
			return toJson(obj, 2);
		} else if (obj instanceof JSON) {
			return toJson(obj, 2);
		} else if (obj instanceof List) {
			return toJson(obj, 2);
		}
		return "";
	}
	
	/** Convert an Map object to JSON object */
	protected static JSON parseMap(Map<String, Object> obj) {
		JSON ret = new JSON();
		for(String key : obj.keySet()) {
			Object value = obj.get(key);
			if (value instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> v = (Map<String, Object>)value;
				value = parseMap(v);
				
			} else if (value instanceof List) {
				JSONArray arr = new JSONArray();
				@SuppressWarnings("unchecked")
				List<Object> list = (List<Object>)value;
				for(Object v : list)
					arr.add(v);
				value = arr;
			}			
			ret.put(key, value);
		}
		return ret;
	}
	
	/** Convert an Map object to JSON object */
	public static JSON parse(Map<String, Object> map) {
		return parseMap(map);
	}
	
	
	/**
	 * Parses a YAML string into a JSON object.
	 *
	 * @param yamlString The YAML string to parse.
	 * @return The parsed JSON object.
	 * @throws IllegalArgumentException If the JSON string is invalid.
	 */
	public static JSON parseYaml(String yamlString) {
		return YAML.parse(yamlString);
	}
	
	/**
	 * Parses a JSON string into a JSON object.
	 *
	 * @param jsonString The JSON string to parse.
	 * @return The parsed JSON object.
	 * @throws IllegalArgumentException If the JSON string is invalid.
	 */
	public static JSON parse(String jsonString) {
		Object o = parse0(jsonString);
		if (o instanceof JSON)
			return (JSON) o;
		throw new RuntimeException("parse result is " + o.getClass().getSimpleName());
	}

	/**
	 * Parses a JSON string into a JSON Array.
	 *
	 * @param jsonString The JSON string to parse.
	 * @return The parsed JSON object.
	 * @throws IllegalArgumentException If the JSON string is invalid.
	 */
	public static JSONArray parseArray(String jsonString) {
		Object o = parse0(jsonString);
		if (o instanceof List) {
			List<?> l = (List<?>)o;
			JSONArray arr = new JSONArray();
			for(int i=0; i<l.size(); i++)
				arr.add(l.get(i));
			return arr;
			
		} else if (ArrayUtil.isArrayObject(o)) {
			int len = ArrayUtil.getLength(o);
			JSONArray arr = new JSONArray();
			for (int i = 0; i < len; i++) {
				arr.add(ArrayUtil.get(o, i));
			}
			return arr;
		}
		
		return null;
	}

	/**
	 * Parses a JSON string into a JSON object.
	 *
	 * @param jsonString The JSON string to parse.
	 * @return The parsed JSON object.
	 * @throws IllegalArgumentException If the JSON string is invalid.
	 */
	private static Object parse0(String jsonString) {
		if (jsonString == null || jsonString.trim().isEmpty()) {
			throw new IllegalArgumentException("JSON string cannot be null or empty");
		}

		JSONParser parser = new JSONParser(jsonString.trim());
		Object ret = parser.parse();
		return ret;
	}

	/**
	 * Inner class for parsing JSON strings.
	 */
	private static class JSONParser {
		private final String json;
		private int index;

		public JSONParser(String json) {
			this.json = json;
			this.index = 0;
		}

		public Object parse() {
			skipWhitespace();
			char current = currentChar();

			if (current == '{') {
				return parseObject();
			} else if (current == '[') {
				return parseArray();
			} else if (current == '"' || current == '\'') {
				return parseString(current);
			} else if (current == 't' || current == 'f') {
				return parseBoolean();
			} else if (current == 'n') {
				parseNull();
				return null;
			} else if (isNumberStart(current)) {
				return parseNumber();
			} else {
				throw new IllegalArgumentException(
						"Invalid JSON at position " + index + ": unexpected character '" + current + "'");
			}
		}

		private JSON parseObject() {
			expectChar('{');
			skipWhitespace();

			JSON obj = new JSON();

			// Check for empty object
			if (currentChar() == '}') {
				index++;
				return obj;
			}

			while (true) {
				skipWhitespace();

				// Parse key
				String key = parseString('\"');
				skipWhitespace();

				// Expect colon
				expectChar(':');
				skipWhitespace();

				// Parse value
				Object value = parse();
				obj.put(key, value);

				skipWhitespace();

				// Check for comma or end of object
				char c = currentChar();
				if (c == ',') {
					index++;
					continue;
				} else if (c == '}') {
					index++;
					break;
				} else {
					throw new IllegalArgumentException("Expected ',' or '}' at position " + index);
				}
			}

			return obj;
		}

		private JSONArray parseArray() {
			expectChar('[');
			skipWhitespace();

			JSONArray array = new JSONArray();

			// Check for empty array
			if (currentChar() == ']') {
				index++;
				return array;
			}

			while (true) {
				skipWhitespace();

				// Parse value
				Object value = parse();
				array.add(value);

				skipWhitespace();

				// Check for comma or end of array
				char c = currentChar();
				if (c == ',') {
					index++;
					continue;
				} else if (c == ']') {
					index++;
					break;
				} else {
					throw new IllegalArgumentException("Expected ',' or ']' at position " + index);
				}
			}

			return array;
		}

		private String parseString(char quoteChar) {
			boolean needQuoteChar = true;
			if (Character.isAlphabetic(currentChar())) {
				needQuoteChar = false;
			} else {
				expectChar(quoteChar);
			}
			
			StringBuilder sb = new StringBuilder();

			while (index < json.length()) {
				char c = json.charAt(index++);
				
				if (needQuoteChar) {
					if (c == quoteChar) return sb.toString();
				} else {
					if (!Character.isJavaIdentifierPart(c)) {
						index--;
						return sb.toString();
					}
				}
				
				if (c == '\\') {
					// Handle escape sequences
					if (index >= json.length()) {
						throw new IllegalArgumentException("Unexpected end of string at position " + index);
					}
					char escaped = json.charAt(index++);
					switch (escaped) {
					case '"':
						sb.append('"');
						break;
					case '\\':
						sb.append('\\');
						break;
					case '/':
						sb.append('/');
						break;
					case 'b':
						sb.append('\b');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'n':
						sb.append('\n');
						break;
					case 'r':
						sb.append('\r');
						break;
					case 't':
						sb.append('	');
						break;
					case 'u':
						// Unicode escape sequence
						if (index + 4 > json.length()) {
							throw new IllegalArgumentException("Invalid unicode escape sequence at position " + index);
						}
						String hex = json.substring(index, index + 4);
						index += 4;
						try {
							int codePoint = Integer.parseInt(hex, 16);
							sb.append((char) codePoint);
						} catch (NumberFormatException e) {
							throw new IllegalArgumentException(
									"Invalid unicode escape sequence at position " + index + ": " + hex);
						}
						break;
					default:
						throw new IllegalArgumentException(
								"Invalid escape sequence at position " + (index - 2) + ": " + escaped);
					}
				} else {
					sb.append(c);
				}
			}

			throw new IllegalArgumentException("Unterminated string at position " + index);
		}

		private Object parseNumber() {
			int start = index;

			// Handle negative sign
			if (currentChar() == '-') {
				index++;
			}

			// Parse integer part
			if (currentChar() == '0') {
				index++;
			} else if (isDigit(currentChar())) {
				while (index < json.length() && isDigit(currentChar())) {
					index++;
				}
			} else {
				throw new IllegalArgumentException("Invalid number at position " + index);
			}

			// Parse fractional part
			if (index < json.length() && currentChar() == '.') {
				index++;
				if (index >= json.length() || !isDigit(currentChar())) {
					throw new IllegalArgumentException(
							"Invalid number at position " + index + ": missing digits after decimal point");
				}
				while (index < json.length() && isDigit(currentChar())) {
					index++;
				}
			}

			// Parse exponent part
			if (index < json.length() && (currentChar() == 'e' || currentChar() == 'E')) {
				index++;
				if (index < json.length() && (currentChar() == '+' || currentChar() == '-')) {
					index++;
				}
				if (index >= json.length() || !isDigit(currentChar())) {
					throw new IllegalArgumentException(
							"Invalid number at position " + index + ": missing digits in exponent");
				}
				while (index < json.length() && isDigit(currentChar())) {
					index++;
				}
			}

			String numberStr = json.substring(start, index);

			// Try to parse as integer first, then as double
			try {
				if (numberStr.contains(".") || numberStr.contains("e") || numberStr.contains("E")) {
					return Double.parseDouble(numberStr);
				} else {
					long value = Long.parseLong(numberStr);
					// If it fits in an int, return int; otherwise return long
					if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
						return (int) value;
					} else {
						return value;
					}
				}
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid number at position " + start + ": " + numberStr);
			}
		}

		private Boolean parseBoolean() {
			if (json.startsWith("true", index)) {
				index += 4;
				return Boolean.TRUE;
			} else if (json.startsWith("false", index)) {
				index += 5;
				return Boolean.FALSE;
			} else {
				throw new IllegalArgumentException("Invalid boolean value at position " + index);
			}
		}

		private void parseNull() {
			if (!json.startsWith("null", index)) {
				throw new IllegalArgumentException("Invalid null value at position " + index);
			}
			index += 4;
		}

		private void skipWhitespace() {
			while (index < json.length() && isWhitespace(currentChar())) {
				index++;
			}
		}

		private char currentChar() {
			if (index >= json.length()) {
				throw new IllegalArgumentException("Unexpected end of JSON at position " + index);
			}
			return json.charAt(index);
		}

		private void expectChar(char expected) {
			if (index >= json.length()) {
				throw new IllegalArgumentException(
						"Unexpected end of JSON, expected '" + expected + "' at position " + index);
			}
			char actual = json.charAt(index++);
			if (actual != expected) {
				throw new IllegalArgumentException(
						"Expected '" + expected + "' but found '" + actual + "' at position " + (index - 1));
			}
		}

		private boolean isWhitespace(char c) {
			return c == ' ' || c == '\t' || c == '\n' || c == '\r';
		}

		private boolean isDigit(char c) {
			return c >= '0' && c <= '9';
		}

		private boolean isNumberStart(char c) {
			return c == '-' || isDigit(c);
		}
	}

}
