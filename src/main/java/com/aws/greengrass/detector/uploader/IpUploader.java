package com.aws.greengrass.detector.uploader;

import com.aws.greengrass.detector.config.Config;
import com.aws.greengrass.detectorclient.Client;
import com.aws.greengrass.logging.api.Logger;
import com.aws.greengrass.logging.impl.LogManager;
import software.amazon.awssdk.services.greengrassv2data.model.ConnectivityInfo;
import software.amazon.awssdk.services.greengrassv2data.model.UpdateConnectivityInfoResponse;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class IpUploader {

    private final Client client;
    private final Config config;
    private final Logger logger = LogManager.getLogger(IpUploader.class);
    private final Object periodicUpdateInProgressLock = new Object();
    private List<String> ipAddresses;

    /**
     * Constructor.
     *
     * @param client client for calling the data plane api(s)
     * @param config config for fetching the configuration values
     */
    @Inject
    public IpUploader(Client client, Config config) {
        this.client = client;
        this.config = config;
    }

    /**
     * Send ip address updates.
     *
     * @param ipAddresses list of ipAddresses
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void updateIpAddresses(List<String> ipAddresses) {
        synchronized (periodicUpdateInProgressLock) {
            if (ipAddresses == null || ipAddresses.isEmpty() || !hasIpsChanged(ipAddresses)) {
                return;
            }

            List<ConnectivityInfo> connectivityInfoItems = new ArrayList<>();
            for (String ipAddress : ipAddresses) {
                ConnectivityInfo connectivityInfo = ConnectivityInfo.builder().hostAddress(ipAddress)
                        .metadata("").id(ipAddress).portNumber(config.getMqttPort()).build();
                connectivityInfoItems.add(connectivityInfo);
            }

            try {
                UpdateConnectivityInfoResponse connectivityInfoResponse =
                        client.updateConnectivityInfo(connectivityInfoItems);
                if (connectivityInfoResponse != null && connectivityInfoResponse.version() != null) {
                    this.ipAddresses = ipAddresses;
                }
            } catch (Exception e) {
                logger.atError().log("Update connectivity call failed {}", e);
            }
        }
    }

    //Default for JUnit Testing
    boolean hasIpsChanged(List<String> ips) {
        if (this.ipAddresses == null) {
            return true;
        } else if (ips == null || ips.equals(this.ipAddresses)) {
            return false;
        }

        return true;
    }

    //For Junit Testing
    void setIpAddresses(List<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }
}
