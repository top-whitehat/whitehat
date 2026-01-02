package top.whitehat.net;

import java.net.UnknownHostException;
import java.util.List;
import com.sun.jna.Platform;
import top.whitehat.NetUtil;
import top.whitehat.examples.Examples;

public class TestMac {

	public static void main(String[] args) throws UnknownHostException {
		if (args.length == 0) {
			printNetCardInfo();
			
		} else {
			Examples.main(args);
		}
	}
	
	public static void printNetCardInfo() {
		System.out.println("os.name=" + System.getProperty("os.name"));
		System.out.println("java.vm.name=" + System.getProperty("java.vm.name"));;
		
		if (Platform.isMac()) {
			System.out.println("isMacOS=" + Platform.isMac());
			String gateway = NetCardInfoMac.getGatewayIP();
			System.out.println("gateway=" + gateway);

			String mac = NetCardInfoMac.getMACAddress(gateway);
			System.out.println("mac=" + mac);
		} else if (Platform.isAndroid()) {
			System.out.println("isAndroid=" + Platform.isAndroid());
			
			String gateway = NetCardInfoAndroid.getGatewayIP();
			System.out.println("gateway=" + gateway);

			String mac = NetCardInfoAndroid.getMACAddress(gateway);
			System.out.println("mac=" + mac);
			
			List<String> dns = NetCardInfoAndroid.getDnsServers();
			System.out.println("dns=" + (dns.size() > 0 ? dns.get(0) : null));
		} else if (Platform.isLinux()) {
			System.out.println("isLinux=" + Platform.isLinux());
			System.out.println("isKali=" + NetCardInfoLinux.isKali());
			
			String gateway = NetCardInfoLinux.getGatewayIP();
			System.out.println("gateway=" + gateway);

			String mac = NetCardInfoLinux.getMACAddress(gateway);
			System.out.println("mac=" + mac);
			
			List<String> dns = NetCardInfoLinux.getDnsServers();
			System.out.println("dns=" + (dns.size() > 0 ? dns.get(0) : null));
		}
		
		System.out.println("inet tcp=" +NetUtil.getInternetLocalAddressByTcp());
		System.out.println("inet ip=" + NetUtil.getInternetLocalAddress());
	}
	
}
