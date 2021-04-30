package com.aws.greengrass.detector.uploader;

import com.aws.greengrass.detectorclient.Client;
import com.aws.greengrass.detector.config.Config;
import com.aws.greengrass.utils.TestConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.greengrassv2data.model.UpdateConnectivityInfoResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
public class IpUploaderTest {
    @Mock
    private Client mockClient;
    @Mock
    private Config mockConfig;

    private IpUploader uploader;

    @Test
    public void GIVEN_ip_addresses_WHEN_updateIpAddresses_THEN_update_conn_called() {
        uploader = new IpUploader(mockClient, mockConfig);
        List<String> ips = new ArrayList<>();
        ips.add(TestConstants.IP_1);
        uploader.updateIpAddresses(ips);
        verify(mockClient, times(1)).updateConnectivityInfo(any());
    }

    @Test
    public void GIVEN_ip_addresses_WHEN_updateIpAddresses_and_null_THEN_update_conn_not_called() {
        uploader = new IpUploader(mockClient, mockConfig);
        uploader.updateIpAddresses(null);
        verify(mockClient, times(0)).updateConnectivityInfo(any());
        uploader.updateIpAddresses(new ArrayList<>());
        verify(mockClient, times(0)).updateConnectivityInfo(any());
    }

    @Test
    public void GIVEN_ip_addresses_WHEN_updateIpAddressesFails_THEN_ip_list_not_updated() {
        uploader = new IpUploader(mockClient, mockConfig);

        ScheduledExecutorService scheduledExecutorService =
                Executors.newSingleThreadScheduledExecutor();

        CountDownLatch latch = new CountDownLatch(3);

        //Throws exception ip list is not updated
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                List<String> ips = new ArrayList<>();
                ips.add(TestConstants.IP_1);
                Mockito.doThrow(Mockito.mock(MockException.class))
                        .when(mockClient).updateConnectivityInfo(Mockito.anyList());
                uploader.updateIpAddresses(ips);
                latch.countDown();
            }
        }, 1, TimeUnit.MILLISECONDS);

        //Ip list gets updated
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                List<String> ips = new ArrayList<>();
                ips.add(TestConstants.IP_1);
                Mockito.doReturn(UpdateConnectivityInfoResponse.builder().version("1").build())
                        .when(mockClient).updateConnectivityInfo(Mockito.anyList());
                uploader.updateIpAddresses(ips);
                latch.countDown();
            }
        }, 1000, TimeUnit.MILLISECONDS);

        //Calling update connectivity with same ip addresses so the cloud api is not called
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                List<String> ips = new ArrayList<>();
                ips.add(TestConstants.IP_1);
                uploader.updateIpAddresses(ips);
                latch.countDown();
            }
        }, 2000, TimeUnit.MILLISECONDS);

        try {
            latch.await();
        } catch (InterruptedException E) {
        }

        verify(mockClient, times(2)).updateConnectivityInfo(any());
    }

    @Test
    public void GIVEN_ips_changed_WHEN_get_ip_addresses_THEN_return_true() {
        uploader = new IpUploader(null, null);

        // old ips null
        assertTrue(uploader.hasIpsChanged(getIps()));
        uploader.setIpAddresses(getIps());
        assertTrue(uploader.hasIpsChanged(getNewIps()));
    }

    @Test
    public void GIVEN_ips_not_changed_WHEN_get_ip_addresses_THEN_return_false() {
        uploader = new IpUploader(null, null);
        uploader.setIpAddresses(getIps());
        assertFalse( uploader.hasIpsChanged(getIps()));
    }

    private List<String> getIps() {
        List<String> ips = new ArrayList<>();
        ips.add(TestConstants.IP_1);
        ips.add(TestConstants.IP_2);
        return ips;
    }

    private List<String> getNewIps() {
        List<String> ips = new ArrayList<>();
        ips.add(TestConstants.IP_1);
        ips.add(TestConstants.IP_3);
        return ips;
    }

    class MockException extends AwsServiceException {
        private static final long serialVersionUID = 42L;
        public MockException(Builder b) {
            super(b);
        }
    }
}

