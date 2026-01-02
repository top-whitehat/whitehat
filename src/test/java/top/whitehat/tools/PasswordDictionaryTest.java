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
package top.whitehat.tools;

import java.io.IOException;




public class PasswordDictionaryTest {

	/**
	 * Example usage: - Generate candidates with length 6–12 - Preview the first 20
	 * - Write all candidates to passwords.txt
	 */
	public static void main(String[] args) {
		int minLen = 6;
		int maxLen = 8;
		String outFile = "passwords.txt";

		Dictionary dict = new Dictionary(minLen, maxLen);
		dict.generate();
		dict.preview(2000);

		try {
			dict.writeToFile(outFile);
			System.out.printf("Wrote %d to %s%n", dict.size(), outFile);
		} catch (IOException e) {
			System.err.println("Failed to write dictionary: " + e.getMessage());
		}
	}
}
