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
import edu.uclm.reper.services.QuMuLazyLoadService;
import edu.uclm.reper.dto.ProjectSummaryDTO;
import edu.uclm.reper.dto.QuMuCircuitDTO;
import edu.uclm.reper.dto.QuMuCycleMatrixDTO;
import edu.uclm.reper.dto.QuMuMutantCircuitDTO;
import edu.uclm.reper.dto.QuMuMutantDTO;
import edu.uclm.reper.dto.QuMuProjectDTO;
import jakarta.servlet.http.HttpServletRequest;

@RestController()
@RequestMapping("qumureper")
public class QuMuController extends BaseController {

    @Autowired
    private QumuService qumuService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private QuMuLazyLoadService qumuLazyLoadService;

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

    @PostMapping("/getProjectLight")
    public ResponseEntity<?> getProjectLight(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        String email = body != null ? (String) body.get("email") : null;
        this.validateCookie(req, email);
        try {
            String projectId = body != null ? (String) body.get("projectId") : null;
            if (projectId == null || email == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields: email, projectId"));
            }
            QuMuProjectDTO project = qumuLazyLoadService.getProject(email, projectId);
            return ResponseEntity.ok(project);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/getCycleMatrix")
    public ResponseEntity<?> getCycleMatrix(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        String email = body != null ? (String) body.get("email") : null;
        this.validateCookie(req, email);
        try {
            String projectId = body != null ? (String) body.get("projectId") : null;
            Object cycleIdObj = body != null ? body.get("cycleId") : null;
            if (projectId == null || email == null || !(cycleIdObj instanceof Number)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing or invalid required fields: email, projectId, cycleId"));
            }
            int cycleId = ((Number) cycleIdObj).intValue();
            QuMuCycleMatrixDTO matrix = qumuLazyLoadService.getCycleMatrix(email, projectId, cycleId);
            return ResponseEntity.ok(matrix);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/getMutant")
    public ResponseEntity<?> getMutant(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        String email = body != null ? (String) body.get("email") : null;
        this.validateCookie(req, email);
        try {
            String projectId = body != null ? (String) body.get("projectId") : null;
            Object cycleIdObj = body != null ? body.get("cycleId") : null;
            Object mutantIndexObj = body != null ? body.get("mutantIndex") : null;
            if (projectId == null || email == null || !(cycleIdObj instanceof Number) || !(mutantIndexObj instanceof Number)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing or invalid required fields: email, projectId, cycleId, mutantIndex"));
            }
            int cycleId = ((Number) cycleIdObj).intValue();
            int mutantIndex = ((Number) mutantIndexObj).intValue();
            QuMuMutantDTO mutant = qumuLazyLoadService.getMutant(email, projectId, cycleId, mutantIndex);
            return ResponseEntity.ok(mutant);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/getMutantCircuits")
    public ResponseEntity<?> getMutantCircuits(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        String email = body != null ? (String) body.get("email") : null;
        this.validateCookie(req, email);
        try {
            String projectId = body != null ? (String) body.get("projectId") : null;
            Object cycleIdObj = body != null ? body.get("cycleId") : null;
            Object fromIndexObj = body != null ? body.get("fromIndex") : null;
            Object limitObj = body != null ? body.get("limit") : null;
            if (projectId == null || email == null || !(cycleIdObj instanceof Number) || !(fromIndexObj instanceof Number) || !(limitObj instanceof Number)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing or invalid required fields: email, projectId, cycleId, fromIndex, limit"));
            }
            int cycleId = ((Number) cycleIdObj).intValue();
            int fromIndex = ((Number) fromIndexObj).intValue();
            int limit = ((Number) limitObj).intValue();
            List<QuMuMutantCircuitDTO> circuits = qumuLazyLoadService.getMutantCircuits(email, projectId, cycleId, fromIndex, limit);
            return ResponseEntity.ok(circuits);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
