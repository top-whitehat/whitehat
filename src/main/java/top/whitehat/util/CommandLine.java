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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * The CommandLine class provides a fluent API to run command line operations on the
 * operating system. This class offers multiple methods to execute commands, files,
 * and scripts with various options for handling input, output, environment variables,
 * timeouts, and process management. It supports both synchronous and asynchronous
 * execution modes and provides comprehensive control over the execution environment.
 * The class is designed with a builder pattern approach to allow method chaining
 * for easy configuration of command execution parameters.
 * 
 * <h3>Usage Example</h3>
 * <pre>
 * // Basic command execution
 * CommandResult result = CommandLine.run("ping", "127.0.0.1");
 * System.out.println("Return value: " + result.returnValue); // Exit code of the process
 * System.out.println("Output: " + result.output);            // Output text from the process
 * 
 * // Advanced execution with options
 * CommandLine.Options options = CommandLine.Options.of("timeout", 5000, "directory", "/tmp");
 * CommandResult result2 = CommandLine.run("ls", "-la", options, null);
 * 
 * // Executing script content directly
 * String script = "echo Hello World";
 * CommandResult result3 = CommandLine.runScript(script);
 * </pre>
 */
public class CommandLine {
	
	/** default timeout (in milliseconds). zero means waiting until process ends */
	public static int DEFAULT_TIMEOUT = 200000;

	/**
	 * Functional interface for command execution event notifications. This interface
	 * allows clients to receive callbacks during command execution, such as when
	 * output lines are received or when the execution status changes. The onNotify
	 * method is called with various types of objects depending on the event type,
	 * typically CommandResult objects or String lines of output. This enables
	 * real-time monitoring and processing of command execution results without
	 * waiting for the entire command to complete. The functional interface design
	 * allows for lambda expressions to be used when setting up event handlers.
	 * 
	 * @see CommandLine#setLineHandler(LineOutputHandler)
	 */
	@FunctionalInterface
	public interface LineOutputHandler {
		/**
		 * Called when an event occurs during command execution. The type of object
		 * passed to this method depends on the specific event being notified. For
		 * output line events, this will typically be a String containing the line
		 * of output. For completion events, this will typically be a CommandResult
		 * object containing the exit code and full output of the command.
		 * 
		 * @param e The event object, which may be a String line, CommandResult, or other relevant object
		 */
		void onLine(String line);
	}
	
	@FunctionalInterface
	public interface ResultParser {
		JSON onResult(CommandResult result);
	}

	/**
	 * Checks whether the current operating system is Windows. This method examines
	 * the "os.name" system property and returns true if it contains the string
	 * "win" (case-insensitive). This is useful for determining platform-specific
	 * behavior, such as using different command shells, path separators, or
	 * executable file extensions. The check is case-insensitive and will match
	 * any Windows variant (Windows 10, Windows Server, etc.) since they all
	 * include "win" in the system name. This method is commonly used in
	 * conditional logic to handle platform-specific command execution requirements.
	 * 
	 * @return true if the current operating system is Windows, false otherwise
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * if (CommandLine.isWindows()) {
	 *     // Use Windows-specific commands or parameters
	 *     CommandLine.run("cmd", "/c", "dir");
	 * } else {
	 *     // Use Unix/Linux-specific commands or parameters
	 *     CommandLine.run("ls", "-la");
	 * }
	 * </pre>
	 */
	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

	/**
	 * Gets the appropriate character set for the current operating system. This
	 * method returns the default character encoding to be used when reading
	 * command output or writing command input. On Windows systems, it checks
	 * the locale to determine if Chinese locale is being used and returns "gbk"
	 * for Chinese locales, otherwise it returns "utf-8". On non-Windows systems,
	 * it always returns "utf-8". This helps ensure proper character encoding
	 * when dealing with command line output that may contain non-ASCII characters,
	 * particularly important when working with internationalized text or file paths
	 * containing special characters. The method provides appropriate encoding
	 * based on common Windows locale settings while maintaining UTF-8 as the
	 * default for other systems.
	 * 
	 * @return The character set name appropriate for the current OS ("utf-8" or "gbk")
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * String encoding = CommandLine.charset();
	 * System.out.println("Using character set: " + encoding);
	 * // Use this encoding when reading command output
	 * </pre>
	 */
	public static String charset() {
		String ret = "utf-8";
		if (isWindows()) {
			String country = Locale.getDefault().getCountry();
			if ("CN".equals(country))
				ret = "gbk";
		}
		return ret;
	}

	/**
	 * Executes a command line with the specified arguments, options, and event handler.
	 * This method provides full control over command execution by allowing you to
	 * specify the command as an array of strings, execution options, and an event
	 * handler for receiving notifications during execution. The command words are
	 * executed as a process, with the options controlling execution parameters like
	 * timeout, working directory, and environment variables. The event handler
	 * receives notifications as the command executes, which is useful for real-time
	 * monitoring of long-running commands. This method creates a new CommandLine
	 * instance, configures it with the specified parameters, and executes the command.
	 * It's the most comprehensive of the run methods, providing access to all
	 * configuration options in a single call.
	 * 
	 * @param words An array of strings representing the command and its arguments (e.g., {"ls", "-la", "/home"})
	 * @param options Configuration options for the command execution (can be null for defaults)
	 * @param handler Event handler to receive notifications during execution (can be null for no notifications)
	 * @return A CommandResult object containing the exit code and output of the executed command
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * String[] cmd = {"ping", "-c", "4", "google.com"};
	 * CommandLine.Options opts = CommandLine.Options.of("timeout", 10000);
	 * CommandResult result = CommandLine.run(cmd, opts, line -> System.out.println("Output: " + line));
	 * </pre>
	 */
	public static CommandResult run(String[] words, Options options, LineOutputHandler handler) {
		return new CommandLine().setLineHandler(handler).options(options).command(words).exec();
	}

	/**
	 * Executes a command line specified as a list of strings with default options
	 * and no event handler. This convenience method allows you to run a command
	 * by providing the command and its arguments as a List of strings. The command
	 * is executed synchronously with default options (no timeout, current directory,
	 * etc.) and without any event notifications. This is useful for simple command
	 * execution where you don't need advanced configuration or real-time output
	 * monitoring. The method converts the List to an array and delegates to the
	 * more comprehensive run method with null options and handler.
	 * 
	 * @param words A List of strings representing the command and its arguments
	 * @return A CommandResult object containing the exit code and output of the executed command
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * List<String> cmd = Arrays.asList("ls", "-la");
	 * CommandResult result = CommandLine.run(cmd);
	 * System.out.println("Exit code: " + result.returnValue);
	 * System.out.println("Output: " + result.output);
	 * </pre>
	 */
	public static CommandResult run(List<String> words) {
		return run(words.toArray(new String[words.size()]), null, null);
	}

	/**
	 * Executes a command line specified as variable arguments with default options
	 * and no event handler. This convenience method allows you to run a command
	 * using varargs syntax, making it easy to specify the command and its arguments
	 * as separate parameters. The command is executed synchronously with default
	 * options and without event notifications. This is the most convenient method
	 * for simple command execution when you want to specify command parts as
	 * separate arguments rather than as an array or list. It's particularly useful
	 * for shell commands with several parameters, as it eliminates the need to
	 * create an array or list explicitly.
	 * 
	 * @param words Variable arguments representing the command and its arguments (e.g., "ls", "-la", "/tmp")
	 * @return A CommandResult object containing the exit code and output of the executed command
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * CommandResult result = CommandLine.run("echo", "Hello", "World");
	 * CommandResult result2 = CommandLine.run("find", "/home", "-name", "*.txt");
	 * </pre>
	 */
	public static CommandResult run(String... words) {
		return run(words, null, null);
	}

	/**
	 * Executes a command line specified as a single string with the provided options
	 * and event handler. This method accepts a complete command line as a single
	 * string (like "ls -la /home") and parses it into individual words before
	 * execution. The parsing handles quoted arguments and escaped characters
	 * properly, making it safe to use with complex commands that have spaces and
	 * special characters. Options control execution parameters like timeout and
	 * working directory, while the event handler allows for real-time monitoring
	 * of the command execution. This method is useful when you have a complete
	 * command string from user input or configuration that needs to be executed
	 * with specific options and monitoring capabilities.
	 * 
	 * @param command_line A complete command line string (e.g., "ls -la /home" or "ping -c 4 google.com")
	 * @param options Configuration options for the command execution (can be null for defaults)
	 * @param handler Event handler to receive notifications during execution (can be null for no notifications)
	 * @return A CommandResult object containing the exit code and output of the executed command
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * String cmd = "find /home -name '*.log' -size +100M";
	 * CommandLine.Options opts = CommandLine.Options.of("timeout", 30000, "directory", "/tmp");
	 * CommandResult result = CommandLine.run(cmd, opts, line -> System.out.println("Progress: " + line));
	 * </pre>
	 */
	public static CommandResult run(String command_line, Options options, LineOutputHandler handler) {
		return new CommandLine().setLineHandler(handler).options(options).line(command_line).exec();
	}

	/**
	 * Executes a command line specified as a single string with default options
	 * and no event handler. This convenience method accepts a complete command
	 * line as a single string and executes it with default parameters. The method
	 * parses the command string to handle quoted arguments and special characters
	 * properly. This is useful for executing simple commands that are available
	 * as complete strings from user input, configuration files, or other sources.
	 * The command is executed synchronously, and the full output is captured in
	 * the returned CommandResult object. This is the simplest way to execute
	 * a command string when you don't need special configuration or real-time
	 * monitoring of the execution process.
	 * 
	 * @param command_line A complete command line string to execute (e.g., "ls -la /home")
	 * @return A CommandResult object containing the exit code and output of the executed command
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * CommandResult result = CommandLine.run("date");
	 * CommandResult result2 = CommandLine.run("echo Hello World");
	 * CommandResult result3 = CommandLine.run("ping -c 3 8.8.8.8");
	 * </pre>
	 */
	public static CommandResult run(String command_line) {
		return run(command_line, null, null);
	}

	/**
	 * Executes a file with the specified arguments, options, and event handler.
	 * This method runs an executable file or script with the given parameters,
	 * handling different file types appropriately based on the operating system.
	 * For script files (with extensions like .bat on Windows or .sh on Unix),
	 * the appropriate interpreter is automatically selected. For executable files,
	 * they are launched directly. The method provides full control over execution
	 * parameters through options and allows real-time monitoring through the event
	 * handler. Arguments passed to this method become command-line arguments to
	 * the executed file. This is useful for launching external programs, scripts,
	 * or executables from within your Java application with complete configuration
	 * control and monitoring capabilities.
	 * 
	 * @param filename The path to the file to execute (executable, script, etc.)
	 * @param args An array of arguments to pass to the executed file (can be null)
	 * @param options Configuration options for the execution (can be null for defaults)
	 * @param handler Event handler to receive notifications during execution (can be null)
	 * @return A CommandResult object containing the exit code and output of the executed file
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * // Run a shell script with arguments
	 * String[] scriptArgs = {"arg1", "arg2"};
	 * CommandLine.Options opts = CommandLine.Options.of("timeout", 10000);
	 * CommandResult result = CommandLine.runFile("/path/to/script.sh", scriptArgs, opts, null);
	 * 
	 * // Run an executable with monitoring
	 * CommandResult result2 = CommandLine.runFile("myprogram.exe", new String[]{"-v"}, null,
	 *     output -> System.out.println("Output: " + output));
	 * </pre>
	 */
	public static CommandResult runFile(String filename, String[] args, Options options, LineOutputHandler handler) {
		return new CommandLine().setLineHandler(handler).options(options).execFile(filename, args);
	}

	/**
	 * Executes a file with the specified event handler and default options.
	 * This convenience method allows you to run an executable file or script
	 * with an event handler to monitor its execution, while using default
	 * configuration options. No additional arguments are passed to the executed
	 * file beyond its name. This method is useful when you want to monitor
	 * the output or execution status of a file in real-time without needing
	 * to specify additional configuration options or command-line arguments.
	 * The execution uses default timeout, working directory, and other options.
	 * The event handler will receive notifications as the file executes, allowing
	 * for real-time output processing or status monitoring.
	 * 
	 * @param filename The path to the file to execute (executable, script, etc.)
	 * @param handler Event handler to receive notifications during execution (can be null)
	 * @return A CommandResult object containing the exit code and output of the executed file
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * // Run a file with event monitoring
	 * CommandResult result = CommandLine.runFile("myprogram.exe", line -> {
	 *     System.out.println("Program output: " + line);
	 * });
	 * </pre>
	 */
	public static CommandResult runFile(String filename, LineOutputHandler handler) {
		return runFile(filename, null, null, handler);
	}

	/**
	 * Executes a file with default options and no event handler or arguments.
	 * This is the simplest form of file execution that takes only a filename
	 * and runs it with completely default settings. The file is executed
	 * synchronously, and the method returns only after the file has completed
	 * execution. This is useful for simple cases where you just need to run
	 * an executable or script file and capture its result without any special
	 * configuration, real-time monitoring, or additional arguments. The method
	 * uses default timeout, working directory, and other execution parameters,
	 * and no event notifications are provided during execution. This provides
	 * a convenient way to launch applications or scripts with minimal setup.
	 * 
	 * @param filename The path to the file to execute (executable, script, etc.)
	 * @return A CommandResult object containing the exit code and output of the executed file
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * // Simple file execution
	 * CommandResult result = CommandLine.runFile("myprogram.exe");
	 * System.out.println("Exit code: " + result.returnValue);
	 * System.out.println("Output: " + result.output);
	 * </pre>
	 */
	public static CommandResult runFile(String filename) {
		return runFile(filename, null, null, null);
	}

	/**
	 * Executes script content directly with the specified arguments, options, and event handler.
	 * This method takes script content as a string and executes it as if it were saved to a file,
	 * providing full control over the execution environment. The script content is executed
	 * using the appropriate interpreter for the operating system (.bat for Windows, .sh for Unix).
	 * The method supports both in-memory execution (without creating temporary files) and
	 * traditional file-based execution based on the 'inMemory' option. This is particularly
	 * useful for executing dynamically generated scripts or for running small scripts without
	 * the overhead of creating temporary files. The arguments, options, and event handler
	 * provide complete control over execution parameters, environment, and real-time monitoring.
	 * 
	 * @param scriptContent The complete script content as a string (e.g., bash commands, batch commands)
	 * @param args An array of arguments to pass to the script (can be null)
	 * @param options Configuration options for the execution (can be null for defaults)
	 * @param handler Event handler to receive notifications during execution (can be null)
	 * @return A CommandResult object containing the exit code and output of the executed script
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * String script = "#!/bin/bash\n" +
	 *                 "echo 'Hello from script'\n" +
	 *                 "ls -la";
	 * String[] args = {"arg1", "arg2"};
	 * CommandLine.Options opts = CommandLine.Options.of("inmemory", true, "timeout", 5000);
	 * CommandResult result = CommandLine.runScript(script, args, opts, line -> {
	 *     System.out.println("Script output: " + line);
	 * });
	 * </pre>
	 */
	public static CommandResult runScript(String scriptContent, String[] args, Options options, LineOutputHandler handler) {
		return new CommandLine().setLineHandler(handler).options(options).execScript(scriptContent, args);
	}

	/***
	 * Executes script content directly with the specified event handler and default options.
	 * This convenience method allows you to run script content with real-time monitoring
	 * while using default execution options. The script content is executed using the
	 * appropriate system interpreter, and the event handler receives notifications as
	 * the script runs. This is useful when you want to monitor script execution in real-time
	 * without needing to specify additional configuration parameters. The method uses
	 * default timeout, working directory, and other execution parameters, but allows
	 * you to capture and process output as it's generated by the script. This is
	 * particularly useful for long-running scripts where you want to provide real-time
	 * feedback to users or log script output as it occurs.
	 * 
	 * @param scriptContent The complete script content as a string to execute
	 * @param handler Event handler to receive notifications during execution (can be null)
	 * @return A CommandResult object containing the exit code and output of the executed script
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * String script = "for i in {1..5}; do\n" +
	 *                 "  echo "Count: $i"\n" +
	 *                 "  sleep 1\n" +
	 *                 "done";
	 * CommandResult result = CommandLine.runScript(script, line -> {
	 *     System.out.println("Live output: " + line);
	 * });
	 * </pre>
	 */
	public static CommandResult runScript(String scriptContent, LineOutputHandler handler) {
		return runScript(scriptContent, null, null, handler);
	}

	/***
	 * Executes script content directly with default options and no event handler or arguments.
	 * This is the simplest form of script execution that takes only script content as a string
	 * and runs it with default settings. The script is executed synchronously, and the method
	 * returns only after the script has completed execution. This method is useful for running
	 * simple scripts or commands that don't require real-time monitoring, special configuration,
	 * or command-line arguments. The execution uses default timeout, working directory, and
	 * other parameters. The method handles the appropriate system interpreter automatically
	 * based on the operating system, making it convenient to run scripts without worrying
	 * about platform differences. This provides a straightforward way to execute dynamic
	 * script content with minimal setup requirements.
	 * 
	 * @param scriptContent The complete script content as a string to execute
	 * @return A CommandResult object containing the exit code and output of the executed script
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * String script = "echo 'Hello World'\n" +
	 *                 "date\n" +
	 *                 "pwd";
	 * CommandResult result = CommandLine.runScript(script);
	 * System.out.println("Exit code: " + result.returnValue);
	 * System.out.println("Output:\n" + result.output);
	 * </pre>
	 */
	public static CommandResult runScript(String scriptContent) {
		return runScript(scriptContent, null, null, null);
	}

	/**
	 * Gets the process ID of the specified Process object. This method is designed
	 * to retrieve the system process identifier for a running process, which can
	 * be useful for process management, monitoring, or system administration tasks.
	 * Currently, this method returns 0 as a placeholder, but includes commented
	 * code for Java 9+ which provides the process.pid() method. The process ID
	 * can be used to identify, monitor, or manage the external process after it
	 * has been started. In future implementations, this method would use the
	 * appropriate Java API based on the version to retrieve the actual process ID.
	 * This method is primarily used internally by the CommandLine class to track
	 * and manage spawned processes when more advanced process control is needed.
	 * 
	 * @param process The Process object for which to retrieve the ID
	 * @return The system process ID, or 0 if not available (current implementation)
	 */
	private static int pid(Process process) {
		return 0;
		// for java 9+
//		try {
//			return (int) process.pid();
//		} catch (Exception e) {
//			return -1;
//		}
	}

	/**
	 * Saves a string to the specified file with optional append mode. This utility
	 * method handles the complete file writing process including directory creation
	 * if needed, proper resource management with try-finally blocks, and null
	 * handling for the input string. If the target file doesn't exist, the method
	 * creates the necessary parent directories and the file itself. The append
	 * parameter controls whether the content is written to the beginning of the
	 * file (overwriting) or appended to the end. This method is primarily used
	 * internally to save script content to temporary files before execution, but
	 * can also be used for other file writing tasks within the CommandLine system.
	 * Proper exception handling ensures that file resources are always closed
	 * even if errors occur during the writing process.
	 * 
	 * @param file The target file to write to
	 * @param str The string content to write (null is treated as empty string)
	 * @param isAppend Whether to append to the file (true) or overwrite (false)
	 * @throws IOException if there's an error writing to the file
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * File tempFile = new File("/tmp/myscript.sh");
	 * CommandLine.saveToFile(tempFile, "#!/bin/bash
echo 'Hello World'", false);
	 * </pre>
	 */
	private static void saveToFile(File file, String str, boolean isAppend) throws IOException {
		if (str == null)
			str = "";

		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}

		FileWriter writer = null;
		BufferedWriter bufferedWriter;
		try {
			writer = new FileWriter(file, isAppend);
			bufferedWriter = new BufferedWriter(writer);
			bufferedWriter.write(str);
			bufferedWriter.flush();
			bufferedWriter.close();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * Converts an object value to a boolean based on its type. This utility method
	 * handles different input types for boolean conversion: Boolean objects are
	 * returned as-is, String objects are converted by case-insensitive comparison
	 * to "true", and Integer objects are converted by checking if they equal 1.
	 * The method throws an IllegalArgumentException if the value type is not
	 * supported for boolean conversion. This is primarily used internally when
	 * parsing command options that expect boolean values, such as "waitfor",
	 * "echo", "inmemory", etc. The method provides type-safe boolean conversion
	 * with clear error messaging when unsupported types are encountered, making
	 * it safe to use with user-provided configuration values that might be of
	 * unexpected types. This ensures robust handling of configuration options
	 * across different input methods and formats.
	 * 
	 * @param key The option key name (used in error messages)
	 * @param value The value to convert to boolean (Boolean, String, or Integer)
	 * @return The boolean representation of the value
	 * @throws IllegalArgumentException if the value type is not supported for boolean conversion
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * boolean wait = CommandLine.toBoolean("waitfor", "true");   // Returns true
	 * boolean echo = CommandLine.toBoolean("echo", 1);          // Returns true
	 * boolean mem = CommandLine.toBoolean("inmemory", true);    // Returns true
	 * </pre>
	 */
	private static boolean toBoolean(String key, Object value) {
		if (value instanceof Boolean) {
			return (Boolean) value;
		} else if (value instanceof String) {
			return ((String) value).equalsIgnoreCase("true");
		} else if (value instanceof Integer) {
			return ((Integer) value) == 1;
		} else {
			String cls = value == null ? "null" : value.getClass().getSimpleName();
			throw new IllegalArgumentException(key + " expect boolean value but " + cls + " is found ");
		}
	}

	/**
	 * Converts an object value to an integer based on its type. This utility method
	 * handles String and Integer input types: String objects are parsed using
	 * Integer.parseInt(), while Integer objects are returned as-is. The method
	 * throws an IllegalArgumentException if the value type is not supported for
	 * integer conversion or if a string cannot be parsed as an integer. This is
	 * primarily used internally when parsing command options that expect integer
	 * values, such as "timeout", "port", or other numeric parameters. The method
	 * provides type-safe integer conversion with clear error messaging when
	 * unsupported types are encountered or when string parsing fails, making it
	 * safe to use with user-provided configuration values that might be of
	 * unexpected types or contain invalid numeric formats. This ensures robust
	 * handling of numeric configuration options across different input methods.
	 * 
	 * @param key The option key name (used in error messages)
	 * @param value The value to convert to integer (String or Integer)
	 * @return The integer representation of the value
	 * @throws IllegalArgumentException if the value type is not supported for integer conversion or string is not a valid integer
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * int timeout = CommandLine.toInt("timeout", "5000");    // Returns 5000
	 * int size = CommandLine.toInt("size", 1024);           // Returns 1024
	 * </pre>
	 */
	private static int toInt(String key, Object value) {
		if (value instanceof String) {
			return Integer.parseInt(((String) value));
		} else if (value instanceof Integer) {
			return (Integer) value;
		} else {
			String cls = value == null ? "null" : value.getClass().getSimpleName();
			throw new IllegalArgumentException(key + " expect int value but " + cls + " is found ");
		}
	}

	/**
	 * Checks whether the specified file is a script file based on its extension.
	 * This method determines if a file should be treated as a script by examining
	 * its file extension, returning true for script file extensions appropriate
	 * to the current operating system. On Windows systems, it checks for the ".bat"
	 * extension (and potentially ".cmd"), while on Unix-like systems it checks for
	 * the ".sh" extension. This is used internally to determine how to execute
	 * files - script files are typically executed through interpreters (cmd, bash)
	 * while executable files might be run directly. The check is case-insensitive
	 * to handle various capitalizations of file extensions. This method ensures
	 * that script files are executed properly regardless of the operating system,
	 * by selecting the appropriate execution strategy based on the file type.
	 * The method also properly handles filenames without extensions by returning
	 * false in such cases, since these are not considered script files by default.
	 * 
	 * @param filename The name of the file to check
	 * @return true if the file has a script extension appropriate for the current OS, false otherwise
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * boolean isScript1 = CommandLine.isScriptFile("myscript.bat");  // On Windows: true
	 * boolean isScript2 = CommandLine.isScriptFile("script.sh");     // On Unix: true
	 * boolean isScript3 = CommandLine.isScriptFile("program.exe");   // On any OS: false
	 * </pre>
	 */
	private static boolean isScriptFile(String filename) {
		int pos = filename.lastIndexOf(".");
		String ext = pos >= 0 ? filename.substring(pos) : "";
		String scriptSuffix = isWindows() ? ".bat" : ".sh";
		return ext.equalsIgnoreCase(scriptSuffix);
	}
	
	/** Convert a line string to words array */
	public static String[] lineToWord(String line) {
		List<String>words = WordParser.getWords(line);
		return words.toArray(new String[words.size()]);
	}
	
	/** Extract arguments array start from specified offset, return a new arguments array */
	public static String[] slice(String[] args, int offset) {
		List<String>words = new ArrayList<String>();
		for(int i=offset; i<args.length; i++)
			words.add(args[i]);
		return words.toArray(new String[words.size()]);
	}
	

	/** parse command line string into word array */
	private static class WordParser {
		public static List<String> getWords(String line) {
			return new WordParser(line).parse();
		}

		String line;
		List<String> words = new ArrayList<String>();
		int index;
		int len;

		public WordParser(String line) {
			this.line = line;
		}

		public List<String> parse() {
			words.clear();
			index = 0;
			len = line.length();

			while (index < len) {
				String word = readWord();
				if (!(word.length() == 0 && index == len)) // skip last empty word
					words.add(word);
			}

			return words;
		}

		private String readWord() {
			String word = "";
			while (index < len) {
				char c = line.charAt(index++);
				if (word.length() == 0) {
					if (c == ' ' || c == '\t') {
						continue;
					} else if (c == '"' || c == '\'') {
						return readString(c);
					} else {
						word += c;
					}
				} else {
					if (c == ' ' || c == '\t') {
						break;
					} else {
						word += c;
					}
				}
			}
			return word;
		}

		private String readString(char quote) {
			String word = "";
			while (index < len) {
				char c = line.charAt(index++);
				if (c == '\\') {
					word += c;
					c = line.charAt(index++);
					word += c;
				} else if (c == quote) {
					break;
				} else {
					word += c;
				}
			}
			return word;
		}

	}

	// =================== members of instance ==============

	/** command arguments */
	private List<String> words = new ArrayList<String>();

	/** waiting timeout in milliseconds. zero means waiting until process ends */
	private int timeout = DEFAULT_TIMEOUT; 

	/** Indicate whether wait for the result of command line */
	private boolean waitFor = true;

	/** Indicate whether run script in memory */
	private boolean inMemory = true;

	/** The stream that write the output */
	private PrintStream echoStream = null;

	/** environments */
	private Map<String, String> envs = new HashMap<>();

	/** working directory */
	private File workingDirectory = null;

	/** input data */
	private InputStream inputStream = null;

	/** line output handler */
	private LineOutputHandler lineHandler = null;
	
	/** result parser that parse CommandResult to JSON */
	private ResultParser resultParser;


	/**
	 * Sets the event handler for receiving notifications during command execution.
	 * This method allows you to specify a CmdEvent implementation that will be
	 * called when various events occur during the execution of the command, such
	 * as when output lines are received or when the execution completes. The event
	 * handler enables real-time monitoring and processing of command output without
	 * waiting for the entire command to complete. This is particularly useful for
	 * long-running commands where you want to provide progress feedback to users
	 * or process output as it's generated. The method returns the CommandLine
	 * instance to enable method chaining, allowing you to configure multiple
	 * parameters in a single expression. Setting the handler to null disables
	 * event notifications for this command execution. This method is part of the
	 * fluent API that allows for flexible configuration of command execution
	 * parameters before calling the exec() method.
	 * 
	 * @param h The CmdEvent handler to receive execution notifications (can be null to disable)
	 * @return This CommandLine instance to allow for method chaining
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * CommandLine cmd = new CommandLine()
	 *     .handler(line -> System.out.println("Output: " + line))
	 *     .line("ping -c 5 google.com");
	 * CommandResult result = cmd.exec();
	 * </pre>
	 */
	private CommandLine setLineHandler(LineOutputHandler h) {
		this.lineHandler = h;
		return this;
	}

	/**
	 * Clears the command words and input stream, resetting this CommandLine instance
	 * to its initial state for the command words and input data. This method
	 * removes all previously set command arguments and clears any configured
	 * input stream, but preserves other configuration settings like timeout,
	 * working directory, and environment variables. The method also closes the
	 * current input stream if one is set, releasing any associated resources.
	 * This is useful when reusing a CommandLine instance for multiple command
	 * executions, as it ensures that previous command data doesn't interfere
	 * with new commands. The clear operation is safe to call at any time and
	 * will not affect the ability to set new command parameters afterward.
	 * This method is typically used internally but can be called manually when
	 * preparing a CommandLine instance for a new command with completely different
	 * parameters. It provides a clean way to reset just the command-specific
	 * elements while preserving other configuration settings that might be
	 * reused across multiple commands.
	 */
	public void clear() {
		words.clear();
		closeInputStream();
	}

	/**
	 * Sets the execution options for this command from a Map of key-value pairs.
	 * This method processes a map of configuration options, interpreting each key
	 * to configure different aspects of command execution. Recognized options
	 * include "waitfor" (boolean) to control synchronous execution, "inmemory"
	 * (boolean) to specify temporary file handling, "echo" (boolean) to control
	 * output display, "directory" (string) to set the working directory, and
	 * "timeout" (integer) to specify execution timeout. Other keys are treated
	 * as environment variables to be set for the executing process. The method
	 * performs type conversion automatically using the internal toBoolean() and
	 * toInt() methods. This provides a flexible way to configure command execution
	 * from external configuration sources like properties files or user input.
	 * The method returns the CommandLine instance to enable method chaining for
	 * fluent API usage. Passing null for options will result in no changes to
	 * the current configuration, returning the instance unchanged for chaining.
	 * This method is essential for programmatically configuring command execution
	 * parameters based on runtime conditions or user preferences. It provides a
	 * unified interface for setting multiple execution parameters at once instead
	 * of calling each configuration method individually. The method handles case
	 * variations of option keys by converting them to lowercase for comparison.
	 * 
	 * @param opts A Map of option keys and values for command execution configuration (can be null)
	 * @return This CommandLine instance to allow for method chaining
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * Map<String, Object> options = new HashMap<>();
	 * options.put("timeout", 5000);
	 * options.put("directory", "/tmp");
	 * options.put("echo", true);
	 * CommandLine cmd = new CommandLine().options(options).line("ls -la");
	 * </pre>
	 */
	public CommandLine options(Map<String, Object> opts) {
		if (opts == null)
			return this;

		for (String key : opts.keySet()) {
			Object value = opts.get(key);
			key = key.toLowerCase();
			switch (key) {
			case "waitfor":
			case "wait":
				this.waitFor(toBoolean(key, value));
				break;
			case "inmemory":
			case "memory":
				this.inMemory(toBoolean(key, value));
				break;
			case "echo":
				this.echo(toBoolean(key, value));
				break;
			case "directory":
			case "dir":
				this.directory((String) value);
				break;
			case "timeout":
				this.timeout(toInt(key, value));
				break;
			default:
				this.env(key, value.toString());
			}
		}
		return this;
	}

	/** insert tokens at the beginning of the words */
	private void addFirst(String... tokens) {
		if (tokens != null) {
			for (int i = tokens.length - 1; i >= 0; i--) {
				words.add(0, tokens[i]);
			}
		}
	}

	/** add words at the end of the words */
	private void addLast(String... tokens) {
		if (tokens != null) {
			for (int i = 0; i < tokens.length; i++) {
				words.add(tokens[i]);
			}
		}
	}

	/**
	 * Adds command arguments to the end of the command line using varargs syntax.
	 * This method allows you to specify command and its arguments as separate
	 * parameters, which are appended to any existing command words in this
	 * CommandLine instance. The method accepts a variable number of string
	 * arguments, making it convenient to specify complex commands with multiple
	 * parameters. The first argument is typically the command itself, followed
	 * by its options and arguments. This method returns the CommandLine instance
	 * to enable method chaining, allowing multiple configuration operations to
	 * be performed in a single expression. This is particularly useful when
	 * building complex commands programmatically or when command parts are
	 * available as separate variables. The method handles null values gracefully
	 * by skipping them during the addition process. This approach provides
	 * flexibility in command construction while maintaining the fluent API
	 * design pattern that enables readable and concise command configuration.
	 * 
	 * @param args Variable arguments representing the command and its parameters (e.g., "ls", "-la", "/home")
	 * @return This CommandLine instance to allow for method chaining
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * CommandLine cmd = new CommandLine()
	 *     .command("find", "/home", "-name", "*.txt", "-size", "+1M")
	 *     .timeout(10000);
	 * </pre>
	 */
	public CommandLine command(String... args) {
		addLast(args);
		return this;
	}

	/**
	 * Returns the list of command words that have been set for this CommandLine instance.
	 * This method provides access to the current command and its arguments that have
	 * been accumulated through previous calls to command() or line() methods. The
	 * returned list contains the command and all its arguments as separate string
	 * elements, in the order they were added. This allows you to inspect the
	 * current command configuration or to modify the command before execution.
	 * The returned list is the internal list used by the CommandLine, so changes
	 * to it will affect the command that gets executed. This can be useful for
	 * debugging or for making last-minute adjustments to command parameters
	 * before execution. The list is maintained in the order that arguments
	 * were added, preserving the intended command structure. This method is
	 * particularly useful when you need to verify the command construction or
	 * when implementing command modification logic. It provides a direct view
	 * into the current state of the command being built by this instance.
	 * 
	 * @return The list of command words representing the current command and its arguments
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * CommandLine cmd = new CommandLine().command("ls", "-la");
	 * List<String> currentCommand = cmd.command(); // Returns ["ls", "-la"]
	 * System.out.println("Command: " + currentCommand);
	 * </pre>
	 */
	public List<String> command() {
		return this.words;
	}

	/**
	 * Adds command arguments to the end of the command line from a List of strings.
	 * This method allows you to specify command and its arguments as a list of
	 * strings, which are appended to any existing command words in this CommandLine
	 * instance. The method handles null lists gracefully by doing nothing, making
	 * it safe to call with potentially null inputs. If the input list is not null,
	 * it's converted to an array and each element is added as a separate command
	 * word. This method returns the CommandLine instance to enable method chaining,
	 * allowing multiple configuration operations to be performed in a single
	 * expression. This is particularly useful when the command parts are available
	 * as a list from another source, such as configuration data, user input,
	 * or command parsing results. The method preserves the order of elements in
	 * the input list, ensuring that command arguments appear in the intended sequence.
	 * This provides flexibility in command construction when working with list-based
	 * data structures while maintaining the fluent API design pattern for easy
	 * integration with other configuration methods.
	 * 
	 * @param args A List of strings representing the command and its parameters (can be null)
	 * @return This CommandLine instance to allow for method chaining
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * List<String> cmdParts = Arrays.asList("grep", "-r", "pattern", "/home");
	 * CommandLine cmd = new CommandLine().command(cmdParts).timeout(5000);
	 * </pre>
	 */
	public CommandLine command(List<String> args) {
		addLast(args == null ? null : args.toArray(new String[args.size()]));
		return this;
	}

	/**
	 * Sets the command line from a single string, parsing it into individual words.
	 * This method takes a complete command line string (like "ls -la /home") and
	 * parses it into individual words using the WordParser class, which properly
	 * handles quoted arguments and escaped characters. This is particularly useful
	 * when you have a complete command string from user input or configuration
	 * that needs to be executed. The parsing ensures that arguments containing
	 * spaces (like filenames with spaces) are preserved as single arguments
	 * rather than being split incorrectly. This method returns the CommandLine
	 * instance to enable method chaining for fluent API usage. The parsing logic
	 * handles both single and double quotes, as well as escape sequences, making
	 * it safe to use with complex command lines that include special characters
	 * or spaces. This provides a convenient way to convert user-friendly command
	 * strings into properly structured command arguments for execution. The method
	 * replaces any previously set command words with the parsed results, so it
	 * should be called before other configuration methods when building commands
	 * from string representations. This is essential for accepting command input
	 * from users or external sources while maintaining proper argument separation.
	 * 
	 * @param str The command line string to parse and execute (e.g., "grep -r 'pattern' /home")
	 * @return This CommandLine instance to allow for method chaining
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * CommandLine cmd = new CommandLine()
	 *     .line("find /home -name '*.log' -size +100M")
	 *     .timeout(30000);
	 * </pre>
	 */
	public CommandLine line(String str) {
		return command(WordParser.getWords(str));
	}

	/**
	 * Sets whether to wait for the result of command line execution synchronously.
	 * When set to true (the default), the exec() method will block until the
	 * command completes and return the full result. When set to false, the command
	 * is executed asynchronously and the exec() method returns immediately with
	 * a result indicating the process was started. On Windows, this adds "start /B"
	 * prefix to run the process in the background, while on Unix-like systems it
	 * adds "nohup" and "&" suffix to achieve the same effect. This setting is
	 * particularly useful for launching long-running processes or GUI applications
	 * that should not block the Java application. The method returns the CommandLine
	 * instance to enable method chaining for fluent API usage. This provides
	 * control over the execution model of commands, allowing applications to
	 * choose between synchronous blocking execution (for commands with expected
	 * output) or asynchronous execution (for application launchers or daemons).
	 * The asynchronous execution is especially valuable when starting external
	 * applications that have their own UI or when launching services that run
	 * independently. This method directly affects the behavior of the exec() method
	 * by controlling whether it waits for process completion before returning.
	 * It's an important configuration option for applications that need to
	 * maintain responsiveness while launching external processes. The method
	 * works in conjunction with the operating system's process management to
	 * achieve the desired execution model without requiring platform-specific
	 * code from the user. This abstraction simplifies cross-platform command
	 * execution while providing the necessary control over execution behavior.
	 * 
	 * @param value true to wait for command completion (synchronous), false to run asynchronously
	 * @return This CommandLine instance to allow for method chaining
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * // Synchronous execution - waits for completion
	 * CommandLine cmd1 = new CommandLine().waitFor(true).line("ping -c 3 google.com");
	 * CommandResult result1 = cmd1.exec(); // Blocks until ping completes
	 * 
	 * // Asynchronous execution - returns immediately
	 * CommandLine cmd2 = new CommandLine().waitFor(false).line("notepad.exe");
	 * CommandResult result2 = cmd2.exec(); // Returns immediately after starting notepad
	 * </pre>
	 */
	public CommandLine waitFor(boolean value) {
		this.waitFor = value;
		return this;
	}

	/**
	 * Gets the current wait-for status of this CommandLine instance, indicating
	 * whether command execution should be synchronous or asynchronous. This method
	 * returns the value set by the waitFor(boolean) method, reflecting whether
	 * the exec() method will block until the command completes or return immediately.
	 * The default value is true, meaning commands will execute synchronously and
	 * the exec() method will wait for completion before returning. When this method
	 * returns false, the command will be executed asynchronously, with the exec()
	 * method returning immediately after starting the process. This status can be
	 * checked before execution to confirm the intended execution model, or after
	 * execution to determine how the command was configured to run. The wait-for
	 * status is an important configuration parameter that affects the behavior
	 * of the entire command execution process. This method provides a way to
	 * query the current execution model configuration, which is useful for
	 * debugging, logging, or conditional logic based on execution mode. The
	 * method returns a simple boolean that directly corresponds to the
	 * synchronous/asynchronous execution behavior of the command. This is
	 * particularly useful in applications that dynamically configure command
	 * execution based on user preferences or operational requirements. It allows
	 * for verification of the execution mode before calling exec() to ensure
	 * the command runs as expected. The returned value reflects the current
	 * state of the execution model configuration at the time of the method call.
	 * 
	 * @return true if command execution will be synchronous (wait for completion), false if asynchronous
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * CommandLine cmd = new CommandLine().waitFor(false);
	 * boolean isAsync = cmd.waitFor(); // Returns false
	 * System.out.println("Command will run asynchronously: " + isAsync);
	 * </pre>
	 */
	public boolean waitFor() {
		return this.waitFor;
	}

	/**
	 * Gets the current timeout value for command execution in milliseconds.
	 * This method returns the timeout period that has been configured for
	 * command execution, which determines how long to wait for command completion
	 * before forcibly terminating the process. A timeout value of 0 (the default)
	 * indicates that there is no timeout, and the command will be allowed to
	 * run indefinitely until it completes naturally. When a positive timeout
	 * value is set, the exec() method will wait for the specified number of
	 * milliseconds for the command to complete, and if it hasn't finished by
	 * then, the process will be terminated forcibly. This provides protection
	 * against commands that hang or run longer than expected, preventing the
	 * application from being blocked indefinitely. The timeout is applied during
	 * the waiting phase of command execution, after the process has been started.
	 * This method allows you to check the current timeout configuration before
	 * executing a command to ensure it matches your requirements. It's particularly
	 * useful in applications that need to manage resource usage or ensure
	 * responsiveness by limiting command execution time. The returned value is
	 * always in milliseconds, providing fine-grained control over timeout duration.
	 * This configuration is part of the command's execution parameters and
	 * affects how the exec() method behaves when waiting for process completion.
	 * The timeout mechanism is implemented using Process.waitFor(timeout, TimeUnit)
	 * to provide reliable timeout handling across different operating systems.
	 * In case of timeout, the process is terminated using destroyForcibly() to
	 * ensure it doesn't continue running as a zombie process. This method provides
	 * visibility into this important execution parameter for monitoring or
	 * debugging purposes.
	 * 
	 * @return The timeout value in milliseconds (0 for no timeout)
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * CommandLine cmd = new CommandLine().timeout(10000); // 10 seconds
	 * int currentTimeout = cmd.timeout(); // Returns 10000
	 * System.out.println("Command timeout: " + currentTimeout + " ms");
	 * </pre>
	 */
	public int timeout() {
		return this.timeout;
	}
	
	/**
	 * Sets the timeout value for command execution in milliseconds. This method
	 * configures how long the exec() method will wait for command completion
	 * before forcibly terminating the process. A value of 0 (the default) means
	 * no timeout - the command will be allowed to run indefinitely. Positive
	 * values specify the maximum time to wait in milliseconds. When the timeout
	 * is exceeded, the executing process is terminated using destroyForcibly()
	 * to prevent it from continuing as a zombie process. This timeout mechanism
	 * is particularly useful for protecting against commands that may hang or
	 * run longer than expected, ensuring that your application remains responsive
	 * and doesn't get blocked indefinitely. The timeout is applied only during
	 * the waiting phase after the process has been started successfully. This
	 * method returns the CommandLine instance to enable method chaining for
	 * fluent API usage. The timeout configuration is an important safety
	 * mechanism for production applications that execute external commands,
	 * preventing resource exhaustion from runaway processes. The timeout is
	 * implemented using Process.waitFor(timeout, TimeUnit.MILLISECONDS) which
	 * provides reliable cross-platform timeout handling. This setting works
	 * in conjunction with the waitFor() method - if waitFor is false (asynchronous
	 * execution), the timeout may not apply since the method returns immediately.
	 * For synchronous execution (the default), the timeout controls how long
	 * to wait for completion before forcibly terminating the process. This
	 * provides an important safety net for command execution in automated systems
	 * or services that must maintain reliability and resource management. The
	 * method enables fine-grained control over command execution duration,
	 * allowing applications to balance between giving commands adequate time
	 * to complete and preventing excessive resource consumption from long-running
	 * or hung processes.
	 * 
	 * @param value The timeout in milliseconds (0 for no timeout)
	 * @return This CommandLine instance to allow for method chaining
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * // Set a 30-second timeout for a potentially long-running command
	 * CommandLine cmd = new CommandLine()
	 *     .timeout(30000)  // 30 seconds
	 *     .line("find /path -name 'pattern'");
	 * CommandResult result = cmd.exec(); // Will timeout after 30 seconds if not completed
	 * </pre>
	 */
	public CommandLine timeout(int value) {
		this.timeout = value;
		return this;
	}

	/**
	 * Gets the current in-memory execution setting for script execution. This method
	 * returns whether scripts should be executed directly in memory without creating
	 * temporary files. When true (the default), script content passed to runScript()
	 * methods will be executed in memory using appropriate system interpreters
	 * without being saved to temporary files first. When false, script content
	 * will be saved to temporary files before execution, which may be necessary
	 * for certain system security policies or when working with complex scripts
	 * that expect to be executed from files. The in-memory execution option
	 * provides better performance by avoiding file I/O and temporary file cleanup,
	 * but may be restricted by some system security policies. This setting affects
	 * how runScript() methods handle script content - whether they create temporary
	 * files or execute directly from memory. The method returns the current
	 * configuration value that was set by the inMemory(boolean) method. This
	 * setting is particularly relevant when executing dynamically generated
	 * scripts or when security policies control file-based execution. It provides
	 * a way to switch between execution modes based on system capabilities or
	 * security requirements. The in-memory mode is generally preferred for
	 * performance and security reasons, but file-based execution may be necessary
	 * in some environments. This method allows you to check the current execution
	 * mode before running scripts to ensure they will be executed as expected.
	 * The setting is part of the execution configuration alongside timeout,
	 * working directory, and other parameters. This provides visibility into
	 * how scripts will be handled when executed through this CommandLine instance.
	 * The default behavior is to execute in memory (true), which is more efficient
	 * and avoids temporary file creation when possible. The method returns a
	 * simple boolean that directly controls the script execution strategy used
	 * by the internal execution methods. This is important for applications that
	 * need to verify their script execution configuration before running potentially
	 * sensitive or complex scripts. The in-memory execution mode may be preferred
	 * in secure environments where temporary file creation is restricted or
	 * monitored. This method provides a way to confirm that the preferred execution
	 * mode is active before proceeding with script execution. It's also useful
	 * for debugging or logging to understand how scripts will be processed by
	 * the current CommandLine configuration. The returned value reflects the
	 * current state of the in-memory execution setting at the time of the method call.
	 * 
	 * @return true if scripts are executed in memory without temporary files, false if temporary files are used
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * CommandLine cmd = new CommandLine().inMemory(false); // Use temp files
	 * boolean usesTempFiles = !cmd.inMemory(); // Returns true
	 * System.out.println("Uses temporary files: " + usesTempFiles);
	 * </pre>
	 */
	public boolean inMemory() {
		return this.inMemory;
	}
	
	/**
	 * Sets whether to run scripts in memory without creating temporary files.
	 * When true (the default), scripts are executed directly from memory without
	 * being saved to temporary files first. When false, script content is saved
	 * to temporary files before execution. In-memory execution provides better
	 * performance by avoiding file I/O and temporary file cleanup, but may be
	 * restricted by some system security policies. This setting affects the
	 * behavior of runScript() methods when executing script content provided
	 * as strings. The method returns the CommandLine instance to enable method
	 * chaining for fluent API usage. This configuration option provides important
	 * control over script execution strategy, allowing applications to adapt
	 * to different security environments or system capabilities. In-memory
	 * execution is generally preferred for performance and security reasons
	 * as it avoids creating temporary files that might be subject to security
	 * scanning or access controls. However, some systems or specific script
	 * types may require file-based execution. The temporary file approach
	 * creates a file with appropriate extension (.bat on Windows, .sh on Unix),
	 * writes the script content to it, makes it executable if needed, executes
	 * it, and then removes it. The in-memory approach passes the script content
	 * directly to the system interpreter (cmd, bash, etc.) through input streams
	 * or command line parameters. This method allows you to configure the
	 * execution strategy based on your specific requirements or environment
	 * constraints. The choice between in-memory and file-based execution can
	 * affect performance, security, and compatibility with different system
	 * configurations. This setting is particularly important in enterprise
	 * environments where security policies may restrict file creation or
	 * certain execution methods. The method provides flexibility to accommodate
	 * different operational requirements while maintaining the same API for
	 * script execution. The configuration is stored as part of the CommandLine
	 * instance and affects all subsequent script executions using that instance.
	 * This allows for consistent behavior across multiple script executions
	 * when reusing the same CommandLine object. The setting can be changed
	 * between executions if different strategies are needed for different scripts.
	 * This provides fine-grained control over execution methods while maintaining
	 * the simplicity of the unified runScript() API. The method enables
	 * applications to dynamically adjust execution strategy based on script
	 * content, system policies, or performance requirements. This is particularly
	 * useful in applications that execute various types of scripts and need
	 * to optimize for different scenarios or work within different security
	 * contexts. The in-memory execution typically works by passing the script
	 * content as input to the system shell, while file-based execution creates
	 * a temporary file with the content and executes that file, which may be
	 * necessary for certain security policies or script requirements. The
	 * method provides a simple boolean parameter to control this complex
	 * execution behavior, abstracting the underlying implementation differences
	 * between the two execution modes. This maintains the simplicity of the
	 * API while providing the necessary control over execution strategy.
	 * 
	 * @param value true to execute scripts in memory (default), false to use temporary files
	 * @return This CommandLine instance to allow for method chaining
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * // Execute scripts in memory (default, more efficient)
	 * CommandLine cmd1 = new CommandLine().inMemory(true).line("echo test");
	 * 
	 * // Execute scripts using temporary files (when required by security policy)
	 * CommandLine cmd2 = new CommandLine().inMemory(false).line("echo test");
	 * </pre>
	 */
	public CommandLine inMemory(boolean value) {
		this.inMemory = value;
		return this;
	}

	/** Set whether echo the output */
	public CommandLine echo(boolean value) {
		this.echoStream = value ? System.out : null;
		return this;
	}

	/** Set whether echo the output to stream */
	public CommandLine echo(PrintStream stream) {
		this.echoStream = stream;
		return this;
	}

	/** Set input data stream */
	public CommandLine input(InputStream stream) {
		this.inputStream = stream;
		return this;
	}

	/** Set input data bytes */
	public CommandLine input(byte[] bytes) {
		Objects.requireNonNull(bytes);
		this.inputStream = new ByteArrayInputStream(bytes);
		return this;
	}

	/** Set input data string */
	public CommandLine input(String data) {
		Objects.requireNonNull(data);
		return input(data.getBytes());
	}

	/**
	 * Write input data to process
	 * 
	 * @throws IOException
	 */
	private void writeInputData(Process process, InputStream in) throws IOException {
		try (OutputStream outputStream = process.getOutputStream()) {
			byte[] buf = new byte[8192];
			int len = 0;
			while ((len = in.read(buf)) != -1) {
				outputStream.write(buf, 0, len);
			}
			outputStream.flush();
		}
	}

	/** Close input stream */
	private void closeInputStream() {
		try {
			if (inputStream != null)
				inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			inputStream = null;
		}

	}

	/** Get environment value of specified key */
	public String env(String key) {
		return envs.getOrDefault(key, null);
	}

	/** Set environment key-value */
	public CommandLine env(String key, String value) {
		envs.put(key, value);
		return this;
	}

	/** Set working directory */
	public CommandLine directory(String dir) {
		Objects.requireNonNull(dir);
		File f = new File(dir);
		this.workingDirectory = f;
		return this;
	}

	/** Create CommandResult */
	protected CommandResult result(int code, Object e) {
		if (e instanceof Exception)
			e = e.getClass() + " " + ((Exception) e).getMessage();

		String output = e == null ? "null" : e.toString();
		CommandResult ret = new CommandResult(code, output);

		if (this.lineHandler != null) {
			lineHandler.onLine(null);  // null means the end of output
		}

		return ret;
	}

	/** process words */
	private void processWords() {
		// when not waitFor
		if (!waitFor()) {
			if (isWindows()) {
				addFirst("start", "/B", "\"\""); // add prefix
			} else {
				addFirst("nohup"); // add prefix
				words.add("&"); // add suffix
			}
		}
		
		if (words.size() > 0 && "start".equals(words.get(0)) && isWindows()) {
			addFirst("cmd", "/c");
		}
	}

	/** print words */
	private void printWords() {
//		if (waitFor)
//			System.out.println("waiting");
//
//		if (echoStream != null)
//			System.out.println("echo");
//
//		for (String word : words)
//			System.out.print(word + " ");
//		System.out.println();
	}

	/** Execute the command line */
	public CommandResult exec() {
		return exec(null);
	}
	
	/** Execute the command line with specified event handler */
	public CommandResult exec(LineOutputHandler handler) {
		if (handler == null) handler = this.lineHandler;
		
		ProcessBuilder processBuilder = new ProcessBuilder();
		Process process;
		
		// process words
		processWords();
		processBuilder.command(words);

		// print words
		printWords();

		// redirect
		processBuilder.redirectErrorStream(true);

		// write environment
		for (String key : envs.keySet())
			processBuilder.environment().put(key, envs.get(key));

		// set working directory
		if (workingDirectory != null)
			processBuilder.directory(workingDirectory);

		// start process
		try {
			process = processBuilder.start();
		} catch (Exception e) {
			return result(500, e.getMessage());
		}

		// if there is input data, write it to process
		if (inputStream != null) {
			try {
				writeInputData(process, inputStream);
			} catch (IOException e) {
				return result(500, e.getMessage());
			} finally {
				closeInputStream();
			}
		}

		// wait for output
		if (waitFor()) {
			String CRLF = isWindows() ? "\r\n" : "\n";
			String charset = charset();
			StringBuilder out = new StringBuilder();
			String line;
			int exitCode = 0;

			try {
				// read output
				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(process.getInputStream(), charset))) {
					while ((line = reader.readLine()) != null) {
						out.append(line).append(CRLF);
						if (handler != null) {
							handler.onLine(line);
						}

						if (echoStream != null) {
							echoStream.println(line);
						}
					}
				}

				// wait for finish
				if (timeout > 0) {
					boolean isFinished = process.waitFor(timeout, TimeUnit.MILLISECONDS);
					if (isFinished) {
						exitCode = process.exitValue();
					} else {
						process.destroyForcibly(); // stop process
						exitCode = -2;
					}
				} else {
					exitCode = process.waitFor();
				}

				return result(exitCode, out);

			} catch (Exception e) {
				out.append(String.format("%s", e.getMessage()));
				return result(pid(process), out);
			}

		} else {
			return result(0, "");
		}
	}

	
	
	/** Set result parser */
	public CommandLine parser(ResultParser resultParser) {
		this.resultParser = resultParser;
		return this;
	}
	
	/** Execute the command line, and return JSON result
	 * 
	 * @return JSON object that from command line output
	 * 
	 * @see a ResultParser must be registered before call json() method.
	 */
	public JSON json() {
		return json(null);
	}
	
	/** Execute the command line, and return JSON result
	 * 
	 * @param handler  Command line event handler
	 * 
	 * @return JSON object that from command line output
	 * 
	 * @see a ResultParser must be registered before call json() method.
	 */
	public JSON json(LineOutputHandler handler) {
		if (resultParser == null) {
			throw new IllegalArgumentException("no ResultParser that convert result to JSON");
		}
		CommandResult ret = this.exec(handler);
		return resultParser.onResult(ret);
	}
	
	/**
	 * Execute a file
	 * 
	 * @param filename The filename to execute
	 * @param args     The arguments
	 * @return CommandResult object
	 */
	public CommandResult execFile(String filename, String[] args) {
		// add prefix words
		if (isScriptFile(filename)) {
			String scriptInterpreter = isWindows() ? "cmd" : "bash";
			String scriptOption = isWindows() ? "/c" : "-c";
			command(scriptInterpreter, scriptOption);
		} else {
			if (isWindows()) {
				command("start");
			} else {
				command("xdg-open");
			}
		}

		// add filename and arguments
		command(filename);
		command(args);

		return exec();

	}

	/**
	 * Execute a file
	 * 
	 * @param filename The filename to execute
	 * @return CommandResult object
	 */
	public CommandResult execFile(String filename) {
		return execFile(filename, null);
	}

	/**
	 * Execute script content. <br>
	 * <br>
	 * 
	 * @param scriptContent The script content
	 * @param args          The arguments
	 * 
	 * @return CommandResult
	 */
	public CommandResult execScript(String scriptContent) {
		return execScript(scriptContent, null);
	}

	/**
	 * Execute script content. <br>
	 * <br>
	 * 
	 * @param scriptContent The script content
	 * @param args          The arguments
	 * 
	 * @return CommandResult
	 */
	public CommandResult execScript(String scriptContent, String[] args) {
		Objects.requireNonNull(scriptContent);

		if (scriptContent.endsWith("\n"))
			scriptContent += "\n";

		if (inMemory) {
			return execScriptInMemory(scriptContent, args);
		}

		clear();

		// save the script content to a temp file, then run file
		Path tempScriptFile = null;
		try {
			// create temp file
			String scriptSuffix = isWindows() ? ".bat" : ".sh";
			tempScriptFile = Files.createTempFile("script", scriptSuffix);

			// write file content
			saveToFile(tempScriptFile.toFile(), scriptContent, false);

			// set executable
			if (!isWindows()) {
				tempScriptFile.toFile().setExecutable(true);
			}

			return execFile(tempScriptFile.toString(), args);

		} catch (Exception e) {
			return result(500, e);

		} finally {
			// important: delete temp file
			if (tempScriptFile != null && waitFor)
				tempScriptFile.toFile().delete();
		}
	}

	/**
	 * Execute script content in memory, without save it to file. <br>
	 * 
	 * @param scriptContent the script content
	 * @param wait          whether wait for the process to end.
	 * 
	 * @return CommandResult
	 */
	protected CommandResult execScriptInMemory(String scriptContent, String[] args) {
		clear();

		// add script interpreter
		String scriptInterpreter = isWindows() ? "cmd" : "bash";
		command(scriptInterpreter);
		// add option
		if (isWindows())
			command("-s");

		// use bytes of script content string as input
		input(scriptContent.getBytes());

		return exec();

	}

}
