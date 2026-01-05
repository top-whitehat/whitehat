

# WhiteHat
Network security tools: packet capture, analysis, intelligence


# Install dependencis
Before using WhiteHat, you must first install the dependency library WinPcap or libpcap. Please choose based on your OS environment:

### Windows
Install WinPcap, Download url: https://www.winpcap.org/install/bin/WinPcap_4_1_3.exe

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


# The first packet capture program 

## Create Java Maven project
Create a new Java Maven project, modify the pom.xml file, and introduce the whitehat dependency into the project. 
```
    <dependency>
    	<groupId>top.whitehat</groupId>
    	<artifactId>whitehat</artifactId>
    	<version>0.2.4</version>
    </dependency>
```
As you can see, the jar package downloaded by Maven is whitehat-0.2.4.jar. It can be opened, and the source code is included.


## Three Steps to Write a Packet Capture Program
```java
import top.whitehat.NetCard;  //network interface card class
 
public class App1 {
 
	public static void main(String[] args) {
		// Step 1:  open the network interface card(The default is the one connected to the internet. )
		NetCard card = NetCard.inet();
		
		// Step 2: Define a handler program to receive packets.
		card.onPacket(packet -> {
			System.out.println(packet);
		});
		
		// Step 3: start packet capture
		card.start(); 
	}
}
```
Running result: (Note: It must be run with root privileges​ on Linux and Mac, but not required on Windows.)
```
Tcp, Src: 192.168.100.212:59838, Dst: 121.36.47.3:443, [ACK], Seq=2424216054, Ack=2578474027, Win=509
Tcp, Src: 192.168.100.212:59838, Dst: 121.36.47.3:443, [ACK], Seq=2424217454, Ack=2578474027, Win=509
Tcp, Src: 121.36.47.3:443, Dst: 192.168.100.212:59838, [ACK], Seq=2578474027, Ack=2424218601, Win=499
Tcp, Src: 121.36.47.3:443, Dst: 192.168.100.212:59838, [ACK], Seq=2578474027, Ack=2424218601, Win=499
Tcp, Src: 121.36.47.3:443, Dst: 192.168.100.212:59838, [ACK], Seq=2578474422, Ack=2424218601, Win=499
Tcp, Src: 192.168.100.212:59838, Dst: 121.36.47.3:443, [ACK], Seq=2424218601, Ack=2578474456, Win=514
Udp, Src: 192.168.100.111:2103, Dst: 192.168.100.255:2103
```
The sole function of this program is to print various data packets captured by the network card, 
which may include: packets of various protocols, your own, and those of others...


# Examples

The source code for the example programs is inside whitehat-0.2.4.jar.

[Example03_Filter.java Introduces BPF packet filter](src/main/java/top/whitehat/examples/Example03_Filter.java)

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








