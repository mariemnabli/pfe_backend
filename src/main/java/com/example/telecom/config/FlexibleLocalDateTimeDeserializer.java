package com.example.telecom.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();
        try {
            return LocalDateTime.parse(trimmed);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDate.parse(trimmed).atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw ctxt.weirdStringException(trimmed, LocalDateTime.class,
                    "Format de date invalide. Utiliser yyyy-MM-dd ou yyyy-MM-ddTHH:mm:ss");
        }
    }
}
