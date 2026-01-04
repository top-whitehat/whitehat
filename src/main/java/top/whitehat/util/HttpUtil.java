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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FileUtil provides utility methods for working with files
 */
public class HttpUtil implements AutoCloseable {

	/**
	 * return URL encoded string
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public static String urlencode(String s) throws UnsupportedEncodingException {
		return URLEncoder.encode(s, "utf-8");
	}

	/** return HTML encoded string */
	public static String htmlEncode(String input) {
		if (input == null)
			return null;
		StringBuilder sb = new StringBuilder();
		int length = input.length();
		int i = 0;
		while (i < length) {
			char c = input.charAt(i++);
			switch (c) {
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			case '\'':
				sb.append("&apos;");
				break;
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
			default:
				sb.append(c);
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
	public static String get(String url) throws IOException {
		return fetch("GET", url, null, null, null);
	}

	/** Get JSON data from specified url */
	public static JSON getJSON(String url, Map<String, String> headers) throws IOException {
		String ret = fetch("GET", url, headers, null, null);
		return JSON.parse(ret);
	}

	/** Post JSON data to specified url */
	public static JSON postJSON(String url, JSON data, Map<String, String> headers) throws IOException {
		String ret = fetch("POST", url, headers, data.toString(), null);
		return JSON.parse(ret);
	}

	/**
	 * Download file from specified url
	 *
	 * @param url          The complete URL string
	 * @param saveFilename The filename to save
	 * 
	 * @throws IOException if there are network connectivity issues, invalid URL
	 *                     format, or problems with reading the response data
	 */
	public static void download(String url, String saveFilename) {
		download(url, saveFilename, null);
	}

	/**
	 * Download file from specified url
	 *
	 * @param url          The complete URL string
	 * @param saveFilename The filename to save
	 * @param handler      handler when download finished
	 * 
	 * @throws IOException if there are network connectivity issues, invalid URL
	 *                     format, or problems with reading the response data
	 */
	public static void download(String url, String saveFilename, NotifyEventHandler handler) {
		Promise promise = new Promise((resolve, reject) -> {
			String fname = null;
			try {
				fname = fetch("GET", url, null, null, saveFilename);
				resolve.accept(fname);
			} catch (Exception e) {
				reject.accept(e);
			}
		});

		promise.then(e -> {
			handler.onNotify(e);
			return e;
		}).catchError(e -> {
			handler.onNotify(e);
			return e;
		});
	}

	/**
	 * POST data to specified url
	 *
	 * @param url     The complete URL string
	 * @param data    The data to post
	 * @param headers The request headers
	 * 
	 * @return response string
	 * 
	 * @throws IOException              if there are network connectivity issues,
	 *                                  invalid URL format, or problems with reading
	 *                                  the response data
	 * @throws URISyntaxException
	 * @throws IllegalArgumentException if the urlString is null or empty
	 */
	public static String post(String url, String data, Map<String, String> headers) throws IOException {
		return fetch("POST", url, headers, data, null);
	}

	/** Check whether the filename is a directory */
	private static boolean isDirectory(String filename) {
		if (filename == null || filename.length() == 0)
			return false;

		char lastChar = filename.charAt(filename.length() - 1);
		if (lastChar == '/' || lastChar == '\\')
			return true;

		if (new File(filename).isDirectory())
			return true;

		return false;
	}

	/** fetch url like file:///xxx.xxx */
	private static String fetchFile(String method, URI uri, Object data) throws IOException {
		String filename = "";
		if (uri.getAuthority() != null)
			filename += uri.getAuthority();
		if (uri.getPath() != null)
			filename += uri.getPath();
		if (filename.length() == 0)
			return null;

		if ("GET".equals(method)) {
			return FileUtil.loadFromFile(filename);

		} else if ("POST".equals(method) && data != null) {
			FileUtil.saveToFile(filename, data.toString());
			return filename;

		} else if ("PUT".equals(method) && data != null) {
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

	/**
	 * fetch specified URL
	 *
	 * @param method       HTTP request method, such as "GET", "POST", "DELETE",
	 *                     "PUT"
	 * @param url          The complete URL string
	 * @param headers      The request headers
	 * @param data         The data to POST or PUT
	 * @param saveFilename The filename or directory
	 * 
	 * @return if saveFilename is null, return content string. if saveFilename is
	 *         not null, return file name saved.
	 * 
	 * @throws IOException              if there are network connectivity issues,
	 *                                  invalid URL format, or problems with reading
	 *                                  the response data
	 * @throws URISyntaxException
	 * @throws IllegalArgumentException if the urlString is null or empty
	 */
	public static String fetch(String method, String url, Map<String, String> headers, Object data, String saveFilename)
			throws IOException {

		// Validate input parameters
		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("URL string cannot be null or empty");
		}

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

		// request
		try (HttpUtil h = new HttpUtil()) {
			h.requestHeaders = headers;
			h.request(method, url, data);

			if (saveFilename != null) {
				return h.saveBodyToFile(saveFilename);
			} else {
				return "HEAD".equals(method) ? JSON.stringify(h.responseHeaders) : h.getBody();
			}
		}
	}

	// ------------- members --------------

	private HttpURLConnection connection = null;
	private int timeout = 5000; // connection timeout;
	private int totalTimeout = 20000; // read timeout;
	private String url;
	private Map<String, String> requestHeaders;
	private Map<String, String> responseHeaders;
	private int responseCode;
	private String contentType = "";
	private int contentLength = -1;

	/**
	 * Send a http request
	 * 
	 * @param method Http request method
	 * @param url    The Url
	 * 
	 * @return this
	 * @throws IOException
	 */
	public HttpUtil request(String method, String url) throws IOException {
		return request(method, url, null);
	}

	/**
	 * Send a http request
	 * 
	 * @param method   Http request method
	 * @param url      The Url
	 * @param postData The data to pose
	 * 
	 * @return this
	 * @throws IOException
	 */
	public HttpUtil request(String method, String url, Object postData) throws IOException {
		// Validate input parameters
		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("URL string cannot be null or empty");
		}
		this.url = url;

		// Create URI object
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			throw new IOException(e.getClass().getSimpleName() + " " + e.getMessage());
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
		if (requestHeaders != null) {
			for (String key : requestHeaders.keySet()) {
				connection.setRequestProperty(key, requestHeaders.get(key));
			}
		}
		connection.setConnectTimeout(timeout); // connection timeout
		connection.setReadTimeout(totalTimeout); // 20 seconds read timeout

		// write post data
		if (postData != null) {
			String dataStr = postData.toString();
			try (OutputStream outputStream = connection.getOutputStream()) {
				outputStream.write(dataStr.getBytes());
				outputStream.flush();
			}
		}

		parseResponseHeaders();
		return this;
	}

	/** Parse response headers */
	protected void parseResponseHeaders() throws IOException {
		// get response code
		responseCode = connection.getResponseCode();

		// get response headers
		responseHeaders = new LinkedHashMap<String, String>();
		Map<String, List<String>> headerFields = connection.getHeaderFields();
		for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
			String headerName = entry.getKey();
			if (headerName != null) {
				List<String> headerValues = entry.getValue();
				StringBuilder sb = new StringBuilder();
				for (String value : headerValues) {
					if (sb.length() == 0) {
						if ("content-type".equalsIgnoreCase(headerName)) {
							contentType = value;
						} else if ("content-length".equalsIgnoreCase(headerName)) {
							try {
								contentLength = Integer.parseInt(value);
							} catch (Exception e) {
							}
						}
					} else {
						sb.append("; ");
					}
					sb.append(value);
				}
				responseHeaders.put(headerName.toLowerCase(), sb.toString());
			}
		}
	}

	/** Get status code */
	public int statusCode() {
		return responseCode;
	}

	/** Get response content type */
	public String contentType() {
		return contentType;
	}

	/** Get response content length. return -1 if no defined. */
	public int contentLength() {
		return contentLength;
	}

	/** Validate status code */
	public HttpUtil validateStatusCode(int code) throws IOException {
		if (responseCode != code) {
			close();
			throw new IOException("expect status code " + code + " but " + responseCode + " is found");
		} else {
			return this;
		}
	}

	/** Get response headers */
	public Map<String, String> getResponseHeaders() {
		return responseHeaders;
	}
	
	/** Get response body as stream */
	public InputStream getBodyStream() throws IOException {
		return new HttpBodyInputStream(this);
	}

	/** Get response body text */
	public String getBody() throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(getBodyStream()))) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\r\n");
			}
			return sb.toString();
		}
	}

	/**
	 * Save response body to a file
	 * 
	 * @param saveDirOrFilename save directory or filename
	 * 
	 * @return the filename that saved.
	 * 
	 * @throws IOException
	 */
	public String saveBodyToFile(String saveDirOrFilename) throws IOException {
		// process file name
		if (isDirectory(saveDirOrFilename)) { // when the name is a directory
			String fname = FileUtil.getFileName(url);
			File f = new File(saveDirOrFilename, fname);
			saveDirOrFilename = f.getAbsolutePath();
		}

		// save to file
		try (InputStream inStream = getBodyStream(); //
				OutputStream outStream = new FileOutputStream(saveDirOrFilename)) {
			byte[] buf = new byte[8192];
			int len = 0;
			while ((len = inStream.read(buf)) != -1) {
				outStream.write(buf, 0, len);
			}
			return saveDirOrFilename;
		}
	}

	@Override
	public void close() {
		if (connection != null) {
//			System.out.println("close connection");
			connection.disconnect();
			connection = null;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
	}

	public static class HttpBodyInputStream extends InputStream {
		HttpUtil util;
		InputStream in;

		public HttpBodyInputStream(HttpUtil util) throws IOException {
			this.util = util;
			this.in = util.connection.getInputStream();
		}

		@Override
		public int read() throws IOException {
			return in.read();
		}

		@Override
		public void close() {
			try {
				in.close();
			} catch (IOException e) {
			}
			try {
				util.close();
			} catch (Exception e) {
			}
		}
	}

}
