package com.aws.greengrass.detector;

import com.aws.greengrass.componentmanager.KernelConfigResolver;
import com.aws.greengrass.config.Topic;
import com.aws.greengrass.dependency.State;
import com.aws.greengrass.lifecyclemanager.GlobalStateChangeListener;
import com.aws.greengrass.lifecyclemanager.GreengrassService;
import com.aws.greengrass.lifecyclemanager.Kernel;
import com.aws.greengrass.testcommons.testutilities.GGExtension;
import com.aws.greengrass.testcommons.testutilities.GGServiceTestUtil;
import com.aws.greengrass.util.Coerce;
import com.aws.greengrass.utils.TestConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class, GGExtension.class})
public class IpDetectorServiceTest extends GGServiceTestUtil {
    private static final long TEST_TIME_OUT_SEC = 30L;

    private Kernel kernel;
    private GlobalStateChangeListener listener;

    @Mock
    private IpDetectorManager ipDetectorManager;

    @TempDir
    Path rootDir;

    @Test
    void GIVEN_Greengrass_ip_detector_WHEN_mqtt_port_updated_THEN_update_connectvity_called()
            throws Exception {
        kernel = new Kernel();
        kernel.getContext().put(IpDetectorManager.class, ipDetectorManager);
        startKernelWithConfig("config.yaml");

        Topic portConfigTopic =
                kernel.locate(IpDetectorService.DECTECTOR_SERVICE_NAME).getConfig()
                        .lookup(KernelConfigResolver.CONFIGURATION_CONFIG_KEY, IpDetectorService.IP_DECTECTOR_PORT);

        kernel.locate(IpDetectorService.DECTECTOR_SERVICE_NAME).getConfig()
                .lookup(KernelConfigResolver.CONFIGURATION_CONFIG_KEY, IpDetectorService.IP_DECTECTOR_PORT)
                .withValue(String.valueOf(TestConstants.PORT_2));
        // Block until subscriber has finished updating
        kernel.getContext().runOnPublishQueueAndWait(() -> {
        });

        assertThat(Coerce.toInt(portConfigTopic), equalTo(TestConstants.PORT_2));
        verify(ipDetectorManager, times(1))
                .checkConnectivityUpdate();
    }

    private void startKernelWithConfig(String configFileName) throws InterruptedException {
        CountDownLatch ipDetectorRunning = new CountDownLatch(1);
        kernel.parseArgs("-r", rootDir.toAbsolutePath().toString(), "-i",
                getClass().getResource(configFileName).toString());
        listener = (GreengrassService service, State was, State newState) -> {
            if (service.getName().equals(IpDetectorService.DECTECTOR_SERVICE_NAME) && service.getState().equals(State.RUNNING)) {
                ipDetectorRunning.countDown();
            }
        };
        kernel.getContext().addGlobalStateChangeListener(listener);
        kernel.launch();

        Assertions.assertTrue(ipDetectorRunning.await(TEST_TIME_OUT_SEC, TimeUnit.SECONDS));
    }
}
