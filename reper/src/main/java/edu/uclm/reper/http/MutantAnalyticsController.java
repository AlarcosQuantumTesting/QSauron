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

import edu.uclm.reper.services.BackgroundJobService;
import edu.uclm.reper.services.CycleStatisticsService;
import edu.uclm.reper.services.QuMuLazyLoadService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("mutantanalytics")
public class MutantAnalyticsController extends BaseController {

    @Autowired
    private MutantAnalyticsProxy mutantAnalyticsProxy;

    @Autowired
    private BackgroundJobService backgroundJobService;

    @Autowired
    private edu.uclm.reper.services.CycleStatisticsService cycleStatisticsService;

    @Autowired
    private edu.uclm.reper.services.QuMuLazyLoadService quMuLazyLoadService;

    @PostMapping("/cycleStatistics/start")
    public ResponseEntity<?> startCycleStatistics(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        String email = body != null ? (String) body.get("email") : null;
        String projectId = body != null ? (String) body.get("projectId") : null;
        this.validateCookie(req, email);
        quMuLazyLoadService.checkAccess(email, projectId);

        try {
            int cycleId = ((Number) body.get("cycleId")).intValue();
            Double threshold = (body.containsKey("threshold") && body.get("threshold") != null)
                    ? ((Number) body.get("threshold")).doubleValue() : null;
            Integer inputBits = (body.containsKey("inputBits") && body.get("inputBits") != null)
                    ? ((Number) body.get("inputBits")).intValue() : null;

            String jobId = backgroundJobService.submit("statistics", email, () ->
                    cycleStatisticsService.calculateCycleStatistics(projectId, cycleId, threshold, inputBits)
            );

            return ResponseEntity.ok(Map.of("jobId", jobId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

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
