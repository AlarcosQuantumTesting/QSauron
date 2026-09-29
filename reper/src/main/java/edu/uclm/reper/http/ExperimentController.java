package edu.uclm.reper.http;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uclm.reper.dto.CreateExperimentDTO;
import edu.uclm.reper.dto.ExperimentProgressDTO;
import edu.uclm.reper.dto.ExperimentSummaryDTO;
import edu.uclm.reper.model.Experiment;
import edu.uclm.reper.services.ExperimentService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("experiments")
public class ExperimentController extends BaseController {

    @Autowired
    private ExperimentService experimentService;

    @PostMapping("/create")
    public ResponseEntity<?> createExperiment(@RequestBody CreateExperimentDTO dto, HttpServletRequest req) {
        try {
            this.validateCookie(req, dto.getEmail());
            Experiment exp = experimentService.createExperiment(dto, dto.getEmail());
            return ResponseEntity.ok(Map.of("message", "Experiment started", "id", exp.getId(), "name", exp.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/active-progress")
    public ResponseEntity<?> getActiveProgress(@RequestBody Map<String, String> body, HttpServletRequest req) {
        try {
            String email = body.get("email");
            this.validateCookie(req, email);
            List<ExperimentProgressDTO> list = experimentService.getActiveProgress(email);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/user")
    public ResponseEntity<?> getUserExperiments(@RequestBody Map<String, String> body, HttpServletRequest req) {
        try {
            String email = body.get("email");
            this.validateCookie(req, email);
            List<ExperimentSummaryDTO> list = experimentService.getUserExperiments(email);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/details")
    public ResponseEntity<?> getExperimentDetails(@RequestBody Map<String, String> body, HttpServletRequest req) {
        try {
            String email = body.get("email");
            String experimentId = body.get("experimentId");
            this.validateCookie(req, email);
            ExperimentSummaryDTO exp = experimentService.getExperimentDetails(experimentId);
            return ResponseEntity.ok(exp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteExperiment(@RequestBody Map<String, String> body, HttpServletRequest req) {
        try {
            String email = body.get("email");
            String experimentId = body.get("experimentId");
            this.validateCookie(req, email);
            experimentService.deleteExperiment(experimentId, email);
            return ResponseEntity.ok(Map.of("message", "Experiment deleted successfully", "id", experimentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
