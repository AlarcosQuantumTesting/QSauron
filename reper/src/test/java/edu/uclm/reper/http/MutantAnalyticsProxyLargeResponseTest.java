package edu.uclm.reper.http;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class MutantAnalyticsProxyLargeResponseTest {

    private HttpServer server;
    private int port;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/calculateStatistics", ex -> respond(ex, statisticsJson()));
        server.createContext("/uploadResults", ex -> respond(ex,
                "{\"conversation_id\":\"c1\",\"analysis\":\"" + "a".repeat(1024 * 1024) + "\"}"));
        server.start();
        port = server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    /** Respuesta v2: kill_rate_per_mutant agrupado y subsumption con una clase enorme. */
    private static String statisticsJson() {
        StringBuilder sb = new StringBuilder(
                "{\"mutation_score\":{\"percentage\":50.0,\"killed\":1,\"total\":2},"
                + "\"kill_rate_per_mutant\":{\"total\":4,\"groups\":["
                + "{\"killed\":0,\"percentage\":0.0,\"mutants\":[");
        appendIds(sb, 0, 15000);
        sb.append("]},{\"killed\":1,\"percentage\":0.25,\"mutants\":[");
        appendIds(sb, 15000, 30000);
        sb.append("]}]},\"subsumption\":{\"summary\":{\"killed_mutants\":30000,\"redundant\":29999,"
                + "\"minimal\":1,\"redundancy_rate\":0.99,\"classes\":1},\"tests\":[\"t0\"],"
                + "\"classes\":[{\"id\":0,\"killed_by\":[0],\"num_tests\":1,\"size\":30000,"
                + "\"redundant\":29999,\"subsumed_by\":\"0\",\"subsumed_by_tests\":1,\"mutants\":[");
        appendIds(sb, 0, 30000);
        return sb.append("]}],\"by_operator\":[]}}").toString();
    }

    private static void appendIds(StringBuilder sb, int from, int to) {
        for (int i = from; i < to; i++) {
            if (i > from) sb.append(',');
            sb.append('"').append(i).append('"');
        }
    }

    private static void respond(HttpExchange ex, String json) throws IOException {
        ex.getRequestBody().readAllBytes();
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static MockMultipartFile csv() {
        return new MockMultipartFile("file", "killing_matrix.csv", "text/csv",
                "Mutant ID,Operator,in0\n0,H_X,Killed\n".getBytes());
    }

    @Test
    void calculateStatisticsAcceptsLargeResponse() {
        MutantAnalyticsProxy proxy = new MutantAnalyticsProxy("http://localhost:" + port);

        Map<String, Object> result = proxy.calculateStatistics(csv(), 0.05).block();

        assertV2(result);
    }

    @SuppressWarnings("unchecked")
    private static void assertV2(Map<String, Object> result) {
        Map<String, Object> perMutant = (Map<String, Object>) result.get("kill_rate_per_mutant");
        assertEquals(4, perMutant.get("total"));
        java.util.List<Map<String, Object>> groups = (java.util.List<Map<String, Object>>) perMutant.get("groups");
        assertEquals(2, groups.size());
        int sum = groups.stream().mapToInt(g -> ((java.util.List<?>) g.get("mutants")).size()).sum();
        assertEquals(30000, sum);
        Map<String, Object> subsumption = (Map<String, Object>) result.get("subsumption");
        java.util.List<Map<String, Object>> classes = (java.util.List<Map<String, Object>>) subsumption.get("classes");
        assertEquals(1, classes.size());
        assertEquals(30000, ((java.util.List<?>) classes.get(0).get("mutants")).size());
    }

    @Test
    void calculateStatisticsFromFileSendsFile() throws IOException {
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("matrix_test_", ".csv");
        java.nio.file.Files.writeString(tempFile, "Mutant ID,Operator,in0\n0,H_X,Killed\n");

        try {
            MutantAnalyticsProxy proxy = new MutantAnalyticsProxy("http://localhost:" + port);
            Map<String, Object> result = proxy.calculateStatisticsFromFile(tempFile, 0.05).block();

            assertNotNull(result);
            assertV2(result);
        } finally {
            java.nio.file.Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void requestsGzipAndReceivesCompressedResponseDecompressed() throws IOException {
        java.util.concurrent.atomic.AtomicReference<String> acceptEncoding = new java.util.concurrent.atomic.AtomicReference<>();
        server.createContext("/gzip/calculateStatistics", ex -> {
            acceptEncoding.set(ex.getRequestHeaders().getFirst("Accept-Encoding"));
            ex.getRequestBody().readAllBytes();
            java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
            try (java.util.zip.GZIPOutputStream gz = new java.util.zip.GZIPOutputStream(buf)) {
                gz.write(statisticsJson().getBytes(StandardCharsets.UTF_8));
            }
            byte[] bytes = buf.toByteArray();
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.getResponseHeaders().add("Content-Encoding", "gzip");
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
        });
        MutantAnalyticsProxy proxy = new MutantAnalyticsProxy("http://localhost:" + port + "/gzip");

        Map<String, Object> result = proxy.calculateStatistics(csv(), 0.05).block();

        assertNotNull(acceptEncoding.get());
        assertTrue(acceptEncoding.get().contains("gzip"), acceptEncoding.get());
        assertV2(result);
    }

    @Test
    void uploadResultsAcceptsLargeResponse() {
        MutantAnalyticsProxy proxy = new MutantAnalyticsProxy("http://localhost:" + port);

        Map<String, Object> result = proxy.uploadResults(Map.of("mutation_score", Map.of())).block();

        assertEquals(1024 * 1024, ((String) result.get("analysis")).length());
    }

    @Test
    void responseOverConfiguredLimitFailsWithClearMessage() {
        MutantAnalyticsProxy proxy = new MutantAnalyticsProxy("http://localhost:" + port, 1024);

        Exception e = assertThrows(Exception.class, () -> proxy.calculateStatistics(csv(), 0.05).block());

        assertTrue(e.getMessage().contains("Error calculation statistics"), e.getMessage());
        assertTrue(e.getMessage().contains("1024"), e.getMessage());
    }
}
