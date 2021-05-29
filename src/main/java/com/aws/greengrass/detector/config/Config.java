/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector.config;

import lombok.Getter;

public class Config {
    @Getter
    private int mqttPort;

    @Getter
    private boolean includeIPv4LoopbackAddrs;

    @Getter
    private boolean includeIPv4LinkLocalAddrs;

    /**
     * Config constructor.
     */
    public Config() {
        // Hardcoding for now till MQTT Broker is not publishing it.
        mqttPort = 8883;
        // IPv4 Loopback addresses and Link-Local addresses are excluded by default
        includeIPv4LoopbackAddrs = false;
        includeIPv4LinkLocalAddrs = false;
    }
}


