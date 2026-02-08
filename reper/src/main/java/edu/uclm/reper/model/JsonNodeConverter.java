package edu.uclm.reper.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;

public class JsonNodeConverter implements AttributeConverter<JsonNode, String>{
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(JsonNode attribute) {
        try {
            return attribute == null ? null : mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando JSON", e);
        }
    }

    @Override
    public JsonNode convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : mapper.readTree(dbData);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializando JSON", e);
        }
    }
}
