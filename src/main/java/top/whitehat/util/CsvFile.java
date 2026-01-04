package top.whitehat.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class CsvFile implements AutoCloseable {

	/** A parser for CSV line */
	protected static class LineParser {		
		public char SEPARATOR = ',';
		public char QUOTE = '"';
		public char ESCAPE = '\\';
		
		public LineParser() {
			
		}
				
		public LineParser(char seperator) {
			if (seperator != '\0')
				this.SEPARATOR = seperator;			
		}

		/** Encode a string */
		private static String encode(String word) {
			StringBuilder sb = new StringBuilder();
			boolean quote = false;
			for(int i=0; i<word.length(); i++) {
				char c = word.charAt(i);
				switch(c) {
				case ',':
				case '\"':					
					if (!quote) sb.insert(0, '"');
					quote = true;	
					sb.append('\\').append(c);									
					break;
				case '\r':				
				case '\n':
					if (!quote) sb.insert(0, '"');
					quote = true;	
					String w = (c == '\r') ? "\\r"  : "\\n";
					sb.append(w);
					break;
				default:
					sb.append(c);
				}				
			}
			if (quote) sb.append('"');
			return sb.toString();
		}
		
		/** Convert list of words to a line */
		public String join(List<String> words) {
			StringBuilder sb = new StringBuilder();
			for(String word: words) {
				if (sb.length() > 0) sb.append(",");
				sb.append(encode(word));
			}
			return sb.toString();
		}
		
		/** Convert words to a line */
		public String join(String... words) {
			StringBuilder sb = new StringBuilder();
			for(String word: words) {
				if (sb.length() > 0) sb.append(",");
				sb.append(encode(word));
			}
			return sb.toString();
		}

		/** Split line into List of words */
		public List<String> split(String line) {
			int offset = 0;
			int length = line.length();
			List<String> fields = new ArrayList<String>();
			fields.clear();
			while (offset < length) {
				offset = readWord(line, offset, length, fields);
			}
			return fields;
		}

		/** Read a word from the line at specified offset
		 *  
		 * @param line
		 * @param offset   The position to start
		 * @param length   The length of the line
		 * @param words   The List that hold the word
		 * @return  return true if success.
		 */
		private int readWord(String line, int offset, int length, List<String> words) {
			if (offset >= length)
				return offset;

			StringBuilder sb = new StringBuilder();
			boolean hasQuote = false;

			while (offset < length) {
				char c = line.charAt(offset++);
				if (!hasQuote) {
					if (c == QUOTE && sb.length() == 0) {
						hasQuote = true;
					} else if (c == SEPARATOR) {
						break;
					} else {
						sb.append(c);
					}
				} else {
					if (c == ESCAPE) {
						if (offset < length -1) {
							c = line.charAt(offset++);
							switch(c) {
							case 'r':
								sb.append('\r');
								break;
							case 'n':
								sb.append('\n');
								break;
							case ',':
							case '"':
								sb.append(c);
								break;
							case '\\':
								sb.append('\\');
								break;
							default:
								sb.append(ESCAPE);
								sb.append(c);
								break;
							}
						} else {
							sb.append(c);
							break;
						}
						
					} else if (c == QUOTE) {
						offset++;
						break;
					} else {
						sb.append(c);
					}
				}
			}

			words.add(sb.toString());
			return offset;
		}

	}

	private static LineParser defaultParser;
	
	/** Return default Parser */
	private static LineParser getParser() {
		if (defaultParser == null) defaultParser = new LineParser();
		return defaultParser;
	}
	
	/**
	 * Split CSV line to List of words
	 * 
	 * @param line The line string in CSV format
	 * 
	 * @return List
	 */
	public static List<String> splitLine(String line) {
		return splitLine(line, -1);
	}

	/**
	 * Split CSV line to List of words
	 * 
	 * @param line  The line string in CSV format
	 * @param limit Limitation of words
	 * 
	 * @return List
	 */
	public static List<String> splitLine(String line, int limit) {
		List<String> words = getParser().split(line);		
		if (limit > 0) {
			while (words.size() > limit) {
				words.remove(words.size() - 1);
			}
		}
		return words;
	}

	/**
	 * Connect words into a line string in CSV format
	 * 
	 * @param words
	 * @return
	 */
	public static String joinLine(List<String> words) {
		return getParser().join(words);
	}
	
	/** Create CsvFile object to read file 
	 * @throws FileNotFoundException */
	@SuppressWarnings("resource")
	public static CsvFile readFile(String filename) throws IOException {
		return new CsvFile().openRead(filename); 
	}
	
	/** Create CsvFile object to write file 
	 * @throws IOException */
	@SuppressWarnings("resource")
	public static CsvFile writeFile(String filename) throws IOException {
		return new CsvFile().openWrite(filename); 
	}

	//-------------- members --------------------------------
	
	private BufferedReader reader;
	
	private BufferedWriter writer;
	
	private LineParser parser;

	/** Create a CsvFile object */
	public CsvFile() {
		parser = new LineParser();
	}
	
	/** Get the separator char */
	public char separator() {
		return parser.SEPARATOR;
	}
	
	/** Set the separator char */
	public CsvFile separator(char c) {
		parser.SEPARATOR = c;
		return  this;
	}
	
	/** Open file to read 
	 * @throws IOException */
	public CsvFile openRead(String filename) throws IOException {
		return openRead(filename, null);
	}
	
	
	protected boolean isResourceFile(String filename) {
		return filename.startsWith("res://");
	}
	

	/** Open file to read 
	 * @throws IOException */
	public CsvFile openRead(String filename, String charset) throws IOException {	
		if (charset == null) charset = "utf-8";	
		Charset cset = Charset.forName(charset);
		
		if (isResourceFile(filename)) {
			// load from resource
			ClassLoader loader = ClassLoader.getSystemClassLoader(); 
			filename = filename.substring(6);
			InputStream in = loader.getResourceAsStream(filename);		
			reader = new BufferedReader(new InputStreamReader(in, cset));
		} else {
			// load from file
			if (!new File(filename).exists())
				throw new FileNotFoundException("file '" + filename + "' not exists");
			InputStream in = new FileInputStream(filename);
			reader = new BufferedReader(new InputStreamReader(in, cset));
		}
		return this;
	}
	
	/** Open file to write. 
	 *  If the file not exist, create it.
	 *  If the file already exist, append content to it.
	 * 
	 * @param filename  the file name

	 * @return this
	 * 
	 * @throws IOException
	 */
	public CsvFile openWrite(String filename) throws IOException {
		return openWrite(filename, null);
	}
			

	/** Open file to write. 
	 *  If the file not exist, create it.
	 *  If the file already exist, append content to it.
	 * 
	 * @param filename  the file name
	 * @param charset   the charset name
	 * 
	 * @return this
	 * 
	 * @throws IOException
	 */
	public CsvFile openWrite(String filename, String charset) throws IOException {
		if (isResourceFile(filename)) {
			throw new IOException("cannot write to resource " + filename);
		}
		File file = new File(filename);
		boolean append = !file.exists();
		if (charset == null) charset = "utf-8";
		Charset cset = Charset.forName(charset);
		OutputStream out = new FileOutputStream(filename, append);
		writer = new BufferedWriter(new OutputStreamWriter(out, cset));
		return this;
	}

	/** Read a row from the file
	 * 
	 * @return return word array, return null if end of file is met 
	 * @throws IOException
	 */
	public String[] readRow() throws IOException {
		String line = reader.readLine();
		if (line == null)
			return null;
		List<String> words = parser.split(line);
		return words == null ? null : words.toArray(new String[words.size()]);
	}

	/** Write a row to the file */
	public CsvFile writeRow(List<String> words) throws IOException {
		String line = parser.join(words);
		writer.write(line);
		writer.newLine();
		return this;
	}
	
	/** Write a row to the file */
	public CsvFile writeRow(String... words) throws IOException {
		String line = parser.join(words);
		writer.write(line);
		writer.newLine();
		return this;
	}
	
	/** Flushes the stream. */
	public CsvFile flush() throws IOException {
		if (writer != null)
			writer.flush();
		return this;
	}

	/** Close the file */
	public void close() {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				// nothing to do
			} finally {
				reader = null;
			}
		}

		if (writer != null) {
			try {
				writer.flush();
			} catch (IOException e) {
			}

			try {
				writer.close();
			} catch (IOException e) {
				// nothing to do
			} finally {
				writer = null;
			}
		}
	}

}
