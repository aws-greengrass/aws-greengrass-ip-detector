package com.aws.greengrass.detector.uploader;

import com.aws.greengrass.detector.client.ClientWrapper;
import com.aws.greengrass.detector.config.Config;
import com.sun.istack.internal.NotNull;
import software.amazon.awssdk.services.greengrassv2data.model.ConnectivityInfo;
import software.amazon.awssdk.services.greengrassv2data.model.UpdateConnectivityInfoResponse;

import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class IpUploader {

    private final ClientWrapper client;
    private final Config config;
    private List<String> ipAddresses;

    /**
     * Constructor.
     *
     * @param client client for calling the data plane api(s)
     * @param config config for fetching the configuration values
     */
    @Inject
    public IpUploader(ClientWrapper client, Config config) {
        this.client = client;
        this.config = config;
    }

    /**
     * Send ip address updates.
     *
     * @param ipAddresses list of ipAddresses
     */
    public void updateIpAddresses(List<InetAddress> ipAddresses) {
        if (ipAddresses == null || ipAddresses.isEmpty()) {
            return;
        }
        List<String> ips = ipAddresses.stream().filter(ip -> ip != null && ip.getHostAddress() != null)
                .map(ip -> ip.getHostAddress()).collect(Collectors.toList());
        uploadedUpdatedAddresses(ips);
    }

    //Default for JUnit Testing
    void uploadedUpdatedAddresses(List<String> ips) {
        synchronized (this) {
            if (!hasIpsChanged(ips)) {
                return;
            }
            List<ConnectivityInfo> connectivityInfoItems = ips.stream().map(ip -> ConnectivityInfo.builder()
                    .hostAddress(ip).metadata("").id(ip).portNumber(config.getMqttPort()).build())
                    .collect(Collectors.toList());
            UpdateConnectivityInfoResponse connectivityInfoResponse =
                    client.updateConnectivityInfo(connectivityInfoItems);
            if (connectivityInfoResponse != null && connectivityInfoResponse.version() != null) {
                this.ipAddresses = ips;
            }
        }
    }

    //Default for JUnit Testing
    boolean hasIpsChanged(@NotNull List<String> ips) {
        if (this.ipAddresses == null) {
            return true;
        } else if (ips.equals(this.ipAddresses)) {
            return false;
        }
        return true;
    }

    //For Junit Testing
    void setIpAddresses(List<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }
}
