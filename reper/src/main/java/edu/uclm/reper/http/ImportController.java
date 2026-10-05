package edu.uclm.reper.http;

import edu.uclm.reper.dto.ConvertRequestDTO;
import edu.uclm.reper.dto.ImportRequestDTO;
import edu.uclm.reper.dto.RepresentRequestDTO;
import edu.uclm.reper.services.ImportException;
import edu.uclm.reper.services.ImportFormat;
import edu.uclm.reper.services.ProjectImportService;
import edu.uclm.reper.services.VirusScanner;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projects/import")
public class ImportController extends BaseController {

    private final ProjectImportService service;
    private final VirusScanner scanner;
    private final int maxFiles;
    private final long maxFileBytes;
    private final long maxTotalBytes;
    private final int maxQubits;

    public ImportController(
            ProjectImportService service,
            VirusScanner scanner,
            @Value("${app.import.max-files:20}") int maxFiles,
            @Value("${app.import.max-file-bytes:524288}") long maxFileBytes,
            @Value("${app.import.max-total-bytes:5242880}") long maxTotalBytes,
            @Value("${app.import.max-qubits:16}") int maxQubits) {
        this.service = service;
        this.scanner = scanner;
        this.maxFiles = maxFiles;
        this.maxFileBytes = maxFileBytes;
        this.maxTotalBytes = maxTotalBytes;
        this.maxQubits = maxQubits;
    }

    @GetMapping("/formats")
    public ResponseEntity<Map<String, Object>> getFormats() {
        List<Map<String, Object>> formats = Arrays.stream(ImportFormat.values())
                .map(ImportFormat::toMap)
                .toList();

        Map<String, Object> limits = new LinkedHashMap<>();
        limits.put("maxFiles", maxFiles);
        limits.put("maxFileBytes", maxFileBytes);
        limits.put("maxTotalBytes", maxTotalBytes);
        limits.put("maxQubits", maxQubits);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("formats", formats);
        response.put("limits", limits);
        response.put("antivirusEnabled", scanner.isEnabled());

        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> importProjects(@RequestBody ImportRequestDTO body, HttpServletRequest req) {
        String email = body != null ? body.getEmail() : null;
        validateCookie(req, email);
        return ResponseEntity.ok(service.importProjects(email, body.getItems()));
    }

    @PostMapping(value = "/convert", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> convertOnly(@RequestBody ConvertRequestDTO body, HttpServletRequest req) {
        String email = body != null ? body.getEmail() : null;
        validateCookie(req, email);
        return ResponseEntity.ok(service.convertOnly(email, body));
    }

    @PostMapping(value = "/represent", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> represent(@RequestBody RepresentRequestDTO body, HttpServletRequest req) {
        String email = body != null ? body.getEmail() : null;
        validateCookie(req, email);
        return ResponseEntity.ok(service.represent(email, body));
    }

    @ExceptionHandler(ImportException.class)
    public ResponseEntity<Map<String, Object>> handleImportException(ImportException e) {
        Map<String, Object> errorBody = Map.of(
                "errorCode", e.getErrorCode(),
                "error", e.getMessage() != null ? e.getMessage() : ""
        );
        return ResponseEntity.status(e.getHttpStatus()).body(errorBody);
    }
}
