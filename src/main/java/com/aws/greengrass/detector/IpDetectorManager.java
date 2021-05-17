package com.aws.greengrass.detector;

import com.aws.greengrass.detector.detector.IpDetector;
import com.aws.greengrass.detector.uploader.ConnectivityUpdater;
import com.aws.greengrass.logging.api.Logger;
import com.aws.greengrass.logging.impl.LogManager;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import javax.inject.Inject;

@SuppressWarnings("PMD.AvoidCatchingGenericException")
public class IpDetectorManager {
    private final ConnectivityUpdater connectivityUpdater;
    private final IpDetector ipDetector;
    private final Logger logger = LogManager.getLogger(IpDetectorManager.class);

    @Inject
    public IpDetectorManager(ConnectivityUpdater connectivityUpdater, IpDetector ipDetector) {
        this.ipDetector = ipDetector;
        this.connectivityUpdater = connectivityUpdater;
    }

    void checkConnectivityUpdate() {
        List<InetAddress> ipAddresses = null;
        try {
            ipAddresses = ipDetector.getAllIpAddresses();
            if (ipAddresses.isEmpty()) {
                logger.atDebug().log("No valid ip Address found in ip detector");
                return;
            }
        } catch (SocketException e) {
            logger.atError().log("IP Detector socket exception {}", e);
            return;
        }
        connectivityUpdater.updateConnectivity(ipAddresses);
    }

    /**
     * Start getting the ip addresses of the device and see if there are any changes.
     */
    public void startConnectivityUpdate() {
        try {
            checkConnectivityUpdate();
        } catch (Exception e) {
            logger.atError().log("Exception occured in updating ip addresses {}", e);
        }
    }
}
