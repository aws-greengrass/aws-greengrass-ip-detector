package com.aws.greengrass.detector.config;

import lombok.Getter;
import lombok.Setter;

public class Config {
    @Getter
    @Setter
    private int mqttPort;

    /**
     * Config constructor.
     */
    public Config() {
        mqttPort = 8883;
    }
}


