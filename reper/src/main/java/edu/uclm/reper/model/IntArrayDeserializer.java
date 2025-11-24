package edu.uclm.reper.model;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Custom deserializer that handles both String and Array formats for int arrays.
 * Supports: "0,1,2" or [0, 1, 2]
 */
public class IntArrayDeserializer extends JsonDeserializer<int[]> {

    @Override
    public int[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.getCurrentToken();
        
        if (token == JsonToken.VALUE_STRING) {
            // Handle string format: "0,1,2"
            String text = p.getText();
            if (text == null || text.trim().isEmpty()) {
                return new int[0];
            }
            return Arrays.stream(text.split(","))
                    .map(String::trim)
                    .mapToInt(Integer::parseInt)
                    .toArray();
        } else if (token == JsonToken.START_ARRAY) {
            // Handle array format: [0, 1, 2]
            return ctxt.readValue(p, int[].class);
        }
        
        return new int[0];
    }
}
