package edu.uclm.reper.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import java.util.HashMap;
import java.util.Map;

@Converter
public class ProbabilityDistributionConverter implements AttributeConverter<Map<String, Integer>, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Integer> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing probability distribution map", e);
        }
    }

    @Override
    public Map<String, Integer> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return new HashMap<>();
        }
        try {
            return mapper.readValue(dbData, new TypeReference<Map<String, Integer>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing probability distribution map", e);
        }
    }
}
