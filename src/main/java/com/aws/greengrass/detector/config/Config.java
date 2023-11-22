/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector.config;

import com.aws.greengrass.componentmanager.KernelConfigResolver;
import com.aws.greengrass.config.Topics;
import com.aws.greengrass.logging.api.Logger;
import com.aws.greengrass.logging.impl.LogManager;
import com.aws.greengrass.util.Coerce;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Config {
    private final Logger logger = LogManager.getLogger(Config.class);

    static final String INCLUDE_IPV4_LOOPBACK_ADDRESSES_CONFIG_KEY = "includeIPv4LoopbackAddrs";
    static final String INCLUDE_IPV4_LINK_LOCAL_ADDRESSES_CONFIG_KEY = "includeIPv4LinkLocalAddrs";
    static final String DEFAULT_PORT_CONFIG_KEY = "defaultPort";
    static final String EXCLUDE_IP_ADDRESSES_CONFIG_KEY = "excludeIPAddrs";
    static final boolean DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES = false;
    static final boolean DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES = false;
    static final int DEFAULT_PORT = 8883;
    // Comma-separated list of IP addresses to be excluded from the IP address list.
    static final String DEFAULT_EXCLUDE_IP_ADDRESSES = "";


    private AtomicInteger defaultPort = new AtomicInteger(DEFAULT_PORT);
    private AtomicBoolean includeIPv4LoopbackAddrs = new AtomicBoolean(DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES);
    private AtomicBoolean includeIPv4LinkLocalAddrs = new AtomicBoolean(DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES);
    private AtomicReference<String> excludeIPAddrs = new AtomicReference<>(DEFAULT_EXCLUDE_IP_ADDRESSES);

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
                this.defaultPort = new AtomicInteger(DEFAULT_PORT);
                this.excludeIPAddrs = new AtomicReference<>(DEFAULT_EXCLUDE_IP_ADDRESSES);
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
            this.excludeIPAddrs = new AtomicReference<>(
                    Coerce.toString(
                            configurationTopics.findOrDefault(
                                    DEFAULT_EXCLUDE_IP_ADDRESSES,
                                    EXCLUDE_IP_ADDRESSES_CONFIG_KEY)));

            logger.atInfo().kv("includeIPv4LoopbackAddrs", includeIPv4LoopbackAddrs.get())
                    .kv("includeIPv4LinkLocalAddrs", includeIPv4LinkLocalAddrs.get())
                    .kv("defaultPort", defaultPort.get())
                    .kv("excludeIPAddrs", excludeIPAddrs.get())
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

    /**
     * excludeIPAddrs getter.
     * @return String excludeIPAddrs
     */
    public String getExcludeIPAddrs() { return this.excludeIPAddrs.get(); }
}


