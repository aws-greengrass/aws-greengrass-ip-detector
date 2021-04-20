package com.aws.greengrass.detectorclient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.client.config.SdkClientConfiguration;
import software.amazon.awssdk.core.client.handler.SyncClientHandler;
import software.amazon.awssdk.services.greengrass.model.UpdateConnectivityInfoRequest;
import software.amazon.awssdk.services.greengrass.model.UpdateConnectivityInfoResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith({MockitoExtension.class})
public class DataPlaneClientDefaultTest {
    private DataPlaneDefaultClient dataPlaneClient;
    @Mock
    private SyncClientHandler clientHandler;

    @Test
    public void GIVEN_update_connectivity_request_WHEN_valid_THEN_update_connectivity_info_response_returned() {

        UpdateConnectivityInfoRequest updateConnectivityInfoRequest = UpdateConnectivityInfoRequest.builder().build();
        Mockito.doReturn(UpdateConnectivityInfoResponse.builder().version("1.0")
                .build()).when(clientHandler).execute(any());

        dataPlaneClient = new DataPlaneDefaultClient(SdkClientConfiguration.builder().build(), clientHandler);
        UpdateConnectivityInfoResponse updateConnectivityInfoResponse =
                dataPlaneClient.updateConnectivityInfo(updateConnectivityInfoRequest);

        assertEquals(updateConnectivityInfoResponse.version(), "1.0");
    }

    @Test
    public void GIVEN_update_connectivity_request_WHEN_null_THEN_null_returned() {
        dataPlaneClient = new DataPlaneDefaultClient(SdkClientConfiguration.builder().build(), clientHandler);
        UpdateConnectivityInfoResponse updateConnectivityInfoResponse =
                dataPlaneClient.updateConnectivityInfo(null);

        assertEquals(updateConnectivityInfoResponse, null);
    }
}
