package com.aws.greengrass.detector.detector;

import com.aws.greengrass.util.Utils;

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
    public List<String> getAllIpAddresses() throws SocketException {
        return checkIpAddressesUpdates(NetworkInterface.getNetworkInterfaces());
    }

    /**
     * Checks if ip addresses are updated.
     * @throws SocketException SocketException
     */
    //Default for JUnit Testing
    List<String> checkIpAddressesUpdates(Enumeration<NetworkInterface> interfaces) throws SocketException {
        if (interfaces == null) {
            return null;
        }

        List<String> ipAddresses = new ArrayList<>();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (!networkInterface.isUp()) {
                continue;
            }

            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress address = interfaceAddress.getAddress();
                String ipAddress = address.getHostAddress();
                if (Utils.isNotEmpty(ipAddress)) {
                    ipAddresses.add(ipAddress);
                }
            }
        }
        return ipAddresses;
    }
}

