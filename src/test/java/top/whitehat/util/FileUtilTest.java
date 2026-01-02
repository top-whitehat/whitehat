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
import java.util.List;




public class FileUtilTest {

	
	static void findNmapScripts() throws IOException {
		String nmap = FileUtil.findFileInPath("nmap", true);
		if (nmap != null) {
			String nmapPath = FileUtil.getFilePath(nmap);
			String scriptPath = FileUtil.joinPath(nmapPath, "scripts");
			if (FileUtil.dirExists(scriptPath)) {
				System.out.println(scriptPath);
				List<String> fs = FileUtil.listFiles(scriptPath, "*.nse");
				for (String f : fs) {
					String filename = FileUtil.joinPath(scriptPath, f);
					String r = StrMatch.fromFile(filename).getWord("[[", "]]").text;

					System.out.println(FileUtil.getPureFilename(f));
					System.out.println(r == null ? "" : r.trim());
					System.out.println();
				}
			}
		}
	}

}
