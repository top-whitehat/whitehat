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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;


/**
 * FileUtil provides utility methods for working with files
 */
public class HttpUtil {
	
	/** return URL encoded string 
	 * @throws UnsupportedEncodingException */
	public static String urlencode(String s) throws UnsupportedEncodingException {
		return URLEncoder.encode(s,  "utf-8");
	}
	
	/** return HTML encoded string */
	public static String htmlEncode(String input) {
	    if (input == null) return null;
	    StringBuilder sb = new StringBuilder();
	    int length = input.length();
	    int i = 0;
	    while (i <length) {
	    	char c = input.charAt(i++);	    	
	        switch (c) {
	            case '<': sb.append("&lt;"); break;
	            case '>': sb.append("&gt;"); break;
	            case '&': sb.append("&amp;"); break;
	            case '"': sb.append("&quot;"); break;
	            case '\'': sb.append("&apos;"); break;
	            case '\r':
	            	if (i < length && input.charAt(i) == '\n') {
	            		i++;
	            		sb.append("<br>\r\n");
	            	} else {
	            		sb.append(c);
	            	}
	            	break;	            
	            case '\n':
	            	sb.append("<br>\n");
	            	break;
	            case '\t':
	            	sb.append("&#9;");
	            	break;
	            case ' ':
	            	if (i < length && input.charAt(i) == ' ') {
	            		i++;
	            		sb.append("&nbsp;&nbsp;");
	            	} else {
	            		sb.append(c); 
	            	}
	            	break;
	            default: sb.append(c);
	        }
	    }
	    return sb.toString();
	}
	
	/**
	 * Get string from specified URL
	 *
	 * @param url The complete URL string
	 * 
	 * @return return content string if success, return null if failed.
	 * 
	 */
	public static String getUrl(String url) {
		try {
			return fetch("GET", url, null, null, null);
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Get from specified url
	 *
	 * @param url The complete URL string
	 * 
	 * @return content string
	 * 
	 * @throws IOException              if there are network connectivity issues,
	 *                                  invalid URL format, or problems with reading
	 *                                  the response data
	 * @throws URISyntaxException
	 * @throws IllegalArgumentException if the urlString is null or empty
	 */
	public static String get(String url) throws IOException, URISyntaxException {
		return fetch("GET", url, null, null, null);
	}
	
	public static JSON getJSON(String url, Map<String, String>headers) throws IOException {
		String ret = fetch("GET", url, headers, null, null);
		return JSON.parse(ret);
	}
	
	public static JSON postJSON(String url, JSON data, Map<String, String>headers) throws IOException {
		String ret = fetch("POST", url, headers, data.toString(), null);
		return JSON.parse(ret);
	}

	/**
	 * Download file from specified url
	 *
	 * @param url           The complete URL string
	 * @param saveFilename  The filename to save
	 * @param headers       The request headers
	 * 
	 * @throws IOException              if there are network connectivity issues,
	 *                                  invalid URL format, or problems with reading
	 *                                  the response data
	 * @throws URISyntaxException
	 * @throws IllegalArgumentException if the urlString is null or empty
	 */
	public static void download(String url, String saveFilename, NotifyEventHandler evt) {
		Promise promise = new Promise((resolve, reject) -> {
			String fname = null;
			try {
				fname = fetch("GET", url, null, null, saveFilename);
				resolve.accept(fname);
			}catch (Exception e) {
				reject.accept(e);
			}
		});
		
		promise.then(e->{
			evt.onNotify(e);
			return e;
		}).catchError(e->{
			evt.onNotify(e);
			return e;
		});
	}
	
	/**
	 * POST data to specified url
	 *
	 * @param url  The complete URL string
	 * @param data The data to post
	 * @param headers      The request headers
	 * 
	 * @return response string
	 * 
	 * @throws IOException              if there are network connectivity issues,
	 *                                  invalid URL format, or problems with reading
	 *                                  the response data
	 * @throws URISyntaxException
	 * @throws IllegalArgumentException if the urlString is null or empty
	 */
	public static String post(String url, String data, Map<String, String>headers) throws IOException {
		return fetch("POST", url, headers, data, null);
	}
	
	/** Check whether the filename is a directory */
	private static boolean isDirectory(String filename) {
		if (filename == null || filename.length() == 0) return false;
		
		char lastChar = filename.charAt(filename.length() - 1);
		if (lastChar == '/' || lastChar == '\\') 
			return true;
		
		if (new File(filename).isDirectory()) 
			return true;
		
		return false;
	}

	/**
	 * fetch specified URL
	 *
	 * @param method       HTTP request method, such as "GET", "POST", "DELETE", "PUT"
	 * @param url          The complete URL string
	 * @param headers      The request headers
	 * @param data         The data to POST or PUT
	 * @param saveFilename The filename or directory
	 * 
	 * @return if saveFilename is null, return content string. 
	 * 		   if saveFilename is not null, return file name saved.
	 * 
	 * @throws IOException              if there are network connectivity issues,
	 *                                  invalid URL format, or problems with reading
	 *                                  the response data
	 * @throws URISyntaxException
	 * @throws IllegalArgumentException if the urlString is null or empty
	 */
	public static String fetch(String method, String url, Map<String, String> headers, Object data, 
			String saveFilename) throws IOException {		
		// Validate input parameters
		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("URL string cannot be null or empty");
		}

		HttpURLConnection connection = null;

		try {
			// Create URI object
			URI uri;
			try {
				uri = new URI(url);
			} catch (URISyntaxException e) {
				throw new IOException(e.getClass().getSimpleName() + " " + e.getMessage());
			}
			
			// if url is like "file://xxxx"
			if ("file".equalsIgnoreCase(uri.getScheme())) {
				return fetchFile(method, uri, data);
			}
			
			
			// Create URL object and establish connection
			URL urlObj = uri.toURL(); // new URL(urlString);
			URLConnection urlConnection = urlObj.openConnection();

			// Ensure we have an HTTP connection
			if (!(urlConnection instanceof HttpURLConnection)) {
				throw new IOException("Invalid protocol - HTTP required");
			}

			connection = (HttpURLConnection) urlConnection;

			// Configure connection settings
			connection.setRequestMethod(method);
			connection.setRequestProperty("Accept", "*/*");
			if (headers != null) {
				for (String key : headers.keySet()) {
					connection.setRequestProperty(key, headers.get(key));
				}
			}
			connection.setConnectTimeout(5000); // 5 seconds connection timeout
			connection.setReadTimeout(20000); // 20 seconds read timeout

			// write request data
			if (data != null) {
				String dataStr = data.toString();
				try (OutputStream outputStream = connection.getOutputStream()) {
					outputStream.write(dataStr.getBytes());
					outputStream.flush();
				}
			}

			// Check HTTP response code
			int responseCode = connection.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new IOException("HTTP error code: " + responseCode + " - " + connection.getResponseMessage());
			}

			// process response
			if (saveFilename == null || saveFilename.length() == 0) {
				// read response as text, line by line
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						sb.append(line).append("\r\n");
					}
					return sb.toString();
				}

			} else {
				// process file name
				if (isDirectory(saveFilename)) { // when the name is a directory
					String fname = FileUtil.getFileName(url);
					File f = new File(saveFilename, fname);
					saveFilename = f.getAbsolutePath();
				}
				
				// save to file
				try (InputStream inStream = connection.getInputStream();
						OutputStream outStream = new FileOutputStream(saveFilename)) {
					byte[] buf = new byte[8192];
					int len = 0;
					while ((len = inStream.read(buf)) != -1) {
						outStream.write(buf, 0, len);
					}
				}
				return saveFilename;
			}

		} catch (IOException e) {
			// Enhance error message with URL information
			throw new IOException("Failed to fetch url: " + url + " - " + e.getMessage(), e);

		} finally {
			// Clean up resources
			if (connection != null) {
				connection.disconnect();
			}
		}

	}
	
	/** fetch url like file:///xxx.xxx */
	private static String fetchFile(String method, URI uri,  Object data) throws IOException {
		String filename = "";
		if (uri.getAuthority() != null) filename += uri.getAuthority();
		if (uri.getPath() != null) filename += uri.getPath();
		if (filename.length() == 0) return null;
		
		if ("GET".equals(method)) {		
			return FileUtil.loadFromFile(filename);
			
		} else if  ("POST".equals(method) && data != null) {
			FileUtil.saveToFile(filename, data.toString());
			return filename;
			
		} else if  ("PUT".equals(method) && data != null) {
			FileUtil.saveToFile(filename, data.toString(), true); // append to file
			return filename;
			
		} else if ("DELETE".equals(method)) {
			File f = new File(filename);
			if (f.exists() && f.delete()) {
				return filename;
			}
		}
		
		return null;
	}

}
