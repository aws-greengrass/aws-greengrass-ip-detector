/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector.detector;

import com.aws.greengrass.detector.config.Config;

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
     *
     * @param config Configuration
     * @return list of IP Addresses
     * @throws SocketException SocketException
     */
    public List<InetAddress> getAllIpAddresses(Config config) throws SocketException {
        return getIpAddresses(NetworkInterface.getNetworkInterfaces(), config);
    }

    //Default for JUnit Testing
    List<InetAddress> getIpAddresses(Enumeration<NetworkInterface> interfaces, Config config) throws SocketException {
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
                    if (!config.isIncludeIPv6Addrs()) {
                        continue;
                    }
                    if (address.isLinkLocalAddress() && !config.isIncludeIPv6LinkLocalAddrs()) {
                        continue;
                    }
                    if (address.isLoopbackAddress() && !config.isIncludeIPv6LoopbackAddrs()) {
                        continue;
                    }
                } else {
                    if (!config.isIncludeIPv4Addrs()) {
                        continue;
                    }
                    if (address.isLoopbackAddress() && !config.isIncludeIPv4LoopbackAddrs()) {
                        continue;
                    }
                    if (address.isLinkLocalAddress() && !config.isIncludeIPv4LinkLocalAddrs()) {
                        continue;
                    }
                }
                ipAddresses.add(address);
            }
        }
        return ipAddresses;
    }
}

