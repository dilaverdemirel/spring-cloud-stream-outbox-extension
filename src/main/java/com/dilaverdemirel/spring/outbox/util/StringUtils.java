package com.dilaverdemirel.spring.outbox.util;

import java.util.Objects;

/**
 * @author dilaverdemirel
 * @since 8.07.2020
 */
public final class StringUtils {

    private StringUtils() {
    }

    public static boolean isBlank(String data) {
        return Objects.isNull(data) ||
                data.equals("") ||
                data.equals(" ");
    }
}
