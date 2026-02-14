package com.naidugudivada.ecommerce.infrastructure.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naidugudivada.ecommerce.infrastructure.exceptions.JacksonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonUtils {

    private final ObjectMapper mapper;

    public <T> String toJson(T value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new JacksonException("Failed to serialize object to JSON", e);
        }
    }

    public <T> T fromJson(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new JacksonException("Failed to deserialize JSON", e);
        }
    }
}
