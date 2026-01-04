package top.whitehat.util;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileInfo {
	
	/**
	 * Get files of specified path of specified repository in github.com
	 * 
	 * @param user       github user name
	 * @param repository github repository name
	 * @param path       path, could be null
	 * 
	 * @return FileInfos object
	 * @throws IOException
	 */
	public static FileInfo github(String user, String repository, String path) throws IOException {
		String p = String.format("https://api.github.com/repos/%s/%s/contents/%s", user, repository, path);
		return new FileInfo(p);
	}
	
	public String  name;       
	public String  fullPath;   
	public boolean isDirectory;     
	public boolean isExists;
	public long size;
	public int permissions;
	public String owner;            
	public String group;
	public LocalDateTime lastModified; 
	public LocalDateTime lastAccessed;
	public LocalDateTime creationTime;
	
	private List<FileInfo> children = new ArrayList<FileInfo>();
	private boolean isUrl;
	File file;
	
	private static boolean isHttp(String pathname) {
		if (pathname.startsWith("http://") || pathname.startsWith("https://")) {
			return true;
		}
		return false;
	}
	
	
	public FileInfo(String pathname) {
		isUrl = isHttp(pathname);
		if (!isUrl) {
			file = new File(pathname);
			getInfo();
		} else {
			fullPath = pathname;
		}
	}
	
	private void getInfo() {
		if (file != null) {
			fullPath = file.getAbsolutePath();
			name = FileUtil.getFileName(fullPath);
			isExists = file.exists();
			isDirectory = file.isDirectory();
			if (isDirectory) {
				CommandResult ret = CommandLine.run("cmd", "/c", "dir", fullPath);
				System.out.println(ret);
			} else {
				
			}
		}
	}

		
	public String toString() {
		StringBuilder sb = new StringBuilder();		
		sb.append(name);		
		sb.append(isDirectory ? "\t\t<DIR>" : "\t\t" + size);
		return sb.toString();
	}

	/**
	 * Get files of specified path of specified repository in github.com
	 * 
	 * @param user       github user name
	 * @param repository github repository name
	 * @param path       path, could be null
	 * 
	 * @return FileInfos object
	 * @throws IOException
	 */
	private void fromGitHub() throws IOException {
		if (fullPath == null)
			fullPath = "";
		
		/*
		String apiUrl = String.format("https://api.github.com/repos/%s/%s/contents/%s", user, repository, path);

		this.children.clear();

		JSON res = HttpUtil.getJSON(apiUrl, null);
		if (res instanceof JSONArray) {
			JSONArray files = (JSONArray) res;
			for (int i = 0; i < files.length(); i++) {
				JSON item = files.getJSON(i);
				FileInfo file = new FileInfo();
				file.name = item.getString("name");
				file.path = path + "/" + file.name;
				file.user = user;
				file.repository = repository;
				file.isDirectory = "dir".equals(item.getString("type")) ? true : false;
				file.size = item.has("size") ? item.getInt("size") : -1;
				this.children.add(file);
			}
		}
		*/
	}

	public FileInfo child(String name) {
		for (FileInfo file : children) {
			if (name.equalsIgnoreCase(file.name))
				return file;
		}
		return null;
	}

	public FileInfo subdir(String subDirName) {
		int pos = subDirName.indexOf("/"); 
		if (pos < 0) {
			FileInfo f = child(subDirName);
			if (f != null && f.isDirectory) {
				//return new FileInfo(f);
			}
			return null;
		} else {
			String firstDir = subDirName.substring(0, pos);
			String nextDir = subDirName.substring(pos + 1);
			FileInfo sub = subdir(firstDir);
			return sub == null ? null : sub.subdir(nextDir);
		}
	}

	public static void main(String[] args) throws IOException {
		FileInfo f = new FileInfo("c:\\");
//		File ff = new File("http://jostudio.com.cn/myfile/monitor.txt");
//		System.out.println(ff.delete());
		
//		FileInfo fs = FileInfo.github("top-whitehat", "whitehat", "src/main/java/top/whitehat/");
//		System.out.println(fs);
//		System.err.println("-----1-----");

//		fs = fs.subdir("src/main/java");
//		System.out.println(fs);
//		System.err.println("-----2-----");

//		fs = fs.subdir("main/top");
//		System.out.println(fs);
//		System.err.println("----3------");
//		
//		fs = fs.subdir("java");
//		System.out.println(fs);
//		System.err.println("----4------");
		
		
		

	}
}
