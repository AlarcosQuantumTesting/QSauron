package edu.uclm.reper.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Lee la respuesta de qumugen mutante a mutante para no tener todos en memoria (antes se leia como List.class y se
 * copiaba, lo que producia OutOfMemoryError con ciclos grandes).
 */
final class QumugenMutantReader {

    private QumugenMutantReader() {
    }

    static int forEachMutant(InputStream in, ObjectMapper mapper, Consumer<JsonNode> consumer) throws IOException {
        try (JsonParser p = mapper.getFactory().createParser(in)) {
            if (p.nextToken() != JsonToken.START_ARRAY) {
                throw new IOException("Respuesta de qumugen inesperada: se esperaba un array JSON");
            }
            int count = 0;
            while (p.nextToken() == JsonToken.START_OBJECT) {
                JsonNode node = mapper.readTree(p);
                consumer.accept(node);
                count++;
            }
            if (p.currentToken() != JsonToken.END_ARRAY) {
                throw new IOException("Respuesta de qumugen incompleta: el array JSON no termina");
            }
            return count;
        }
    }

    static JsonNode circuitOf(JsonNode mutant) {
        if (mutant.get("quirk") != null && mutant.get("quirk").isObject()) {
            return mutant.get("quirk");
        }
        if (mutant.get("qCircuit") != null && mutant.get("qCircuit").isObject()) {
            return mutant.get("qCircuit");
        }
        if (mutant.get("circuit") != null && mutant.get("circuit").isObject()) {
            return mutant.get("circuit");
        }
        if (mutant.has("cols")) {
            return mutant;
        }
        return null;
    }
}
