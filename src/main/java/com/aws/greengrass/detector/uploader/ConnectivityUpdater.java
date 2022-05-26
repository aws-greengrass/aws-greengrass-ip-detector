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
import com.aws.greengrass.util.Utils;
import lombok.NonNull;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.greengrassv2data.model.ConnectivityInfo;
import software.amazon.awssdk.services.greengrassv2data.model.UpdateConnectivityInfoRequest;
import software.amazon.awssdk.services.greengrassv2data.model.UpdateConnectivityInfoResponse;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class ConnectivityUpdater {
    private final Logger logger = LogManager.getLogger(ConnectivityUpdater.class);

    private final DeviceConfiguration deviceConfiguration;
    private final GreengrassServiceClientFactory clientFactory;
    private List<String> ipAddresses;
    private int defaultPort;

    /**
     * Constructor.
     *
     * @param deviceConfiguration client to get the device details
     * @param clientFactory factory to get data plane client
     */
    @Inject
    public ConnectivityUpdater(DeviceConfiguration deviceConfiguration, GreengrassServiceClientFactory clientFactory) {
        this.deviceConfiguration = deviceConfiguration;
        this.clientFactory = clientFactory;
    }

    /**
     * Upload IP addresses to cloud if they have changed.
     *
     * @param ipAddresses list of ipAddresses
     * @param config Configuration values
     */
    public void updateIpAddresses(List<InetAddress> ipAddresses, Config config) {
        if (ipAddresses == null || ipAddresses.isEmpty()) {
            return;
        }
        List<String> ips = ipAddresses.stream().filter(ip -> ip != null && ip.getHostAddress() != null)
                .map(InetAddress::getHostAddress).collect(Collectors.toList());
        uploadAddresses(ips, config);
    }

    @SuppressWarnings("PMD.AvoidInstanceofChecksInCatchClause")
    synchronized void uploadAddresses(List<String> ips, Config config) {
        int defaultPort = config.getDefaultPort();
        if (!hasIpsChanged(ips) && defaultPort == this.defaultPort) {
            return;
        }
        List<ConnectivityInfo> connectivityInfoItems = ips.stream().map(ip -> ConnectivityInfo.builder()
                .hostAddress(ip).metadata("").id(ip).portNumber(defaultPort).build())
                .collect(Collectors.toList());
        try {
            UpdateConnectivityInfoResponse connectivityInfoResponse =
                    updateConnectivityInfo(connectivityInfoItems);
            if (connectivityInfoResponse != null && connectivityInfoResponse.version() != null) {
                this.ipAddresses = ips;
                this.defaultPort = defaultPort;
                logger.atInfo().kv("IPs", ips).kv("defaultPort", defaultPort).log("Uploaded IP addresses");
            }
        } catch (SdkException e) {
            if (Utils.getUltimateCause(e) instanceof UnknownHostException) {
                // Let the user know if Internet connectivity is lost so they do not try to debug their IAM policies
                //   immediately
                logger.atWarn()
                        .log("Failed to upload the IP addresses. An unknown host exception was thrown. "
                                + "This may indicate that Internet connectivity has been lost.");

                return;
            }
            if (e instanceof SdkServiceException
                    && HttpStatusCode.FORBIDDEN == ((SdkServiceException) e).statusCode()) {
                logger.atWarn()
                        .log("Failed to upload the IP addresses. Make sure that the core device's IoT policy "
                                + "grants the greengrass:UpdateConnectivityInfo permission. "
                                + "Also the Greengrass service role must be associated to your AWS account with the "
                                + "iot:GetThingShadow and iot:UpdateThingShadow permissions.", e);
                return;
            }
            // Catch all error message
            logger.atWarn()
                    .log("Failed to upload the IP addresses.", e);
        }
    }

    //Default for JUnit Testing
    boolean hasIpsChanged(@NonNull List<String> ips) {
        if (this.ipAddresses == null) {
            return true;
        } else {
            return this.ipAddresses.size() != ips.size() || !this.ipAddresses.containsAll(ips);
        }
    }

    //Default for JUnit Testing
    boolean hasPortChanged(int port) {
        return this.defaultPort != port;
    }

    private UpdateConnectivityInfoResponse updateConnectivityInfo(List<ConnectivityInfo> connectivityInfoItems) {
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
        this.defaultPort = port;
    }
}
