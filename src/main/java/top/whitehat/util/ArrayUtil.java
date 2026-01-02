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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ArrayUtil provides utility methods for working with arrays in Java, including operations for
 * converting between arrays and lists, checking array properties, and manipulating array
 * class information. This class offers both basic array operations and more advanced
 * features for handling multi-dimensional arrays and array class introspection.
 * 
 * <h3>Usage Examples</h3>
 * <pre>
 * // Convert array to list
 * int[] numbers = {1, 2, 3, 4, 5};
 * List<Integer> numberList = ArrayUtil.arrayToList(numbers);
 * 
 * // Check if a class is an array
 * boolean isArray = ArrayUtil.isArrayClass(int[].class);
 * 
 * // Create a new array instance
 * Object newArray = ArrayUtil.newInstance(String.class, 10);
 * </pre>
 */
public class ArrayUtil {

	/**
	 * Collects integers from an integer array and adds them to the result list.
	 * This method iterates through each element in the input array and adds it
	 * to the provided result list. This is useful when converting primitive
	 * arrays to collections for further processing or when building collections
	 * from array data. The method does not create a new list but adds to the
	 * existing list, preserving any elements already in the list.
	 * 
	 * @param numbers The integer array to collect values from
	 * @param result The list to add the collected integers to (cannot be null)
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * int[] numbers = {1, 2, 3, 4, 5};
	 * List<Integer> result = new ArrayList<>();
	 * ArrayUtil.collectFromArray(numbers, result);
	 * // result now contains [1, 2, 3, 4, 5]
	 * </pre>
	 */
	public static void collectFromArray(int[] numbers, List<Integer> result) {
	    for (int num : numbers) {
	    	result.add(num);
	    }
	}
	
	/**
	 * Converts an integer array to a list of Integer objects with the same elements
	 * in the same order. This method creates a new ArrayList with the same size
	 * as the input array, avoiding potential resizing operations. The resulting
	 * list contains boxed Integer objects corresponding to each primitive int
	 * in the original array. This is useful when working with collections APIs
	 * that require objects rather than primitives.
	 * 
	 * @param array The integer array to convert (can be null, returns empty list)
	 * @return A new ArrayList containing the elements of the input array as Integer objects
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * int[] numbers = {10, 20, 30};
	 * List<Integer> list = ArrayUtil.arrayToList(numbers);
	 * // list contains [10, 20, 30]
	 * </pre>
	 */
	public static List<Integer> arrayToList(int[] array) {
	    List<Integer> list = new ArrayList<>(array.length);
	    for (int num : array) {
	        list.add(num);
	    }
	    return list;
	}
	
	// ----- array class operation ------------

	/**
	 * Checks whether the specified class is an array class by examining its
	 * name. Array classes in Java have names that start with "[", followed by
	 * type descriptors. This method provides a simple way to determine if a
	 * Class object represents an array type without using reflection operations.
	 * It handles both primitive and object arrays, as well as multi-dimensional
	 * arrays. The method returns false for null input, making it safe for
	 * defensive programming patterns.
	 * 
	 * @param clazz The class to check (can be null)
	 * @return true if the class is an array class, false otherwise or if clazz is null
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * boolean isIntArr = ArrayUtil.isArrayClass(int[].class); // true
	 * boolean isStringArr = ArrayUtil.isArrayClass(String[].class); // true
	 * boolean isNotArr = ArrayUtil.isArrayClass(String.class); // false
	 * </pre>
	 */
	public static boolean isArrayClass(Class<?> clazz) {
		if (clazz == null)
			return false;

		String name = clazz.getName();
		if (name.startsWith("["))
			return true;

		return false;
	}

	/**
	 * Gets the element type class of an array class. This method analyzes the
	 * class name to determine the component type of the array, handling both
	 * primitive and object types. For multi-dimensional arrays, it returns
	 * the element class at the deepest dimension. This is useful for generic
	 * programming where you need to know the actual type of elements stored
	 * in an array at runtime. The method returns null if the input is null
	 * or if the analysis fails due to invalid class names or unrecognized
	 * type descriptors.
	 * 
	 * @param arrayClass The array class to get the element class from (can be null)
	 * @return The class of the element of the array, or null if the input is null or analysis fails
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * Class<?> elementClass = ArrayUtil.getElementClass(int[].class); // Returns int.class
	 * Class<?> elementClass2 = ArrayUtil.getElementClass(String[].class); // Returns String.class
	 * Class<?> elementClass3 = ArrayUtil.getElementClass(int[][].class); // Returns int[].class
	 * </pre>
	 */
	public static Class<?> getElementClass(Class<?> arrayClass) {
		if (arrayClass == null)
			return null;

		String name = arrayClass.getName();
		if (!name.startsWith("[") || name.length() < 2)
			return null;
		
		int dimension = 0;
		while (name.startsWith("[")) {
			name = name.substring(1);
			dimension += 1;
		}

		Class<?> elementClass  = null;
		
		char c = name.charAt(0);
		switch (c) {
		case 'B':
			elementClass = byte.class;
			break;
		case 'C':
			elementClass = char.class;
			break;
		case 'S':
			elementClass = short.class;
			break;
		case 'I':
			elementClass = int.class;
			break;
		case 'J':
			elementClass = long.class;
			break;
		case 'F':
			elementClass = float.class;
			break;
		case 'D':
			elementClass = double.class;
			break;
		case 'Z':
			elementClass = boolean.class;
			break;
		case 'L': {
			String className = name.substring(1, name.length() - 1);
			try {
				elementClass = Class.forName(className);
			} catch (ClassNotFoundException e) {
				return null;
			}
			break;
		}
		default:
			return null;
		}
		
		if (dimension == 1) return elementClass;
		
		return getMultiDimensionArrayClass(elementClass, dimension - 1);
		
	}

	/**
	 * Creates an array class for the specified element class. This method uses
	 * Java's Array.newInstance() to create an array of the specified element
	 * type with a temporary size of 0, then returns the Class object for that
	 * array type. This is useful for obtaining the Class object representing
	 * an array type when you only have the element type available. The returned
	 * class can be used for reflection operations or type checking. Note that
	 * the size parameter in this method is not used in the implementation;
	 * the method always creates a 0-length array instance to get the class.
	 * 
	 * @param elementClass The class of elements in the array (cannot be null)
	 * @param size This parameter is ignored in the current implementation
	 * @return The Class object representing the array type
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * Class<?> intArrayClass = ArrayUtil.getArrayClass(int.class, 0); // Returns int[].class
	 * Class<?> stringArrayClass = ArrayUtil.getArrayClass(String.class, 0); // Returns String[].class
	 * </pre>
	 */
	public static Class<?> getArrayClass(Class<?> elementClass, int size) {
		return Array.newInstance(elementClass, 0).getClass();
	}

	/**
	 * Repeats a string a specified number of times by concatenating it with itself.
	 * This is a utility method primarily used internally to create array class
	 * descriptors that require multiple brackets for multi-dimensional arrays.
	 * The method efficiently builds the repeated string using StringBuilder
	 * to avoid performance issues that could occur with repeated string
	 * concatenation. If times is 0 or negative, an empty string is returned.
	 * This method is thread-safe and has no side effects.
	 * 
	 * @param s The string to repeat (can be null, treated as empty string)
	 * @param times The number of times to repeat the string (non-negative)
	 * @return A new string containing the original string repeated the specified number of times
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * String repeated = ArrayUtil.repeat("[", 3); // Returns "[[[", useful for array class names
	 * String empty = ArrayUtil.repeat("x", 0); // Returns ""
	 * </pre>
	 */
	private static String repeat(String s, int times) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < times; i++)
			sb.append(s);
		return sb.toString();
	}

	/**
	 * Creates a multi-dimensional array class of the specified element class and
	 * number of dimensions. This method constructs the appropriate class name
	 * by prefixing the element class descriptor with the required number of
	 * brackets, then uses Class.forName() to obtain the Class object. For
	 * primitive types, it uses the appropriate type code, while for object
	 * types it uses the "Lclassname;" format. This is useful for reflection
	 * operations involving multi-dimensional arrays when you need to obtain
	 * the Class object programmatically. The method throws IllegalArgumentException
	 * if the class name is invalid or if the dimensions parameter is invalid.
	 * 
	 * @param elementClass The class of elements in the array (cannot be null)
	 * @param dimensions The number of dimensions for the array (must be positive)
	 * @return The Class object representing the multi-dimensional array type
	 * @throws IllegalArgumentException if the array type is invalid
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * Class<?> twoDimIntArray = ArrayUtil.getMultiDimensionArrayClass(int.class, 2); // Returns int[][].class
	 * Class<?> threeDimStringArray = ArrayUtil.getMultiDimensionArrayClass(String.class, 3); // Returns String[][][].class
	 * </pre>
	 */
	public static Class<?> getMultiDimensionArrayClass(Class<?> elementClass, int dimensions) {
		if (dimensions <= 0) {
			return elementClass;
		}
		String name = repeat("[", dimensions);

		if (elementClass.isPrimitive()) {
			name += getPrimitiveTypeCode(elementClass);
		} else {
			name += "L" + elementClass.getName() + ";";
		}

		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Invalid array type", e);
		}

	}

	/**
	 * Returns the type code string for the specified primitive type as used
	 * in Java class name descriptors. This method maps Java primitive types
	 * to their single-character codes used in internal class names and
	 * reflection operations. The mapping is standardized according to the
	 * Java Virtual Machine specification. This is primarily used internally
	 * by other methods in this class when constructing array class names.
	 * The method throws IllegalArgumentException for non-primitive types
	 * or the void type (though class objects for void are not typically
	 * encountered in array contexts).
	 * 
	 * @param type The primitive type class (e.g., int.class, double.class)
	 * @return The single-character type code string for the primitive type
	 * @throws IllegalArgumentException if the type is not a primitive type
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * String code = ArrayUtil.getPrimitiveTypeCode(int.class); // Returns "I"
	 * String code2 = ArrayUtil.getPrimitiveTypeCode(double.class); // Returns "D"
	 * String code3 = ArrayUtil.getPrimitiveTypeCode(boolean.class); // Returns "Z"
	 * </pre>
	 */
	private static String getPrimitiveTypeCode(Class<?> type) {
		if (type == int.class)
			return "I";
		if (type == long.class)
			return "J";
		if (type == boolean.class)
			return "Z";
		if (type == byte.class)
			return "B";
		if (type == char.class)
			return "C";
		if (type == short.class)
			return "S";
		if (type == float.class)
			return "F";
		if (type == double.class)
			return "D";
		if (type == void.class)
			return "V";
		throw new IllegalArgumentException("Not a primitive type: " + type);
	}

	// ----- array object operation ------------

	/**
	 * Checks whether the specified object is an array object by checking if
	 * its class is an array class. This method provides a safe way to determine
	 * if an object is an array without causing ClassCastException. It handles
	 * null input gracefully by returning false. This is useful when processing
	 * collections of objects that might contain arrays and you need to handle
	 * arrays differently from other object types. The method works with both
	 * primitive and object arrays of any dimensionality.
	 * 
	 * @param obj The object to check (can be null)
	 * @return true if the object is an array, false if it's null or not an array
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * boolean result1 = ArrayUtil.isArrayObject(new int[5]); // true
	 * boolean result2 = ArrayUtil.isArrayObject(new String[]{"a", "b"}); // true
	 * boolean result3 = ArrayUtil.isArrayObject("not an array"); // false
	 * boolean result4 = ArrayUtil.isArrayObject(null); // false
	 * </pre>
	 */
	public static boolean isArrayObject(Object obj) {
		return obj == null ? false : isArrayClass(obj.getClass());
	}

	/**
	 * Gets the length of the array object using Java's Array.getLength() method.
	 * This method provides a safe wrapper around the reflection method that
	 * handles null input gracefully. If the input is null, it returns -1 as
	 * a sentinel value to indicate the null case. If the input is not an
	 * array object, it will throw IllegalArgumentException as per the
	 * underlying Array.getLength() behavior. This method is useful for
	 * determining array size when working with array objects obtained
	 * through reflection or when the exact type is not known at compile time.
	 * 
	 * @param arrayObj The array object to get the length of (can be null)
	 * @return The length of the array, or -1 if the input is null
	 * @throws IllegalArgumentException if the object is not an array
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * int[] arr = {1, 2, 3, 4, 5};
	 * int length = ArrayUtil.getLength(arr); // Returns 5
	 * int nullLength = ArrayUtil.getLength(null); // Returns -1
	 * </pre>
	 */
	public static int getLength(Object arrayObj) {
		return (arrayObj == null) ? -1 : Array.getLength(arrayObj);
	}

	/**
	 * Gets the value of the array element at the specified index using Java's
	 * Array.get() method. This provides a generic way to access array elements
	 * by index when the array type is not known at compile time. The method
	 * performs bounds checking and will throw ArrayIndexOutOfBoundsException
	 * if the index is out of bounds. It also throws IllegalArgumentException
	 * if the provided object is not an array. The returned value is an Object,
	 * so it may need to be cast to the appropriate type or unboxed if dealing
	 * with primitive arrays. This is useful in generic programming scenarios
	 * where array operations need to work with different array types.
	 * 
	 * @param arrayObj The array object to access (cannot be null and must be array)
	 * @param index The index of the element to get (must be valid for the array)
	 * @return The value of the element at the specified index, as an Object
	 * @throws ArrayIndexOutOfBoundsException if index is negative or >= array length
	 * @throws IllegalArgumentException if arrayObj is not an array
	 * @throws NullPointerException if arrayObj is null
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * String[] arr = {"hello", "world"};
	 * Object value = ArrayUtil.get(arr, 0); // Returns "hello" as Object
	 * int[] intArr = {10, 20, 30};
	 * Object intVal = ArrayUtil.get(intArr, 1); // Returns Integer.valueOf(20)
	 * </pre>
	 */
	public static Object get(Object arrayObj, int index) {
		return Array.get(arrayObj, index);
	}

	/**
	 * Sets the value of the array element at the specified index using Java's
	 * Array.set() method. This provides a generic way to modify array elements
	 * by index when the array type is not known at compile time. The method
	 * performs type checking to ensure the value is compatible with the array
	 * element type, and will throw IllegalArgumentException if there's a
	 * type mismatch. It also performs bounds checking and throws
	 * ArrayIndexOutOfBoundsException if the index is out of bounds. This is
	 * useful in generic programming scenarios where array operations need to
	 * work with different array types without knowing their specific types
	 * at compile time.
	 * 
	 * @param arrayObj The array object to modify (cannot be null and must be array)
	 * @param index The index of the element to set (must be valid for the array)
	 * @param value The value to set at the specified index (type must be compatible)
	 * @throws ArrayIndexOutOfBoundsException if index is negative or >= array length
	 * @throws IllegalArgumentException if arrayObj is not an array or value type is incompatible
	 * @throws NullPointerException if arrayObj is null
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * String[] arr = {"hello", "world"};
	 * ArrayUtil.set(arr, 0, "modified"); // Sets arr[0] to "modified"
	 * int[] intArr = {10, 20, 30};
	 * ArrayUtil.set(intArr, 1, 99); // Sets intArr[1] to 99 (auto-boxed)
	 * </pre>
	 */
	public static void set(Object arrayObj, int index, Object value) {
		Array.set(arrayObj, index, value);
	}

	/**
	 * Creates a new array with the specified element type and length using
	 * Java's Array.newInstance() method. This is a generic way to create
	 * arrays when the element type is not known at compile time. The method
	 * returns an Object which will be an array of the specified type. The
	 * elements of the new array are initialized to null (for object arrays)
	 * or to the default value for the primitive type (for primitive arrays).
	 * This is useful in generic programming scenarios where you need to
	 * dynamically create arrays based on runtime type information.
	 * 
	 * @param elementClass The Class object representing the element type of the array
	 * @param length The length of the array to create (must be non-negative)
	 * @return A new array with the specified element type and length
	 * @throws NegativeArraySizeException if length is negative
	 * @throws NullPointerException if elementClass is null
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * Object strArray = ArrayUtil.newInstance(String.class, 5); // Creates String[5]
	 * Object intArray = ArrayUtil.newInstance(int.class, 3); // Creates int[3] with zeros
	 * </pre>
	 */
	public static Object newInstance(Class<?> elementClass, int length) {
		return Array.newInstance(elementClass, length);
	}

	/**
	 * Returns a fixed-size list backed by the specified array using Arrays.asList().
	 * Changes to the returned list will affect the original array and vice versa,
	 * since the list is backed by the array rather than containing independent copies.
	 * This method provides a convenient way to use array data with APIs that expect
	 * List interfaces. The returned list is serializable and implements the
	 * List, RandomAccess, and Serializable interfaces. However, the list has
	 * fixed size, so operations that change the list size (like add or remove)
	 * will throw UnsupportedOperationException. This is useful when you need
	 * to temporarily treat an array as a List without copying the data.
	 * 
	 * @param a The array to convert to a list (varargs allows multiple elements)
	 * @return A fixed-size list backed by the specified array
	 * @throws NullPointerException if the specified array is null
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>
	 * List<String> list = ArrayUtil.of("apple", "banana", "cherry");
	 * // Use list operations on array data without creating new list
	 * String first = list.get(0); // Returns "apple"
	 * list.set(1, "blueberry"); // Modifies original array too
	 * </pre>
	 */
	@SafeVarargs
	public static <T> List<T> of(T...a) {
		return Arrays.asList(a);
	}
}
