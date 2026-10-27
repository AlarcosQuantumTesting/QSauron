package edu.uclm.reper.http;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("mutantanalytics")
public class MutantAnalyticsController extends BaseController {

    @Autowired
    private MutantAnalyticsProxy mutantAnalyticsProxy;

    @PostMapping("/calculateStatistics")
    public ResponseEntity<?> calculateStatistics(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "threshold", required = false) Double threshold,
            HttpServletRequest request) {
        // Validation could be added here if needed, but the requirement is to proxy the request
        try {
            Map<String, Object> response = mutantAnalyticsProxy.calculateStatistics(file, threshold).block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/uploadResults")
    public ResponseEntity<?> uploadResults(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        try {
            Map<String, Object> response = mutantAnalyticsProxy.uploadResults(payload).block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/chatQuestion")
    public ResponseEntity<?> chatQuestion(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        try {
            Map<String, Object> response = mutantAnalyticsProxy.chatQuestion(payload).block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
