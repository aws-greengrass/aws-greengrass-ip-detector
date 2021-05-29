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
import static org.mockito.ArgumentMatchers.anyBoolean;
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
        ipDetectorManager = new IpDetectorManager(connectivityUpdater, ipDetector, config);
        List <InetAddress> ips = new ArrayList<>();
        ips.add(Mockito.mock(InetAddress.class));
        when(ipDetector.getAllIpAddresses(anyBoolean(), anyBoolean())).thenReturn(ips);
        ipDetectorManager.updateIps();
        verify(connectivityUpdater, times(1)).updateIpAddresses(ips);
    }

    @Test
    public void GIVEN_ip_addresses_not_found_WHEN_initialize_THEN_upload_called() throws SocketException {
        ipDetectorManager = new IpDetectorManager(connectivityUpdater, ipDetector, config);
        when(ipDetector.getAllIpAddresses(anyBoolean(), anyBoolean())).thenReturn(new ArrayList<>());
        ipDetectorManager.updateIps();
        verify(connectivityUpdater, times(0)).updateIpAddresses(any());
    }
}
