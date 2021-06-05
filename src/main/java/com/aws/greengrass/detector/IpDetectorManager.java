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
    private final Logger logger = LogManager.getLogger(IpDetectorManager.class);

    /**
     * Constructor.
     *
     * @param connectivityUpdater client to update connectivity information
     * @param ipDetector utility to detect IP addresses
     */
    @Inject
    public IpDetectorManager(ConnectivityUpdater connectivityUpdater, IpDetector ipDetector) {
        this.ipDetector = ipDetector;
        this.connectivityUpdater = connectivityUpdater;
    }

    void updateIps(Config config) {
        List<InetAddress> ipAddresses = null;
        try {
            ipAddresses = ipDetector.getAllIpAddresses(config);
            if (ipAddresses.isEmpty()) {
                logger.atDebug().log("No valid IP address found");
                return;
            }
        } catch (SocketException e) {
            logger.atError().log("Encountered a socket exception {}", e);
            return;
        }
        connectivityUpdater.updateIpAddresses(ipAddresses, config);
    }

    /**
     * Start getting the ip addresses of the device and see if there are any changes.
     *
     * @param config Configuration
     */
    public void startIpDetection(Config config) {
        try {
            updateIps(config);
        } catch (Exception e) {
            logger.atError().log("Exception occurred when updating IP addresses {}", e);
        }
    }
}
