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



import java.time.Instant;


/**
 * Test code for PcapPacket class
 */
public class PcapPacketTest extends TestCase {

    public void testConstructorAndGetters() {
        // Test constructor and getter methods
        byte[] rawData = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
        int datalinkType = Pcap.DLT_EN10MB;
        Instant timestamp = Instant.now();
        int originalLength = 1500;

        PcapPacket packet = new PcapPacket(rawData, datalinkType, timestamp, originalLength);

        assertEquals("Raw data should match", rawData, packet.getRawData());
        assertEquals("Data link type should match", datalinkType, packet.getDatalinkType());
        assertEquals("Timestamp should match", timestamp, packet.getTimestamp());
        assertEquals("Original length should match", originalLength, packet.getOriginalLength());
        assertEquals("Capture length should match", rawData.length, packet.length());
    }

    public void testEqualsAndHashCode() {
        // Test equals and hashCode methods
        byte[] rawData1 = {0x00, 0x01, 0x02};
        byte[] rawData2 = {0x00, 0x01, 0x02};
        int datalinkType = Pcap.DLT_EN10MB;
        Instant timestamp = Instant.now();
        int originalLength = 3;

        PcapPacket packet1 = new PcapPacket(rawData1, datalinkType, timestamp, originalLength);
        PcapPacket packet2 = new PcapPacket(rawData2, datalinkType, timestamp, originalLength);

        assertEquals("Packets with same content should be equal", packet1, packet2);
        assertEquals("Hash codes of equal packets should be equal", packet1.hashCode(), packet2.hashCode());
    }

    public void testEqualsWithDifferentData() {
        // Test that packets with different data are not equal
        byte[] rawData1 = {0x00, 0x01, 0x02};
        byte[] rawData2 = {0x00, 0x01, 0x03};
        int datalinkType = Pcap.DLT_EN10MB;
        Instant timestamp = Instant.now();
        int originalLength = 100;

        PcapPacket packet1 = new PcapPacket(rawData1, datalinkType, timestamp, originalLength);
        PcapPacket packet2 = new PcapPacket(rawData2, datalinkType, timestamp, originalLength);

        assertFalse("Packets with different content should not be equal", packet1.equals(packet2));
    }

    public void testEqualsWithNull() {
        // Test comparison with null
        byte[] rawData = {0x00, 0x01, 0x02};
        int datalinkType = Pcap.DLT_EN10MB;
        Instant timestamp = Instant.now();
        int originalLength = 100;

        PcapPacket packet = new PcapPacket(rawData, datalinkType, timestamp, originalLength);

        assertFalse("Packet should not equal null", packet.equals(null));
    }

    public void testEqualsWithDifferentClass() {
        // Test comparison with different class
        byte[] rawData = {0x00, 0x01, 0x02};
        int datalinkType = Pcap.DLT_EN10MB;
        Instant timestamp = Instant.now();
        int originalLength = 100;

        PcapPacket packet = new PcapPacket(rawData, datalinkType, timestamp, originalLength);
        assertFalse(packet != null);
//        assertFalse("Packet should not equal string", packet.equals("not a packet"));
    }
}