package com.qanunqapisi.config.jpa;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class UuidListConverter implements AttributeConverter<List<UUID>, Object> {
    @Override
    public Object convertToDatabaseColumn(List<UUID> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return attribute.toArray(new UUID[0]);
    }

    @Override
    public List<UUID> convertToEntityAttribute(Object dbData) {
        if (dbData == null) {
            return new ArrayList<>();
        }

        try {
            if (dbData instanceof Array) {
                Array array = (Array) dbData;
                Object[] objects = (Object[]) array.getArray();
                return Arrays.stream(objects)
                    .map(obj -> (UUID) obj)
                    .toList();
            } else if (dbData instanceof UUID[]) {
                return Arrays.asList((UUID[]) dbData);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to convert UUID array from database", e);
        }

        return new ArrayList<>();
    }
}
