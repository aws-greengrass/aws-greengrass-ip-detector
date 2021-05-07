package com.aws.greengrass.detector.client;

import com.aws.greengrass.config.Topic;
import com.aws.greengrass.dependency.Context;
import com.aws.greengrass.deployment.DeviceConfiguration;
import com.aws.greengrass.util.GreengrassServiceClientFactory;
import com.aws.greengrass.utils.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.greengrassv2data.GreengrassV2DataClient;
import software.amazon.awssdk.services.greengrassv2data.model.ConnectivityInfo;
import software.amazon.awssdk.services.greengrassv2data.model.UpdateConnectivityInfoRequest;

import java.util.ArrayList;
import java.util.List;

import static com.aws.greengrass.deployment.DeviceConfiguration.DEVICE_PARAM_THING_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith({MockitoExtension.class})
public class ClientWrapperTest {
    private ClientWrapper clientWrapper;
    @Mock
    private DeviceConfiguration deviceConfiguration;

    @Mock
    private GreengrassV2DataClient greengrassV2DataClient;

    @Mock
    private GreengrassServiceClientFactory clientFactory;

    @Mock
    protected Context context;

    @BeforeEach
    void beforeEach() {
        lenient().when(clientFactory.getGreengrassV2DataClient()).thenReturn(greengrassV2DataClient);
    }

    @Test
    public void GIVEN_connectivity_info_WHEN_sent_THEN_update_connectivity_info_called() {
        Topic thingNameTopic = Topic.of(context, DEVICE_PARAM_THING_NAME, "testThing");
        Mockito.doReturn(thingNameTopic).when(deviceConfiguration).getThingName();
        ConnectivityInfo connectivityInfo = ConnectivityInfo.builder().hostAddress(TestConstants.IP_2)
                .portNumber(TestConstants.PORT).metadata("").build();
        List<ConnectivityInfo> connectivityInfoItems = new ArrayList<>();
        connectivityInfoItems.add(connectivityInfo);
        clientWrapper = new ClientWrapper(deviceConfiguration, clientFactory);
        clientWrapper.updateConnectivityInfo(connectivityInfoItems);

        verify(greengrassV2DataClient, times(1))
                .updateConnectivityInfo(any(UpdateConnectivityInfoRequest.class));
    }

    @Test
    public void GIVEN_connectivity_info_WHEN_null_THEN_update_connectivity_info_not_called() {
        Topic thingNameTopic = Topic.of(context, DEVICE_PARAM_THING_NAME, "testThing");
        lenient().doReturn(thingNameTopic).when(deviceConfiguration).getThingName();
        clientWrapper = new ClientWrapper(deviceConfiguration, clientFactory);
        clientWrapper.updateConnectivityInfo(null);
        verify(greengrassV2DataClient, times(0))
                .updateConnectivityInfo(any(UpdateConnectivityInfoRequest.class));

        clientWrapper.updateConnectivityInfo(new ArrayList<>());
        verify(greengrassV2DataClient, times(0))
                .updateConnectivityInfo(any(UpdateConnectivityInfoRequest.class));
    }
}
