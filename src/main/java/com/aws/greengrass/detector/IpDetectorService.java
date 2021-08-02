/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector;

import com.aws.greengrass.componentmanager.KernelConfigResolver;
import com.aws.greengrass.config.Topic;
import com.aws.greengrass.config.Topics;
import com.aws.greengrass.dependency.ImplementsService;
import com.aws.greengrass.detector.config.Config;
import com.aws.greengrass.lifecyclemanager.PluginService;
import com.aws.greengrass.util.Coerce;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;


@ImplementsService(name = IpDetectorService.DETECTOR_SERVICE_NAME)
public class IpDetectorService extends PluginService {
    public static final String DETECTOR_SERVICE_NAME = "aws.greengrass.clientdevices.IPDetector";
    static final String PORT = "ssl_port";
    private final IpDetectorManager ipDetectorManager;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Config ipDetectorConfig;
    private Future<?> executorServiceFuture;
    private Topic portConfig;

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
     */
    @Override
    public void startup() throws InterruptedException {
        logger.atInfo().log("Start IP detection task");
        this.executorServiceFuture = scheduledExecutorService.scheduleAtFixedRate(() -> {
            subscribeToConfigs();
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
        if (executorServiceFuture != null) {
            executorServiceFuture.cancel(true);
        }
        super.shutdown();
    }

    private void subscribeToConfigs() {
        if (portConfig == null) {
            portConfig = this.config.lookup(KernelConfigResolver.CONFIGURATION_CONFIG_KEY, PORT);
            if (portConfig != null) {
                subscribeToMqttPortConfig();
                logger.atInfo().log("Successfully subscribed to new port config");
            }
        }
    }

    private void subscribeToMqttPortConfig() {
        portConfig.subscribe((whatHappened, node) -> {
            Integer port = Coerce.toInt(portConfig);
            if (port != null) {
                ipDetectorConfig.setMqttPort(port);
                ipDetectorManager.updateIps(ipDetectorConfig);
            }
        });
    }
}
