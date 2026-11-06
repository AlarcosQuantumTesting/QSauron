package edu.uclm.reper.http;

import com.fasterxml.jackson.databind.JsonNode;
import edu.uclm.reper.services.ConverterCatalogService;
import edu.uclm.reper.services.ImportException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traductor de código cuántico: reper es el único punto de acceso a q-convert-api.
 * El catálogo de formatos y la conversión se reenvían tal cual; la conversión
 * exige administrador.
 */
@RestController
@RequestMapping("/converter")
public class ConverterController extends BaseController {

    private final QConvertProxy proxy;
    private final ConverterCatalogService catalog;
    private final long maxRequestBytes;

    public ConverterController(QConvertProxy proxy, ConverterCatalogService catalog,
            @Value("${app.qconvert.max-request-bytes:4194304}") long maxRequestBytes) {
        this.proxy = proxy;
        this.catalog = catalog;
        this.maxRequestBytes = maxRequestBytes;
    }

    @GetMapping("/formats")
    public ResponseEntity<JsonNode> formats() {
        return ResponseEntity.ok(catalog.raw());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        if (!proxy.health()) {
            throw new ImportException(ImportException.CONVERTER_UNAVAILABLE, "Code converter is not available", 503);
        }
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping(value = "/convert", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode> convert(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        String email = body != null && body.get("email") instanceof String s ? s : null;
        validateAdmin(req, email);

        Object code = body.get("code");
        if (code instanceof String c && c.getBytes(StandardCharsets.UTF_8).length > maxRequestBytes) {
            throw new ImportException(ImportException.FILE_TOO_LARGE, "Code exceeds " + (maxRequestBytes / 1024) + " KB", 413);
        }

        Map<String, Object> forward = new LinkedHashMap<>(body);
        forward.remove("email");
        QConvertProxy.ProxyResponse response = proxy.convertRaw(forward);
        return ResponseEntity.status(response.status()).body(response.body());
    }
}
