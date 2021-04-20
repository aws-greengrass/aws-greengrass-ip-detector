package com.aws.greengrass.detectorclient;

import software.amazon.awssdk.awscore.client.builder.AwsSyncClientBuilder;


public interface DataPlaneClientBuilder extends AwsSyncClientBuilder<DataPlaneClientBuilder, DataPlaneClient>,
        DataPlaneClientBaseBuilder<DataPlaneClientBuilder, DataPlaneClient> {
}
