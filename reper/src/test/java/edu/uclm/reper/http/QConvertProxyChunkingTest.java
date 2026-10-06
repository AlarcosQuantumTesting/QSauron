package edu.uclm.reper.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QConvertProxyChunkingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testThree2MbElementsResultInThreeChunks() {
        // 1. 3 elementos de 2 MB con maxBytes=4194304 -> 3 trozos de 1 elemento
        List<Map<String, Object>> items = new ArrayList<>();
        String payload2Mb = "x".repeat(2 * 1024 * 1024);
        for (int i = 0; i < 3; i++) {
            items.add(Map.of("code", payload2Mb));
        }

        List<List<Integer>> chunks = QConvertProxy.planChunks(items, mapper, 4194304, 100);

        assertEquals(3, chunks.size());
        assertEquals(List.of(0), chunks.get(0));
        assertEquals(List.of(1), chunks.get(1));
        assertEquals(List.of(2), chunks.get(2));
    }

    @Test
    void testTen100KbElementsResultInOneChunk() {
        // 2. 10 elementos de 100 KB -> 1 trozo
        List<Map<String, Object>> items = new ArrayList<>();
        String payload100Kb = "x".repeat(100 * 1024);
        for (int i = 0; i < 10; i++) {
            items.add(Map.of("code", payload100Kb));
        }

        List<List<Integer>> chunks = QConvertProxy.planChunks(items, mapper, 4194304, 100);

        assertEquals(1, chunks.size());
        assertEquals(10, chunks.get(0).size());
    }

    @Test
    void test150SmallElementsResultInTwoChunks() {
        // 3. 150 elementos pequeños -> 2 trozos (100 + 50)
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            items.add(Map.of("code", "h q[0];"));
        }

        List<List<Integer>> chunks = QConvertProxy.planChunks(items, mapper, 4194304, 100);

        assertEquals(2, chunks.size());
        assertEquals(100, chunks.get(0).size());
        assertEquals(50, chunks.get(1).size());
    }

    @Test
    void testOne5MbElementResultsInSingleChunk() {
        // 4. un elemento de 5 MB -> un trozo solo
        String payload5Mb = "x".repeat(5 * 1024 * 1024);
        List<Map<String, Object>> items = List.of(Map.of("code", payload5Mb));

        List<List<Integer>> chunks = QConvertProxy.planChunks(items, mapper, 4194304, 100);

        assertEquals(1, chunks.size());
        assertEquals(List.of(0), chunks.get(0));
    }

    @Test
    void testOrderIsPreservedAcrossChunks() {
        // 5. el orden se conserva (concatenar los trozos da 0..n-1)
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < 125; i++) {
            items.add(Map.of("code", "index" + i));
        }

        List<List<Integer>> chunks = QConvertProxy.planChunks(items, mapper, 4194304, 100);

        List<Integer> concatenated = new ArrayList<>();
        for (List<Integer> chunk : chunks) {
            concatenated.addAll(chunk);
        }

        assertEquals(125, concatenated.size());
        for (int i = 0; i < 125; i++) {
            assertEquals(i, concatenated.get(i));
        }
    }
}
