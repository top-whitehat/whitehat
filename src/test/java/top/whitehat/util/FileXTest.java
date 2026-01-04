package top.whitehat.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FileXTest {

	@Test
	public void test1() {
		FileX f1 = new FileX("d:\\java_app\\monitor.txt");
		FileX f2 = new FileX("http://jostudio.com.cn/myfile/", "monitor.txt");

		assertEquals(f1.getName(), f2.getName());
		assertEquals(f1.exists(), f2.exists());
		assertEquals(f1.exists(), f2.exists());
		assertEquals(f1.length(), f2.length());
		assertEquals(f1.isFile(), f2.isFile());
		assertEquals(f1.isDirectory(), f2.isDirectory());
		
//		System.out.println(f1.getAbsolutePath());
//		System.out.println(f2.getAbsolutePath());
//				
//		System.out.println(f1.getPath());
//		System.out.println(f2.getPath());
//		
//		System.out.println(f1.getAbsoluteFile());
//		System.out.println(f2.getAbsoluteFile());
//		
//		System.out.println(f1.getParent());
//		System.out.println(f2.getParent());
//		
//		System.out.println(f1.getParentFile());
//		System.out.println(f2.getParentFile());
		
	}
}
