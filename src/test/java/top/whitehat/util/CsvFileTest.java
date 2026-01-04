package top.whitehat.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import org.junit.Test;

public class CsvFileTest {

	static File tempFile;

	@Test
	public void test() throws IOException {
		tempFile = File.createTempFile("test_", ".csv");
		try {
			write();
			read();
		} finally {
			if (tempFile.exists())
				tempFile.delete();
		}
	}

	void write() throws IOException {
		int count = 0;
		try (CsvFile f = CsvFile.writeFile(tempFile.getAbsolutePath())) {
			while (count < 1000) {
				String sn = "" + (count++);
				f.writeRow(sn, "say \"hello,world\"\r\nline " + sn);
			}
		}
	}

	void read() throws IOException {
		int count = 0;
		try (CsvFile f = CsvFile.readFile(tempFile.getAbsolutePath())) {
			String[] words;
			while ((words = f.readRow()) != null) {
				// printWords(words);
				assertEquals(words[0], "" + count);
				count++;
			}
		}
	}

	static void printWords(String[] words) {
		for (String word : words) {
			System.out.print(word);
			System.out.print("\t");
		}
		System.out.println();
		System.out.flush();
	}

}
