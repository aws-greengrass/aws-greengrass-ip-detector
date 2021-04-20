package com.aws.greengrass.detector;

import com.aws.greengrass.detector.detector.IpDetector;
import com.aws.greengrass.detector.uploader.IpUploader;
import com.aws.greengrass.logging.api.Logger;
import com.aws.greengrass.logging.impl.LogManager;
import org.apache.commons.lang3.RandomUtils;

import java.net.SocketException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

@SuppressWarnings("PMD.AvoidCatchingGenericException")
public class CIU {
    public static final int DEFAULT_PERIODIC_UPDATE_INTERVAL_SEC = 180;
    private final IpUploader ipUploader;
    private final IpDetector ipDetector;
    private final Logger logger = LogManager.getLogger(CIU.class);

    @Inject
    public CIU(IpUploader ipUploader, IpDetector ipDetector) {
        this.ipDetector = ipDetector;
        this.ipUploader = ipUploader;
    }

    void updateIps() {
        try {
            List<String> ipAddresses = ipDetector.getAllIpAddresses();
            if (ipAddresses == null || ipAddresses.isEmpty()) {
                logger.atInfo().log("No valid ip Address found in ip detector");
                return;
            }
            ipUploader.updateIpAddresses(ipAddresses);
        } catch (SocketException e) {
            logger.atError().log("IP Detector socket exception {}", e);
        }
    }

    /**
     * Start getting the ip addresses of the device and see if there are any changes.
     * @throws InterruptedException when interrupted
     */
    public void startIpDetection() {
        //this may happen in Junit test where we start the kernel
        if (ipDetector == null || ipUploader == null) {
            return;
        }

        ScheduledExecutorService scheduledExecutorService =
                Executors.newSingleThreadScheduledExecutor();

        long initialDelay = RandomUtils.nextLong(0, DEFAULT_PERIODIC_UPDATE_INTERVAL_SEC);
        try {
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                updateIps();
                }, initialDelay, 10, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.atError().log("Ip detector check interrupted {}", e);
        }
    }
}
