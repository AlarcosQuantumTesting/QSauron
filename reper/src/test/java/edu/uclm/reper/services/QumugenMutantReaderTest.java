package edu.uclm.reper.services;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class QumugenMutantReaderTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private static ByteArrayInputStream stream(String json) {
        return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void leeTodosEnOrden() throws IOException {
        List<Integer> seen = new ArrayList<>();
        int n = QumugenMutantReader.forEachMutant(
                stream("[{\"mutantIndex\":0,\"a\":1},{\"mutantIndex\":1},{\"mutantIndex\":2}]"), mapper,
                node -> seen.add(node.get("mutantIndex").asInt()));
        assertEquals(3, n);
        assertEquals(List.of(0, 1, 2), seen);
    }

    @Test
    void arrayVacio() throws IOException {
        List<JsonNode> seen = new ArrayList<>();
        assertEquals(0, QumugenMutantReader.forEachMutant(stream("[]"), mapper, seen::add));
        assertTrue(seen.isEmpty());
    }

    @Test
    void noEsArray() {
        assertThrows(IOException.class,
                () -> QumugenMutantReader.forEachMutant(stream("{\"error\":\"x\"}"), mapper, n -> {
                }));
    }

    @Test
    void respuestaCortadaFalla() {
        assertThrows(Exception.class, () -> QumugenMutantReader
                .forEachMutant(stream("[{\"mutantIndex\":0},{\"mutantIndex\":1"), mapper, n -> {
                }));
    }

    @Test
    @SuppressWarnings("rawtypes")
    void mismoCircuitoQueAntes() throws IOException {
        String json = "[{\"mutantIndex\":0,\"mutationDetail\":\"X/0.01\",\"quirk\":{\"cols\":[[\"H\",1,\"•\"],[1,\"X\",0.01,-2,1.5e-3]],"
                + "\"gates\":[{\"id\":\"~g\",\"name\":\"g\",\"circuit\":{\"cols\":[[\"H\"],[\"X\"]]}}],\"init\":[0,1]}}]";
        Map m = (Map) mapper.readValue(json, List.class).get(0);
        JsonNode viejo = mapper.convertValue(m.get("quirk"), JsonNode.class);

        List<JsonNode> nodes = new ArrayList<>();
        QumugenMutantReader.forEachMutant(stream(json), mapper, nodes::add);
        JsonNode nuevo = QumugenMutantReader.circuitOf(nodes.get(0));

        assertEquals(viejo, nuevo);
        assertEquals(mapper.writeValueAsString(viejo), mapper.writeValueAsString(nuevo));
    }

    @Test
    void circuitOfAlternativas() throws IOException {
        assertEquals("qc", QumugenMutantReader.circuitOf(mapper.readTree("{\"qCircuit\":{\"id\":\"qc\"}}")).get("id").asText());
        assertEquals("c", QumugenMutantReader.circuitOf(mapper.readTree("{\"circuit\":{\"id\":\"c\"}}")).get("id").asText());
        JsonNode root = mapper.readTree("{\"cols\":[]}");
        assertSame(root, QumugenMutantReader.circuitOf(root));
        assertNull(QumugenMutantReader.circuitOf(mapper.readTree("{\"x\":1}")));
    }
}
