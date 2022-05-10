/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector.uploader;

import com.aws.greengrass.config.Topic;
import com.aws.greengrass.dependency.Context;
import com.aws.greengrass.deployment.DeviceConfiguration;
import com.aws.greengrass.deployment.exceptions.DeviceConfigurationException;
import com.aws.greengrass.detector.config.Config;
import com.aws.greengrass.util.GreengrassServiceClientFactory;
import com.aws.greengrass.utils.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.greengrassv2data.GreengrassV2DataClient;
import software.amazon.awssdk.services.greengrassv2data.model.GreengrassV2DataException;
import software.amazon.awssdk.services.greengrassv2data.model.UpdateConnectivityInfoRequest;
import software.amazon.awssdk.services.greengrassv2data.model.UpdateConnectivityInfoResponse;

import java.util.ArrayList;
import java.util.List;

import static com.aws.greengrass.deployment.DeviceConfiguration.DEVICE_PARAM_THING_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class ConnectivityUpdaterTest {
    private ConnectivityUpdater connectivityUpdater;

    @Mock
    private GreengrassServiceClientFactory clientFactory;

    @Mock
    private DeviceConfiguration deviceConfiguration;

    @Mock
    private GreengrassV2DataClient greengrassV2DataClient;

    @Mock
    protected Context context;

    @BeforeEach
    void beforeEach() {
        lenient().when(clientFactory.getGreengrassV2DataClient()).thenReturn(greengrassV2DataClient);
    }

    @Test
    public void GIVEN_ip_addresses_WHEN_updateIpAddresses_THEN_update_conn_called()
            throws DeviceConfigurationException {
        Topic thingNameTopic = Topic.of(context, DEVICE_PARAM_THING_NAME, "testThing");
        Mockito.doReturn(thingNameTopic).when(deviceConfiguration).getThingName();
        connectivityUpdater = new ConnectivityUpdater(deviceConfiguration, clientFactory);
        Mockito.doReturn(UpdateConnectivityInfoResponse.builder().version("1").build())
                .when(greengrassV2DataClient).updateConnectivityInfo(Mockito.any(UpdateConnectivityInfoRequest.class));
        List<String> ips = new ArrayList<>();
        ips.add(TestConstants.IPV4_LOOPBACK);
        connectivityUpdater.uploadAddresses(ips, Mockito.mock(Config.class));
        verify(greengrassV2DataClient, times(1))
                .updateConnectivityInfo(any(UpdateConnectivityInfoRequest.class));
    }

    @Test
    public void GIVEN_ip_addresses_WHEN_updateIpAddresses_throws_THEN_passes() throws DeviceConfigurationException {
        Topic thingNameTopic = Topic.of(context, DEVICE_PARAM_THING_NAME, "testThing");
        Mockito.doReturn(thingNameTopic).when(deviceConfiguration).getThingName();
        connectivityUpdater = new ConnectivityUpdater(deviceConfiguration, clientFactory);
        when(greengrassV2DataClient.updateConnectivityInfo(Mockito.any(UpdateConnectivityInfoRequest.class)))
                .thenThrow(GreengrassV2DataException.builder().build());
        List<String> ips = new ArrayList<>();
        ips.add(TestConstants.IPV4_LOOPBACK);
        connectivityUpdater.uploadAddresses(ips, Mockito.mock(Config.class));
    }

    @Test
    public void GIVEN_ip_addresses_WHEN_updateIpAddresses_and_null_THEN_update_conn_not_called()
            throws DeviceConfigurationException {
        connectivityUpdater = new ConnectivityUpdater(deviceConfiguration, clientFactory);
        connectivityUpdater.updateIpAddresses(null, Mockito.mock(Config.class));
        verify(greengrassV2DataClient, times(0))
                .updateConnectivityInfo(any(UpdateConnectivityInfoRequest.class));
        verify(greengrassV2DataClient, times(0))
                .updateConnectivityInfo(any(UpdateConnectivityInfoRequest.class));
    }

    @Test
    public void GIVEN_ip_addresses_WHEN_updateIpAddressesFails_THEN_ip_list_not_updated()
            throws DeviceConfigurationException {
        Topic thingNameTopic = Topic.of(context, DEVICE_PARAM_THING_NAME, "testThing");
        Mockito.doReturn(thingNameTopic).when(deviceConfiguration).getThingName();
        Mockito.doReturn(UpdateConnectivityInfoResponse.builder().version("1").build())
                .when(greengrassV2DataClient).updateConnectivityInfo(Mockito.any(UpdateConnectivityInfoRequest.class));
        connectivityUpdater = new ConnectivityUpdater(deviceConfiguration, clientFactory);
        List<String> ips = new ArrayList<>();
        ips.add(TestConstants.IPV4_LOOPBACK);
        connectivityUpdater.uploadAddresses(ips, Mockito.mock(Config.class));
        connectivityUpdater.uploadAddresses(ips, Mockito.mock(Config.class));

        verify(greengrassV2DataClient, times(1))
                .updateConnectivityInfo(any(UpdateConnectivityInfoRequest.class));
    }

    @Test
    public void GIVEN_ips_changed_WHEN_get_ip_addresses_THEN_return_true() {
        connectivityUpdater = new ConnectivityUpdater(deviceConfiguration,null);

        // old ips null
        assertTrue(connectivityUpdater.hasIpsChanged(getIps()));
        connectivityUpdater.setIpAddressesAndPort(getIps(), TestConstants.PORT_1);
        assertTrue(connectivityUpdater.hasIpsChanged(getNewIps()));
    }

    @Test
    public void GIVEN_ips_and_port_not_changed_WHEN_has_ips_or_port_changed_THEN_return_false() {
        connectivityUpdater = new ConnectivityUpdater(deviceConfiguration,null);
        connectivityUpdater.setIpAddressesAndPort(getIps(), TestConstants.PORT_1);
        assertFalse(connectivityUpdater.hasIpsChanged(getIps()));
        assertFalse(connectivityUpdater.hasPortChanged(TestConstants.PORT_1));
    }

    @Test
    public void GIVEN_port_changed_WHEN_has_ips_or_port_not_changed_THEN_return_true() {
        connectivityUpdater = new ConnectivityUpdater(deviceConfiguration,null);
        connectivityUpdater.setIpAddressesAndPort(getIps(), TestConstants.PORT_1);
        assertTrue(connectivityUpdater.hasPortChanged(TestConstants.PORT_2));
    }

    private List<String> getIps() {
        List<String> ips = new ArrayList<>();
        ips.add(TestConstants.IPV4_LOOPBACK);
        ips.add(TestConstants.IP_1);
        return ips;
    }

    private List<String> getNewIps() {
        List<String> ips = new ArrayList<>();
        ips.add(TestConstants.IPV4_LOOPBACK);
        ips.add(TestConstants.IP_2);
        return ips;
    }
}

