/*
 * Copyright 2026 The WhiteHat Project
 *
 * The WhiteHat Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package top.whitehat.pcap;

import junit.framework.TestCase;



import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Test code for PcapAddress class
 */
public class PcapAddressTest extends TestCase {

    public void testConstructorWithInetAddress() {
        // Test constructing PcapAddress with InetAddress
        try {
            InetAddress address = InetAddress.getByName("192.168.1.100");
            InetAddress netmask = InetAddress.getByName("255.255.255.0");
            InetAddress broadcast = InetAddress.getByName("192.168.1.255");
            InetAddress destination = InetAddress.getByName("192.168.1.1");

            PcapAddress pcapAddress = new PcapIpV4Address(address, netmask, broadcast, destination);

            assertEquals("Address should match", address, pcapAddress.getAddress());
            assertEquals("Netmask should match", netmask, pcapAddress.getNetmask());
            assertEquals("Broadcast address should match", broadcast, pcapAddress.getBroadcast());
            assertEquals("Destination address should match", destination, pcapAddress.getDestination());
        } catch (UnknownHostException e) {
            fail("Failed to create InetAddress: " + e.getMessage());
        }
    }

    public void testGetNetworkPrefixLength() {
        // Test getting network prefix length
        try {
            InetAddress address = InetAddress.getByName("192.168.1.100");
            InetAddress netmask = InetAddress.getByName("255.255.255.0");
            InetAddress broadcast = InetAddress.getByName("192.168.1.255");
            InetAddress destination = InetAddress.getByName("192.168.1.1");

            PcapAddress pcapAddress = new PcapIpV4Address(address, netmask, broadcast, destination);
            short prefixLength = pcapAddress.getNetworkPrefixLength();
            
            assertEquals("Network prefix length should be 24", 24, prefixLength);
        } catch (UnknownHostException e) {
            fail("Failed to create InetAddress: " + e.getMessage());
        }
    }

    public void testGetNetworkPrefixLengthInvalidNetmask() {
        // Test network prefix length with invalid netmask
        try {
            InetAddress address = InetAddress.getByName("192.168.1.100");
            InetAddress netmask = InetAddress.getByName("255.255.255.1"); // Invalid netmask
            InetAddress broadcast = InetAddress.getByName("192.168.1.255");
            InetAddress destination = InetAddress.getByName("192.168.1.1");

            PcapAddress pcapAddress = new PcapIpV4Address(address, netmask, broadcast, destination);
            short prefixLength = pcapAddress.getNetworkPrefixLength();
            
            assertEquals("Prefix length for invalid netmask should be -1", -1, prefixLength);
        } catch (UnknownHostException e) {
            fail("Failed to create InetAddress: " + e.getMessage());
        }
    }

    public void testEqualsAndHashCode() {
        // Test equals and hashCode methods
        try {
            InetAddress address1 = InetAddress.getByName("192.168.1.100");
            InetAddress netmask1 = InetAddress.getByName("255.255.255.0");
            InetAddress broadcast1 = InetAddress.getByName("192.168.1.255");
            InetAddress destination1 = InetAddress.getByName("192.168.1.1");
            
            InetAddress address2 = InetAddress.getByName("192.168.1.100");
            InetAddress netmask2 = InetAddress.getByName("255.255.255.0");
            InetAddress broadcast2 = InetAddress.getByName("192.168.1.255");
            InetAddress destination2 = InetAddress.getByName("192.168.1.1");

            PcapAddress pcapAddress1 = new PcapIpV4Address(address1, netmask1, broadcast1, destination1);
            PcapAddress pcapAddress2 = new PcapIpV4Address(address2, netmask2, broadcast2, destination2);

            assertEquals("Addresses with same content should be equal", pcapAddress1, pcapAddress2);
            assertEquals("Hash codes of equal addresses should be equal", pcapAddress1.hashCode(), pcapAddress2.hashCode());
        } catch (UnknownHostException e) {
            fail("Failed to create InetAddress: " + e.getMessage());
        }
    }

    public void testEqualsWithDifferentAddresses() {
        // Test comparison with different addresses
        try {
            InetAddress address1 = InetAddress.getByName("192.168.1.100");
            InetAddress netmask1 = InetAddress.getByName("255.255.255.0");
            InetAddress broadcast1 = InetAddress.getByName("192.168.1.255");
            InetAddress destination1 = InetAddress.getByName("192.168.1.1");
            
            InetAddress address2 = InetAddress.getByName("192.168.1.101"); // Different address
            InetAddress netmask2 = InetAddress.getByName("255.255.255.0");
            InetAddress broadcast2 = InetAddress.getByName("192.168.1.255");
            InetAddress destination2 = InetAddress.getByName("192.168.1.1");

            PcapAddress pcapAddress1 = new PcapIpV4Address(address1, netmask1, broadcast1, destination1);
            PcapAddress pcapAddress2 = new PcapIpV4Address(address2, netmask2, broadcast2, destination2);

            assertFalse("Addresses with different content should not be equal", pcapAddress1.equals(pcapAddress2));
        } catch (UnknownHostException e) {
            fail("Failed to create InetAddress: " + e.getMessage());
        }
    }
}