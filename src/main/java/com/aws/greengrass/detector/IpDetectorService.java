/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector;

import com.aws.greengrass.config.Topics;
import com.aws.greengrass.dependency.ImplementsService;
import com.aws.greengrass.dependency.State;
import com.aws.greengrass.lifecyclemanager.PluginService;

import javax.inject.Inject;

@ImplementsService(name = IpDetectorService.DECTECTOR_SERVICE_NAME)
public class IpDetectorService extends PluginService {
    public static final String DECTECTOR_SERVICE_NAME = "aws.greengrass.clientdevices.IpDetector";
    private final IpDetectorManager ipDetectorManager;

    /**
     * Constructor.
     *
     * @param topics  Root Configuration topic for this service
     * @param ipDetectorManager Ip detector
     *
     */
    @Inject
    public IpDetectorService(Topics topics, IpDetectorManager ipDetectorManager) {
        super(topics);
        this.ipDetectorManager = ipDetectorManager;
    }

    @Override
    public void startup() {
        reportState(State.RUNNING);
        logger.atInfo().log("Starting ...");
        ipDetectorManager.startIpDetection();
    }

    @Override
    public void shutdown() {
        ipDetectorManager.stopIpDetection();
    }
}
