package com.aws.greengrass.detectorclient;


public class DataPlaneClientDefaultBuilder  extends DataPlaneClientBaseDefaultBuilder<DataPlaneClientBuilder,
        DataPlaneClient> implements DataPlaneClientBuilder  {
    DataPlaneClientDefaultBuilder() {
        super();
    }

    @Override
    protected final DataPlaneDefaultClient buildClient() {
        return new DataPlaneDefaultClient(super.syncClientConfiguration());
    }
}
