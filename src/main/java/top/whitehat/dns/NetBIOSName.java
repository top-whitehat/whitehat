package top.whitehat.dns;


/** NetBios name encode and decode */
public class NetBIOSName {
	private static final char[] ENCODING_TABLE = { //
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P' };

	/** Convert encoded char to a number (0-15) */
	private static int charToValue(char c) {
		if (c < 'A' || c > 'P') {
			throw new IllegalArgumentException("encode char should in 'A' to 'P'"); 
		}
		return c - 'A';
	}

	/** Encode name bytes to String<br>
	 *  根据RFC1002中QUESTION_NAME字段的编码: 将16字节的原始NetBIOS名转换为32字节的ASCII字符串<br>
	 *  每个字节的高4位和低4位分别被转换为一个ASCII字符（0->'A', 1->'B', ..., 15->'P'）.
	 */		
	private static String encode(byte[] nameBytes) {
		if (nameBytes.length != 16) {
			throw new IllegalArgumentException("NetBIOS name should be 16 bytes");
		}

		StringBuilder encoded = new StringBuilder(32);

		for (byte b : nameBytes) {
			// get higher 4 bits
			int high = (b & 0xFF) >>> 4;
			// get lower 4 bits
			int low = b & 0x0F;

			// map to char 'A' - 'P'
			encoded.append(ENCODING_TABLE[high]);
			encoded.append(ENCODING_TABLE[low]);
		}

		return encoded.toString();
	}
	
	/** Create encoded query name */
	public static String createQueryName(String queryName) {
		byte[] bs = queryName.getBytes();
		if (bs.length > 16) {
			throw new IllegalArgumentException("the query name should less than 16 bytes");
		}
		
		byte[] nameBytes = new byte[16];
		for(int i=0; i<bs.length; i++) nameBytes[i] = bs[i];
		
		return encode(nameBytes);
	}

	/** Decode string to name bytes<br> */
	private static byte[] decode(String encodedString) {
		if (encodedString.length() != 32) {
			throw new IllegalArgumentException("the encoded string should be 32 bytes");
		}

		byte[] decodedBytes = new byte[16];
		char[] chars = encodedString.toUpperCase().toCharArray();

		for (int i = 0; i < 16; i++) {
			char highChar = chars[2 * i];
			char lowChar = chars[2 * i + 1];

			// convert high char and low char to number
			// 将字符反向映射为数字（A->0, B->1, ..., P->15）
			int high = charToValue(highChar);
			int low = charToValue(lowChar);

			// compose high, low into one byte
			decodedBytes[i] = (byte) ((high << 4) | low);
		}

		return decodedBytes;
	}

	// ------------ members --------------
	
	/** name of service */
	public String name = "";
	
	/** type of the name */
	public int nameType;
	
	/** flags */
	public int flags;
	
	/** Indicate whether it is a group */
	public boolean isGroup;
    
	/** Create an empty NetBIOSName object */
	public NetBIOSName() {
	}
	
	/** Create a NetBIOSName object from encoded string */
	public NetBIOSName(String encodedStr) {
		if (encodedStr.length() > 0) {
			byte[] decodedBytes = NetBIOSName.decode(encodedStr);
			nameType = decodedBytes[15];
			name = new String(decodedBytes, 0, 15).trim();
		}
	}

	public String toString() {
		if (name.length() == 0) return name;
		if ("*".equals(name) && nameType == 0) return name;
				
		return name + "<" + String.format("%02x", nameType) + ">" ;
	}

}

