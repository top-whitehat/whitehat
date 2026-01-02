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

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

/**
 * Lightweight YAML Parser & Generator (No Third-Party Dependencies) <br>
 * Class Name: YAML <br>
 * Supports: - Parsing YAML from String/InputStream to JSON
 */
public class YAML {

	/**
	 * Parses YAML from an InputStream
	 * 
	 * @param inputStream YAML content stream
	 * @return JSON
	 */
	public static JSON parse(InputStream inputStream) {
		StringBuilder yamlBuilder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				yamlBuilder.append(line).append("\r\n");
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to read YAML from InputStream", e);
		}
		return parse(yamlBuilder.toString());
	}

	/**
	 * Parses YAML from a string
	 * 
	 * @param inputStream YAML content stream
	 * @return JSON
	 */
	public static JSON parse(String yamlContent) {
		JSON j = new YAML().parseString(yamlContent);
		return j;
	}

	private static final String CRLF = "\r\n";

	/** Split text into line array */
	private static String[] splitLines(String text) {
		String[] lines = text.split(CRLF);
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].endsWith("\r"))
				lines[i] = lines[i].substring(0, lines[i].length() - 1);
		}
		return lines;
	}

	/** Counts leading whitespace (spaces/tabs = 1 level each) */
	private static int countIndent(String line) {
		int count = 0;
		for (char c : line.toCharArray()) {
			if (c == ' ' || c == '\t')
				count++;
			else
				break;
		}
		return count;
	}

	ListIterator<String> iterator; // Iterator of lines
	String currentListKey; // Tracks the current list's parent key (reset on indent backtrack)
	Deque<JSON> stack; // Stack for nested Maps
	Deque<Integer> indentStack; // Stack for indentation levels
	JSON root; // Root JSON object

	/** print debug information */
	private void debug(String str) {
		System.err.println(str);
	}

	/**
	 * Parses YAML string to JSON
	 * 
	 * @param yamlContent YAML content as String
	 * @return Parsed JSON
	 */
	private JSON parseString(String yamlContent) {
		String[] lines = splitLines(yamlContent); // yamlContent.split(CRLF);
		List<String> linesList = java.util.Arrays.asList(lines);
		iterator = linesList.listIterator();

		// initialize parameters
		stack = new ArrayDeque<>(); // Stack for nested Maps
		stack.push(new JSON());
		indentStack = new ArrayDeque<>(); // Stack for indentation levels
		indentStack.push(0);

		// process each line
		while (iterator.hasNext()) {
			String rawLine = iterator.next();
			String trimmedLine = rawLine.trim();
			int currentIndent = countIndent(rawLine);

			// Skip empty lines/comments
			if (trimmedLine.isEmpty() || trimmedLine.startsWith("#"))
				continue;
			
			// cut line comment
			int pos = trimmedLine.lastIndexOf('#');
			if ( pos > 0) {
				trimmedLine = trimmedLine.substring(0, pos-1).trim();
				pos = rawLine.lastIndexOf('#');
				if(pos >= 0) rawLine= rawLine.substring(0, pos-1).trim();
			}

			// when indent decrease, backtrack stack to match current indentation
			while (!indentStack.isEmpty() && currentIndent < indentStack.peek()) {
				stack.pop();
				indentStack.pop();
				currentListKey = null;
			}

			// when indent increase
			if (currentIndent > indentStack.peek()) {
				indentStack.push(currentIndent);
			}

			if (trimmedLine.startsWith("- ")) {
				// Handle list item
				String item = trimmedLine.substring(2).trim();
				addListItem(rawLine, item);

			} else if (trimmedLine.contains(":")) {
				// Handle key-value pair
				if (currentIndent == indentStack.peek()) {
					addKeyValue(rawLine, trimmedLine);

				} else {
					indentStack.push(currentIndent);
					addSubKey(rawLine, trimmedLine);
				}

			} else {
				debug("Warning: Unrecognized line format: " + rawLine);
			}

		}

		return stack.getLast();
	}

	private JSON current() {
		return stack.peek();
	}

	private JSONArray currentArray() {
		return (JSONArray) (stack.peek());
	}

	private void addListItem(String rawLine, String item) {
		// if the current is empty, change it to JSONArray
		if (!current().isArray() && current().size() == 0 && stack.size() == 1) {
			stack.pop();
			stack.push(new JSONArray());
		}

		if (!current().isArray()) {
			throw new RuntimeException("current() should be array");
		}

		int colonIdx = item.indexOf(':');

		if (colonIdx > 0) {
			// when the item is a map of key: value
			JSON j = new JSON();
			currentArray().add(j);
			stack.push(j);
			rawLine = rawLine.replaceFirst("-", " ");
			int currentIndent = countIndent(rawLine);
			if (currentIndent > indentStack.peek())
				indentStack.push(currentIndent);
			addKeyValue(rawLine, item);

		} else {
			// add the item to current array
			currentArray().add(item);
		}
	}

	private void addKeyValue(String rawLine, String line) {
		// if the current is empty array, change it to JSON
		if (current().isArray() && current().size() == 0) {
			stack.pop();
			JSON last = current();
			if (currentListKey != null && !last.isArray()) {
				JSON child = new JSON();
				last.put(currentListKey, child);
				stack.push(child);
			} else {
				throw new RuntimeException("current() should be JSON object");
			}
		}

		// the current should not be an Array
		if (current().isArray()) {
			throw new RuntimeException("current() should be JSON object");
		}

		// get separate char ':'
		int colonIdx = line.indexOf(':');
		if (colonIdx == -1) {
			debug("Warning: Invalid key-value line (no colon): " + line);
			return;
		}

		// get key, value
		String key = line.substring(0, colonIdx).trim();
		String valuePart = line.substring(colonIdx + 1).trim();

		// if the value is multi-line strings
		if (valuePart.startsWith("|") || valuePart.startsWith(">")
				|| (valuePart.startsWith("\"") && (!valuePart.endsWith("\"")))) {
			// Process multi-line strings (| = preserve newlines, > = fold newlines)
			String type = valuePart.substring(0, 1);
			int currentLevelIndent = countIndent(rawLine);
			StringBuilder content = new StringBuilder();

			// add string after '"'
			if (type.equals("\"")) {
				int pos = rawLine.indexOf("\"");
				String s = pos >= 0 ? rawLine.substring(pos + 1) : valuePart.substring(1);
				content.append(s).append(CRLF);
			}

			// Read subsequent indented lines for multi-line content
			while (iterator.hasNext()) {
				String nextRawLine = iterator.next();
				int nextIndent = countIndent(nextRawLine);
				String nextTrimmed = nextRawLine.trim();

				// process indent
				if (type.equals("\"")) {
					// nothing to do

				} else {
					if (nextIndent <= currentLevelIndent) {
						iterator.previous(); // Roll back to unprocessed line
						break;
					}
				}

				// process content
				if (type.equals(">")) {
					// Fold consecutive newlines into spaces
					if (!content.toString().endsWith(" ")) {
						content.append(" ");
					}
					content.append(nextTrimmed.trim());
				} else if (type.equals("|")) {
					// Preserve newlines for |
					content.append(nextTrimmed).append(CRLF);
				} else {
					if (nextRawLine.endsWith("\"")) {
						content.append(nextRawLine.substring(0, nextRawLine.length() - 1)).append(CRLF);
						break;
					} else {
						content.append(nextRawLine).append(CRLF);
					}
				}
			}

			String finalContent = content.toString();
			if (type.equals(">"))
				finalContent = finalContent.trim(); // Remove trailing space

			current().put(key, finalContent);

		} else {
			if (!valuePart.isEmpty()) {
				// Simple scalar value (number/boolean/string)
				current().put(key, parseValue(valuePart));
			} else {
				// when the value is empty, prepare for next array
				JSONArray array = new JSONArray();
				current().put(key, array);
				currentListKey = key;
				stack.push(array);
			}
		}
	}

	private void addSubKey(String rawLine, String line) {
		// if the current is empty array
		if (current().isArray()) {
			stack.push(new JSON());
		} else {
			stack.push(new JSON());
		}

		addKeyValue(rawLine, line);
	}

	/** Parses basic scalar values (numbers/booleans/quoted strings) */
	private static Object parseValue(String valueStr) {
		if (valueStr.equalsIgnoreCase("true"))
			return true;

		if (valueStr.equalsIgnoreCase("false"))
			return false;

		if (valueStr.matches("-?\\d+"))
			return Integer.parseInt(valueStr);

		if (valueStr.matches("-?\\d+\\.\\d+([eE][+-]?\\d+)?"))
			return Double.parseDouble(valueStr); // Scientific notation

		if (valueStr.startsWith("\"") && valueStr.endsWith("\""))
			return escape('\"', valueStr.substring(1, valueStr.length() - 1)); // Remove quotes "

		if (valueStr.startsWith("'") && valueStr.endsWith("'"))
			return escape('\'', valueStr.substring(1, valueStr.length() - 1)); // Remove quotes '

		return valueStr; // Default to string
	}

	private static String escape(char quote, String s) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		int len = s.length();
		while (i < len) {
			char c = s.charAt(i++);

			if (c != '\\') {
				// c is normal char
				sb.append(c);

			} else {
				// c is escape char
				if (i == len)
					continue; // c is last char
				// read next char
				c = s.charAt(i++);
				switch (c) {
				case '"':
				case '\'':
					if (c == quote)
						sb.append(c);
					else
						sb.append('\\').append(c);
					break;
				case '\\':
					sb.append(c);
					break;
				case 't':
					sb.append('\t');
					break;
				case 'r':
					sb.append('\r');
					break;
				case 'n':
					sb.append('\n');
					break;
				case 'b':
					sb.append('\b');
					break;
				case 'f':
					sb.append('\f');
					break;
				default:
					sb.append('\\').append(c);
					break;
				}
			}

		}
		return sb.toString();
	}

}