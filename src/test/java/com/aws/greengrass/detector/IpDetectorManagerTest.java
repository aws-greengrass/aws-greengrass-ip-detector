/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector;

import com.aws.greengrass.detector.config.Config;
import com.aws.greengrass.detector.detector.IpDetector;
import com.aws.greengrass.detector.uploader.ConnectivityUpdater;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class IpDetectorManagerTest {

    @Mock
    private ConnectivityUpdater connectivityUpdater;
    @Mock
    private IpDetector ipDetector;
    @Mock
    private Config config;

    IpDetectorManager ipDetectorManager;

    @Test
    public void GIVEN_ip_addresses_found_WHEN_initialize_THEN_upload_called() throws SocketException {
        ipDetectorManager = new IpDetectorManager(connectivityUpdater, ipDetector);
        List <InetAddress> ips = new ArrayList<>();
        ips.add(Mockito.mock(InetAddress.class));
        when(ipDetector.getAllIpAddresses(any(Config.class))).thenReturn(ips);
        ipDetectorManager.updateIps(config);
        verify(connectivityUpdater, times(1)).updateIpAddresses(ips, config);
    }

    @Test
    public void GIVEN_ip_addresses_not_found_WHEN_initialize_THEN_upload_called() throws SocketException {
        ipDetectorManager = new IpDetectorManager(connectivityUpdater, ipDetector);
        when(ipDetector.getAllIpAddresses(any(Config.class))).thenReturn(new ArrayList<>());
        ipDetectorManager.updateIps(config);
        verify(connectivityUpdater, times(0)).updateIpAddresses(any(), any());
    }
}
