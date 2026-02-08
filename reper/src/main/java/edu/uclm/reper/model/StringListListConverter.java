package edu.uclm.reper.model;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

@Converter
public class StringListListConverter implements AttributeConverter<List<List<String>>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<List<String>> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (IOException e) {
            throw new RuntimeException("Error converting list of lists to JSON", e);
        }
    }

    @Override
    public List<List<String>> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<List<String>>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON to list of lists", e);
        }
    }
}
