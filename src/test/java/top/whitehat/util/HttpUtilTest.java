package top.whitehat.util;

import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.Test;

public class HttpUtilTest {

	@Test
	public void testHtmlEncode() {
//		System.out.println(HttpUtil.htmlEncode("姓名：\t张三\t年龄：\t20"));
	}

	@Test
	public void test1() throws IOException {
		String url = "https://www.jostudio.com.cn/myfile/sample.txt";
		String content = HttpUtil.get(url);
		assertTrue(content.length() > 0);
	}

	@Test
	public void test2() throws IOException {
		String url = "https://www.jostudio.com.cn/myfile/sample.txt";
		HttpUtil.download(url, "sample.txt", filename -> {
			System.out.println(filename + " downloaded");
		});
	}

	@Test
	public void testOld() throws IOException {
//		String url = "https://www.jostudio.com.cn/myfile/sample.txt";
//		try(HttpUtil h = new HttpUtil()) {
//			h.request("GET", url, null);
//			InputStream in = h.getBodyStream();
//		}
//
////		// save to file
////		String saveFilename = "1.txt";
////		try (InputStream inStream = h.getBodyStream(); //
////				OutputStream outStream = new FileOutputStream(saveFilename)) {
////			byte[] buf = new byte[8192];
////			int len = 0;
////			while ((len = inStream.read(buf)) != -1) {
////				outStream.write(buf, 0, len);
////			}
////		}
//		
////		System.out.println(h.getBody());
	}

}
