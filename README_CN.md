

# WhiteHat
WhiteHat， 中文名白帽， 是一个网卡抓包、包分析处理的 java 库。
抓包(Packet Capture,  简称 pcap )，就是通过网卡接收网络上的各种数据包(Packet)，包括：自己的数据包，也包括别人的数据包。包括TCP、UDP等各种协议的数据包
收取数据包后，可以深入分析数据包，分析或获取情报。当然，也可以通过网卡发送任意数据包。
数据包处理是维护网络安全的重要手段。白帽黑客通过各种技术手段排查漏洞、维护网络安全。


# 安装依赖库
使用 WhiteHat前,  必须要先安装依赖库 WinPcap 或者 libpcap。请根据你的OS环境选择:

### Windows
安装 WinPcap, 下载地址： https://www.winpcap.org/install/bin/WinPcap_4_1_3.exe

### Ubuntu
```bash
apt install libpcap-dev
```

### CentOS
```bash
yum install libpcap-devel
```

### MacOS
```bash
brew install libpcap
```


# 第一个抓包程序

## 新建一个java maven 项目
新建一个java maven 项目，修改 pom.xml, 将 whitehat 引入项目中 
```
    <dependency>
    	<groupId>top.whitehat</groupId>
    	<artifactId>whitehat</artifactId>
    	<version>0.2.4</version>
    </dependency>
```
可以看到，Maven下载下来的 jar包是 whitehat-0.2.4.jar,  可以打开它，有源码的。
其中： top.whitehat.examples 中有本文所有的示例源码程序。


## 三个步骤写一个抓包程序
```java
import top.whitehat.NetCard;  //network interface card class
 
public class App1 {
 
	public static void main(String[] args) {
		// 第一步：打开网卡 （如果本机有多个网卡，自动首选连接互联网的网卡 )
		NetCard card = NetCard.inet();
		
		// 第二步：定义一个接收数据包的 handler 处理程序
		card.onPacket(packet -> {
			System.out.println(packet);
		});
		
		// 第三步：开始抓包
		card.start(); 
	}
}
```
运行结果：(注意：在Linux， Mac上运行必须具有 root权限, Windows上不用)
```
Tcp, Src: 192.168.100.212:59838, Dst: 121.36.47.3:443, [ACK], Seq=2424216054, Ack=2578474027, Win=509
Tcp, Src: 192.168.100.212:59838, Dst: 121.36.47.3:443, [ACK], Seq=2424217454, Ack=2578474027, Win=509
Tcp, Src: 121.36.47.3:443, Dst: 192.168.100.212:59838, [ACK], Seq=2578474027, Ack=2424218601, Win=499
Tcp, Src: 121.36.47.3:443, Dst: 192.168.100.212:59838, [ACK], Seq=2578474027, Ack=2424218601, Win=499
Tcp, Src: 121.36.47.3:443, Dst: 192.168.100.212:59838, [ACK], Seq=2578474422, Ack=2424218601, Win=499
Tcp, Src: 192.168.100.212:59838, Dst: 121.36.47.3:443, [ACK], Seq=2424218601, Ack=2578474456, Win=514
Udp, Src: 192.168.100.111:2103, Dst: 192.168.100.255:2103
```
这个程序的唯一作用是打印网卡抓到的各种数据包，可以看到：有各种协议的，有自己的，别人的... 

这个程序是个死循环，请在IDE中强行中断运行。

# 示例程序源码在whitehat.jar中

The source code for the example programs is inside whitehat-0.2.4.jar.

top.whitehat.examples.Example03_Filter.java     Introduces BPF packet filter

top.whitehat.examples.Example04_Dump.java       Save the packets into .pcap file

top.whitehat.examples.Example05_ReadDump.java   Read packets from the .pcap file

top.whitehat.examples.Example06_ARP.java        Receive and send ARP protocol packets

top.whitehat.examples.Example07_ICMP.java       Receive and send ICMP protocol packets

top.whitehat.examples.Example08_DNS.java        Receive and send DNS protocol packets

top.whitehat.examples.Example09_DHCP.java        Receive and send DHCP protocol packets

top.whitehat.examples.Example09_DHCP.java        Receive and send DHCP protocol packets

top.whitehat.examples.Example10_TCP_SYN.java     Receive and send TCP SYN packets

top.whitehat.examples.Example11_ScanIp.java      Scan an IP address range for exists IPs

top.whitehat.examples.Example12_ScanPort.java    Scan specified ports on specified IP range for exists ports

top.whitehat.examples.Example13_PacketParser.java    Example of parse raw bytes of a captured packet 

top.whitehat.examples.Example14_NetBIOS.java       NetBIOS send name service query and got reply

top.whitehat.examples.Example15_NetBIOS_Scan.java    Scan IPs of local network, and find NetBIOS names of exists ip.

top.whitehat.examples.Example16_mDNS.java          Sends a mDNS query and then receives a mDNS response.








