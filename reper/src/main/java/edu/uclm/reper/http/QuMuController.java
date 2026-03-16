package edu.uclm.reper.http;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uclm.reper.model.OperatorType;
import edu.uclm.reper.services.QumuService;
import edu.uclm.reper.services.ProjectService;
import edu.uclm.reper.dto.ProjectSummaryDTO;
import jakarta.servlet.http.HttpServletRequest;

@RestController()
@RequestMapping("qumureper")
public class QuMuController extends BaseController {

    @Autowired
    private QumuService qumuService;

    @Autowired
    private ProjectService projectService;

    @GetMapping("/getoperatorsByFamily")
    public List<OperatorType> getOperatorsByFamily() {
        try {
            return qumuService.getOperatorsByFamily();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/getSidebarProjects")
    public ResponseEntity<?> getSidebarProjects(@RequestBody Map<String, String> request, HttpServletRequest req) {
        this.validateCookie(req, request.get("email"));
        try {
            List<ProjectSummaryDTO> projects = projectService.getSidebarProjectsByUser(request.get("email"));
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/saveMutantsBatch")
    public ResponseEntity<?> saveMutantsBatch(@RequestBody Map<String, Object> request, HttpServletRequest req) {
        try {
            this.validateCookie(req, (String) request.get("email"));
            String projectId = (String) request.get("projectId");
            int cycleId = (Integer) request.get("cycleId");

            ObjectMapper mapper = new ObjectMapper();
            List<edu.uclm.reper.model.Mutant> mutants = mapper.convertValue(
                    request.get("mutants"),
                    new com.fasterxml.jackson.core.type.TypeReference<List<edu.uclm.reper.model.Mutant>>() {
                    });

            projectService.saveMutantsBatch(projectId, cycleId, mutants);
            return ResponseEntity.ok(Map.of("message", "Batch of " + mutants.size() + " mutants saved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/saveExecutionsBatch")
    public ResponseEntity<?> saveExecutionsBatch(@RequestBody Map<String, Object> request, HttpServletRequest req) {
        try {
            this.validateCookie(req, (String) request.get("email"));
            String projectId = (String) request.get("projectId");
            int cycleId = (Integer) request.get("cycleId");

            ObjectMapper mapper = new ObjectMapper();
            List<edu.uclm.reper.model.Mutant> mutantsWithResults = mapper.convertValue(
                    request.get("mutants"),
                    new com.fasterxml.jackson.core.type.TypeReference<List<edu.uclm.reper.model.Mutant>>() {
                    });

            projectService.saveExecutionsBatch(projectId, cycleId, mutantsWithResults);
            return ResponseEntity.ok(Map.of("message",
                    "Batch of " + mutantsWithResults.size() + " mutant executions saved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/deleteMutantCycle")
    public ResponseEntity<?> deleteMutantCycle(@RequestBody Map<String, Object> request, HttpServletRequest req) {
        try {
            String projectId = (String) request.get("projectId");
            int cycleId = (Integer) request.get("cycleId");

            projectService.deleteMutantCycle(projectId, cycleId);
            return ResponseEntity.ok(Map.of("message", "Mutant cycle deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
