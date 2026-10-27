package edu.uclm.reper.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuirkCircuitUtilsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testBellCircuit() throws Exception {
        String json = "{\"cols\":[[\"H\"],[\"•\",\"X\"]]}";
        JsonNode quirk = mapper.readTree(json);
        assertEquals(2, QuirkCircuitUtils.countQubits(quirk));
        assertEquals(2, QuirkCircuitUtils.countColumns(quirk));
    }

    @Test
    void testSparseColumnCircuit() throws Exception {
        String json = "{\"cols\":[[\"H\",1,1,\"X\"]]}";
        JsonNode quirk = mapper.readTree(json);
        assertEquals(4, QuirkCircuitUtils.countQubits(quirk));
        assertEquals(1, QuirkCircuitUtils.countColumns(quirk));
    }

    @Test
    void testOraclePlacedAtRow1() throws Exception {
        String json = "{" +
                "\"cols\":[[1, \"~oracle\"]]," +
                "\"gates\":[{\"id\":\"~oracle\", \"matrix\":\"{{1,0,0,0,0,0,0,0},{0,1,0,0,0,0,0,0},{0,0,1,0,0,0,0,0},{0,0,0,1,0,0,0,0},{0,0,0,0,1,0,0,0},{0,0,0,0,0,1,0,0},{0,0,0,0,0,0,1,0},{0,0,0,0,0,0,0,1}}\"}]" +
                "}";
        JsonNode quirk = mapper.readTree(json);
        assertEquals(4, QuirkCircuitUtils.countQubits(quirk));
    }

    @Test
    void testEmptyCols() throws Exception {
        String json = "{\"cols\":[]}";
        JsonNode quirk = mapper.readTree(json);
        assertEquals(0, QuirkCircuitUtils.countQubits(quirk));
        assertEquals(0, QuirkCircuitUtils.countColumns(quirk));
    }

    @Test
    void testRangeCsv() {
        assertEquals("0,1,2", QuirkCircuitUtils.rangeCsv(3));
        assertEquals("0", QuirkCircuitUtils.rangeCsv(1));
        assertEquals("", QuirkCircuitUtils.rangeCsv(0));
    }
}
