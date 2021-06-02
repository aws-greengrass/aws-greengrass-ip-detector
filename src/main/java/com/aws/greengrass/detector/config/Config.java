/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector.config;

import com.aws.greengrass.componentmanager.KernelConfigResolver;
import com.aws.greengrass.config.Topics;
import com.aws.greengrass.util.Coerce;
import lombok.Getter;

public class Config {
    @Getter
    private int mqttPort;

    @Getter
    private boolean includeIPv4LoopbackAddrs;

    @Getter
    private boolean includeIPv4LinkLocalAddrs;

    static final String INCLUDE_IPV4_LOOPBACK_ADDRESSES_CONFIG_KEY = "includeIPv4LoopbackAddrs";
    static final String INCLUDE_IPV4_LINK_LOCAL_ADDRESSES_CONFIG_KEY = "includeIPv4LinkLocalAddrs";
    static final int DEFAULT_MQTT_PORT = 8883;
    static final boolean DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES = false;
    static final boolean DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES = false;

    /**
     * Config constructor.
     *
     * @param topics Root Configuration topic
     */
    public Config(Topics topics) {
        Topics configurationTopics = topics.lookupTopics(KernelConfigResolver.CONFIGURATION_CONFIG_KEY);
        configurationTopics.subscribe((whatHappened, node) -> {
            // Hardcoding port for now till MQTT Broker is not publishing it.
            this.mqttPort = DEFAULT_MQTT_PORT;

            if (configurationTopics.isEmpty()) {
                this.includeIPv4LoopbackAddrs = DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES;
                this.includeIPv4LinkLocalAddrs = DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES;
                return;
            }

            this.includeIPv4LoopbackAddrs = Coerce.toBoolean(
                    configurationTopics.findOrDefault(
                            DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES, INCLUDE_IPV4_LOOPBACK_ADDRESSES_CONFIG_KEY));
            this.includeIPv4LinkLocalAddrs = Coerce.toBoolean(
                    configurationTopics.findOrDefault(
                            DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES, INCLUDE_IPV4_LINK_LOCAL_ADDRESSES_CONFIG_KEY));
        });
    }
}


