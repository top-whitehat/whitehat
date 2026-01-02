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
import java.net.URISyntaxException;

/**
 * Utility class for retrieving information about the currently running JAR file.
 * Provides methods to get the name and path of the JAR file containing the application.
 */
public class JarUtil {

    /**
     * Gets the name of the JAR file from which the specified class is loaded.
     * This method uses the class's ProtectionDomain and CodeSource to locate the JAR file.
     *
     * @param clazz the Class object used to locate the JAR file
     * @return the name of the JAR file (e.g., "myapp.jar"), or null if not running from a JAR
     * @throws SecurityException if a security manager denies access to the protection domain
     */
	public static String getCurrentJarName(Class<?> clazz) {
        // Validate input parameter
        if (clazz == null) {
            throw new IllegalArgumentException("Class parameter cannot be null");
        }

        try {
            // Check if security manager allows access to protection domain
//            if (System.getSecurityManager() != null) {
//                System.getSecurityManager().checkPermission(new RuntimePermission("getProtectionDomain"));
//            }

            // Get the code source location via protection domain
            java.security.CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
            
            // Verify that code source is available
            if (codeSource == null) {
                System.err.println("Warning: CodeSource is null - class may not be loaded from a JAR");
                return null;
            }

            // Get the location URL and convert to file path
            java.net.URL location = codeSource.getLocation();
            if (location == null) {
                System.err.println("Warning: Location URL is null");
                return null;
            }

            // Convert URL to File object and validate
            File jarFile = new File(location.toURI());
            
            // Verify that the path points to a valid JAR file
            if (!jarFile.exists()) {
                System.err.println("Warning: JAR file does not exist: " + jarFile.getAbsolutePath());
                return null;
            }
            
            if (!jarFile.isFile()) {
                System.err.println("Warning: Path is not a file: " + jarFile.getAbsolutePath());
                return null;
            }
            
            // Check if the file has a .jar extension
            String fileName = jarFile.getName();
            if (!fileName.toLowerCase().endsWith(".jar")) {
                System.err.println("Warning: File does not appear to be a JAR: " + fileName);
                return null;
            }

            return fileName;

        } catch (URISyntaxException e) {
            // Handle malformed URI syntax
            System.err.println("Error: Invalid URI syntax for JAR file location: " + e.getMessage());
            return null;
        } catch (SecurityException e) {
            // Handle security restrictions
            System.err.println("Error: Security manager denied access to protection domain: " + e.getMessage());
            throw e; // Re-throw security exceptions as they are critical
        } catch (Exception e) {
            // Handle any other unexpected exceptions
            System.err.println("Error: Unexpected exception while getting JAR name: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets the full absolute path of the JAR file from which the specified class is loaded.
     *
     * @param clazz the Class object used to locate the JAR file
     * @return the absolute path of the JAR file, or null if not running from a JAR
     * @throws SecurityException if a security manager denies access to the protection domain
     */
    public static String getCurrentJarPath(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class parameter cannot be null");
        }

        try {
            java.security.CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
            if (codeSource == null) {
                return null;
            }

            java.net.URL location = codeSource.getLocation();
            if (location == null) {
                return null;
            }

            File jarFile = new File(location.toURI());
            return jarFile.exists() && jarFile.isFile() ? jarFile.getAbsolutePath() : null;

        } catch (Exception e) {
            System.err.println("Error getting JAR path: " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if the application is currently running from a JAR file.
     *
     * @param clazz the Class object to check
     * @return true if running from a JAR file, false otherwise
     */
    public static boolean isRunningFromJar(Class<?> clazz) {
        try {
            String jarName = getCurrentJarName(clazz);
            return jarName != null && jarName.toLowerCase().endsWith(".jar");
        } catch (Exception e) {
            return false;
        }
    }

    
}