package com.qanunqapisi.config.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Converter
public class UuidListConverter implements AttributeConverter<List<UUID>, UUID[]> {
    @Override
    public UUID[] convertToDatabaseColumn(List<UUID> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return attribute.toArray(new UUID[0]);
    }

    @Override
    public List<UUID> convertToEntityAttribute(UUID[] dbData) {
        if (dbData == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(dbData);
    }
}
