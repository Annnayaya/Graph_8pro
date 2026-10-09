package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import junit.framework.TestCase;
import java.net.InetAddress;

public class UdpIpFilterTest extends TestCase {

    public void testExactIpMatch() throws Exception {
        InetAddress sender = InetAddress.getByName("192.168.0.214");
        InetAddress target = InetAddress.getByName("192.168.0.214");
        assertTrue("相同 IP 报文必须允许通过", GisUDP.isPacketSourceMatched(sender, target));
    }

    public void testIpMismatch() throws Exception {
        InetAddress sender = InetAddress.getByName("192.168.0.214");
        InetAddress target1 = InetAddress.getByName("192.168.10.60");
        InetAddress target2 = InetAddress.getByName("192.168.10.61");
        InetAddress target3 = InetAddress.getByName("192.168.1.100");

        assertFalse("非目标 IP (192.168.10.60) 报文必须被丢弃", GisUDP.isPacketSourceMatched(sender, target1));
        assertFalse("非目标 IP (192.168.10.61) 报文必须被丢弃", GisUDP.isPacketSourceMatched(sender, target2));
        assertFalse("非目标 IP (192.168.1.100) 报文必须被丢弃", GisUDP.isPacketSourceMatched(sender, target3));
    }

    public void testLoopbackMatch() throws Exception {
        InetAddress sender = InetAddress.getByName("127.0.0.1");
        InetAddress target = InetAddress.getByName("127.0.0.1");
        assertTrue("本地回环 IP 报文允许通过", GisUDP.isPacketSourceMatched(sender, target));
    }

    public void testNullSafety() throws Exception {
        InetAddress valid = InetAddress.getByName("192.168.0.214");
        assertFalse("目标为 null 时必须拒绝", GisUDP.isPacketSourceMatched(valid, null));
        assertFalse("来源为 null 时必须拒绝", GisUDP.isPacketSourceMatched(null, valid));
        assertFalse("两者为 null 时必须拒绝", GisUDP.isPacketSourceMatched(null, null));
    }
}
