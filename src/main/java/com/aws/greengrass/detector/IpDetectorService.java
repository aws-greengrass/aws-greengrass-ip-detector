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
import com.aws.greengrass.lifecyclemanager.Kernel;
import com.aws.greengrass.lifecyclemanager.PluginService;
import com.aws.greengrass.lifecyclemanager.exceptions.ServiceLoadException;
import com.aws.greengrass.mqttbroker.MQTTService;
import com.aws.greengrass.util.Coerce;
import org.apache.commons.lang3.RandomUtils;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;


@ImplementsService(name = IpDetectorService.DECTECTOR_SERVICE_NAME)
public class IpDetectorService extends PluginService {
    public static final String DECTECTOR_SERVICE_NAME = "aws.greengrass.clientdevices.IpDetector";
    static final String MOQUETTE = "moquette";
    static final String IP_DECTECTOR_PORT = "port";
    private static final int DEFAULT_PERIODIC_UPDATE_INTERVAL_SEC = 180;
    private final IpDetectorManager ipDetectorManager;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Config ipDetectorConfig;
    private Future<?> future;
    private final Kernel kernel;
    private Topic portConfigTopic;

    /**
     * Constructor.
     *
     * @param kernel greengrass kernel
     * @param topics  Root Configuration topic for this service
     * @param ipDetectorManager Ip detector
     * @param scheduledExecutorService schedule task for ip detection
     * @param config config for service
     *
     */
    @Inject
    public IpDetectorService(Kernel kernel, Topics topics, IpDetectorManager ipDetectorManager,
                             ScheduledExecutorService scheduledExecutorService,
                             Config  config) {
        super(topics);
        this.kernel = kernel;
        this.ipDetectorManager = ipDetectorManager;
        this.scheduledExecutorService = scheduledExecutorService;
        this.ipDetectorConfig = config;
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
            ipDetectorManager.startConnectivityUpdate();
        }, initialDelay, 60, TimeUnit.SECONDS);
        this.future = future;
    }

    @Override
    public void install() {

        try {
            portConfigTopic = kernel.locate(MQTTService.SERVICE_NAME).getConfig()
                    .find(KernelConfigResolver.CONFIGURATION_CONFIG_KEY,
                            MOQUETTE, IP_DECTECTOR_PORT);
            if (portConfigTopic != null) {
                portConfigTopic.subscribe((whatHappened, node) -> {
                    Integer port = Coerce.toInt(portConfigTopic);
                    if (port != null) {
                        this.ipDetectorConfig.setMqttPort(port);
                        ipDetectorManager.checkAndUpdateConnectivity();
                        logger.atInfo().log("Ip Detector port changed to " + port);
                    }
                });
            }
        } catch (ServiceLoadException e) {
            logger.atWarn().log("Exception in fetching the port config falling back to default port 8883");
        }
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
