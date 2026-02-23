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

    @PostMapping("/deleteMutantCycle")
    public ResponseEntity<?> deleteMutantCycle(@RequestBody Map<String, Object> request, HttpServletRequest req) {
        try {
            String projectId = (String) request.get("projectId");
            int cycleId = Integer.parseInt(request.get("cycleId").toString());
            qumuService.deleteMutantCycle(projectId, cycleId);
            return ResponseEntity.ok(Map.of("message", "Cycle deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

}
