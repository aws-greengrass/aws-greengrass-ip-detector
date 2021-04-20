package com.aws.greengrass.detectorclient;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.runtime.transform.Marshaller;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.protocols.core.OperationInfo;
import software.amazon.awssdk.protocols.core.ProtocolMarshaller;
import software.amazon.awssdk.protocols.json.BaseAwsJsonProtocolFactory;
import software.amazon.awssdk.services.greengrass.model.GreengrassException;
import software.amazon.awssdk.services.greengrass.model.UpdateConnectivityInfoRequest;
import software.amazon.awssdk.utils.Validate;


public class DataPlaneClientMarshaller  implements Marshaller<UpdateConnectivityInfoRequest> {
    private static final OperationInfo SDK_OPERATION_BINDING;
    private final BaseAwsJsonProtocolFactory protocolFactory;

    /**
     * Constructor.
     *
     * @param protocolFactory protocol factory to set the protocol information
     */
    public DataPlaneClientMarshaller(BaseAwsJsonProtocolFactory protocolFactory) {
        this.protocolFactory = protocolFactory;
    }

    /**
     * Marshall the update connectivity request.
     *
     * @param updateConnectivityInfoRequest updateConnectivityInfoRequest which is supposed to be marshalled
     */
    @Override
    public SdkHttpFullRequest marshall(UpdateConnectivityInfoRequest updateConnectivityInfoRequest) {
        Validate.paramNotNull(updateConnectivityInfoRequest, "updateConnectivityInfoRequest");

        try {
            ProtocolMarshaller<SdkHttpFullRequest> protocolMarshaller = this.protocolFactory
                    .createProtocolMarshaller(SDK_OPERATION_BINDING);
            return protocolMarshaller.marshall(updateConnectivityInfoRequest);
        } catch (GreengrassException exception) {
            throw SdkClientException.builder().message("Unable to marshall request to JSON: "
                    + exception.getMessage()).cause(exception).build();
        }
    }

    static {
        SDK_OPERATION_BINDING = OperationInfo.builder()
                .requestUri("/com/aws/greengrass/connectivityInfo/thing/{ThingName}")
                .httpMethod(SdkHttpMethod.POST).hasExplicitPayloadMember(false).hasPayloadMembers(true).build();
    }
}
