/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector.detector;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class IpDetector {

    /**
     * Fetches the device ip address.
     * @param includeIPv4LoopbackAddrs whether to include IPv4 Loopback Addresses
     * @param includeIPv4LinkLocalAddrs whether to include IPv4 Link Local Addresses
     * @throws SocketException SocketException
     */
    public List<InetAddress> getAllIpAddresses(boolean includeIPv4LoopbackAddrs,
                                               boolean includeIPv4LinkLocalAddrs) throws SocketException {
        return getIpAddresses(
                NetworkInterface.getNetworkInterfaces(), includeIPv4LoopbackAddrs, includeIPv4LinkLocalAddrs);
    }

    //Default for JUnit Testing
    List<InetAddress> getIpAddresses(Enumeration<NetworkInterface> interfaces,
                                     boolean includeIPv4LoopbackAddrs,
                                     boolean includeIPv4LinkLocalAddrs) throws SocketException {
        List<InetAddress> ipAddresses = new ArrayList<>();
        if (interfaces == null) {
            return ipAddresses;
        }

        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (!networkInterface.isUp()) {
                continue;
            }

            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress address = interfaceAddress.getAddress();
                if (address instanceof Inet6Address) {
                    continue;
                }
                if (address.isLoopbackAddress() && !includeIPv4LoopbackAddrs) {
                    continue;
                }
                if (address.isLinkLocalAddress() && !includeIPv4LinkLocalAddrs) {
                    continue;
                }
                ipAddresses.add(address);
            }
        }
        return ipAddresses;
    }
}

