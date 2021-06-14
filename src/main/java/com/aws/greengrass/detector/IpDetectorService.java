/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector;

import com.aws.greengrass.componentmanager.KernelConfigResolver;
import com.aws.greengrass.config.Topic;
import com.aws.greengrass.config.Topics;
import com.aws.greengrass.dependency.ImplementsService;
import com.aws.greengrass.dependency.State;
import com.aws.greengrass.detector.config.Config;
import com.aws.greengrass.lifecyclemanager.Kernel;
import com.aws.greengrass.lifecyclemanager.PluginService;
import com.aws.greengrass.lifecyclemanager.exceptions.ServiceLoadException;
import com.aws.greengrass.mqttbroker.MQTTService;
import com.aws.greengrass.util.Coerce;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;


@ImplementsService(name = IpDetectorService.DETECTOR_SERVICE_NAME)
public class IpDetectorService extends PluginService {
    public static final String DETECTOR_SERVICE_NAME = "aws.greengrass.clientdevices.IPDetector";
    static final String MOQUETTE = "moquette";
    static final String IP_DETECTOR_PORT = "port";
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
     *
     */
    @Inject
    public IpDetectorService(Kernel kernel, Topics topics, IpDetectorManager ipDetectorManager,
                             ScheduledExecutorService scheduledExecutorService) {
        super(topics);
        this.kernel = kernel;
        this.ipDetectorManager = ipDetectorManager;
        this.scheduledExecutorService = scheduledExecutorService;
        this.ipDetectorConfig = new Config(this.config);
    }

    @Override
    public synchronized void install() {
        try {
            portConfigTopic = kernel.locate(MQTTService.SERVICE_NAME).getConfig()
                    .find(KernelConfigResolver.CONFIGURATION_CONFIG_KEY, MOQUETTE, IP_DETECTOR_PORT);
            if (portConfigTopic != null) {
                portConfigTopic.subscribe((whatHappened, node) -> {
                    Integer port = Coerce.toInt(portConfigTopic);
                    if (port != null) {
                        this.ipDetectorConfig.setMqttPort(port);
                        logger.atInfo().log("Ip Detector port changed to " + port);
                    }
                    if (inState(State.RUNNING)) {
                        ipDetectorManager.updateIps(ipDetectorConfig);
                    }
                });
            }
        } catch (ServiceLoadException e) {
            logger.atWarn().log("Exception in fetching the port config falling back to default port 8883");
        }
    }

    /**
     * Start IP Detection service.
     */
    @Override
    public synchronized void startup() {
        this.future = scheduledExecutorService.scheduleAtFixedRate(() -> {
            ipDetectorManager.startIpDetection(this.ipDetectorConfig);
        }, 0, 60, TimeUnit.SECONDS);
        reportState(State.RUNNING);
    }

    /**
     * Stop Ip Detection service.
     *
     * @throws  InterruptedException if the thread interrupted
     */
    @Override
    public synchronized void shutdown() throws InterruptedException {
        if (future != null) {
            future.cancel(true);
        }
        super.shutdown();
    }
}
