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
import edu.uclm.reper.services.BackgroundJobService;
import edu.uclm.reper.services.DeletionRegistry;
import edu.uclm.reper.services.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/projects")
public class ProjectController extends BaseController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private BackgroundJobService backgroundJobService;

    @Autowired
    private DeletionRegistry deletionRegistry;

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

    @PostMapping("/delete")
    public ResponseEntity<?> deleteProject(@RequestBody Map<String, String> request, HttpServletRequest req) {
        String userId = request != null ? (request.get("userId") != null ? request.get("userId") : request.get("email")) : null;
        String projectId = request != null ? request.get("projectId") : null;
        this.validateCookie(req, userId);

        if (projectId == null || projectId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing projectId"));
        }

        if (!deletionRegistry.registerProject(projectId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Project deletion already in progress"));
        }

        try {
            String jobId = backgroundJobService.submit("delete", userId, () -> {
                try {
                    projectService.deleteProject(projectId);
                    return Map.of("message", "Project deleted successfully", "id", projectId);
                } finally {
                    deletionRegistry.unregisterProject(projectId);
                }
            });
            return ResponseEntity.ok(Map.of("message", "Project deletion started", "jobId", jobId, "id", projectId));
        } catch (Exception e) {
            deletionRegistry.unregisterProject(projectId);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
