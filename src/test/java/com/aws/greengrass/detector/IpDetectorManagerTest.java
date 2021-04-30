package com.aws.greengrass.detector;

import com.aws.greengrass.detector.detector.IpDetector;
import com.aws.greengrass.detector.uploader.IpUploader;
import com.aws.greengrass.utils.TestConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private IpUploader ipUploader;
    @Mock
    private IpDetector ipDetector;

    IpDetectorManager ipDetectorManager;

    @Test
    public void GIVEN_ip_addresses_found_WHEN_initialize_THEN_upload_called() throws SocketException {
        ipDetectorManager = new IpDetectorManager(ipUploader, ipDetector);
        List <String> ips = new ArrayList<>();
        ips.add(TestConstants.IP_1);
        when(ipDetector.getAllIpAddresses()).thenReturn(ips);
        ipDetectorManager.updateIps();
        verify(ipUploader, times(1)).updateIpAddresses(ips);
    }

    @Test
    public void GIVEN_ip_addresses_not_found_WHEN_initialize_THEN_upload_called() throws SocketException {
        ipDetectorManager = new IpDetectorManager(ipUploader, ipDetector);
        when(ipDetector.getAllIpAddresses()).thenReturn(new ArrayList<>());
        ipDetectorManager.updateIps();
        verify(ipUploader, times(0)).updateIpAddresses(any());
    }
}
