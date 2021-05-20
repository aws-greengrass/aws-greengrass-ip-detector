/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector;

import com.aws.greengrass.componentmanager.KernelConfigResolver;
import com.aws.greengrass.config.Topic;
import com.aws.greengrass.config.Topics;
import com.aws.greengrass.detector.config.Config;
import com.aws.greengrass.lifecyclemanager.Kernel;
import com.aws.greengrass.mqttbroker.MQTTService;
import com.aws.greengrass.testcommons.testutilities.GGExtension;
import com.aws.greengrass.testcommons.testutilities.GGServiceTestUtil;
import com.aws.greengrass.utils.TestConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, GGExtension.class})
public class IpDetectorServiceTest extends GGServiceTestUtil {

    private IpDetectorService ipDetectorService;

    @Test
    void GIVEN_Greengrass_ip_detector_WHEN_mqtt_port_updated_THEN_update_connectvity_called()
            throws Exception {

        initializeMockedConfig();
        Config mockConfig = mock(Config.class);
        ScheduledExecutorService scheduledExecutorService = mock(ScheduledExecutorService.class);
        MQTTService mockMQTTService = mock(MQTTService.class);
        Kernel mockKernel = mock(Kernel.class);
        when(mockKernel.locate(MQTTService.SERVICE_NAME)).thenReturn(mockMQTTService);
        IpDetectorManager ipDetectorManager = mock(IpDetectorManager.class);

        ipDetectorService = new IpDetectorService(mockKernel, config, ipDetectorManager, scheduledExecutorService,
                mockConfig);

        Topics mockMoquetteConfig = mock(Topics.class);
        when(mockMQTTService.getConfig()).thenReturn(mockMoquetteConfig);

       Topic portTopic = Topic.of(context, IpDetectorService.IP_DECTECTOR_PORT, TestConstants.PORT_2);
        when(mockMoquetteConfig.find(KernelConfigResolver.CONFIGURATION_CONFIG_KEY, IpDetectorService.MOQUETTE,
                IpDetectorService.IP_DECTECTOR_PORT)).thenReturn(portTopic);

        ipDetectorService.install();
        verify(ipDetectorManager, times(1))
                .checkAndUpdateConnectivity();
    }
}
