package com.aws.greengrass.detector.config;

import lombok.Getter;

public class Config {
    @Getter
    private int mqttPort;

    /**
     * Config constructor.
     */
    public Config() {
        // Hardcoding for now till MQTT Broker is not publishing it.
        mqttPort = 8883;
    }
}


