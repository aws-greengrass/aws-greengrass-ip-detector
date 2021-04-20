package com.aws.greengrass.detectorclient;

import com.aws.greengrass.utils.TestConstants;
import com.fasterxml.jackson.core.JsonFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.AwsRequestOverrideConfiguration;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.protocols.core.OperationInfo;
import software.amazon.awssdk.protocols.json.BaseAwsJsonProtocolFactory;
import software.amazon.awssdk.protocols.json.SdkJsonGenerator;
import software.amazon.awssdk.protocols.json.internal.marshall.JsonProtocolMarshallerBuilder;
import software.amazon.awssdk.services.greengrass.model.ConnectivityInfo;
import software.amazon.awssdk.services.greengrass.model.UpdateConnectivityInfoRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith({MockitoExtension.class})
public class DataPlaneClientMarshallerTest {


    private DataPlaneClientMarshaller dataPlaneClientMarshaller;
    @Mock
    private BaseAwsJsonProtocolFactory baseAwsJsonProtocolFactory;

    @Test
    public void GIVEN_update_connectivity_request_WHEN_valid_THEN_marshall_request_returned() throws URISyntaxException {
        UpdateConnectivityInfoRequest updateConnectivityInfoRequest = UpdateConnectivityInfoRequest.builder()
                .thingName("thingName")
                .connectivityInfo(new ArrayList<>())
                .overrideConfiguration(AwsRequestOverrideConfiguration.builder()
                        .credentialsProvider(DefaultCredentialsProvider.builder().build()).build())
                .connectivityInfo(ConnectivityInfo.builder().hostAddress(TestConstants.IP_2).portNumber(0)
                        .id(TestConstants.IP_2).build())
                .build();

        OperationInfo operationInfo = OperationInfo.builder()
                .requestUri(String.format("/%s/%s", TestConstants.PATH, TestConstants.THING_NAME))
                .httpMethod(SdkHttpMethod.POST).hasExplicitPayloadMember(false).hasPayloadMembers(true).build();

        Mockito.doReturn(JsonProtocolMarshallerBuilder.create()
                .endpoint(new URI(String.format("%s://%s", TestConstants.PROTOCOL, TestConstants.IP_2)))
                .jsonGenerator(new SdkJsonGenerator(JsonFactory.builder()
                        .build(), "json"))
                .operationInfo(operationInfo).build())
                .when(baseAwsJsonProtocolFactory).createProtocolMarshaller(any());

        dataPlaneClientMarshaller = new DataPlaneClientMarshaller(baseAwsJsonProtocolFactory);
        SdkHttpFullRequest marshall = dataPlaneClientMarshaller.marshall(updateConnectivityInfoRequest);
        assertEquals(String.format("%s://%s/%s/%s", TestConstants.PROTOCOL, TestConstants.IP_2, TestConstants.PATH,
                TestConstants.THING_NAME), marshall.getUri().toString());
    }

    @Test
    public void GIVEN_update_connectivity_request_WHEN_invalid_THEN_throws_exception() throws URISyntaxException {

        OperationInfo operationInfo = OperationInfo.builder()
                .requestUri(String.format("/%s/%s", TestConstants.PATH, TestConstants.THING_NAME))
                .httpMethod(SdkHttpMethod.POST).hasExplicitPayloadMember(false).hasPayloadMembers(true).build();

        Mockito.lenient().doReturn(JsonProtocolMarshallerBuilder.create()
                .endpoint(new URI(String.format("%s://%s", TestConstants.PROTOCOL, TestConstants.IP_2)))
                .jsonGenerator(new SdkJsonGenerator(JsonFactory.builder()
                        .build(), "json"))
                .operationInfo(operationInfo).build())
                .when(baseAwsJsonProtocolFactory).createProtocolMarshaller(any());

        dataPlaneClientMarshaller = new DataPlaneClientMarshaller(baseAwsJsonProtocolFactory);
        NullPointerException ex = Assertions.assertThrows(NullPointerException.class,
                () -> dataPlaneClientMarshaller.marshall(null));

        assertThat(ex.getMessage(), containsString("updateConnectivityInfoRequest must not be null"));
    }
}
