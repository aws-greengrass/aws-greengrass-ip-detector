package com.aws.greengrass.detectorclient;

import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;

public interface DataPlaneClientBaseBuilder<B extends DataPlaneClientBaseBuilder<B, C>, C>
        extends AwsClientBuilder<B, C> {
}
