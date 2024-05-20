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

@SuppressWarnings("PMD.DataClass")
public class Config {
    private final Logger logger = LogManager.getLogger(Config.class);

    static final String INCLUDE_IPV4_LOOPBACK_ADDRESSES_CONFIG_KEY = "includeIPv4LoopbackAddrs";
    static final String INCLUDE_IPV6_LOOPBACK_ADDRESSES_CONFIG_KEY = "includeIPv6LoopbackAddrs";
    static final String INCLUDE_IPV4_LINK_LOCAL_ADDRESSES_CONFIG_KEY = "includeIPv4LinkLocalAddrs";
    static final String INCLUDE_IPV6_LINK_LOCAL_ADDRESSES_CONFIG_KEY = "includeIPv6LinkLocalAddrs";
    static final String INCLUDE_IPV4_ADDRESSES_CONFIG_KEY = "includeIPv4Addrs";
    static final String INCLUDE_IPV6_ADDRESSES_CONFIG_KEY = "includeIPv6Addrs";
    static final String DEFAULT_PORT_CONFIG_KEY = "defaultPort";
    static final boolean DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES = false;
    static final boolean DEFAULT_INCLUDE_IPV6_LOOPBACK_ADDRESSES = false;
    static final boolean DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES = false;
    static final boolean DEFAULT_INCLUDE_IPV6_LINK_LOCAL_ADDRESSES = false;
    static final boolean DEFAULT_INCLUDE_IPV4_ADDRESSES = true;
    static final boolean DEFAULT_INCLUDE_IPV6_ADDRESSES = false;
    static final int DEFAULT_PORT = 8883;

    private final AtomicInteger defaultPort = new AtomicInteger(DEFAULT_PORT);
    private final AtomicBoolean includeIPv4LoopbackAddrs
            = new AtomicBoolean(DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES);
    private final AtomicBoolean includeIPv6LoopbackAddrs
            = new AtomicBoolean(DEFAULT_INCLUDE_IPV6_LOOPBACK_ADDRESSES);
    private final AtomicBoolean includeIPv4LinkLocalAddrs
            = new AtomicBoolean(DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES);
    private final AtomicBoolean includeIPv6LinkLocalAddrs
            = new AtomicBoolean(DEFAULT_INCLUDE_IPV6_LINK_LOCAL_ADDRESSES);
    private final AtomicBoolean includeIPv4Addrs
            = new AtomicBoolean(DEFAULT_INCLUDE_IPV4_ADDRESSES);
    private final AtomicBoolean includeIPv6Addrs
            = new AtomicBoolean(DEFAULT_INCLUDE_IPV6_ADDRESSES);

    /**
     * Config constructor.
     *
     * @param topics Root Configuration topic
     */
    public Config(Topics topics) {
        Topics configurationTopics = topics.lookupTopics(KernelConfigResolver.CONFIGURATION_CONFIG_KEY);
        configurationTopics.subscribe((whatHappened, node) -> {
            if (configurationTopics.isEmpty()) {
                this.includeIPv4LoopbackAddrs.set(DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES);
                this.includeIPv6LoopbackAddrs.set(DEFAULT_INCLUDE_IPV6_LOOPBACK_ADDRESSES);
                this.includeIPv4LinkLocalAddrs.set(DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES);
                this.includeIPv6LinkLocalAddrs.set(DEFAULT_INCLUDE_IPV6_LINK_LOCAL_ADDRESSES);
                this.includeIPv4Addrs.set(DEFAULT_INCLUDE_IPV4_ADDRESSES);
                this.includeIPv6Addrs.set(DEFAULT_INCLUDE_IPV6_ADDRESSES);
                this.defaultPort.set(DEFAULT_PORT);
                return;
            }

            this.includeIPv4LoopbackAddrs.set(
                    Coerce.toBoolean(
                            configurationTopics.findOrDefault(
                                    DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES,
                                    INCLUDE_IPV4_LOOPBACK_ADDRESSES_CONFIG_KEY)));
            this.includeIPv6LoopbackAddrs.set(
                    Coerce.toBoolean(
                            configurationTopics.findOrDefault(
                                    DEFAULT_INCLUDE_IPV6_LOOPBACK_ADDRESSES,
                                    INCLUDE_IPV6_LOOPBACK_ADDRESSES_CONFIG_KEY)));
            this.includeIPv4LinkLocalAddrs.set(
                    Coerce.toBoolean(
                            configurationTopics.findOrDefault(
                                    DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES,
                                    INCLUDE_IPV4_LINK_LOCAL_ADDRESSES_CONFIG_KEY)));
            this.includeIPv6LinkLocalAddrs.set(
                    Coerce.toBoolean(
                            configurationTopics.findOrDefault(
                                    DEFAULT_INCLUDE_IPV6_LINK_LOCAL_ADDRESSES,
                                    INCLUDE_IPV6_LINK_LOCAL_ADDRESSES_CONFIG_KEY)));
            this.includeIPv4Addrs.set(
                    Coerce.toBoolean(
                            configurationTopics.findOrDefault(
                                    DEFAULT_INCLUDE_IPV4_ADDRESSES,
                                    INCLUDE_IPV4_ADDRESSES_CONFIG_KEY)));
            this.includeIPv6Addrs.set(
                    Coerce.toBoolean(
                            configurationTopics.findOrDefault(
                                    DEFAULT_INCLUDE_IPV6_ADDRESSES,
                                    INCLUDE_IPV6_ADDRESSES_CONFIG_KEY)));
            this.defaultPort.set(
                    Coerce.toInt(
                            configurationTopics.findOrDefault(DEFAULT_PORT,
                                    DEFAULT_PORT_CONFIG_KEY)));

            logger.atInfo().kv("includeIPv4LoopbackAddrs", includeIPv4LoopbackAddrs.get())
                    .kv("includeIPv4LinkLocalAddrs", includeIPv4LinkLocalAddrs.get())
                    .kv("includeIPv6LoopbackAddrs", includeIPv6LoopbackAddrs.get())
                    .kv("includeIPv6LinkLocalAddrs", includeIPv6LinkLocalAddrs.get())
                    .kv("includeIPv4Addrs", includeIPv4Addrs.get())
                    .kv("includeIPv6Addrs", includeIPv6Addrs.get())
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
     * includeIPv6LoopbackAddrs getter.
     * @return boolean includeIPv6LoopbackAddrs
     */
    public boolean isIncludeIPv6LoopbackAddrs() {
        return this.includeIPv6LoopbackAddrs.get();
    }

    /**
     * includeIPv6LinkLocalAddrs getter.
     * @return boolean includeIPv6LinkLocalAddrs
     */
    public boolean isIncludeIPv6LinkLocalAddrs() {
        return this.includeIPv6LinkLocalAddrs.get();
    }

    /**
     * includeIPv4Addrs getter.
     * @return boolean includeIPv4Addrs
     */
    public boolean isIncludeIPv4Addrs() {
        return this.includeIPv4Addrs.get();
    }

    /**
     * includeIPv6Addrs getter.
     * @return boolean includeIPv6Addrs
     */
    public boolean isIncludeIPv6Addrs() {
        return this.includeIPv6Addrs.get();
    }

    /**
     * defaultPort getter.
     * @return integer defaultPort
     */
    public int getDefaultPort() {
        return this.defaultPort.get();
    }
}


