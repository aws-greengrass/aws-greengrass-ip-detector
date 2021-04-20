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

@ImplementsService(name = DetectorService.DECTECTOR_SERVICE_NAME)
public class DetectorService extends PluginService {
    public static final String DECTECTOR_SERVICE_NAME = "aws.greengrass.clientdevices.IpDetector";
    private final CIU ciu;

    /**
     * Constructor.
     *
     * @param topics  Root Configuration topic for this service
     * @param ciu Ip detector
     *
     */
    @Inject
    public DetectorService(Topics topics, CIU ciu) {
        super(topics);
        this.ciu = ciu;
    }

    @Override
    protected void install() throws InterruptedException {
        super.install();
        ciu.startIpDetection();
        logger.atInfo().log("Starting ...");

    }

    @Override
    public void startup() {
        reportState(State.RUNNING);
    }
}
