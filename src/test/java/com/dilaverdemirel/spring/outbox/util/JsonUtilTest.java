package com.dilaverdemirel.spring.outbox.util;

import com.dilaverdemirel.spring.outbox.dto.DummyMessagePayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author dilaverdemirel
 * @since 17.05.2020
 */
@ExtendWith(MockitoExtension.class)
class JsonUtilTest {

    @Test
    public void testConvertToJson_it_should_convert_object_to_json_when_object_is_not_null() {
        //Given

        //When
        final var json = JsonUtil.convertToJson(DummyMessagePayload.builder().id("id-1").name("name-1").build());

        //Then
        assertEquals("{\"id\":\"id-1\",\"name\":\"name-1\"}", json);
    }

    @Test
    public void testConvertToJson_it_should_return_empty_string_when_object_is_null() {
        //Given

        //When
        final var json = JsonUtil.convertToJson(null);

        //Then
        assertEquals("", json);
    }
}