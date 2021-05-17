/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector.config;

import lombok.Getter;
import lombok.Setter;

import javax.inject.Singleton;

@Singleton
public class Config {
    @Getter
    @Setter
    private volatile int mqttPort;

    /**
     * Config constructor.
     */
    public Config() {
        mqttPort = 8883;
    }
}


