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



import java.util.List;


/**
 * Test code for PcapNetworkInterface class
 */
public class PcapNetworkInterfaceTest extends TestCase {

    public void testFindAllDevs() {
        // Test finding all network interfaces
        List<PcapNetworkInterface> interfaces = PcapNetworkInterface.findAllDevs();
        assertNotNull("Interface list should not be null", interfaces);
        assertTrue("Interface count should be greater than or equal to 0", interfaces.size() >= 0);
    }

    public void testGetByIndex() {
        // Test getting network interface by index
        List<PcapNetworkInterface> interfaces = PcapNetworkInterface.findAllDevs();
        if (interfaces.size() > 0) {
            PcapNetworkInterface nif = PcapNetworkInterface.getByIndex(0);
            assertNotNull("Interface at index 0 should not be null", nif);
        }
    }

    public void testFindByName() {
        // Test finding network interface by name
        List<PcapNetworkInterface> interfaces = PcapNetworkInterface.findAllDevs();
        if (interfaces.size() > 0) {
            String name = interfaces.get(0).getName();
            assertNotNull("Interface name should not be null", name);
            
            PcapNetworkInterface nif = PcapNetworkInterface.getByName(name);
            assertNotNull("Interface found by name should not be null", nif);
        }
    }

    public void testGetAddresses() {
        // Test getting address list of network interface
        List<PcapNetworkInterface> interfaces = PcapNetworkInterface.findAllDevs();
        if (interfaces.size() > 0) {
            PcapNetworkInterface nif = interfaces.get(0);
            List<PcapAddress> addresses = nif.getAddresses();
            assertNotNull("Address list should not be null", addresses);
        }
    }

    public void testGetHardwareAddress() {
        // Test getting hardware address
        List<PcapNetworkInterface> interfaces = PcapNetworkInterface.findAllDevs();
        if (interfaces.size() > 0) {
            PcapNetworkInterface nif = interfaces.get(0);
            byte[] mac = nif.getHardwareAddress();
            // May be null, but should not throw exception
            if (mac != null) {
                assertTrue("MAC address length should be 6 or 0", mac.length == 6 || mac.length == 0);
            }
        }
    }

    public void testFind() {
        // Test finding network interface by IP or name
        List<PcapNetworkInterface> interfaces = PcapNetworkInterface.findAllDevs();
        if (interfaces.size() > 0) {
            PcapNetworkInterface firstInterface = interfaces.get(0);
            String name = firstInterface.getName();
            if (name != null) {
                PcapNetworkInterface found = PcapNetworkInterface.find(name);
                assertNotNull("Interface found by name should not be null", found);
            }
        }
    }
}