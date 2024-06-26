/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.utils;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public final class TestConstants {

    public static final String IPV4_LOOPBACK = "127.0.0.1";
    public static final String IP_1 = "0.61.124.18";
    public static final String IPV6_LOOPBACK = "::1/128";
    public static final String IPV6_LINK_LOCAL_1 = "fe80::bd48:478f:3447:befd";
    public static final String IPV6_LINK_LOCAL_2 = "fe80::2711:7760:46f7:e23a";
    public static final String IPV4_LINK_LOCAL = "169.254.0.0";
    public static final String IPV6_1 = "2001:db8:1234::1";
    public static final int PORT_1 = 8883;
    public static final int PORT_2 = 8884;

    private TestConstants() {}
}
