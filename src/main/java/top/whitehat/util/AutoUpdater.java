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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * Software Update: check version, download package, update package and restart.
 */
public class AutoUpdater {

	public AutoUpdater() {
	}

	/** Check version, get download url for new version
	 * 
	 * @param currentVersion  current version
	 * @param versionUrl      the URL that provide version information.<br>
	 *         a version.json should be in the http server, which looks like:
	<pre>
	{
		"version": "2.0.1",
		"url": "https://example.com/update/myapp-v2.0.1.jar",
		"description": "fix some bugs"
	}
	</pre>
	 * @return   
	 *          return a String of download url if the current version is old.<br>
	 *          return null if the current version is latest.  
	 * @throws IOException
	 */
	public String getDownloadUrl(String currentVersion, String versionUrl) throws IOException {
		JSON info = HttpUtil.getJSON(versionUrl, null);
		String latestVersion = info.getString("version");
		String downloadUrl = info.getString("url");
		if (!currentVersion.equals(latestVersion))
			return downloadUrl;
		return null;
	}

	/** download file from specified url
	 * 
	 * @param downloadUrl    the url of download file
	 * @param saveDirectory  save directory(must exists)
	 * @param handler        the handler when download is complete
	 */
	public void download(String downloadUrl, String saveDirectory, NotifyEventHandler handler) {
		if (saveDirectory == null)
			saveDirectory = System.getProperty("java.io.tmpdir");
		HttpUtil.download(downloadUrl, saveDirectory, handler);
	}
	
	/** wrap filename in command line */
	private String wrap(String filename) {
		StringBuilder sb = new StringBuilder();
		boolean needQuote = false;
		
		for(int i=0; i<filename.length(); i++) {
			char c = filename.charAt(i);
			switch(c) {
			case ' ':
				needQuote = true;
				break;
			case '"':
				needQuote = true;
				sb.append('\\');
				break;
			default:
				break;
			}
			sb.append(c);
		}
		
		if (needQuote)
			return "\"" + sb.toString() + "\"";
		else
			return sb.toString();
	}
	
	public boolean isGUI() {
		return false;
	}

	/** Replace current jar file with new jar file.
	 *  restart this jar application
	 * 
	 * @param newJarFileName  
	 * @throws IOException
	 */
	public void updateJarAndRestart(String newJarFileName) throws IOException {
		File newJarFile = new File(newJarFileName);
		if (!newJarFile.exists()) throw new FileNotFoundException(newJarFileName);
		String currentJarFileName = JarUtil.getCurrentJarName(AutoUpdater.class);
		if (currentJarFileName == null)
			throw new IOException("current application is not running from a jar file");
		
		// compose a script
		StringBuilder sb = new StringBuilder();
		if (CommandLine.isWindows()) {
			// compose a Windows batch script
//			sb.append("@echo off\r\n");
			// wait 5 seconds for the application exit
			sb.append("timeout /t 5 /nobreak &"); 
			// replace current jar file with new jar file
			sb.append(String.format("copy /Y %s %s &", wrap(newJarFileName), wrap(currentJarFileName)));
//			// delete new jar file
//			sb.append(String.format("del /Q %s\r\n", wrap(newJarFileName)));
			// start application
			String javaExe = isGUI() ? "javaw" : "java";
			sb.append(String.format("%s -jar %s", wrap(javaExe), wrap(currentJarFileName)));
			
		} else {
			// compose a Linux sh script
			sb.append("#!/bin/bash\n");
			// wait 5 seconds for the application exit
			sb.append("sleep 5\r\n"); 
			// replace current jar file with new jar file
			sb.append(String.format("cp -f %s %s\n", wrap(newJarFileName), wrap(currentJarFileName)));
//			// delete new jar file
//			sb.append(String.format("rm -f %s\r\n", wrap(newJarFileName)));
			// start application
			String javaExe = isGUI() ? "javaw" : "java";
			sb.append(String.format("%s -jar %s & \n", wrap(javaExe), wrap(currentJarFileName)));
		}
		
		// run script, do no wait for finish
		System.out.println("run update script");
		System.out.println(sb.toString());
//		CommandLine.runScript(sb.toString(), null, Options.of("wait", false, "memory", false), null);
		CommandLine.run(sb.toString(), Options.of("wait", false), null);
		
		// terminate current application
		System.out.println("application exit");
		System.exit(0);
	}

}
