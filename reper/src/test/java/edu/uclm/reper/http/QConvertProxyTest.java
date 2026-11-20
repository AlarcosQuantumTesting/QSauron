package edu.uclm.reper.http;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import edu.uclm.reper.services.ImportException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QConvertProxyTest {

    private HttpServer server;
    private int status = 200;
    private String body = "{}";
    private String lastRequestBody;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/convert", ex -> {
            lastRequestBody = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            respond(ex, status, body);
        });
        server.createContext("/api/formats", ex -> respond(ex, 200, "{\"sourceFormats\":[\"qasm\"]}"));
        server.createContext("/api/health", ex -> respond(ex, 200, "{\"status\":\"ok\"}"));
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private QConvertProxy proxy() {
        return new QConvertProxy("http://localhost:" + server.getAddress().getPort(), 10, 4194304);
    }

    private static void respond(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    @Test
    void getFormatsAndHealth() {
        assertEquals("qasm", proxy().getFormats().get("sourceFormats").get(0).asText());
        assertTrue(proxy().health());
    }

    @Test
    void healthIsFalseWhenDown() {
        QConvertProxy p = proxy();
        server.stop(0);
        assertFalse(p.health());
    }

    @Test
    void convertRawReturnsFullBodyAnd422() {
        status = 422;
        body = "{\"error\":\"bad qasm\",\"extra\":1}";
        QConvertProxy.ProxyResponse r = proxy().convertRaw(Map.of("code", "x", "sourceFormat", "qasm", "destFormat", "quirk"));
        assertEquals(422, r.status());
        assertEquals("bad qasm", r.body().get("error").asText());
        assertEquals(1, r.body().get("extra").asInt());
        assertTrue(lastRequestBody.contains("\"destFormat\":\"quirk\""));
    }

    @Test
    void convertRaw200KeepsAllFields() {
        body = "{\"success\":true,\"output\":\"o\",\"filename\":\"f.json\",\"pipeline\":[\"qasm\",\"quirk\"]}";
        QConvertProxy.ProxyResponse r = proxy().convertRaw(Map.of("code", "x"));
        assertEquals(200, r.status());
        assertEquals("f.json", r.body().get("filename").asText());
        assertEquals(2, r.body().get("pipeline").size());
    }

    @Test
    void serverErrorIs503() {
        status = 500;
        ImportException e = assertThrows(ImportException.class, () -> proxy().convertRaw(Map.of("code", "x")));
        assertEquals(503, e.getHttpStatus());
        assertEquals(ImportException.CONVERTER_UNAVAILABLE, e.getErrorCode());
    }

    @Test
    void convertOneBehavesAsBefore() {
        body = "{\"success\":true,\"output\":\"out\"}";
        QConvertProxy.QConvertResult ok = proxy().convertOne("c", "qasm", "quirk");
        assertTrue(ok.success());
        assertEquals("out", ok.output());

        status = 422;
        body = "{\"error\":\"boom\"}";
        QConvertProxy.QConvertResult bad = proxy().convertOne("c", "qasm", "quirk");
        assertFalse(bad.success());
        assertEquals("boom", bad.error());

        status = 500;
        assertThrows(ImportException.class, () -> proxy().convertOne("c", "qasm", "quirk"));
    }
}
