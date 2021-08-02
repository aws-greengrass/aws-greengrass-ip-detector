/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector;

import com.aws.greengrass.componentmanager.KernelConfigResolver;
import com.aws.greengrass.config.Topic;
import com.aws.greengrass.detector.config.Config;
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

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        ipDetectorService = new IpDetectorService(config, ipDetectorManager, scheduler);

        Topic portTopic = Topic.of(context, IpDetectorService.DETECTOR_SERVICE_NAME, TestConstants.PORT_2);
        lenient().when(config.lookup(KernelConfigResolver.CONFIGURATION_CONFIG_KEY, IpDetectorService.PORT))
                .thenReturn(portTopic);

        ipDetectorService.startup();
        Thread.sleep(10000);
        verify(ipDetectorManager, times(1))
                .updateIps(any(Config.class));
    }

    @Test
    void GIVEN_Greengrass_ip_detector_WHEN_mqtt_port_is_null_THEN_update_test_pass()
            throws Exception {

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        ipDetectorService = new IpDetectorService(config, ipDetectorManager, scheduler);
        lenient().when(config.lookup(KernelConfigResolver.CONFIGURATION_CONFIG_KEY, IpDetectorService.PORT))
                .thenReturn(null);

        ipDetectorService.startup();
        Thread.sleep(10000);
        verify(ipDetectorManager, times(0))
                .updateIps(any(Config.class));
    }
}