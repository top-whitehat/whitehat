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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.nio.charset.StandardCharsets;

/**
 * FileUtil provides utility methods for working with files
 */
public class FileUtil {

	/** get full path from filename */
	public static String getFullPath(String filename) {
		return new File(filename).getAbsolutePath();
	}

	/** get file path */
	public static String getFilePath(String filename) {
		File file = new File(filename);
		return file.getParent() != null ? file.getParent() : ".";
	}

	/** get pure filename */
	public static String getPureFilename(String filepath) {
		File file = new File(filepath);
		String filename = file.getName();
		int lastDotIndex = filename.lastIndexOf('.');
		return lastDotIndex >= 0 ? filename.substring(0, lastDotIndex) : filename;
//		return Paths.get(filepath).getFileName().toString();
	}

	/** get file extension */
	public static String getFileExt(String fileName) {
		int lastDotIndex = fileName.lastIndexOf(".");

		if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
			return fileName.substring(lastDotIndex + 1);
		}

		return "";
	}

	/** Change file extension */
	public static String changeFileExt(String filename, String newExtension) {
		int lastDot = filename.lastIndexOf('.');
		if (lastDot > 0) {
			return filename.substring(0, lastDot) + "." + newExtension;
		} else {
			return filename + "." + newExtension;
		}
	}

	/**
	 * Search specified filename in PATH system env
	 * 
	 * @param fileName The file name
	 * 
	 * @return return filename if found. return null if not found.
	 */
	public static String findFileInPath(String fileName) {
		return findFileInPath(fileName, isWindowsExecutable(fileName));
	}

	/**
	 * Search specified filename in PATH system env
	 * 
	 * @param fileName     The file name
	 * @param isExecutable Indicate whether find executable file
	 * 
	 * @return return filename if found. return null if not found.
	 */
	public static String findFileInPath(String fileName, boolean isExecutable) {
		// get PATH env
		String pathEnv = System.getenv("PATH");
		if (pathEnv == null)
			return null;
		return findFileInPaths(pathEnv, fileName, isExecutable);

	}

	/**
	 * Search specified filename in PATH system env
	 * 
	 * @param paths        paths
	 * @param fileName     The file name
	 * @param isExecutable Indicate whether find executable file
	 * 
	 * @return return filename if found. return null if not found.
	 */
	protected static String findFileInPaths(String paths, String fileName, boolean isExecutable) {
		// get path separator depend on OS
		String pathSeparator = System.getProperty("path.separator", File.pathSeparator);

		// split PATH into array
		String[] pathDirs = paths.split(pathSeparator);

		// for each dir in the path array
		for (String dir : pathDirs) {
			dir = dir.trim();
			if (dir.length() > 0) {
				// check file with possible file extension
				String fname = findFile(dir, fileName, isExecutable);
				if (fname != null)
					return fname;
			}
		}

		return null;
	}

	/** Windows: file extension of executable file */
	private static String[] executableFileExts = null;

	/** Check whether current OS is Windows */
	protected static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}

	/** return file extension array of executable file */
	private static String[] getExecutableFileExts() {
		if (executableFileExts == null) {
			if (isWindows()) {
				// get PATHEXT env, which include executable file extensions
				String pathExt = System.getenv("PATHEXT");
				if (pathExt != null) {
					executableFileExts = pathExt.split(";");
				} else {
					executableFileExts = new String[] { ".EXE", ".COM", ".BAT", ".CMD" };
				}
			}
		}
		return executableFileExts;
	}

	/** Judge whether specified filename is executable in Windows */
	private static boolean isWindowsExecutable(String fileName) {
		if (isWindows()) {
			String extension = getFileExt(fileName);
			if (extension.length() > 0) {
				String[] exts = getExecutableFileExts();
				for (String ext : exts) {
					if (extension.equalsIgnoreCase(ext))
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Find file in specified directory
	 * 
	 * @param dir          the directory
	 * @param fileName     the file name
	 * @param isExecutable whether the file is executable
	 * @return return filename if found. return null if not found.
	 */
	private static String findFile(String dir, String fileName, boolean isExecutable) {
		File file = new File(dir, fileName);

		if (isExecutable) {
			boolean hasNoExt = getFileExt(fileName).length() == 0;

			// check filename without appending extension
			if (file.exists() && file.canExecute() && file.isFile()) {
				if (!(isWindows() && hasNoExt))
					return file.getAbsolutePath();
			}

			// In Windows, check filename with executable extension
			if (isWindows() && hasNoExt) {
				String[] extensions = getExecutableFileExts();
				if (extensions.length > 0) {
					for (String ext : extensions) {
						File fileWithExt = new File(dir, fileName + ext.toLowerCase());
						if (fileWithExt.exists() && fileWithExt.canExecute()) {
							return fileWithExt.getAbsolutePath();
						}
					}
				}
			}

			return null;

		} else {
			// return file.exists() && file.canExecute() == isExecutable ?
			// file.getAbsolutePath() : null;
			return file.exists() ? file.getAbsolutePath() : null;
		}
	}

	/** Join path and filename */
	public static String joinPath(String directory, String filename) {
		File dir = new File(directory);
		File file = new File(dir, filename);
		return file.getPath();
	}

	/** Check whether specified file exists */
	public static boolean fileExists(String filename) {
		File file = new File(filename);
		return file.exists() && file.isFile();
	}

	/** If specified file not exists, throw RuntimeException */
	public static void fileMustExist(String filename, String message) {
		if (!fileExists(filename)) {
			message = message != null ? message : "file %s not exists";
			if (message.contains("%s"))
				message = String.format(message, filename);
			throw new RuntimeException(message);
		}
	}

	/** Check whether specified directory exists */
	public static boolean dirExists(String path) {
		File file = new File(path);
		return file.exists() && file.isDirectory();
	}

	/** If specified directory not exists, throw RuntimeException */
	public static void dirMustExist(String filename, String message) {
		if (!dirExists(filename)) {
			message = message != null ? message : "file %s not exists";
			if (message.contains("%s"))
				message = String.format(message, filename);
			throw new RuntimeException(message);
		}
	}

	/**
	 * Find files in specified folder, return a List of filenames
	 * 
	 * @param folder  The folder to search
	 * @param pattern The filename or a pattern such as "*.txt"
	 * 
	 * @return List of filenames
	 * @throws IOException
	 */
	public static List<String> listResources(String folder, String pattern) throws IOException {
		return listResources(folder, pattern, false);
	}

	/**
	 * Find files in specified folder, return a List of filenames
	 * 
	 * @param folder    The folder to search
	 * @param pattern   The filename or a pattern such as "*.txt"
	 * @param recursive Indicate whether recursive sub-directories
	 * 
	 * @return List of filenames
	 * @throws IOException
	 */
	public static List<String> listResources(String folder, String pattern, boolean recursive) throws IOException {
		List<String> result = new ArrayList<String>();

		if (folder == null)
			folder = "";

		URL resourceUrl = FileUtil.class.getResource(folder);
		if (resourceUrl == null)
			return result;

		URI uri;
		try {
			uri = resourceUrl.toURI();
		} catch (URISyntaxException e) {
			throw new IOException(e.getMessage());
		}

		Path path;

		// Judge whether the resource is in a jar
		if (uri.getScheme().equals("jar")) {
			// for jar file, perform special process for file system
			try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
				path = fileSystem.getPath(folder);
				listFiles(path.toString(), pattern, recursive, result);
			}
		} else {
			// for normal file system
			path = Paths.get(uri);
			listFiles(path.toString(), pattern, recursive, result);
		}

		return result;
	}

	/**
	 * get filename list in specified path
	 * 
	 * @param path            The path to search
	 * @param filenamePattern The filename pattern, such as "*.txt"
	 * @param recursive       Whether search sub-directory
	 * @return
	 * @throws IOException
	 */
	public static List<String> listFiles(String dir, String filenamePattern) {
		return listFiles(dir, filenamePattern, false);
	}

	/**
	 * get filename list in specified path
	 * 
	 * @param path            The path to search
	 * @param filenamePattern The filename pattern, such as "*.txt"
	 * @param recursive       Whether search sub-directory
	 * @return
	 * @throws IOException
	 */
	public static List<String> listFiles(String dir, String filenamePattern, boolean recursive) {
		return listFiles(dir, filenamePattern, recursive, null);
	}

	/**
	 * for each file in specified directory, call handler function
	 * 
	 * @param filePathPattern file pathname with pattern, such as "/path/to/*.java"
	 * @param handler         handler function to process File object
	 * 
	 * @throws IOException
	 */
	public static void forEachFile(String filePathPattern, Consumer<File> handler) throws IOException {
		String dir = FileUtil.getDir(filePathPattern);
		String namePattern = FileUtil.getFileName(filePathPattern);
		Path path = Paths.get(dir);
		int dirDepth = 99999;

		PathMatcher matcher = (namePattern == null || namePattern.length() == 0) ? null
				: FileSystems.getDefault().getPathMatcher("glob:" + namePattern);

		// use files.walk to iterate the files under the path
		try (Stream<Path> paths = Files.walk(path, dirDepth)) {
			paths.filter(Files::isRegularFile) //
					.filter(filePath -> matcher == null || matcher.matches(filePath.getFileName())) // Match
					.forEach(filePath -> {
						handler.accept(filePath.toFile());
					});
		}

	}

	/**
	 * get filename list in specified path
	 * 
	 * @param path            The path to search
	 * @param filenamePattern The filename pattern, such as "*.txt"
	 * @param recursive       Whether search sub-directory
	 * @param list            The List to store result.
	 * 
	 * @return
	 * @throws IOException
	 */
	private static List<String> listFiles(String dir, String filenamePattern, boolean recursive, List<String> list) {
		Path path = Paths.get(dir);
		List<String> result = list == null ? new ArrayList<String>() : list;

		PathMatcher matcher = (filenamePattern == null || filenamePattern.length() == 0) ? null
				: FileSystems.getDefault().getPathMatcher("glob:" + filenamePattern);

		boolean filenameOnly = recursive == false;
		int dirDepth = recursive ? 999999 : 1;

		// use files.walk to iterate the files under the path
		try {
			try (Stream<Path> paths = Files.walk(path, dirDepth)) {
				paths.filter(Files::isRegularFile) // filter normal files
						.filter(filePath -> matcher == null || matcher.matches(filePath.getFileName())) // Match
																										// filename
						.forEach(filePath -> {
							// add filename to list
							String filename = filenameOnly ? filePath.getFileName().toString() : filePath.toString();
							result.add(filename);
						});
			}
		} catch (Exception e) {

		}

		return result;
	}

	/**
	 * Get user directory
	 * 
	 * @return
	 */
	public static String getUserDir() {
		return System.getProperty("user.dir");
	}

	/**
	 * Get the directory of .jar file which include the specified class
	 * 
	 * @return
	 */
	public static String getJarDir(Class<?> cls) {
		if (cls == null)
			cls = FileUtil.class;

		// get the class path
		String path = cls.getProtectionDomain().getCodeSource().getLocation().getPath();

		boolean isWindows = System.getProperty("os.name").contains("indows");

		// if it is Windows, remove the first char '/'
		if (isWindows && path.startsWith("/"))
			path = path.substring(1, path.length());

		// if the path has ".jar", means it is a jar file
		if (path.contains("jar")) {
			// cut the file extension
			path = path.substring(0, path.lastIndexOf("."));
			// get the directory of the file
			path = path.substring(0, path.lastIndexOf("/"));
			// if it is windows, and the path is like "C:", append char '\'
			if (path.endsWith(":") && isWindows)
				path += "/";
		} else {
			// cut the last '/'
			if (path.endsWith("/"))
				path = path.substring(0, path.length() - 1);
		}

		// if it is windows, convert path delimiter
		if (isWindows)
			path = path.replace('/', '\\');

		return path;
	}

	/**
	 * Get file extension
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getExension(String fileName) {
		if (fileName != null) {
			int index = fileName.lastIndexOf('.');
			if (index >= 0) {
				return fileName.substring(index);
			} else {
				return "";
			}
		}

		return fileName;
	}

	/**
	 * Get directory of the file name
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getDir(String fileName) {
		if (fileName != null) {
			int index = fileName.lastIndexOf(File.separator);
			if (index >= 0) {
				return fileName.substring(0, index);
			} else {
				return fileName;
			}
		}

		return null;

	}

	/** Check whether the filename is from http */
	private static boolean isHttp(String filename) {
		if (filename != null) {
			if (filename.startsWith("http://") || filename.startsWith("https://"))
				return true;
		}
		return false;
	}

	/** Check whether the filename is from resource */
	private static boolean isResource(String filename) {
		if (filename != null) {
			if (filename.startsWith("res://"))
				return true;
		}
		return false;
	}

	/** get string after protocol */
	private static String afterProtocol(String filename) {
		int pos = filename.indexOf("://");
		return pos >= 0 ? filename.substring(pos + 3) : filename;
	}

	/**
	 * Get file name without path
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileName(String fileName) {
		String separator = File.separator;

		if (isHttp(fileName)) {
			separator = "/";
			fileName = fileName.substring(fileName.indexOf("//") + 2);
			int pos = fileName.lastIndexOf("?");
			if (pos > 0)
				fileName = fileName.substring(0, pos);
		}

		if (fileName != null) {
			int index = fileName.lastIndexOf(separator);
			if (index >= 0) {
				return fileName.substring(index + 1);
			} else {
				return fileName;
			}
		}

		return null;

	}

	/**
	 * Get file name without extension
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileNameWithoutExt(String fileName) {
		fileName = getFileName(fileName);

		if (fileName != null) {
			int index = fileName.lastIndexOf('.');
			if (index >= 0) {
				return fileName.substring(0, index);
			} else {
				return fileName;
			}
		}

		return null;
	}

	/**
	 * Connect several elements to a file name
	 * 
	 * @param args
	 * @return
	 */
	public static String join(String... args) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (i > 0 && !args[i - 1].endsWith(File.separator))
				builder.append(File.separator);
			builder.append(args[i]);
		}
		return builder.toString();
	}

	/**
	 * Save string to the specified file
	 * 
	 * @param filename    The file name
	 * @param content     The content to save

	 * @throws IOException
	 */
	public static void saveToFile(String filename, String content) throws IOException {
		saveToFile(filename, content, false);
	}

	/**
	 * Save string to the specified file
	 * 
	 * @param filename    The file name
	 * @param content     The content to save
	 * @param isApppend   Indicate whether append to existing file
	 * 
	 * @throws IOException
	 */
	public static void saveToFile(String filename, String content, boolean isAppend) throws IOException {
		if (isResource(filename)) throw new IOException("cannot save file to resource");
		if (isHttp(filename)) throw new IOException("cannot save file to http");
		
		if (content == null)
			content = "";

		File file = new File(filename);
		if (!file.exists()) {
			if (file.getParentFile() != null)
				file.getParentFile().mkdirs();
			file.createNewFile();
		}

		FileWriter writer = null;
		BufferedWriter bufferedWriter;
		try {
			writer = new FileWriter(file, isAppend);
			bufferedWriter = new BufferedWriter(writer);
			bufferedWriter.write(content);
			bufferedWriter.flush();
			bufferedWriter.close();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}


	/** open file as input stream */
	public static InputStream openInputStream(String filename) throws IOException {
		if (isResource(filename)) {
			// open input stream reader from resource
			ClassLoader loader = ClassLoader.getSystemClassLoader();
			filename = afterProtocol(filename);
			return loader.getResourceAsStream(filename);
		} else if (isHttp(filename)) {
			// open input stream reader from http
			@SuppressWarnings("resource")
			HttpUtil h = new HttpUtil();
			return h.request("GET", filename, null).getBodyStream();
		} else {
			// open input stream reader from file
			if (!new File(filename).exists())
				throw new FileNotFoundException("file '" + filename + "' not exists");
			return new FileInputStream(filename);
		}
	}

	/**
	 * Load string from the specified file name
	 * 
	 * @param fileName The file name
	 * @param charset  The Charset
	 * 
	 * @return the file content string
	 * @throws IOException
	 */
	public static String loadFromFile(String filename, String charset) throws IOException {
		Charset cset = charset != null ? Charset.forName(charset) : StandardCharsets.UTF_8;

		try (InputStream in = openInputStream(filename);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, cset))) {
			StringBuilder builder = new StringBuilder();
			char[] buffer = new char[4096];
			int bytes = 0;

			do {
				bytes = reader.read(buffer);
				if (bytes > 0)
					builder.append(buffer, 0, bytes);
			} while (bytes != -1);

			return builder.toString();
		}
	}

	/**
	 * Load string from the specified file name
	 * 
	 * @param fileName The file name
	 * @return the file content string
	 * @throws IOException
	 */
	public static String loadFromFile(String filename) throws IOException {
		return loadFromFile(filename, null);
	}

	/** Load specified bytes from file
	 * 
	 * @param filename  The filename
	 * @param offset    Start offset
	 * @param length    copy byte length. set value to -1 means copy to the end of the file.
	 * 
	 * @return  byte array
	 * @throws IOException
	 */
	public static byte[] loadBinaryFile(String filename, int offset, int length) throws IOException {
		if (offset < 0)
			throw new IllegalArgumentException("offset is negative");
		
		if (length == 0)
			return new byte[0];

		try (InputStream inputStream = openInputStream(filename); //
				ByteArrayOutputStream outStream = new ByteArrayOutputStream(0)) {
			byte[] buffer = new byte[4096];
			int index = 0;
			int len = 0;
			int bytes = 0;

			do {
				bytes = inputStream.read(buffer);
				for (int i = 0; i < bytes; i++) {
					if (index >= offset) {
						if (length < 0 || len < length) {
							outStream.write(buffer, i, 1);
							len++;
						} else {
							bytes = -1;
							break;
						}
					}
					index++;
				}
			} while (bytes != -1);

			return outStream.toByteArray();
		}
	}

	/** Load all bytes from the file */
	public static byte[] loadBinaryFile(String filename) throws IOException {
		try (InputStream inputStream = openInputStream(filename); //
				ByteArrayOutputStream outStream = new ByteArrayOutputStream(0)) {
			byte[] buffer = new byte[4096];
			int bytes = 0;

			do {
				bytes = inputStream.read(buffer);
				if (bytes > 0)
					outStream.write(buffer, 0, bytes);
			} while (bytes != -1);

			return outStream.toByteArray();
		}
	}

	/** Save bytes to file
	 * 
	 * @param filename    The file name
	 * @param startIndex  The start position of the file
	 * @param bytes       The byte arrays
	 * @param offset      Start offset in the byte array 
	 * @param length      Length of data to write
	 * 
	 * @throws IOException
	 */
	public static void saveBinaryFile(String filename, int startIndex, byte[] bytes, int offset, int length) throws IOException {
		if (isResource(filename)) throw new IOException("cannot save file to resource");
		if (isHttp(filename)) throw new IOException("cannot save file to http");
		if (offset < 0 || offset + length > bytes.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		
		try (RandomAccessFile raf = new RandomAccessFile(filename, "rw")) {
            if (startIndex > 0) raf.seek(startIndex);
            if (length < 0) length = bytes.length;
            raf.write(bytes, offset, length);

        }
	}

	/** Save bytes to file */
	public static void saveBinaryFile(String filename, byte[] bytes) throws IOException {
		if (isResource(filename)) throw new IOException("cannot save file to resource");
		if (isHttp(filename)) throw new IOException("cannot save file to http");
		
		try (OutputStream outputStream = new FileOutputStream(filename)) {
			outputStream.write(bytes);
		};
	}
}
