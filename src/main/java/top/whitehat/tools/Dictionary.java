package top.whitehat.tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;



/**
 * Common weak password dictionary generator (for authorized security testing
 * only). <br>
 * <br>
 * Generates candidate passwords from typical patterns: base words,
 * company terms, names with years, common patterns with digits/dates, leet
 * speak, prefixes/suffixes, keyboard walks, and date fragments. <br><br>
 * 
 * This tool is intended strictly for authorized security testing​ and
 * self-assessment. Do not use it against systems or accounts without explicit,
 * written permission. <br>
 * <br>
 * To reduce output size, adjust minLen​ and maxLen​ or prune categories (e.g.,
 * remove KEYBOARD_PATTERNS​ or L33T_MAP​ expansions).<br>
 */
public class Dictionary {

	/** generate password dictionary */
	public static List<String> gen() {
		return new Dictionary().generate().list();
	}
	
	/** generate password dictionary
	 * 
	 * @param minLength  minimum password length 
	 * @param maxLength  maximum password length 
	 * @return
	 */
	public static List<String> gen(int minLength, int maxLength) {
		return new Dictionary(minLength, maxLength).generate().list();
	}

	// Character sets
	protected static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
	protected static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	protected static final String SPECIAL = "!@#$%^&*-_=+|;:,.<>?";
	private static final String DIGITS = "0123456789";

	/** commonly used weak passwords  */
	public static List<String> PASSWORDS = Arrays.asList( //
			"000000", "1111", "111111", "11111111", "112233", "123123", "123321", "123456", "1234567", "12345678",
			"1234", "12345", "123456789", "654321", "666666", "888888", "66666666", "88888888", "abcdef", "abcabc",
			"abc123", "a1b2c3", "aaa111", "123qwe", "password", "p@ssword", "pass", "passwd", "password1",
			"password123", "5201314", "abc123", "admin", "root", "guest", "test", "abc123", "monkey", "superman",
			"passw0rd", "football", "qwerty", "asdfghjkl", "letmein", "welcome", "user", "login", "sunshine",
			"iloveyou", "princess", "dragon", "baseball", "football", "charlie", "thomas", "jordan", "michael",
			"shadow", "master", "hello", "freedom", "whatever", "qazwsx", "1q2w3e4r", "1qaz2wsx", "zxcvbnm", "asdfgh",
			"qwertyuiop", "123qwe");
	

// Organization/product terms to mix into patterns
	private static final List<String> COMPANY_TERMS = Arrays.asList("company", "corp", "inc", "ltd", "tech", "cloud", "dev",
			"app", "service", "platform", "internal", "prod", "staging", "test", "uat", "demo", "portal", "admin",
			"web", "api");
	
// First and last names for name-based patterns
	private static final List<String> FIRST_NAMES = Arrays.asList("alice", "bob", "charlie", "david", "eve", "frank", "grace",
			"heidi", "ivan", "judy");
	private static final List<String> LAST_NAMES = Arrays.asList("smith", "johnson", "williams", "jones", "brown", "davis",
			"miller", "wilson", "moore", "taylor");

	// Common keyboard walks
	private static final List<String> KEYBOARD_PATTERNS = Arrays.asList("qwerty", "qwertyuiop", "asdfghjkl", "zxcvbnm",
			"1qaz", "2wsx", "3edc", "4rfv", "5tgb", "6yhn", "7ujm", "8ik,", "9ol.");

	// Common suffixes and prefixes
	private static final List<String> SUFFIXES = Arrays.asList("", "123", "1234", "12345", "123456", "!", "@", "#", "1!",
			"12!", "123!");
	private static final List<String> PREFIXES = Arrays.asList("", "admin", "root", "test", "user", "guest");

	// Year and date fragments
	private static final List<String> YEARS = Arrays.asList("2025", "2024", "2023", "2022", "2021", "2020", "2019", "2018",
			"2017", "2016", "2015");
	private static final List<String> YEAR_SHORTS = Arrays.asList("25", "24", "23", "22", "21", "20", "19", "18", "17", "16",
			"15");
	private static final List<String> MONTHS = Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11",
			"12");
	private static final List<String> DAYS = Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11",
			"12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
			"30", "31");

	// Leet (1337) substitution map
	private static final Map<Character, List<String>> L33T_MAP = createL33TMap();
	
	/** Creates the L33T_MAP using Java 8 compatible syntax */
	private static Map<Character, List<String>> createL33TMap() {
		Map<Character, List<String>> map = new HashMap<>();
		map.put('a', Arrays.asList("a", "A", "4", "@"));
		map.put('e', Arrays.asList("e", "E", "3"));
		map.put('i', Arrays.asList("i", "I", "1", "!"));
		map.put('o', Arrays.asList("o", "O", "0"));
		map.put('s', Arrays.asList("s", "S", "5", "$"));
		map.put('t', Arrays.asList("t", "T", "7"));
		map.put('l', Arrays.asList("l", "L", "1", "|"));
		map.put('g', Arrays.asList("g", "G", "9"));
		map.put('b', Arrays.asList("b", "B", "8"));
		return map;
	}

	private final int minLen;
	private final int maxLen;
	private final List<String> candidates;
//	private final Random rnd = ThreadLocalRandom.current();

	
	public Dictionary() {
		this.minLen = 0;
		this.maxLen = 0;
		this.candidates = new ArrayList<>();
	}
	
	/**
	 * Constructs the generator with inclusive min/max password length bounds.
	 *
	 * @param minLen minimum allowed password length (>= 1)
	 * @param maxLen maximum allowed password length (>= minLen)
	 */
	
	/** Constructor */
	public Dictionary(int minLen, int maxLen) {
		this.minLen = Math.max(1, minLen);
		this.maxLen = Math.max(this.minLen, maxLen);
		this.candidates = new ArrayList<>();
	}

	/**
	 * Triggers the full generation pipeline: base words, company terms, name+year
	 * patterns, common patterns, keyboard walks, leet variants, prefix/suffix, and
	 * date patterns.
	 */
	public Dictionary generate() {
		addBaseWords();
		if (this.maxLen > 0) {
			addCompanyTerms();
			addNameYearPatterns();
			addCommonPatterns();
			addKeyboardPatterns();
			// addLeetAndCaseVariants();
			addPrefixSuffixPatterns();
			addDatePatterns();
		}
		return this;
	}

	/** Adds common base weak passwords. */
	private void addBaseWords() {
		candidates.addAll(PASSWORDS);
	}

	/** Adds company/product terms and frequent suffixes. */
	private void addCompanyTerms() {
		for (String term : COMPANY_TERMS) {
			candidates.add(term);
			candidates.add(term + "123");
			candidates.add(term + "!@#");
			candidates.add(term + "2025");
			candidates.add(term + "2024");
			candidates.add(term + "01");
			candidates.add(term + "12");
		}
	}

	/** Generates name + last name combinations with optional year suffixes. */
	private void addNameYearPatterns() {
		for (String first : FIRST_NAMES) {
			for (String last : LAST_NAMES) {
				String full = first + last;
				candidates.add(full);
				candidates.add(last + first);
				candidates.add(first + "." + last);
				candidates.add(first + "_" + last);
				for (String y : YEARS) {
					candidates.add(full + y);
					candidates.add(last + y);
					candidates.add(first + y);
					candidates.add(full + y.substring(2)); // short year
				}
				for (String ys : YEAR_SHORTS) {
					candidates.add(full + ys);
				}
			}
		}
	}

	/** Appends digits and common suffixes to base words. */
	private void addCommonPatterns() {
		for (String w : PASSWORDS) {
			for (int i = 0; i < DIGITS.length(); i++) {
				String d = "" + DIGITS.charAt(i);
				candidates.add(w + d);
				candidates.add(d + w);
			}

			for (String suf : SUFFIXES) {
				candidates.add(w + suf);
			}

			for (String pre : PREFIXES) {
				candidates.add(pre + w);
			}
		}
	}

	/** Adds keyboard walk patterns and their common suffixes. */
	private void addKeyboardPatterns() {
		candidates.addAll(KEYBOARD_PATTERNS);
		for (String p : KEYBOARD_PATTERNS) {
			candidates.add(p + "123");
			candidates.add("123" + p);
			candidates.add(p + "!");
			candidates.add("!" + p);
		}
	}

	/** Generates leet-speak and mixed-case variants for existing candidates. */
	protected void addLeetAndCaseVariants() {
		Set<String> leetVariants = new HashSet<>();
		for (String w : candidates) {
			List<List<String>> replacements = new ArrayList<>();
			boolean hasReplaceable = false;
			for (char c : w.toCharArray()) {
				List<String> repl = L33T_MAP.getOrDefault(c, Arrays.asList(String.valueOf(c)));
				replacements.add(repl);
				if (repl.size() > 1)
					hasReplaceable = true;
			}
			if (!hasReplaceable)
				continue;
			// Cartesian product to build all leet variants
			cartesianProduct(replacements, new ArrayList<>(), leetVariants, w.length());
		}
		candidates.addAll(leetVariants);
	}

	/** Helper to compute the Cartesian product of character replacement lists. */
	private void cartesianProduct(List<List<String>> lists, List<String> current, Set<String> result, int targetLen) {
		if (current.size() == lists.size()) {
			String cand = String.join("", current);
			if (cand.length() <= maxLen)
				result.add(cand);
			return;
		}
		for (String s : lists.get(current.size())) {
			List<String> next = new ArrayList<>(current);
			next.add(s);
			cartesianProduct(lists, next, result, targetLen);
		}
	}

	/**
	 * Prepends prefixes and appends suffixes to existing candidates (within length
	 * bounds).
	 */
	private void addPrefixSuffixPatterns() {
		Set<String> more = new HashSet<>();
		for (String w : candidates) {
			if (w.length() > maxLen)
				continue;
			for (String pre : PREFIXES) {
				String c = pre + w;
				if (c.length() >= minLen && c.length() <= maxLen)
					more.add(c);
			}
			for (String suf : SUFFIXES) {
				String c = w + suf;
				if (c.length() >= minLen && c.length() <= maxLen)
					more.add(c);
			}
		}
		candidates.addAll(more);
	}

	/** Adds stand alone years and common date fragments (YYYY, YY, MM, DD). */
	private void addDatePatterns() {
		for (String y : YEARS) {
			candidates.add(y);
			candidates.add("20" + y.substring(2));
		}
		candidates.addAll(YEAR_SHORTS);
		candidates.addAll(MONTHS);
		candidates.addAll(DAYS);
	}

	/**
	 * Returns the generated candidates as an immutable list.
	 *
	 * @return unmodifiable list of candidate passwords
	 */
	public List<String> getCandidates() {
		return Collections.unmodifiableList(new ArrayList<>(candidates));
	}

	/**
	 * Writes the candidates to the specified file (UTF-8), one per line.
	 *
	 * @param path output file path
	 * @throws IOException if an I/O error occurs
	 */
	public void writeToFile(String path) throws IOException {
		Path p = Paths.get(path);
		try (BufferedWriter out = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING)) {
			for (String c : candidates) {
				out.write(c);
				out.newLine();
			}
		}
	}

	/**
	 * Prints the first N candidates to stdout for preview.
	 *
	 * @param limit number of candidates to preview (<= 0 to disable)
	 */
	public void preview(int limit) {
		if (limit <= 0)
			return;
		candidates.stream().limit(limit).forEach(System.out::println);
	}

	/** get size */
	public int size() {
		return candidates.size();
	}

	/** return the List */
	public List<String> list() {
		return candidates;
	}

}