/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector;

import com.aws.greengrass.detector.config.Config;
import com.aws.greengrass.detector.detector.IpDetector;
import com.aws.greengrass.detector.uploader.ConnectivityUpdater;
import com.aws.greengrass.logging.api.Logger;
import com.aws.greengrass.logging.impl.LogManager;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import javax.inject.Inject;

@SuppressWarnings("PMD.AvoidCatchingGenericException")
public class IpDetectorManager {
    private final ConnectivityUpdater connectivityUpdater;
    private final IpDetector ipDetector;
    private final Config config;
    private final Logger logger = LogManager.getLogger(IpDetectorManager.class);

    /**
     * Constructor.
     *
     * @param connectivityUpdater client to update connectivity information
     * @param ipDetector utility to detect IP addresses
     * @param config config for fetching the configuration values
     */
    @Inject
    public IpDetectorManager(ConnectivityUpdater connectivityUpdater, IpDetector ipDetector, Config config) {
        this.ipDetector = ipDetector;
        this.connectivityUpdater = connectivityUpdater;
        this.config = config;
    }

    void updateIps() {
        List<InetAddress> ipAddresses = null;
        try {
            ipAddresses = ipDetector.getAllIpAddresses(
                    config.isIncludeIPv4LoopbackAddrs(), config.isIncludeIPv4LinkLocalAddrs());
            if (ipAddresses.isEmpty()) {
                logger.atDebug().log("No valid ip Address found in ip detector");
                return;
            }
        } catch (SocketException e) {
            logger.atError().log("IP Detector socket exception {}", e);
            return;
        }
        connectivityUpdater.updateIpAddresses(ipAddresses);
    }

    /**
     * Start getting the ip addresses of the device and see if there are any changes.
     */
    public void startIpDetection() {
        try {
            updateIps();
        } catch (Exception e) {
            logger.atError().log("Exception occured in updating ip addresses {}", e);
        }
    }
}
