package com.aws.greengrass.detectorclient;

import com.aws.greengrass.config.Topic;
import com.aws.greengrass.dependency.Context;
import com.aws.greengrass.deployment.DeviceConfiguration;
import com.aws.greengrass.utils.TestConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.greengrass.model.ConnectivityInfo;

import java.util.ArrayList;
import java.util.List;

import static com.aws.greengrass.deployment.DeviceConfiguration.DEVICE_PARAM_THING_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith({MockitoExtension.class})
public class ClientTest {
    private Client client;
    @Mock
    private DeviceConfiguration deviceConfiguration;

    @Mock
    private DataPlaneClient greengrassClient;

    @Mock
    protected Context context;

    @Test
    public void GIVEN_connectivity_info_WHEN_sent_THEN_update_connectivity_info_called() {
        Topic thingNameTopic = Topic.of(context, DEVICE_PARAM_THING_NAME, "testThing");
        Mockito.doReturn(thingNameTopic).when(deviceConfiguration).getThingName();
        ConnectivityInfo connectivityInfo = ConnectivityInfo.builder().hostAddress(TestConstants.IP_2)
                .portNumber(TestConstants.PORT).metadata("").build();
        List<ConnectivityInfo> connectivityInfoItems = new ArrayList<>();
        connectivityInfoItems.add(connectivityInfo);
        client = new Client(deviceConfiguration, greengrassClient);
        client.updateConnectivityInfo(connectivityInfoItems);

        verify(greengrassClient, times(1)).updateConnectivityInfo(any());
    }

    @Test
    public void GIVEN_connectivity_info_WHEN_null_THEN_update_connectivity_info_not_called() {
        Topic thingNameTopic = Topic.of(context, DEVICE_PARAM_THING_NAME, "testThing");
        Mockito.lenient().doReturn(thingNameTopic).when(deviceConfiguration).getThingName();
        client = new Client(deviceConfiguration, greengrassClient);
        client.updateConnectivityInfo(null);
        verify(greengrassClient, times(0)).updateConnectivityInfo(any());

        client.updateConnectivityInfo(new ArrayList<>());
        verify(greengrassClient, times(0)).updateConnectivityInfo(any());
    }
}
