/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.detector.config;


import com.aws.greengrass.config.ChildChanged;
import com.aws.greengrass.config.Topic;
import com.aws.greengrass.config.Topics;
import com.aws.greengrass.util.Coerce;
import com.aws.greengrass.utils.TestConstants;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

class ConfigTest {

    Config config;

    @Test
    public void GIVEN_config_topics_WHEN_initialize_THEN_configuration_created() {
        Topics topics = Mockito.mock(Topics.class);
        Topics configTopics = Mockito.mock(Topics.class);
        Topic excludedIpTopic = Mockito.mock(Topic.class);
        String mockIncludeIPv4LoopbackAddrsConfig = "true";
        String mockIncludeIPv4LinkLocalAddrsConfig = "true";
        String mockExcludeIPsConfig = String.format("[%s, %s]", TestConstants.IP_1, TestConstants.IP_2);
        List<String> mockList = Arrays.asList(TestConstants.IP_1, TestConstants.IP_2);
        int mockPortValue = 9000;

        // stub subscribe() to call just the callback method without adding watcher
        doAnswer((Answer<Void>) invocation -> {
            ChildChanged childChanged = invocation.getArgument(0);
            childChanged.childChanged(null, null);
            return null;
        }).when(configTopics).subscribe(any());

        Mockito.doReturn(false).when(configTopics).isEmpty();
        Mockito.doReturn(mockIncludeIPv4LoopbackAddrsConfig)
                .when(configTopics).findOrDefault(anyBoolean(), eq(Config.INCLUDE_IPV4_LOOPBACK_ADDRESSES_CONFIG_KEY));
        Mockito.doReturn(mockIncludeIPv4LinkLocalAddrsConfig)
                .when(configTopics).findOrDefault(anyBoolean(), eq(Config.INCLUDE_IPV4_LINK_LOCAL_ADDRESSES_CONFIG_KEY));
        Mockito.doReturn(mockPortValue)
                .when(configTopics).findOrDefault(anyInt(), eq(Config.DEFAULT_PORT_CONFIG_KEY));

        Mockito.doReturn(excludedIpTopic).when(configTopics).find(Config.EXCLUDED_IP_ADDRESSES_CONFIG_KEY);
        Mockito.doReturn(mockList).when(excludedIpTopic).getOnce();
        Mockito.doReturn(configTopics).when(topics).lookupTopics(anyString());
        config = new Config(topics);

        assertNotNull(config);
        assertEquals(mockPortValue, config.getDefaultPort());
        assertEquals(Coerce.toBoolean(mockIncludeIPv4LoopbackAddrsConfig), config.isIncludeIPv4LoopbackAddrs());
        assertEquals(Coerce.toBoolean(mockIncludeIPv4LinkLocalAddrsConfig), config.isIncludeIPv4LinkLocalAddrs());
        assertEquals(config.getExcludedIPAddresses().size(), Coerce.toStringList(mockExcludeIPsConfig).size());
        assertTrue(config.getExcludedIPAddresses().containsAll(Coerce.toStringList(mockExcludeIPsConfig)));
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
        assertEquals(Config.DEFAULT_INCLUDE_IPV4_LOOPBACK_ADDRESSES, config.isIncludeIPv4LoopbackAddrs());
        assertEquals(Config.DEFAULT_INCLUDE_IPV4_LINK_LOCAL_ADDRESSES, config.isIncludeIPv4LinkLocalAddrs());
        assertEquals(Config.DEFAULT_PORT, config.getDefaultPort());
        assertTrue(config.getExcludedIPAddresses().isEmpty());
    }

    @Test
    public void GIVEN_invalid_excluded_ips_list_WHEN_initialize_THEN_default_configuration_created() {
        Topics topics = Mockito.mock(Topics.class);
        Topics configTopics = Mockito.mock(Topics.class);

        // stub subscribe() to call just the callback method without adding watcher
        doAnswer((Answer<Void>) invocation -> {
            ChildChanged childChanged = invocation.getArgument(0);
            childChanged.childChanged(null, null);
            return null;
        }).when(configTopics).subscribe(any());

        Mockito.doReturn(false).when(configTopics).isEmpty();
        Mockito.doReturn(configTopics).when(topics).lookupTopics(anyString());
        Topic excludedIpTopic = Mockito.mock(Topic.class);
        Mockito.doReturn(excludedIpTopic).when(configTopics).find(Config.EXCLUDED_IP_ADDRESSES_CONFIG_KEY);
        Mockito.doReturn("bad-config").when(excludedIpTopic).getOnce();
        config = new Config(topics);

        assertNotNull(config);
        assertTrue(config.getExcludedIPAddresses().isEmpty());
    }
}
