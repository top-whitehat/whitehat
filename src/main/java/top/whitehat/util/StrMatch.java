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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StrMatch {

	public static class Match {
		public int offset;
		public int length;
		public String text;

		public Match(int offset, int length) {
			this.offset = offset;
			this.length = length;
		}

		public Match() {
			// TODO Auto-generated constructor stub
		}

		public int end() {
			if (offset < 0)
				throw new RuntimeException("Not matched: offset < 0");
			return offset + length;
		}

		public Match of(String s) {
			if (offset < 0)
				throw new RuntimeException("Not matched: offset < 0");
			this.text = extract(s);
			return this;
		}

		public String toString() {
			return String.format("[offset=%d, length=%d, text=%s]", offset, length,
					text == null ? "null" : "'" + text + "'");
		}

		public String extract(String s) {
			if (offset >= 0 && offset < s.length()) {
				return s.substring(offset, offset + length);
			}
			return "";
		}

		public boolean find() {
			return offset >= 0;
		}
	}

	/**
	 * Create Str object from reading the file content
	 * 
	 * @throws IOException
	 */
	public static StrMatch fromFile(String fileName) throws IOException {
		return new StrMatch(FileUtil.loadFromFile(fileName));
	}

	/** /** Create Str object from string */
	public static StrMatch of(String s) {
		return new StrMatch(s);
	}

	protected String text;

	public StrMatch(String s) {
		this.text = s;
	}
	
	public String getText() {
		return text;
	}
	
	public StrMatch setText(String text) {
		this.text = text;
		return this;
	}

	/**
	 * Match the finding conditions in the string, return matched offset, length.
	 * 
	 * @param text         the string
	 * @param findings    List of finding conditions, each condition could be a
	 *                    string or a regex (format is /regex/)
	 * @param startOffset the offset to start finding.
	 * @return return Match object that contains offset, length. If not found,
	 *         offset is -1.
	 */
	public Match matchOffsetLength(List<String> findings, int startOffset) {
		if (findings == null || findings.size() == 0)
			return new Match(startOffset, 0).of(text);
		if (text == null)
			return new Match(-1, 0);

		int offset_begin = -1;
		int offset_length = 0;

		int length = 0;
		int offset = -1;

		int startIndex = 0;
		boolean isOr = false;

		// if the first finding is '||', it means findings are OR relations
		if (findings.size() > 0 && "||".equalsIgnoreCase(findings.get(0))) {
			isOr = true;
			startIndex = 1;
		}

		// for each finding item
		for (int i = startIndex; i < findings.size(); i++) {
			String item = findings.get(i);
			if (item == null || item.length() == 0)
				continue;

			// search for a item
			Pattern pattern = TextUtil.getPattern(item);
			if (pattern != null) {
				// the item is a pattern
				Matcher m = pattern.matcher(text);
				if (m.find(startOffset)) {
					// pattern is found
					offset = m.start();
					length = m.end() - m.start();
					offset_begin = (offset_begin < 0 && offset >= 0) ? offset : offset_begin;
				} else {
					// pattern is not found
					offset = -1;
					length = 0;
				}
			} else {
				// the item is a string
				offset = text.indexOf(item, startOffset);
				length = offset >= 0 ? item.length() : 0;
				offset_begin = (offset_begin < 0 && offset >= 0) ? offset : offset_begin;
			}

			// after search
			if (isOr) {
				if (offset >= 0) {
					if (offset <= offset_begin) {
						offset_begin = offset;
						offset_length = length;
					}
				}
			} else {
				if (offset < 0) {
					return new Match(-1, 0);
				} else {
					startOffset = offset + length;
				}
			}
		}

		// after all item is searched
		if (isOr) {
			return new Match(offset_begin, offset_length).of(text);
		} else {
			return new Match(offset_begin, startOffset - offset_begin).of(text);
		}

	}

	/**
	 * Whether the string match the finding conditions.
	 * 
	 * @param text         the string
	 * @param findings    List of finding conditions, each condition could be a
	 *                    string or a regex (format is /regex/)
	 * @param startOffset the offset to start finding.
	 * @return return true if the str match the conditions. return false if not
	 *         match.
	 */
	public boolean match(List<String> findings, int startOffset) {
		Match m = matchOffsetLength(findings, startOffset);
		return m.offset >= 0;
	}

	/**
	 * Get a word from text, the word is between before condition, and after
	 * condition.
	 * 
	 * @param text    the string
	 * @param before the texts which is before the finding word
	 * @param after  the texts which is after the finding word
	 * @param begin  the texts indicate the offset to start finding.
	 * 
	 * @return return Match object that contains text, offset, length
	 */
	public Match getWord(List<String> before, List<String> after, List<String> begin) {
		int startOffset = 0;

		// convert begin to startOffset
		if (begin != null && begin.size() > 0) {
			Match m = matchOffsetLength(begin, startOffset);
			if (!m.find())
				return null;
			startOffset = m.offset + m.length;
		}

		return getWord(before, after, startOffset);
	}
	
	public List<String> toList(String s) {
		return toList(s, "|");
	}
	
	public List<String> toList(String s, String regex) {
		String[] arr =  s.split(regex);
		List<String> ret = new ArrayList<String>();
		for(String word : arr) ret.add(word);
		return ret;
	}
	
	/** return the position before specified word */
	public int before(String word) {
		if (word == null || word.length() == 0) return 0;
		Match m = matchOffsetLength(toList(word), 0);
		if (!m.find()) throw new RuntimeException("'" + word + "' not found");
		return m.offset;
	}
	
	/** return the position after specified word */
	public int after(String word) {
		if (word == null || word.length() == 0) return 0;
		Match m = matchOffsetLength(toList(word), 0);
		if (!m.find()) throw new RuntimeException("'" + word + "' not found");
		return m.end();
	}
	

	/**
	 * Get a word from text, the word is between before condition, and after
	 * condition.
	 * 
	 * @param text         the string
	 * @param before      the texts which is before the finding word
	 * @param after       the texts which is after the finding word
	 * @param startOffset the offset to start finding.
	 * 
	 * @return return Match object that contains text, offset, length
	 */
	public Match getWord(List<String> before, List<String> after, int startOffset) {
		Match start = matchOffsetLength(before, startOffset);
		
		if (start.find()) {
			startOffset = start.offset + start.length;
			Match end = matchOffsetLength(after, startOffset);
			if (end.offset >= startOffset) {
				Match found = new Match(startOffset, end.offset - startOffset).of(text);
				return found;
			}
		}

		return new Match(-1, 0);
	}
	
	/**
	 * Get a word from text, the word is between before condition, and after
	 * condition.
	 * 
	 * @param text         the string
	 * @param before      the texts which is before the finding word
	 * @param after       the texts which is after the finding word
	 * @param startOffset the offset to start finding.
	 * 
	 * @return return Match object that contains text, offset, length
	 */
	public Match getWord(String before, String after, int startOffset) {
		return getWord(toList(before), toList(after), startOffset);
	}
	
	
	/**
	 * Get a word from text, the word is between before condition, and after
	 * condition.
	 * 
	 * @param text         the string
	 * @param before      the texts which is before the finding word
	 * @param after       the texts which is after the finding word
	 * 
	 * @return return Match object that contains text, offset, length
	 */
	public Match getWord(String before, String after) {
		return getWord(before, after, 0);
	}

	/** get a list of keywords from the text.
	 * 
	 * @param begin    begin condition
	 * @param betweens words definition list, each item is a keyword definition which contains a before and after condition.
	 * @param end  (optional) end condition
	 * @return
	 */
	public List<List<String>> getWordList(List<String> begin, //
			List<List<String>> betweens, List<String> end) {
		List<List<String>> result = new ArrayList<List<String>>();

		// start offset
		Match m = matchOffsetLength(begin, 0);
		int offset = m.end();

		// end offset
		int end_offset = text.length();
		m = matchOffsetLength(end, 0);
		if (m.offset >= 0) end_offset = m.offset;
		
		// how many word per row
		int wordsPerRow = betweens.size() / 2;
		
		// search
		while (offset >= 0) {
			List<String> row = new ArrayList<String>();
			
			// search one row
			int i = 0;
			while (i < betweens.size() - 1) {
				List<String> before = betweens.get(i);
				List<String> after = betweens.get(i + 1);
				i += 2;
				
				m = getWord(before, after, offset);
				offset = m.offset;
				
				if (end_offset > 0 && offset >= end_offset) {
					m.text = null;
					offset = -1;
				}
				
				if (m.text != null) {
					row.add(m.text);
				} else {
					offset = -1;
					break;
				}
				
				// if words size match one row's word count 
				if (row.size() == wordsPerRow) break;
			}
			
			// add one row to result
			if (row.size()>0) result.add(row);
		}

		return result;
	}

	
	/** get a list of keywords from the text.
	 * 
	 * @param begin    begin condition
	 * @param betweens words definition list, each item is a keyword definition which contains a before and after condition.
	 * @param end  (optional) end condition
	 * @return
	 */
	public List<List<String>> getWordList(String begin, //
			List<String> betweens, String end) {
		List<List<String>> bs = new ArrayList<List<String>>();
		for(String word : betweens) {
			bs.add(toList(word));
		}
		
		return getWordList(toList(begin), bs, toList(end));
	}
}
