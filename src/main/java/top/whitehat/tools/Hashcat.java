package top.whitehat.tools;

import top.whitehat.util.CommandLine;
import top.whitehat.util.CommandResult;
import top.whitehat.util.FileUtil;
import top.whitehat.util.Options;

/**
 * hashcat command line
 * 
 * SEE: https://hashcat.net
 * 
 * https://blog.csdn.net/weixin_34163553/article/details/90252932
 * 
 * https://blog.csdn.net/qq_38069830/article/details/131123794
 * https://blog.csdn.net/qq_44637753/article/details/127297197
 * https://blog.csdn.net/u010132177/article/details/134286978
 * 
 * convert cap to hc22000: https://hashcat.net/cap2hashcat/
 * 
 */
public class Hashcat {
	
	public static final int DICTIONRY_ATTACK = 0;
	public static final int MASK_ATTACK = 3;

	/** executable file name */
	private static String executable = "hashcat";

	/** Check whether hashcat is installed on current OS */
	public static boolean exists() {
		return version() != null;
	}

	/** Get version of hashcat */
	public static String version() {
		CommandResult ret = CommandLine.run(executable, "--version");
		if (ret.getReturnValue() == 0)
			return ret.getOutput().trim();
		else
			return null;
	}

	/**
	 * Compose a command line to calculate hash
	 * address
	 * 
	 * @param hashType         Hash-type, such as:  1000, 22000
	 * @param attackMode       Attack-mode, such as: 0(dictionary), 3(pattern)
	 * @param hashFile         hash filename or hash value
	 * @param dictionaryOrMask Dictionary filename or mask or directory
	 * @param outputFile       Output filename
	 * @param options
	 * 
	 * @return return CommandLine object. <br>
	 *         use CommandLine.exec() method to run the command line.<br>
	 *         use CommdLine.json() method to run the command line and get result in
	 *         JSON format which is a JsonArray, each item is a item of port =>
	 *         service status.
	 */
	public static void hash(int hashType, int attackMode, String hashFile, String dictionaryOrMask, String outputFile,
			Options options) {
		// Usage: hashcat [options]... hash|hashfile [dictionary|mask|directory]...
		CommandLine cmd = new CommandLine().command(executable);
		cmd.command("-m", String.valueOf(hashType));
		cmd.command("-a", String.valueOf(attackMode));
		if (outputFile != null)
			cmd.command("-o", outputFile);
		if (options != null)
			cmd.command(options.toArgs());
		if (dictionaryOrMask != null)
			cmd.command(hashFile, dictionaryOrMask);
		
		cmd.echo(true).waitFor(false).exec();
	}
	
	/** create hash file */
	protected static String createHashFile(String filename) {
		String ext = FileUtil.getFileExt(filename);
//		String dir = FileUtil.getFilePath(filename);
		// create hash file 
		switch(ext) {
		case ".pdf":
			break;
		case ".docx":
		case ".xlsx":
		case ".pptx":
		case ".doc":
		case ".xls":
		case ".ppt":
			break;
		case ".zip":
		case ".rar":			
		case ".z":
			break;
		case ".cap":
			break;
		}
		return null;
	}

	protected static int getHashType(String filename, String hashFile) {
		return 220000; //TODO
	}
	
	/**
	 * Compose a command line to calculate hash of specified file
	 * address
	 * 
	 * @param filename  The filename, support PDF(.pdf), Office file(.docx, .xlsx, .pptx),
	 * 					 Zip file(.zip), WIFI capture packets(.hc22000) 
	 * 
	 * @return return CommandLine object. <br>
	 *         use CommandLine.exec() method to run the command line.<br>
	 *         use CommdLine.json() method to run the command line and get result in
	 *         JSON format which is a JsonArray, each item is a item of port =>
	 *         service status.
	 */
	public static void hashFile(String filename, String outputFile, Options options) {
		FileUtil.fileMustExist(filename, null);
		String hashFile = createHashFile(filename);
		FileUtil.fileMustExist(hashFile, "cannot create hash file from the file %s");
				
		int hashType = getHashType(filename, hashFile);
		
		String dictionaryFile = "common.txt";
		
		if (options == null) options = new Options();
		options.add("wait", true);
		
		hash(hashType, DICTIONRY_ATTACK, hashFile,  dictionaryFile, outputFile, options);		
		
	}

}
