/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector.config;

import com.aws.greengrass.componentmanager.KernelConfigResolver;
import com.aws.greengrass.config.Topic;
import com.aws.greengrass.config.Topics;
import com.aws.greengrass.logging.api.Logger;
import com.aws.greengrass.logging.impl.LogManager;
import com.aws.greengrass.util.Coerce;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Config {
    private final Logger logger = LogManager.getLogger(Config.class);

    static final String INCLUDE_IPV4_LOOPBACK_ADDRESSES_CONFIG_KEY = "includeIPv4LoopbackAddrs";
    static final String INCLUDE_IPV4_LINK_LOCAL_ADDRESSES_CONFIG_KEY = "includeIPv4LinkLocalAddrs";
    static final String EXCLUDED_IP_ADDRESSES_CONFIG_KEY = "excludedIPAddresses";
    static final String DEFAULT_PORT_CONFIG_KEY = "defaultPort";
    static final boolean DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES = false;
    static final boolean DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES = false;
    static final int DEFAULT_PORT = 8883;

    private AtomicInteger defaultPort = new AtomicInteger(DEFAULT_PORT);
    private AtomicBoolean includeIPv4LoopbackAddrs = new AtomicBoolean(DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES);
    private AtomicBoolean includeIPv4LinkLocalAddrs = new AtomicBoolean(DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES);
    @Getter
    private final List<String> excludedIPAddresses = new ArrayList<>();

    /**
     * Config constructor.
     *
     * @param topics Root Configuration topic
     */
    public Config(Topics topics) {
        Topics configurationTopics = topics.lookupTopics(KernelConfigResolver.CONFIGURATION_CONFIG_KEY);
        configurationTopics.subscribe((whatHappened, node) -> {
            if (configurationTopics.isEmpty()) {
                this.includeIPv4LoopbackAddrs = new AtomicBoolean(DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES);
                this.includeIPv4LinkLocalAddrs = new AtomicBoolean(DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES);
                this.excludedIPAddresses.clear();
                this.defaultPort = new AtomicInteger(DEFAULT_PORT);
                return;
            }

            this.includeIPv4LoopbackAddrs = new AtomicBoolean(
                    Coerce.toBoolean(
                            configurationTopics.findOrDefault(
                                    DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES,
                                    INCLUDE_IPV4_LOOPBACK_ADDRESSES_CONFIG_KEY)));
            this.includeIPv4LinkLocalAddrs = new AtomicBoolean(
                    Coerce.toBoolean(
                            configurationTopics.findOrDefault(
                                    DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES,
                                    INCLUDE_IPV4_LINK_LOCAL_ADDRESSES_CONFIG_KEY)));
            this.defaultPort = new AtomicInteger(
                    Coerce.toInt(
                            configurationTopics.findOrDefault(DEFAULT_PORT,
                                    DEFAULT_PORT_CONFIG_KEY)));
            Topic excludedIPTopic = configurationTopics.find(EXCLUDED_IP_ADDRESSES_CONFIG_KEY);
            if (excludedIPTopic != null) {
                if (excludedIPTopic.getOnce() instanceof List) {
                    this.excludedIPAddresses.clear();
                    this.excludedIPAddresses.addAll(Coerce.toStringList(excludedIPTopic.getOnce()));
                } else {
                    logger.atWarn().kv("value", excludedIPTopic.getOnce()).log("Invalid config value for"
                            + " excludedIPAddresses. The config must be input as a list");
                }
            }

            logger.atInfo().kv("includeIPv4LoopbackAddrs", includeIPv4LoopbackAddrs.get())
                    .kv("includeIPv4LinkLocalAddrs", includeIPv4LinkLocalAddrs.get())
                    .kv("excludedIPAddresses", excludedIPAddresses)
                    .kv("defaultPort", defaultPort.get())
                    .log("Configuration updated");
        });
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
     * defaultPort getter.
     * @return integer defaultPort
     */
    public int getDefaultPort() {
        return this.defaultPort.get();
    }
}


