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



/**
 * LibPcap Tester
 */
public class PcapLibTest extends TestCase {

    public static void checkLibPcapExists() {
        assertTrue("libpcap should exist", PcapLib.isExists());
    }

    public void testIsExists() {
        assertTrue("libpcap should exist", PcapLib.isExists());
    }

    public void testGetVersion() {
        String version = PcapLib.pcap_lib_version();
        assertNotNull("version should not be null", version);
        assertFalse("version should not be empty", version.isEmpty());
    }

    
    public void testDatalinkNameToVal() {
        int dlt = PcapLib.pcap_datalink_name_to_val("EN10MB");
        assertTrue("EN10MB should be valid", dlt >= 0);
    }

    
    public void testDatalinkValToName() {
        String name = PcapLib.pcap_datalink_val_to_name(Pcap.DLT_EN10MB);
        System.out.println(name);
        assertEquals("DLT_EN10MB name should match", "EN10MB", name);
    }

    
    public void testDatalinkValToDescription() {
        String description = PcapLib.pcap_datalink_val_to_description(Pcap.DLT_EN10MB);
        assertNotNull("DLT_EN10MB description should not be null", description);
        assertFalse("DLT_EN10MB description should not be empty", description.isEmpty());
    }
}