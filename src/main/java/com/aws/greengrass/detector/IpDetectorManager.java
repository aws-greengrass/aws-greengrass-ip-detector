package com.aws.greengrass.detector;

import com.aws.greengrass.detector.detector.IpDetector;
import com.aws.greengrass.detector.uploader.IpUploader;
import com.aws.greengrass.logging.api.Logger;
import com.aws.greengrass.logging.impl.LogManager;
import org.apache.commons.lang3.RandomUtils;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

@SuppressWarnings("PMD.AvoidCatchingGenericException")
public class IpDetectorManager {
    public static final int DEFAULT_PERIODIC_UPDATE_INTERVAL_SEC = 180;
    private final IpUploader ipUploader;
    private final IpDetector ipDetector;
    private final ScheduledExecutorService scheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor();
    private final Logger logger = LogManager.getLogger(IpDetectorManager.class);

    @Inject
    public IpDetectorManager(IpUploader ipUploader, IpDetector ipDetector) {
        this.ipDetector = ipDetector;
        this.ipUploader = ipUploader;
    }

    void updateIps() {
        List<InetAddress> ipAddresses = null;
        try {
            ipAddresses = ipDetector.getAllIpAddresses();
            if (ipAddresses.isEmpty()) {
                logger.atDebug().log("No valid ip Address found in ip detector");
                return;
            }
        } catch (SocketException e) {
            logger.atError().log("IP Detector socket exception {}", e);
        }
        ipUploader.updateIpAddresses(ipAddresses);
    }

    /**
     * Start getting the ip addresses of the device and see if there are any changes.
     */
    public void startIpDetection() {
        long initialDelay = RandomUtils.nextLong(0, DEFAULT_PERIODIC_UPDATE_INTERVAL_SEC);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                updateIps();
            } catch (Exception e) {
                logger.atError().log("Exception occured in updating ip addresses {}", e);
            }
        }, initialDelay, 60, TimeUnit.SECONDS);

    }

    /**
     * Stop ip detection service.
     */
    public void stopIpDetection() {
        scheduledExecutorService.shutdown();
    }
}
