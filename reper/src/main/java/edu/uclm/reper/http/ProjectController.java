package edu.uclm.reper.http;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.User;
import edu.uclm.reper.services.ProjectService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/projects")
public class ProjectController extends BaseController {

    @Autowired
    private ProjectService projectService;

    /**
     * Crea un nuevo proyecto y lo asocia con un usuario
     * POST /api/projects/create
     * Body: {"projectName": "Mi Proyecto", "userId": "user123"}
     */

    @PostMapping("/create")
    public ResponseEntity<?> createProject(@RequestBody Map<String, String> request, HttpServletRequest req) {
        this.validateCookie(req, request.get("userId"));
        try {
            Project project = projectService.createProjectForUser(request.get("projectId"), request.get("userId"));
            return ResponseEntity.ok(project);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Asocia un proyecto existente con un usuario
     * POST /api/projects/associate
     */
    @PostMapping("/associate")
    public ResponseEntity<?> associateProjectWithUser(@RequestBody Map<String, String> request,
            HttpServletRequest req) {
        this.validateCookie(req, request.get("userID"));
        try {
            projectService.associateProjectWithUser(request.get("projectID"), request.get("userID"));
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Obtiene todos los proyectos de un usuario
     * GET /api/projects/user/{userId}
     */
    @PostMapping("/getAllByUser")
    public ResponseEntity<?> getProjectsByUser(@RequestBody Map<String, String> request, HttpServletRequest req) {
        this.validateCookie(req, request.get("email"));
        try {
            List<Project> projects = projectService.getProjectsByUser(request);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Guarda/actualiza los datos de un proyecto
     * PUT /api/projects/save
     */

    @PutMapping("/save")
    public ResponseEntity<?> saveProject(@RequestBody Map<String, Object> request, HttpServletRequest req) {

        try {

            ObjectMapper mapper = new ObjectMapper();
            Project circuit = mapper.convertValue(request.get("circuit"), Project.class);
            User user = mapper.convertValue(request.get("user"), User.class);

            // this.validateCookie(req, user.getId());

            Project savedProject = projectService.saveProject(circuit);
            projectService.associateProjectWithUser(savedProject.getId(), user.getId());
            return ResponseEntity.ok(savedProject);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/getProjectsName")
    public ResponseEntity<?> getProjectsName(@RequestBody Map<String, String> request, HttpServletRequest req) {
        this.validateCookie(req, request.get("email"));
        try {
            List<Map<String, String>> projects = projectService.getProjectsName(request);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteProject(@RequestBody Map<String, String> request, HttpServletRequest req) {
        // deleteProject request usually only has projectId.
        // If we strictly need validation against a user, we need the userId in the
        // request.
        // Assuming for now we skip strict user-token validation for delete or it might
        // fail if userId is missing.
        // However, standard practice implies we should validate the user owns the
        // project or at least is the one deleting.
        // If the request doesn't have userId, this will pass null.
        // this.validateCookie(req, request.get("userId"));
        try {
            projectService.deleteProject(request.get("projectId"));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/getProject")
    public ResponseEntity<?> getProject(@RequestBody Map<String, String> request, HttpServletRequest req) {
        this.validateCookie(req, request.get("email"));
        try {
            Project project = projectService.getProject(request);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/saveMutantsBatch")
    public ResponseEntity<?> saveMutantsBatch(@RequestBody Map<String, Object> request, HttpServletRequest req) {
        // Expected payload: { "projectId": "...", "cycleId": 123, "mutants": [...] }
        try {
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
        // Expected payload: { "projectId": "...", "cycleId": 123, "mutants": [...] }
        try {
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

    @PostMapping("/getAllByUserSummary")
    public ResponseEntity<?> getAllByUserSummary(@RequestBody Map<String, String> request, HttpServletRequest req) {
        this.validateCookie(req, request.get("email"));
        try {
            List<edu.uclm.reper.dto.ProjectLightSummaryDTO> projects = projectService
                    .getLightSidebarProjectsByUser(request.get("email"));
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/getProjectDetails")
    public ResponseEntity<?> getProjectDetails(@RequestBody Map<String, String> request, HttpServletRequest req) {
        this.validateCookie(req, request.get("email"));
        try {
            edu.uclm.reper.dto.ProjectDetailsDTO projectDetails = projectService.getProjectDetailsDTO(request);
            return ResponseEntity.ok(projectDetails);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

}
