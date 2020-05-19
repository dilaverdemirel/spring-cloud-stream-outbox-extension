package com.dilaverdemirel.spring.outbox.util;

import com.dilaverdemirel.spring.outbox.exception.OutboxMessagePayloadJsonConvertException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author dilaverdemirel
 * @since 17.05.2020
 */
@Slf4j
public final class JsonUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtil() {
    }

    public static String convertToJson(Object object) {
        try {
            if (Objects.nonNull(object)) {
                return OBJECT_MAPPER.writeValueAsString(object);
            }
        } catch (JsonProcessingException processingException) {
            log.error(processingException.getMessage(), processingException);
            throw new OutboxMessagePayloadJsonConvertException("Object is not eligible to json conversion!");
        }

        return "";
    }
}
