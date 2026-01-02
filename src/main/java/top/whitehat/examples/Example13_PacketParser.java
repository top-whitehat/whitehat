package top.whitehat.examples;

import top.whitehat.NetCard;
import top.whitehat.NetUtil;
import top.whitehat.Match;

import top.whitehat.packet.Ethernet;
import top.whitehat.packet.IpV4Packet;
import top.whitehat.packet.UdpPacket;
import top.whitehat.dns.DnsPacket;
import top.whitehat.dns.DnsQueryType;

/**
 * Example of parse raw bytes of a captured packet 
 * 
 */
public class Example13_PacketParser {
	public static void main(String[] args) {

		// Open the network card that is connected to Internet
		NetCard card = NetCard.inet();
		card.filter("udp");

		// Define a listener to handle incoming packets
		card.onPacket(packet -> {
			// An UDP packet with 53 port is wanted
			if (Match.port(packet, 53)) {
				// raw data bytes that captured
				byte[] data = packet.array();
				System.out.println("When a packet is captured, we got raw bytes:");
				System.out.println(NetUtil.toHex(data, " ", 0, data.length, 32));
				System.out.println("---------------------------------------------------------------");

				// the outer layer is Ethernet packet
				Ethernet ether = new Ethernet(data);
				System.out.println("The outer layer packet is Ethernet packet ");
				System.out.println("head length: " + ether.headerLength());
				System.out.println("dstMac : " + ether.dstMac());
				System.out.println("srcMac : " + ether.srcMac());
				System.out.println("type   : " + String.format("0x%04x", ether.type()));

				byte[] payload = ether.payload();
				System.out.println("payload: ");
				System.out.println(NetUtil.toHex(payload, " ", 0, payload.length, 32));
				System.out.println("---------------------------------------------------------------");

				// Next layer: when ether.type() = 0x0800, the payload is an IPv4 packet
				IpV4Packet ipV4Packet = new IpV4Packet(payload);
				System.out.println("The next layer packet is IpV4Packet ");
				System.out.println("head length: " + ipV4Packet.headerLength());
				System.out.println("srcIp   : " + ipV4Packet.srcIp());
				System.out.println("dstIp   : " + ipV4Packet.dstIp());
				System.out.println("protocol: " + String.format("0x%04x", ipV4Packet.protocol()));

				payload = ipV4Packet.payload();
				System.out.println("payload: ");
				System.out.println(NetUtil.toHex(payload, " ", 0, payload.length, 32));
				System.out.println("---------------------------------------------------------------");

				// Next layer: when ipV4Packet.protocol() = 0x11, the payload is an UdpPacket
				UdpPacket udpPacket = new UdpPacket(payload);
				System.out.println("The next layer packet is UdpPacket ");
				System.out.println("head length: " + udpPacket.headerLength());
				System.out.println("srcPort  : " + udpPacket.srcPort());
				System.out.println("dstPort  : " + udpPacket.dstPort());

				payload = udpPacket.payload();
				System.out.println("payload: ");
				System.out.println(NetUtil.toHex(payload, " ", 0, payload.length, 32));
				System.out.println("---------------------------------------------------------------");

				// Next layer: when udp.port() = 53, the payload is a DnsPacket
				DnsPacket dnsPacket = new DnsPacket(payload);
				System.out.println("The next layer packet is DnsPacket ");
				System.out.println("queryType  : " + dnsPacket.queryType() + "(" + DnsQueryType.of(dnsPacket.queryType()) + ")");
				System.out.println("queryName  : " + dnsPacket.queryName());
				System.out.println("---------------------------------------------------------------");

				card.stop();
			}
		});

		// Start packet capture. this will not return until stop() is called.
		card.start();

		// This will run after stop() is called.
		System.out.println("program ends.");
	}

}
