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
     * @throws SocketException SocketException
     */
    public List<InetAddress> getAllIpAddresses() throws SocketException {
        return getIpAddresses(NetworkInterface.getNetworkInterfaces());
    }

    //Default for JUnit Testing
    List<InetAddress> getIpAddresses(Enumeration<NetworkInterface> interfaces) throws SocketException {
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
                ipAddresses.add(address);
            }
        }
        return ipAddresses;
    }
}

