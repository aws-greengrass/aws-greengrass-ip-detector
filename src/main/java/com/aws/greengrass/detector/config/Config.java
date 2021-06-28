/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector.config;

import com.aws.greengrass.componentmanager.KernelConfigResolver;
import com.aws.greengrass.config.Topics;
import com.aws.greengrass.util.Coerce;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("PMD.DataClass")
public class Config {
    static final String INCLUDE_IPV4_LOOPBACK_ADDRESSES_CONFIG_KEY = "includeIPv4LoopbackAddrs";
    static final String INCLUDE_IPV4_LINK_LOCAL_ADDRESSES_CONFIG_KEY = "includeIPv4LinkLocalAddrs";
    static final int DEFAULT_MQTT_PORT = 8883;
    static final boolean DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES = false;
    static final boolean DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES = false;

    private final AtomicInteger mqttPort = new AtomicInteger(DEFAULT_MQTT_PORT);
    private final AtomicBoolean includeIPv4LoopbackAddrs = new AtomicBoolean(DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES);
    private final AtomicBoolean includeIPv4LinkLocalAddrs =
            new AtomicBoolean(DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES);

    /**
     * Config constructor.
     *
     * @param topics Root Configuration topic
     */
    public Config(Topics topics) {
        Topics configurationTopics = topics.lookupTopics(KernelConfigResolver.CONFIGURATION_CONFIG_KEY);
        if (configurationTopics != null) {
            configurationTopics.subscribe((whatHappened, node) -> {
                if (configurationTopics.isEmpty()) {
                    this.includeIPv4LoopbackAddrs.set(DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES);
                    this.includeIPv4LinkLocalAddrs.set(DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES);
                    return;
                }

                this.includeIPv4LoopbackAddrs.set(Coerce.toBoolean(configurationTopics
                        .findOrDefault(DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES,
                                INCLUDE_IPV4_LOOPBACK_ADDRESSES_CONFIG_KEY)));
                this.includeIPv4LinkLocalAddrs.set(Coerce.toBoolean(configurationTopics
                        .findOrDefault(DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES,
                                INCLUDE_IPV4_LINK_LOCAL_ADDRESSES_CONFIG_KEY)));
            });
        }
    }

    /**
     * includeIPv4LoopbackAddrs getter.
     * @return boolean includeIPv4LoopbackAddrs
     */
    public boolean isIncludeIPv4LoopbackAddrs() {
        return this.includeIPv4LoopbackAddrs.get();
    }

    /**
     * includeIPv4LinkLocalAddrs getter.
     * @return boolean includeIPv4LinkLocalAddrs
     */
    public boolean isIncludeIPv4LinkLocalAddrs() {
        return this.includeIPv4LinkLocalAddrs.get();
    }

    /**
     * MQTT Port getter.
     * @return integer MQTT Port
     */
    public int getMqttPort() {
        return this.mqttPort.get();
    }

    public void setMqttPort(int mqttPort) {
        this.mqttPort.set(mqttPort);
    }
}


