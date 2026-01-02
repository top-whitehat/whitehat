package top.whitehat.examples;

import top.whitehat.NetCard;

public class Example00_Hello {

	public static void main(String[] args) {
		// Open the network interface card which is connected to the Internet
		NetCard card = NetCard.inet();
		
		// Define a handler when packet is incoming
		card.onPacket(packet -> {
			// process the packet
			System.out.println(packet);
		});
		
		// start packet capture
		card.start(); 
	}
}
