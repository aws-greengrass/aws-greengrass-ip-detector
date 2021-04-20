package com.aws.greengrass.detector.config;

import lombok.Getter;

import javax.inject.Inject;

public class Config {
    @Getter
    private int mqttPort;

    /**
     * Config constructor.
     */
    @Inject
    public Config() {
        // Hardcoding for now till MQTT Broker is not publishing it.
        mqttPort = 8883;
    }
}


