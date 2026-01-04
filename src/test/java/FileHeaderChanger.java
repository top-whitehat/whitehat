import java.io.File;
import java.io.IOException;


import top.whitehat.util.FileUtil;

public class FileHeaderChanger {
    private static int fileCount = 0;
    private static int processedFileCount = 0;
    
	public static void main(String[] args) throws IOException {
		
		// Get target directory from args or use current directory
		String targetPath = (args.length > 0) ? args[0] : ".";

		String filepathName = new File(targetPath, "*.java").toString();
		
		// header content
		String header = "/*\r\n"
				+ " * Copyright 2026 The WhiteHat Project\r\n"
				+ " *\r\n"
				+ " * The WhiteHat Project licenses this file to you under the Apache License,\r\n"
				+ " * version 2.0 (the \"License\"); you may not use this file except in compliance\r\n"
				+ " * with the License. You may obtain a copy of the License at:\r\n"
				+ " *\r\n"
				+ " *   https://www.apache.org/licenses/LICENSE-2.0\r\n"
				+ " *\r\n"
				+ " * Unless required by applicable law or agreed to in writing, software\r\n"
				+ " * distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT\r\n"
				+ " * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the\r\n"
				+ " * License for the specific language governing permissions and limitations\r\n"
				+ " * under the License.\r\n"
				+ " */\r\n";
		
		fileCount = 0;
		processedFileCount = 0;
		
		// for each file in directory
		FileUtil.forEachFile(filepathName, file->{
			try {
				fileCount++;
				System.out.println(file);
				
				// read file content
				String content = FileUtil.loadFromFile(file.toString());
				
				// find "package " declaration
				int offset = findPackageDeclare(content);
				
				// change header before "package " declaration
				if (offset >= 0) {
					content = header + content.substring(offset);
					FileUtil.saveToFile(file.getAbsolutePath(), content);
					processedFileCount++;
				}
				
			} catch (IOException e) {
				System.err.println(e);
			}
		});
		
		System.out.printf("total files: %d, processed files: %d ", fileCount, processedFileCount);
	}
	
	// find "package " declaration
	static int findPackageDeclare(String content) {		
		int offset =  content.indexOf("\npackage ");				
		if (offset < 0) {
			if (content.startsWith("package ")) offset = 0;
		} else {
			offset++;
		}
		return offset;
	}
	

    
}
