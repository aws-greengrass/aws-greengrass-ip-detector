package com.aws.greengrass.detector.config;


import com.aws.greengrass.config.ChildChanged;
import com.aws.greengrass.config.Topics;
import com.aws.greengrass.util.Coerce;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

class ConfigTest {

    Config config;

    @Test
    public void GIVEN_config_topics_WHEN_initialize_THEN_configuration_created() {
        Topics topics = Mockito.mock(Topics.class);
        Topics configTopics = Mockito.mock(Topics.class);
        String mockIncludeIPv4LoopbackAddrsConfig = "true";
        String mockIncludeIPv4LinkLocalAddrsConfig = "true";

        // stub subscribe() to call just the callback method without adding watcher
        doAnswer((Answer<Void>) invocation -> {
            ChildChanged childChanged = invocation.getArgument(0);
            childChanged.childChanged(null, null);
            return null;
        }).when(configTopics).subscribe(any());

        Mockito.doReturn(false).when(configTopics).isEmpty();
        Mockito.doReturn(mockIncludeIPv4LoopbackAddrsConfig)
                .when(configTopics).findOrDefault(false, Config.INCLUDE_IPV4_LOOPBACK_ADDRESSES_CONFIG_KEY);
        Mockito.doReturn(mockIncludeIPv4LinkLocalAddrsConfig)
                .when(configTopics).findOrDefault(false, Config.INCLUDE_IPV4_LINK_LOCAL_ADDRESSES_CONFIG_KEY);

        Mockito.doReturn(configTopics).when(topics).lookupTopics(anyString());
        config = new Config(topics);

        assertNotNull(config);
        assertEquals(config.getMqttPort(), Config.DEFAULT_MQTT_PORT);
        assertEquals(config.isIncludeIPv4LoopbackAddrs(), Coerce.toBoolean(mockIncludeIPv4LoopbackAddrsConfig));
        assertEquals(config.isIncludeIPv4LinkLocalAddrs(), Coerce.toBoolean(mockIncludeIPv4LinkLocalAddrsConfig));
    }

    @Test
    public void GIVEN_empty_config_topics_WHEN_initialize_THEN_default_configuration_created() {
        Topics topics = Mockito.mock(Topics.class);
        Topics configTopics = Mockito.mock(Topics.class);

        // stub subscribe() to call just the callback method without adding watcher
        doAnswer((Answer<Void>) invocation -> {
            ChildChanged childChanged = invocation.getArgument(0);
            childChanged.childChanged(null, null);
            return null;
        }).when(configTopics).subscribe(any());

        Mockito.doReturn(true).when(configTopics).isEmpty();
        Mockito.doReturn(configTopics).when(topics).lookupTopics(anyString());
        config = new Config(topics);

        assertNotNull(config);
        assertEquals(config.getMqttPort(), Config.DEFAULT_MQTT_PORT);
        assertEquals(config.isIncludeIPv4LoopbackAddrs(), Config.DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES);
        assertEquals(config.isIncludeIPv4LinkLocalAddrs(), Config.DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES);
    }
}
