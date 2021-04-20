package com.aws.greengrass.detectorclient;

import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.awscore.client.builder.AwsDefaultClientBuilder;
import software.amazon.awssdk.core.client.config.SdkAdvancedClientOption;
import software.amazon.awssdk.core.client.config.SdkClientConfiguration;
import software.amazon.awssdk.core.client.config.SdkClientOption;
import software.amazon.awssdk.core.interceptor.ClasspathInterceptorChainFactory;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.core.signer.Signer;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;

public abstract class DataPlaneClientBaseDefaultBuilder<B extends DataPlaneClientBaseBuilder<B, C>, C>
        extends AwsDefaultClientBuilder<B, C> {
    DataPlaneClientBaseDefaultBuilder() {
        super();
    }

    @Override
    protected final String serviceEndpointPrefix() {
        return "com/aws/greengrass";
    }

    @Override
    protected final String serviceName() {
        return "Greengrass";
    }

    @Override
    protected final SdkClientConfiguration mergeServiceDefaults(SdkClientConfiguration config) {
        return config.merge((c) -> {
            c.option(SdkAdvancedClientOption.SIGNER, this.defaultSigner())
                    .option(SdkClientOption.CRC32_FROM_COMPRESSED_DATA_ENABLED, false);
        });
    }

    @Override
    protected final SdkClientConfiguration finalizeServiceConfiguration(SdkClientConfiguration config) {
        ClasspathInterceptorChainFactory interceptorFactory = new ClasspathInterceptorChainFactory();
        List<ExecutionInterceptor> interceptors = interceptorFactory
                .getInterceptors("software/amazon/awssdk/services/greengrass/execution.interceptors");
        interceptors = CollectionUtils.mergeLists(interceptors, (List)config
                .option(SdkClientOption.EXECUTION_INTERCEPTORS));

        return config.toBuilder().option(SdkClientOption.EXECUTION_INTERCEPTORS, interceptors).build();
    }

    @Override
    protected final String signingName() {
        return "com/aws/greengrass";
    }

    private Signer defaultSigner() {
        return Aws4Signer.create();
    }

}
