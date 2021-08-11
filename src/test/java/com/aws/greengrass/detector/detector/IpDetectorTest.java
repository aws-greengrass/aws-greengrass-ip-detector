/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector.detector;

import com.aws.greengrass.detector.config.Config;
import com.aws.greengrass.utils.TestConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({MockitoExtension.class})
class IpDetectorTest {

    private IpDetector ipDetector;

    @Test
    public void GIVEN_validIps_WHEN_get_ipAddresses_THEN_ip_addresses_returned() throws SocketException {
        NetworkInterface networkInterface1 = Mockito.mock(NetworkInterface.class);
        Config config = Mockito.mock(Config.class);

        List<NetworkInterface> networkInterfaces = new ArrayList<>();
        List<InterfaceAddress> interfaceAddresses = getAllAddresses();

        Mockito.doReturn(interfaceAddresses).when(networkInterface1).getInterfaceAddresses();
        Mockito.doReturn(true).when(networkInterface1).isUp();
        // include IPv4 Loopback addresses and Link-Local addresses
        Mockito.doReturn(true).when(config).isIncludeIPv4LoopbackAddrs();
        Mockito.doReturn(true).when(config).isIncludeIPv4LinkLocalAddrs();

        networkInterfaces.add(networkInterface1);
        Enumeration<NetworkInterface> enumeration = Collections.enumeration(networkInterfaces);
        ipDetector = new IpDetector();
        List<InetAddress> ipAddresses = ipDetector.getIpAddresses(enumeration, config);

        assertEquals(3, ipAddresses.size());
        assertEquals(TestConstants.IPV4_LOOPBACK, ipAddresses.get(0).getHostAddress());
        assertEquals(TestConstants.IP_1, ipAddresses.get(1).getHostAddress());
        assertEquals(TestConstants.IPV4_LINK_LOCAL, ipAddresses.get(2).getHostAddress());
    }

    @Test
    public void GIVEN_loopbackAddress_linkLocalAddress_WHEN_get_ipAddresses_THEN_loopbackAddress_linkLocalAddress_filtered() throws SocketException {
        NetworkInterface networkInterface = Mockito.mock(NetworkInterface.class);
        Config config = Mockito.mock(Config.class);

        List<NetworkInterface> networkInterfaces = new ArrayList<>();
        List<InterfaceAddress> interfaceAddresses = getAllAddresses();

        Mockito.doReturn(interfaceAddresses).when(networkInterface).getInterfaceAddresses();
        Mockito.doReturn(true).when(networkInterface).isUp();
        // Exclude IPv4 Loopback addresses and Link-Local addresses
        Mockito.doReturn(false).when(config).isIncludeIPv4LoopbackAddrs();
        Mockito.doReturn(false).when(config).isIncludeIPv4LinkLocalAddrs();

        networkInterfaces.add(networkInterface);
        Enumeration<NetworkInterface> enumeration = Collections.enumeration(networkInterfaces);
        ipDetector = new IpDetector();
        List<InetAddress> ipAddresses = ipDetector.getIpAddresses(enumeration, config);

        assertEquals(1, ipAddresses.size());
        assertEquals(TestConstants.IP_1, ipAddresses.get(0).getHostAddress());
    }

    @Test
    public void GIVEN_noIps_WHEN_get_ipAddresses_THEN_null_returned() throws SocketException {
        ipDetector = new IpDetector();
        List<InetAddress> ipAddresses = ipDetector.getIpAddresses(null, Mockito.mock(Config.class));
        assertEquals(0, ipAddresses.size());
    }

    @Test
    public void GIVEN_network_down_WHEN_get_ipAddresses_THEN_null_returned() throws SocketException {
        NetworkInterface networkInterface1 = Mockito.mock(NetworkInterface.class);
        List<NetworkInterface> networkInterfaces = new ArrayList<>();
        Mockito.doReturn(false).when(networkInterface1).isUp();
        networkInterfaces.add(networkInterface1);
        Enumeration<NetworkInterface> enumeration = Collections.enumeration(networkInterfaces);
        ipDetector = new IpDetector();
        List<InetAddress> ipAddresses = ipDetector.getIpAddresses(enumeration, Mockito.mock(Config.class));
        assertTrue(ipAddresses.isEmpty());
    }

    private List<InterfaceAddress> getAllAddresses() {
        List<InterfaceAddress> interfaceAddresses = new ArrayList<>();
        InterfaceAddress interfaceAddress1 = Mockito.mock(InterfaceAddress.class);
        InterfaceAddress interfaceAddress2 = Mockito.mock(InterfaceAddress.class);
        InterfaceAddress interfaceAddress3 = Mockito.mock(InterfaceAddress.class);
        InterfaceAddress interfaceAddress4 = Mockito.mock(InterfaceAddress.class);
        InterfaceAddress interfaceAddress5 = Mockito.mock(InterfaceAddress.class);
        InterfaceAddress interfaceAddress6 = Mockito.mock(InterfaceAddress.class);

        InetAddress inetAddress1 = Mockito.mock(Inet4Address.class);
        InetAddress inetAddress2 = Mockito.mock(Inet4Address.class);
        InetAddress inetAddress3 = Mockito.mock(Inet6Address.class);
        InetAddress inetAddress4 = Mockito.mock(Inet6Address.class);
        InetAddress inetAddress5 = Mockito.mock(Inet6Address.class);
        InetAddress inetAddress6 = Mockito.mock(Inet4Address.class);

        Mockito.lenient().doReturn(TestConstants.IPV4_LOOPBACK).when(inetAddress1).getHostAddress();
        Mockito.doReturn(TestConstants.IP_1).when(inetAddress2).getHostAddress();
        Mockito.lenient().doReturn(TestConstants.IP_2).when(inetAddress3).getHostAddress();
        Mockito.lenient().doReturn(TestConstants.IP_3).when(inetAddress4).getHostAddress();
        Mockito.lenient().doReturn(TestConstants.IP_4).when(inetAddress5).getHostAddress();
        Mockito.lenient().doReturn(TestConstants.IPV4_LINK_LOCAL).when(inetAddress6).getHostAddress();

        Mockito.doReturn(true).when(inetAddress1).isLoopbackAddress();
        Mockito.doReturn(true).when(inetAddress6).isLinkLocalAddress();

        Mockito.doReturn(inetAddress1).when(interfaceAddress1).getAddress();
        Mockito.doReturn(inetAddress2).when(interfaceAddress2).getAddress();
        Mockito.doReturn(inetAddress3).when(interfaceAddress3).getAddress();
        Mockito.doReturn(inetAddress4).when(interfaceAddress4).getAddress();
        Mockito.doReturn(inetAddress5).when(interfaceAddress5).getAddress();
        Mockito.doReturn(inetAddress6).when(interfaceAddress6).getAddress();

        interfaceAddresses.add(interfaceAddress1);
        interfaceAddresses.add(interfaceAddress2);
        interfaceAddresses.add(interfaceAddress3);
        interfaceAddresses.add(interfaceAddress4);
        interfaceAddresses.add(interfaceAddress5);
        interfaceAddresses.add(interfaceAddress6);

        return interfaceAddresses;
    }

}
