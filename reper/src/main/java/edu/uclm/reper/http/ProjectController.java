package edu.uclm.reper.http;

import edu.uclm.reper.model.MutantCycle;
import edu.uclm.reper.model.Project;
import edu.uclm.reper.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
     * POST /api/projects/associate/{projectId}{userId}
     */
    @PostMapping("/associate")
    public ResponseEntity<?> associateProjectWithUser(@RequestBody Map<String, String> request) {
        try {
            projectService.associateProjectWithUser(request.get("projectId"), request.get("userId"));
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
    public ResponseEntity<?> getProjectsByUser(@RequestBody Map<String,String> request) {
        
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
    @PutMapping(value = "/save", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> saveProject(@RequestBody Project project) {
        try {
            Project savedProject = projectService.saveProject(project);
            return ResponseEntity.ok(savedProject);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Guarda/actualiza los datos de un proyecto (POST también)
     * POST /api/projects/save
     */
    @PostMapping(value = "/save", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> saveProjectPost(@RequestBody Project project) {
        try {
            Project savedProject = projectService.saveProject(project);
            return ResponseEntity.ok(savedProject);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    

}
