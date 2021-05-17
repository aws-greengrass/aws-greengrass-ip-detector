/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector.uploader;

import com.aws.greengrass.deployment.DeviceConfiguration;
import com.aws.greengrass.detector.config.Config;
import com.aws.greengrass.logging.api.Logger;
import com.aws.greengrass.logging.impl.LogManager;
import com.aws.greengrass.util.Coerce;
import com.aws.greengrass.util.GreengrassServiceClientFactory;
import lombok.NonNull;
import software.amazon.awssdk.services.greengrassv2data.model.ConnectivityInfo;
import software.amazon.awssdk.services.greengrassv2data.model.UpdateConnectivityInfoRequest;
import software.amazon.awssdk.services.greengrassv2data.model.UpdateConnectivityInfoResponse;

import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class ConnectivityUpdater {

    private final DeviceConfiguration deviceConfiguration;
    private final GreengrassServiceClientFactory clientFactory;
    private final Config config;
    private final Logger logger = LogManager.getLogger(ConnectivityUpdater.class);
    private List<String> ipAddresses;
    private int port;

    /**
     * Constructor.
     *
     * @param deviceConfiguration client to get the device details
     * @param clientFactory factory to get data plane client
     * @param config config for fetching the configuration values
     */
    @Inject
    public ConnectivityUpdater(DeviceConfiguration deviceConfiguration,
                               GreengrassServiceClientFactory clientFactory, Config config) {
        this.deviceConfiguration = deviceConfiguration;
        this.clientFactory = clientFactory;
        this.config = config;
    }

    /**
     * Send ip address updates.
     *
     * @param ipAddresses list of ipAddresses
     */
    public void updateConnectivity(List<InetAddress> ipAddresses) {
        if (ipAddresses == null || ipAddresses.isEmpty()) {
            return;
        }
        List<String> ips = ipAddresses.stream().filter(ip -> ip != null && ip.getHostAddress() != null)
                .map(ip -> ip.getHostAddress()).collect(Collectors.toList());
        checkAndUploadConnectivityUpdate(ips);
    }

    //Default for JUnit Testing
    synchronized void checkAndUploadConnectivityUpdate(List<String> ips) {
        int port = config.getMqttPort();
        if (!hasIpsOrPortChanged(ips, port)) {
            return;
        }
        List<ConnectivityInfo> connectivityInfoItems = ips.stream().map(ip -> ConnectivityInfo.builder()
                .hostAddress(ip).metadata("").id(ip).portNumber(port).build())
                .collect(Collectors.toList());
        UpdateConnectivityInfoResponse connectivityInfoResponse =
                sendConnectivityUpdate(connectivityInfoItems);
        if (connectivityInfoResponse != null && connectivityInfoResponse.version() != null) {
            this.ipAddresses = ips;
            this.port = port;
            logger.atDebug().log("Connectivity information updated by ip detector");
        }
    }

    //Default for JUnit Testing
    boolean hasIpsOrPortChanged(@NonNull List<String> ips, int port) {
        if (this.ipAddresses == null) {
            return true;
        } else if (this.port == port && this.ipAddresses.size() == ips.size() && this.ipAddresses.containsAll(ips)) {
            return false;
        }
        return true;
    }

    private UpdateConnectivityInfoResponse sendConnectivityUpdate(List<ConnectivityInfo> connectivityInfoItems) {
        if (connectivityInfoItems == null || connectivityInfoItems.isEmpty()) {
            return null;
        }

        UpdateConnectivityInfoRequest updateConnectivityInfoRequest =
                UpdateConnectivityInfoRequest.builder().thingName(Coerce.toString(deviceConfiguration.getThingName()))
                        .connectivityInfo(connectivityInfoItems).build();

        return clientFactory.getGreengrassV2DataClient().updateConnectivityInfo(updateConnectivityInfoRequest);
    }

    //For Junit Testing
    void setIpAddressesAndPort(List<String> ipAddresses, int port) {
        this.ipAddresses = ipAddresses;
        this.port = port;
    }
}
