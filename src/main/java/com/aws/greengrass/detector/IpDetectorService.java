/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector;

import com.aws.greengrass.config.Topics;
import com.aws.greengrass.dependency.ImplementsService;
import com.aws.greengrass.detector.config.Config;
import com.aws.greengrass.lifecyclemanager.PluginService;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;


@ImplementsService(name = IpDetectorService.IP_DETECTOR_SERVICE_NAME)
public class IpDetectorService extends PluginService {
    public static final String IP_DETECTOR_SERVICE_NAME = "aws.greengrass.clientdevices.IPDetector";
    private final IpDetectorManager ipDetectorManager;
    private final ScheduledExecutorService scheduledExecutorService;
    private Future<?> future;
    private final Config ipDetectorConfig;

    /**
     * Constructor.
     *
     * @param topics  Root Configuration topic for this service
     * @param ipDetectorManager Ip detector
     * @param scheduledExecutorService schedule task for ip detection
     *
     */
    @Inject
    public IpDetectorService(Topics topics, IpDetectorManager ipDetectorManager,
                             ScheduledExecutorService scheduledExecutorService) {
        super(topics);
        this.ipDetectorManager = ipDetectorManager;
        this.scheduledExecutorService = scheduledExecutorService;
        this.ipDetectorConfig = new Config(this.config);
    }

    /**
     * Start IP Detection service.
     *
     * @throws  InterruptedException if the thread interrupted
     */
    @Override
    public void startup() throws InterruptedException {
        logger.atInfo().log("Start IP detection task");
        this.future = scheduledExecutorService.scheduleAtFixedRate(() -> {
            ipDetectorManager.startIpDetection(this.ipDetectorConfig);
        }, 0, 60, TimeUnit.SECONDS);
        super.startup();
    }

    /**
     * Stop Ip Detection service.
     *
     * @throws  InterruptedException if the thread interrupted
     */
    @Override
    public void shutdown() throws InterruptedException {
        logger.atInfo().log("Stop IP detection task");
        if (future != null) {
            future.cancel(true);
        }
        super.shutdown();
    }
}
