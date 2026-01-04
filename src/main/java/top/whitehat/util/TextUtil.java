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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Provides static methods of text processing */
public class TextUtil {

	/** remove space char in the string */
	public static String removeSpace(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {

			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Attempts to parse a string into a numeric value, trying Long first, then
	 * Double. If both attempts fail, returns the original string.
	 * 
	 * @param value The string to parse
	 * @return The parsed numeric value (Long or Double) or the original string
	 */
	protected static Object parseValue(String value) {
		try {
			return Long.parseLong(value);
		} catch (Exception e) {
		}

		try {
			return Double.parseDouble(value);
		} catch (Exception e) {
		}
		return value;
	}

	/**
	 * Checks if a string represents a number (integer or floating point)
	 * 
	 * @param value The string to check
	 * @return true if the string represents a number, false otherwise
	 */
	public static boolean isNumber(String value) {
		try {
			Double.parseDouble(value);
			return true;
		} catch (Exception e) {
		}

		try {
			Long.parseLong(value);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/** Checks if a string represents a integer */
	public static boolean isInteger(String value) {
		try {
			Long.parseLong(value);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * Checks if both strings represent numbers
	 * 
	 * @param value1 The first string to check
	 * @param value2 The second string to check
	 * @return true if both strings represent numbers, false otherwise
	 */
	public static boolean isNumbers(String value1, String value2) {
		return isNumber(value1) && isNumber(value2);
	}

	/**
	 * Compares two string values according to the specified operator
	 * 
	 * @param value1 The first value to compare
	 * @param op     The comparison operator (=, ==, >, <, >=, <=)
	 * @param value2 The second value to compare
	 * @return The result of the comparison
	 */
	protected static boolean compareNumber(String value1, String op, String value2) {
		switch (op) {
		case "=":
		case "==":
			if (isInteger(value1) && isInteger(value2))
				return Long.parseLong(value1) == Long.parseLong(value2);
			else
				return Math.abs(Double.parseDouble(value1) - Double.parseDouble(value2)) < 0.00000000001;
		case "!=":
		case "<>":
			if (isInteger(value1) && isInteger(value2))
				return Long.parseLong(value1) != Long.parseLong(value2);
			else
				return Math.abs(Double.parseDouble(value1) - Double.parseDouble(value2)) > 0.00000000001;
		case ">":
			return Double.parseDouble(value1) > Double.parseDouble(value2);
		case "<":
			return Double.parseDouble(value1) < Double.parseDouble(value2);
		case ">=":
			return Double.parseDouble(value1) >= Double.parseDouble(value2);
		case "<=":
			return Double.parseDouble(value1) <= Double.parseDouble(value2);
		}
		return false;
	}

	/**
	 * Compares two string values according to the specified operator If both values
	 * are numeric, performs a numeric comparison, otherwise performs a string
	 * comparison
	 * 
	 * @param value1 The first value to compare
	 * @param op     The comparison operator (=, ==, >, <, >=, <=)
	 * @param value2 The second value to compare
	 * @return The result of the comparison
	 */
	public static boolean compare(String value1, String op, String value2) {
		if (isNumbers(value1, value2))
			return compareNumber(value1, op, value2);

		switch (op) {
		case "=":
		case "==":
			return value1 == null ? (value2 == null) : value1.equals(value2);
		case "!=":
		case "<>":
			return value1 == null ? (value2 != null) : !value1.equals(value2);
		case ">":
			return value1 == null ? false : value1.compareTo(value2) > 0;
		case "<":
			return value1 == null ? false : value1.compareTo(value2) < 0;
		case ">=":
			return value1 == null ? false : value1.compareTo(value2) >= 0;
		case "<=":
			return value1 == null ? false : value1.compareTo(value2) <= 0;
		}
		return false;
	}

	/** parse printf() format expression, such as: %d %8.3f %s %i %x */
	protected static int parseFormatExpr(String format, int i, StringBuilder sb) {
		int len = format.length();
		int length = 0;
		int precision = -1;
		char c = format.charAt(i++);

		// when next char is a digit, read length
		if (c >= '0' && c <= '9') {
			do {
				length = length * 10 + (c - '0');
				c = format.charAt(i++);
			} while (c >= '0' && c <= '9' && i < len);
		}

		// when next char is a dot, read precision
		if (c == '.') {
			c = format.charAt(i++);
			precision = 0;
			if (c >= '0' && c <= '9') {
				do {
					precision = precision * 10 + (c - '0');
					c = format.charAt(i++);
				} while (c >= '0' && c <= '9' && i < len);
			}
		}

		// parse %?
		switch (c) {
		case '%':
			sb.append(c);
			break;
		case 's':
			if (length > 0)
				sb.append("([^\\r\\n]{" + length + "})");
			else
				sb.append("([^\\r\\n]*?)");
			break;
		case 'S':
			if (length > 0)
				sb.append("[ \\t]{" + length + "}");
			else
				sb.append("[ \\t]+");
			break;
		case 'f':
			if (length > 0) {
				sb.append("([ \\t0-9.+-]{" + length + "})");
			} else {
				sb.append("([0-9+-.]+)");
			}
			break;
		case 'c':
			if (length > 0) {
				sb.append("(.{" + length + "})");
			} else {
				sb.append("(.)");
			}
			break;
		case 'i':
		case 'd':
			if (length > 0) {
				sb.append("([ \\t0-9+-]{" + length + "})");
			} else {
				sb.append("([0-9+-]+)");
			}
			break;
		case 'x':
			if (length > 0) {
				sb.append("([ \\t0-9A-Za-z+-]{" + length + "})");
			} else {
				sb.append("([0-9A-Za-z+-]+)");
			}
			break;
		default:
			throw new RuntimeException("invalid char " + c);
		}

		return i;
	}

	/** Convert printf() format to regex */
	public static String printfFormatToRegex(String format) {
		if (format.length() >= 4 && format.startsWith("//") && format.endsWith("//")) {
			format = format.substring(2, format.length() - 2);
		}

		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (i < format.length()) {
			char c = format.charAt(i++);
			switch (c) {
			case '%':
				i = parseFormatExpr(format, i, sb);
				break;
			default:
				if (".^$[]\\/(){}|*+?,".indexOf(c) >= 0) { // chars need escape
					sb.append("\\").append(c);
				} else {
					switch (c) {
					case '\t':
						sb.append("\\t");
						break;
					case '\r':
						sb.append("\\r");
						break;
					case '\n':
						sb.append("\\n");
						break;
					case '\b':
						sb.append("\\b");
						break;
					case '\f':
						sb.append("\\f");
						break;
					default:
						sb.append(c);
						break;
					}

				}
			}
		}

		return sb.toString();

	}

	/**
	 * Extracts a regular expression pattern from a string expression Expression.
	 * 
	 * String format is /pattern/flags. Available flags are: 'i' indicates
	 * case-insensitive matching 'm' indicates multi-line matching 's' indicates
	 * dot(.) can match newline characters (\n, \r) 'u' treats the pattern as a
	 * sequence of unicode code points;
	 * 
	 * @param expr The expression containing the regular expression
	 * @return The compiled Pattern object, or null if the expression is not a valid
	 *         regex
	 */
	public static Pattern getPattern(String expr) {
		if (expr.startsWith("//") && expr.endsWith("//")) {
			expr = "/" + printfFormatToRegex(expr) + "/";
		}

		if (expr.startsWith("/")) {
			int last = expr.lastIndexOf('/');
			if (last > 0) {
				String re = expr.substring(1, last);
				String opts = expr.substring(last + 1);
				int flags = 0;
				for (int i = 0; i < opts.length(); i++) {
					char c = opts.charAt(i);
					switch (c) {
					case 'i':
						flags |= Pattern.CASE_INSENSITIVE;
						break;
					case 'm':
						flags |= Pattern.MULTILINE;
						break;
					case 'u':
						flags |= Pattern.UNICODE_CASE;
						break;
					case 's':
						flags |= Pattern.DOTALL;
						break;
					default:
						break;
					}
				}
				return flags == 0 ? Pattern.compile(re) : Pattern.compile(re, flags);
			}
		}
		return null;
	}

	/** Extract words form line by regex */

	/** Extract word from specified line by using regex expression */
	public static String[] extractWords(String line, String regex) {
		return extractWords(line, getPattern(regex));
	}

	/** Extract word from specified line by using regex Pattern */
	public static String[] extractWords(String line, Pattern p) {
		List<String> result = new ArrayList<String>();
		Matcher m = p.matcher(line);
		if (m.find()) {
			for (int n = 1; n <= m.groupCount(); n++) {
				String word = m.group(n);
				result.add(word);
			}
		}
		return result.toArray(new String[result.size()]);
	}

	/**
	 * Checks if a line matches an expression (string or regular expression)
	 * 
	 * @param line The line to match against
	 * @param expr The expression to match (can be a plain string or regex)
	 * @return true if the line matches the expression, false otherwise
	 */
	public static boolean match(String line, String expr) {
		if (expr == null || expr.length() == 0)
			return line.trim().length() == 0;

		Pattern p = getPattern(expr);
		if (p != null)
			return match(line, p);
		boolean ret = line.indexOf(expr) >= 0;
		return ret;
	}

	/**
	 * Checks if a line matches a regex Pattern
	 * 
	 * @param line    The line to match against
	 * @param pattern The regex pattern to match
	 * @return true if the line matches the pattern, false otherwise
	 */
	public static boolean match(String line, Pattern pattern) {
		return pattern.matcher(line).find();
	}

	/**
	 * Create a copy of the List<String>
	 * 
	 * @param l The List
	 * @return a new created List<String>
	 */
	public static List<String> clone(List<String> l) {
		if (l == null)
			return l;

		List<String> ret = new ArrayList<String>();
		for (String s : l)
			ret.add(s);
		return ret;
	}

	/**
	 * Split a line to a String List by a series of column indexes
	 * 
	 * @param line          the line
	 * @param columnIndexes indexes of columns
	 * 
	 * @return an String array which contains each words in the line.
	 */
	public static List<String> split(String line, int... columnIndexes) {
		int start = 0;
		if (columnIndexes.length > 0 && columnIndexes[0] <= 0)
			start = 1;

		List<String> result = new ArrayList<String>();
//				new String[columnIndexes.length - start + 1];

		int beginIndex = 0;
		for (int i = start; i < columnIndexes.length; i++) {
			int endIndex = columnIndexes[i];

			if (endIndex > line.length())
				endIndex = line.length();

			String cell = "";
			if (endIndex >= beginIndex)
				cell = line.substring(beginIndex, endIndex).trim();
			result.add(cell);
			beginIndex = endIndex;
		}
		String ss = line.substring(beginIndex);
		result.add(ss.trim());

//		result[columnIndexes.length - start] = line.substring(beginIndex).trim();		
		return result;
	}

	public static List<String> split(String line, String separatorChars) {
		return split(line, separatorChars, 0);
	}

	/**
	 * Split a line to a String List by separator chars
	 * 
	 * @param line          the line
	 * @param columnIndexes indexes of columns
	 * 
	 * @return an String array which contains each words in the line.
	 */
	public static List<String> split(String line, String separatorChars, int limit) {
		List<String> result = new ArrayList<String>();
		int index = 0;
		int startIndex = -1;

		while (index < line.length()) {
			char c = line.charAt(index++);
			boolean isSep = separatorChars.indexOf(c) >= 0;

			if (isSep) {
				if (startIndex >= 0) {
					if (limit > 0 && result.size() == limit - 1) {
						String word = line.substring(startIndex);
						result.add(word);
						startIndex = -1;
						break;
					} else {
						String word = line.substring(startIndex, index - 1);
						result.add(word);
						startIndex = -1;
					}
				}
			} else {
				if (startIndex < 0)
					startIndex = index - 1;
			}

		}

		// process last words
		if (startIndex >= 0) {
			String word = line.substring(startIndex);
			if (word.length() > 0)
				result.add(word);
		}

		return result;
	}

	/** cut down the text between before and after words */
	public static String cut(String row, String before, String after) {
		int startIndex = before == null ? 0 : row.indexOf(before);
		if (startIndex >= 0) {
			startIndex = startIndex + (before == null ? 0 : before.length());
			int endIndex = after == null ? row.length() : row.indexOf(after);
			if (endIndex >= 0 && endIndex >= startIndex) {
				row = row.substring(startIndex, endIndex);
				return row;
			}
		}
		return null;
	}

	/** make text reach specified length */
	public static String toLength(String text, int length) {
		int len = length > 0 ? length : -length;
		if (text == null)
			text = "";

		while (text.length() < len) {
			if (length < 0)
				text = " " + text;
			else
				text += " ";

		}

		return text;
	}


	
}
