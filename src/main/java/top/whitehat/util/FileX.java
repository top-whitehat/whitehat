package top.whitehat.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.channels.FileChannel;

public class FileX extends File {
	private static final long serialVersionUID = 5577300978097097264L;

	protected URI uri;

	private static boolean isHttp(String pathname) {
		if (pathname.startsWith("http://") || pathname.startsWith("https://")) {
			return true;
		}
		return false;
	}

	public FileX(File file) {
		super(file.getAbsolutePath());
	}

	public FileX(URI uri) {
		super("");
		this.uri = uri;
	}

	public FileX(String pathname) {
		super(isHttp(pathname) ? "" : pathname);
		if (isHttp(pathname)) {
			try {
				uri = new URI(pathname);
			} catch (URISyntaxException e) {
				throw new RuntimeException("invalid url");
			}
		}
	}

	public FileX(String parent, String child) {
		super(isHttp(parent) ? "" : parent, isHttp(parent) ? "" : child);
		if (isHttp(parent)) {
			try {
				uri = new URI(parent);
				uri = uri.resolve(child);
			} catch (URISyntaxException e) {
				throw new RuntimeException("invalid url");
			}
		}
	}

	public String getAbsolutePath() {
		return uri == null ? super.getAbsolutePath() : uri.toString();
	}

	public FileX getAbsoluteFile() {
		return uri == null ? new FileX(super.getAbsoluteFile()) : this;
	}

	public String getName() {
		if (uri == null)
			return super.getName();
		String path = uri.getPath();
		int pos = path.lastIndexOf("/");
		return pos >= 0 ? path.substring(pos + 1) : "";
	}

	public String getParent() {
		if (uri == null)
			return super.getParent();
		String path = uri.getPath();
		int pos = path.lastIndexOf("/");
		return pos >= 0 ? path.substring(0, pos) : "";
	}

	public FileX getParentFile() {
		if (uri == null)
			return new FileX(super.getParentFile());
		String path = getParent();
		URI p = uri.resolve(path);
		return new FileX(p);
	}

	public String getPath() {
		return uri == null ? super.getPath() : uri.getPath();
	}

	public boolean exists() {
		if (uri == null)
			return super.exists();
		return length() >= 0;
	}

	private String headUrlCache = null;

	private String headResponceCache = null;

	private String httpHead(String fieldName) {
		try {
			String url = uri.toURL().toString();
			String json;
			if (url.equals(headUrlCache) && headResponceCache != null) {
				json = headResponceCache;
			} else {
				json = HttpUtil.fetch("HEAD", url, null, null, null);
				headUrlCache = url;
				headResponceCache = json;
			}
			JSON data = JSON.parse(json);
			return data.getString(fieldName);
		} catch (Exception e) {
			return null;
		}
	}

	public long length() {
		if (uri == null) {
			return super.length();

		} else {
			try {
				String len = httpHead("content-length");
				return Long.parseLong(len);
			} catch (Exception e) {
				return -1;
			}
		}
	}

	public boolean canRead() {
		return uri == null ? super.canRead() : (length() >= 0);
	}

	public boolean canWrite() {
		return uri == null ? super.canWrite() : false;
	}

	public boolean canExecute() {
		return uri == null ? super.canExecute() : false;
	}

	public boolean delete() {
		return uri == null ? super.delete() : false;
	}

	public boolean isFile() {
		if (uri == null) {
			return super.isFile();
		} else {
			String len = httpHead("content-length");
			return len == null ? false : true;
		}
	}

	public boolean isDirectory() {
		if (uri == null) {
			return super.isDirectory();
		} else {
			String len = httpHead("content-length");
			return len == null ? true : false;
		}
	}

	public boolean isHidden() {
		return uri == null ? super.isHidden() : false;
	}

	public Path toPath() {
		return uri == null ? super.toPath() : null;
	}

	public URI toURI() {
		return uri == null ? super.toURI() : uri;
	}

	@SuppressWarnings("deprecation")
	public URL toURL() throws MalformedURLException {
		return uri == null ? super.toURL() : uri.toURL();
	}

	public String toString() {
		return uri == null ? super.toString() : uri.toString();
	}

	public File saveTo(String filename) throws IOException {
		if (uri == null) {
			File destFile = new File(filename);
			try (FileInputStream fis = new FileInputStream(this);
					FileOutputStream fos = new FileOutputStream(destFile);
					FileChannel sourceChannel = fis.getChannel();
					FileChannel destChannel = fos.getChannel()) {

				destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());

			}
			return destFile;
		} else {
			String url = uri.toString();
			HttpUtil.download(url, filename, null);
			File destFile = new File(filename);
			return destFile.exists() ? destFile : null;
		}		
	}

}
