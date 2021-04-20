package com.aws.greengrass.detectorclient;

import com.aws.greengrass.componentmanager.ClientConfigurationUtils;
import com.aws.greengrass.deployment.DeviceConfiguration;
import com.aws.greengrass.logging.api.Logger;
import com.aws.greengrass.logging.impl.LogManager;
import com.aws.greengrass.util.Coerce;
import com.aws.greengrass.util.Utils;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.greengrass.model.ConnectivityInfo;
import software.amazon.awssdk.services.greengrass.model.UpdateConnectivityInfoRequest;
import software.amazon.awssdk.services.greengrass.model.UpdateConnectivityInfoResponse;

import java.net.URI;
import java.util.List;
import javax.inject.Inject;

public class Client {
    private final Logger logger = LogManager.getLogger(Client.class);
    private final DeviceConfiguration deviceConfiguration;
    private DataPlaneClient greengrassClient;

    public DataPlaneClient getClient() {
        return greengrassClient;
    }
    /**
     * Constructor.
     *
     * @param deviceConfiguration client to get the device details
     */

    @Inject
    public Client(DeviceConfiguration deviceConfiguration) {
        this.deviceConfiguration = deviceConfiguration;
        configureClient();
    }

    Client(DeviceConfiguration deviceConfiguration, DataPlaneClient greengrassClient) {
        this.deviceConfiguration = deviceConfiguration;
        this.greengrassClient = greengrassClient;
    }

    /**
     * Constructor.
     *
     * @param connectivityInfoItems list of connectivity info items
     * @throws AwsServiceException when service throws exception
     * @throws SdkClientException when sdk client throws exception
     */
    @SuppressWarnings("PMD.AvoidUncheckedExceptionsInSignatures")
    public UpdateConnectivityInfoResponse updateConnectivityInfo(List<ConnectivityInfo> connectivityInfoItems)
            throws AwsServiceException, SdkClientException {
        if (connectivityInfoItems == null || connectivityInfoItems.isEmpty()) {
            return null;
        }

        UpdateConnectivityInfoRequest updateConnectivityInfoRequest = UpdateConnectivityInfoRequest.builder()
                .thingName(Coerce.toString(deviceConfiguration.getThingName()))
                .connectivityInfo(connectivityInfoItems)
                .build();

        return greengrassClient.updateConnectivityInfo(updateConnectivityInfoRequest);
    }


    @SuppressWarnings("PMD.ConfusingTernary")
    private void configureClient() {
        ApacheHttpClient.Builder httpClient = ClientConfigurationUtils.getConfiguredClientBuilder(deviceConfiguration);

        DataPlaneClientBuilder clientBuilder = DataPlaneClient.builder()
                // Use an empty credential provider because our requests don't need SigV4
                // signing, as they are going through IoT Core instead
                .credentialsProvider(AnonymousCredentialsProvider.create())
                .httpClient(httpClient.build())
                .overrideConfiguration(ClientOverrideConfiguration.builder().retryPolicy(RetryMode.STANDARD).build());
        String region = Coerce.toString(deviceConfiguration.getAWSRegion());

        if (!Utils.isEmpty(region)) {
            String greengrassServiceEndpoint = ClientConfigurationUtils
                    .getGreengrassServiceEndpoint(deviceConfiguration);
            if (!Utils.isEmpty(greengrassServiceEndpoint)) {
                // Region and endpoint are both required when updating endpoint config
                logger.atInfo("initialize-greengrass-client").addKeyValue("service-endpoint",
                        greengrassServiceEndpoint).addKeyValue("service-region", region).log();

                clientBuilder.endpointOverride(URI.create(greengrassServiceEndpoint));
                clientBuilder.region(Region.of(region));
            } else {
                // This section is to override default region if needed
                logger.atInfo("initialize-greengrass-client").addKeyValue("service-region", region).log();
                clientBuilder.region(Region.of(region));
            }
        }

        greengrassClient = clientBuilder.build();
    }
}
