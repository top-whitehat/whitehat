package top.whitehat.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TextTest {
	static int count = 0;
	
	static void print(List<String> words) {
		for(String w : words) {
			System.out.print(w + "|");
		}
		System.out.println();
	}
	
	public static void main(String[] args) throws IOException {
		Text output = new Text();
		System.out.println("start"); System.out.flush();	
		
		String inputFile = "D:\\java_app\\device_name\\supported_devices2.csv";
		Text.readFile(inputFile, Text.CSV, row->{
			String brand = row.get(0);
			String name = row.get(1);
			String model = row.get(3);
			
			if (!name.contains(brand)) {
				name = brand + " " + name;
			}
			row = TextRow.fromWords(model, name);
			output.table.add(row);
			if (count++ > 100000) throw new RuntimeException();
		});
		
		
		output.toCSV("test.csv");
		System.out.println("end"); System.out.flush();
		output.setFieldNames("model","name");
		Map<String, Object> map = output.toMap("model", "name");
		System.out.println(map.getOrDefault("YAL-AL00", null));
	}
}
