package top.whitehat.util;

public class HttpUtilTest {

	@org.junit.Test
	public void testHtmlEncode() {
		System.out.println(HttpUtil.htmlEncode("姓名：\t张三\t年龄：\t20"));
	}
}
