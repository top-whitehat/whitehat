package top.whitehat.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The Options class extends HashMap to provide a convenient way to specify
 * configuration options for command execution. This class offers a fluent API
 * for building option sets with key-value pairs that control various aspects
 * of command execution, such as timeout, working directory, environment
 * variables, and execution mode. The Options class provides static factory
 * methods for easy construction and includes built-in validation to ensure
 * type safety for common configuration parameters. This makes it simple to
 * pass multiple configuration options to command execution methods in a
 * structured way.
 * 
 * <h3>Usage Example</h3>
 * <pre>
 * // Create options with timeout and working directory
 * Options opts = Options.of("timeout", 5000, "directory", "/tmp", "echo", true);
 * 
 * // Add additional options using the fluent interface
 * opts.add("wait", true).add("timeout", 100);
 * </pre>
 */
public class Options extends LinkedHashMap<String, Object> {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates an Options object with the specified key-value pairs. This static
	 * factory method allows for convenient creation of options with multiple
	 * key-value pairs in a single call. The method accepts a primary key-value
	 * pair followed by alternating keys and values in the 'others' parameter.
	 * Each key must be a String, and the values can be of any type. The method
	 * validates the types of keys and ensures that each key has a corresponding
	 * value, throwing an IllegalArgumentException if the validation fails.
	 * This provides a compact way to create options with multiple settings at once.
	 * 
	 * @param key The first option key (String)
	 * @param value The first option value (any Object)
	 * @param others Alternating sequence of keys and values (key1, value1, key2, value2, ...)
	 * @return A new Options object containing all the specified key-value pairs
	 * @throws IllegalArgumentException if any key is not a String or if there's a mismatch in key-value pairs
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * // Create options with multiple parameters
	 * Options opts = Options.of("timeout", 5000, "directory", "/tmp", "echo", true);
	 * </pre>
	 */
	public static Options of(String key, Object value, Object... others) {
		Options ret = new Options();
		ret.add(key, value);
		int i = 0;
		while (i < others.length) {
			Object k = others[i++];
			if (!(k instanceof String))
				throw new IllegalArgumentException("Error type at index " + i + ", key should be String");
			if (i == others.length)
				throw new IllegalArgumentException("Error type at index " + i + ", no value");
			Object v = others[i++];
			ret.put((String) k, v);
		}
		return ret;
	}

	/**
	 * Adds a key-value pair to this Options object and returns the instance for
	 * method chaining. This method enables the fluent API pattern by returning
	 * 'this' after adding the specified key-value pair to the options map. The
	 * key should be a String that corresponds to one of the recognized option
	 * names, and the value can be of any type appropriate for that option. This
	 * method is a convenience wrapper around the put method that enables
	 * chaining multiple option additions together in a single expression.
	 * Common options include "timeout", "directory", "echo", "waitfor", and "inmemory".
	 * 
	 * @param name The option name (key) as a String
	 * @param value The option value as an Object
	 * @return This Options instance to allow for method chaining
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * Options opts = new Options();
	 * opts.add("timeout", 5000).add("directory", "/tmp").add("echo", true);
	 * </pre>
	 */
	public Options add(String name, Object value) {
		put(name, value);
		return this;
	}

	/** Convert to String array */
	public String[] toArgs() {
		List<String> args = new ArrayList<>();
		for(String key : this.keySet()) {
			Object value = this.get(key);
			args.add(key);
			if (value != null) args.add(value.toString());
		}
		return args.toArray(new String[args.size()]);
	}
}
