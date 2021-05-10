/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector;

import com.aws.greengrass.config.Topics;
import com.aws.greengrass.dependency.ImplementsService;
import com.aws.greengrass.lifecyclemanager.PluginService;
import org.apache.commons.lang3.RandomUtils;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;


@ImplementsService(name = IpDetectorService.DECTECTOR_SERVICE_NAME)
public class IpDetectorService extends PluginService {
    public static final String DECTECTOR_SERVICE_NAME = "aws.greengrass.clientdevices.IpDetector";
    public static final int DEFAULT_PERIODIC_UPDATE_INTERVAL_SEC = 180;
    private final IpDetectorManager ipDetectorManager;
    private final ScheduledExecutorService scheduledExecutorService;
    private Future<?> future;

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
    }

    /**
     * Start Ip Detection service.
     *
     * @throws  InterruptedException if the thread interrupted
     */
    @Override
    public void startup() throws InterruptedException {
        super.startup();
        logger.atInfo().log("Starting ...");
        long initialDelay = RandomUtils.nextLong(0, DEFAULT_PERIODIC_UPDATE_INTERVAL_SEC);
        Future<?> future = scheduledExecutorService.scheduleAtFixedRate(() -> {
            ipDetectorManager.startIpDetection();
        }, initialDelay, 60, TimeUnit.SECONDS);
        this.future = future;
    }

    /**
     * Stop Ip Detection service.
     *
     * @throws  InterruptedException if the thread interrupted
     */
    @Override
    public void shutdown() throws InterruptedException {
        if (future != null) {
            future.cancel(true);
        }
        super.shutdown();
    }
}
