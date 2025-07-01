package edu.uclm.reper.http;

import edu.uclm.reper.model.Project;
import edu.uclm.reper.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * Crea un nuevo proyecto y lo asocia con un usuario
     * POST /api/projects/create
     * Body: {"projectName": "Mi Proyecto", "userId": "user123"}
     */
    @PostMapping("/create")
    public ResponseEntity<?> createProject(@RequestBody Map<String,String> request) {
        try {
            Project project = projectService.createProjectForUser(request.get("projectId"), request.get("userId"));
            return ResponseEntity.ok(project);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }


    /**
     * Asocia un proyecto existente con un usuario
     * POST /api/projects/{projectId}/associate/{userId}
     */
    @PostMapping("/{projectId}/associate/{userId}")
    public ResponseEntity<?> associateProjectWithUser(@PathVariable Long projectId, @PathVariable String userId) {
        try {
            Project project = projectService.associateProjectWithUser(projectId, userId);
            return ResponseEntity.ok(project);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Obtiene todos los proyectos de un usuario
     * GET /api/projects/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getProjectsByUser(@PathVariable String userId) {
        try {
            List<Project> projects = projectService.getProjectsByUser(userId);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Remueve un usuario de un proyecto
     * DELETE /api/projects/{projectId}/user/{userId}
     */
    @DeleteMapping("/{projectId}/user/{userId}")
    public ResponseEntity<?> removeUserFromProject(@PathVariable Long projectId, @PathVariable String userId) {
        try {
            Project project = projectService.removeUserFromProject(projectId, userId);
            return ResponseEntity.ok(project);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

}
