package edu.uclm.reper.http;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.User;
import edu.uclm.reper.services.ProjectService;

// Trigger recompile

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * Crea un nuevo proyecto y lo asocia con un usuario
     * POST /api/projects/create
     * Body: {"projectName": "Mi Proyecto", "userId": "user123"}
     */

    @PostMapping("/create")
    public ResponseEntity<?> createProject(@RequestBody Map<String, String> request) {
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
    public ResponseEntity<?> associateProjectWithUser(@RequestBody Map<String, String> request) {
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
    public ResponseEntity<?> getProjectsByUser(@RequestBody Map<String, String> request) {

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
    public ResponseEntity<?> saveProject(@RequestBody Map<String, Object> request) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            Project circuit = mapper.convertValue(request.get("circuit"), Project.class);
            User user = mapper.convertValue(request.get("user"), User.class);

            Project savedProject = projectService.saveProject(circuit);
            projectService.associateProjectWithUser(savedProject.getId(), user.getId());
            return ResponseEntity.ok(savedProject);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/getProjectsName")
    public ResponseEntity<?> getProjectsName(@RequestBody Map<String, String> request) {

        try {
            List<Map<String, String>> projects = projectService.getProjectsName(request);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteProject(@RequestBody Map<String, String> request) {

        try {
            projectService.deleteProject(request.get("projectId"));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/getProject")
    public ResponseEntity<?> getProject(@RequestBody Map<String, String> request) {

        try {
            Project project = projectService.getProject(request);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

}
