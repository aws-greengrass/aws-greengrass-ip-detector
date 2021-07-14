/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector;

import com.aws.greengrass.componentmanager.KernelConfigResolver;
import com.aws.greengrass.config.Topic;
import com.aws.greengrass.config.Topics;
import com.aws.greengrass.detector.config.Config;
import com.aws.greengrass.lifecyclemanager.GreengrassService;
import com.aws.greengrass.lifecyclemanager.Kernel;
import com.aws.greengrass.testcommons.testutilities.GGExtension;
import com.aws.greengrass.testcommons.testutilities.GGServiceTestUtil;
import com.aws.greengrass.utils.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;

@ExtendWith({MockitoExtension.class, GGExtension.class})
public class IpDetectorServiceTest extends GGServiceTestUtil {

    private IpDetectorService ipDetectorService;

    @Mock
    private IpDetectorManager ipDetectorManager;

    @BeforeEach
    void beforeEach() {
        initializeMockedConfig();
    }


    @Test
    void GIVEN_Greengrass_ip_detector_WHEN_mqtt_port_updated_THEN_update_connectvity_called()
            throws Exception {

        GreengrassService moquetteService = mock(GreengrassService.class);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Kernel mockKernel = mock(Kernel.class);
        lenient().when(mockKernel.locate(IpDetectorService.MOQUETTE_SERVICE_NAME))
                .thenReturn(moquetteService);

        ipDetectorService = new IpDetectorService(mockKernel, config, ipDetectorManager, scheduler);

        Topics mockMoquetteConfig = mock(Topics.class);
        lenient().when(moquetteService.getConfig()).thenReturn(mockMoquetteConfig);

        Topic portTopic = Topic.of(context, IpDetectorService.DETECTOR_SERVICE_NAME, TestConstants.PORT_2);
        lenient().when(mockMoquetteConfig.find(KernelConfigResolver.CONFIGURATION_CONFIG_KEY,
                IpDetectorService.MOQUETTE, IpDetectorService.PORT)).thenReturn(portTopic);

        ipDetectorService.startup();
        Thread.sleep(10000);
        verify(ipDetectorManager, times(1))
                .updateIps(any(Config.class));
    }

    @Test
    void GIVEN_Greengrass_ip_detector_WHEN_mqtt_port_is_null_THEN_update_test_pass()
            throws Exception {

        GreengrassService moquetteService = mock(GreengrassService.class);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Kernel mockKernel = mock(Kernel.class);
        lenient().when(mockKernel.locate(IpDetectorService.MOQUETTE_SERVICE_NAME))
                .thenReturn(moquetteService);

        ipDetectorService = new IpDetectorService(mockKernel, config, ipDetectorManager, scheduler);

        Topics mockMoquetteConfig = mock(Topics.class);
        lenient().when(moquetteService.getConfig()).thenReturn(mockMoquetteConfig);

        lenient().when(mockMoquetteConfig.find(KernelConfigResolver.CONFIGURATION_CONFIG_KEY,
                IpDetectorService.MOQUETTE, IpDetectorService.PORT)).thenReturn(null);

        ipDetectorService.startup();
        Thread.sleep(10000);
        verify(ipDetectorManager, times(0))
                .updateIps(any(Config.class));
    }
}