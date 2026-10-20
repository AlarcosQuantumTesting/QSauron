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

    private static String statisticsJson() {
        StringBuilder sb = new StringBuilder(
                "{\"mutation_score\":{\"percentage\":50.0,\"killed\":1,\"total\":2},\"kill_rate_per_mutant\":{");
        for (int i = 0; i < 30000; i++) {
            if (i > 0) sb.append(',');
            sb.append('"').append(i).append("\":{\"percentage\":0.0,\"killed\":0,\"total\":4}");
        }
        return sb.append("}}").toString();
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
    @SuppressWarnings("unchecked")
    void calculateStatisticsAcceptsLargeResponse() {
        MutantAnalyticsProxy proxy = new MutantAnalyticsProxy("http://localhost:" + port);

        Map<String, Object> result = proxy.calculateStatistics(csv(), 0.05).block();

        Map<String, Object> perMutant = (Map<String, Object>) result.get("kill_rate_per_mutant");
        assertEquals(30000, perMutant.size());
        assertEquals(4, ((Map<String, Object>) perMutant.get("29999")).get("total"));
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
