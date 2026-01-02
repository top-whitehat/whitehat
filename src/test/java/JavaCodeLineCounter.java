
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Java Source Code Line Counter
 * This program recursively counts lines of code in Java source files within a directory and its sub directories.
 * It can distinguish between code lines, comment lines, and blank lines.
 */
public class JavaCodeLineCounter {
    
    // Statistical counters
    private long totalLines = 0;
    private long codeLines = 0;
    private long commentLines = 0;
    private long blankLines = 0;
    private int fileCount = 0;
    
    // List to store all Java files found
    private List<File> javaFiles = new ArrayList<>();
    
    /**
     * Main method - program entry point
     * @param args Command line arguments (optional: directory path)
     */
    public static void main(String[] args) {
        JavaCodeLineCounter counter = new JavaCodeLineCounter();
        
        // Get target directory from args or use current directory
        String targetPath = (args.length > 0) ? args[0] : ".";
        File directory = new File(targetPath);
        
        // Validate directory
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Error: Directory does not exist or path is invalid - " + targetPath);
            return;
        }
        
        System.out.println("Scanning directory: " + directory.getAbsolutePath());
        System.out.println("================================================");
        
        // Start counting process
        counter.scanDirectory(directory);
        counter.analyzeFiles();
        counter.printResults();
    }
    
    /**
     * Recursively scan directory for Java source files
     * @param directory The directory to scan
     */
    private void scanDirectory(File directory) {
    	if (directory.getName() == "test") return ;
    	
        File[] files = directory.listFiles();
        
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                // Recursively scan sub directories
                scanDirectory(file);
            } else if (file.isFile() && file.getName().endsWith(".java")) {
                // Add Java files to the list
                javaFiles.add(file);
            }
        }
    }
    
    /**
     * Analyze all found Java files and count different types of lines
     */
    private void analyzeFiles() {
        for (File javaFile : javaFiles) {
            analyzeSingleFile(javaFile);
            fileCount++;
        }
    }
    
    /**
     * Analyze a single Java file and count its lines
     * @param file The Java file to analyze
     */
    private void analyzeSingleFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean inBlockComment = false;
            int fileTotalLines = 0;
            int fileCodeLines = 0;
            int fileCommentLines = 0;
            int fileBlankLines = 0;
            
            while ((line = reader.readLine()) != null) {
                fileTotalLines++;
                String trimmedLine = line.trim();
                
                // Check if currently inside a block comment
                if (inBlockComment) {
                    fileCommentLines++;
                    // Check if block comment ends on this line
                    if (trimmedLine.contains("*/")) {
                        inBlockComment = false;
                    }
                    continue;
                }
                
                // Check for blank lines
                if (trimmedLine.isEmpty()) {
                    fileBlankLines++;
                    continue;
                }
                
                // Check for single-line comments
                if (trimmedLine.startsWith("//")) {
                    fileCommentLines++;
                    continue;
                }
                
                // Check for block comment start
                if (trimmedLine.startsWith("/*")) {
                    fileCommentLines++;
                    inBlockComment = !trimmedLine.contains("*/");
                    continue;
                }
                
                // Check for lines ending with block comment end (unlikely but possible)
                if (trimmedLine.endsWith("*/")) {
                    fileCommentLines++;
                    continue;
                }
                
                // If none of the above, it's a code line
                fileCodeLines++;
            }
            
            // Update global counters
            totalLines += fileTotalLines;
            codeLines += fileCodeLines;
            commentLines += fileCommentLines;
            blankLines += fileBlankLines;
            
            // Print individual file results
            System.out.printf("📄 %-40s | Total: %4d | Code: %4d | Comments: %3d | Blank: %2d%n",
                    abbreviateFileName(file.getName()), fileTotalLines, fileCodeLines, 
                    fileCommentLines, fileBlankLines);
                    
        } catch (IOException e) {
            System.err.println("Error reading file: " + file.getAbsolutePath() + " - " + e.getMessage());
        }
    }
    
    /**
     * Abbreviate long file names for better display
     * @param fileName The original file name
     * @return Abbreviated file name if too long
     */
    private String abbreviateFileName(String fileName) {
        return (fileName.length() > 35) ? fileName.substring(0, 32) + "..." : fileName;
    }
    
    /**
     * Print final statistical results
     */
    private void printResults() {
        System.out.println("================================================");
        System.out.println("📊 STATISTICAL RESULTS");
        System.out.println("================================================");
        System.out.printf("📁 Total Files Analyzed: %d%n", fileCount);
        System.out.printf("📈 Total Lines: %d%n", totalLines);
        System.out.printf("💻 Code Lines: %d (%.1f%%)%n", codeLines, 
                totalLines > 0 ? (codeLines * 100.0 / totalLines) : 0);
        System.out.printf("💬 Comment Lines: %d (%.1f%%)%n", commentLines, 
                totalLines > 0 ? (commentLines * 100.0 / totalLines) : 0);
        System.out.printf("⬜ Blank Lines: %d (%.1f%%)%n", blankLines, 
                totalLines > 0 ? (blankLines * 100.0 / totalLines) : 0);
        System.out.printf("📊 Average Lines Per File: %.1f%n", 
                fileCount > 0 ? (double) totalLines / fileCount : 0);
        
        // Calculate comment-to-code ratio (important metric for code quality)
        double commentRatio = codeLines > 0 ? (double) commentLines / codeLines : 0;
        System.out.printf("📋 Comment-to-Code Ratio: %.2f%n", commentRatio);
        
        // Code density (percentage of actual code from total lines)
        double codeDensity = totalLines > 0 ? (codeLines * 100.0 / totalLines) : 0;
        System.out.printf("🎯 Code Density: %.1f%%%n", codeDensity);
    }
}
