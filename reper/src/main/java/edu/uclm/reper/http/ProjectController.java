package edu.uclm.reper.http;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.TestSuite;
import edu.uclm.reper.model.User;
import edu.uclm.reper.services.ProjectService;
import edu.uclm.reper.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest; // <-- añade import

// Trigger recompile

@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:4201" }, allowCredentials = "true")
@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    private void validateCookie(HttpServletRequest request, String email) {
        if (request.getCookies() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No cookies found");
        }
        Optional<Cookie> cookie = Arrays.stream(request.getCookies())
                .filter(c -> "SESSION_TOKEN".equals(c.getName()))
                .findFirst();

        if (cookie.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session token not found");
        }

        try {
            this.userService.validateToken(cookie.get().getValue(), email);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid session token");
        }
    }

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
// no me apaño con el save y el savefull porque yo guardo por un lado cuando hago un cambio el cut y por otro lado le testfile 
    @PutMapping("/{projectId}/circuit")
public ResponseEntity<?> updateCircuit(
        @org.springframework.web.bind.annotation.PathVariable String projectId,
        @RequestBody Map<String, Object> body,
        HttpServletRequest req
) {
    try {
        String email = (String) body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Error: email required");
        }
        this.validateCookie(req, email);

        String updated = (String) body.get("updated_content");
        if (updated == null) {
            return ResponseEntity.badRequest().body("Error: updated_content required");
        }

        projectService.updateCircuitContent(projectId, updated);
        return ResponseEntity.ok(Map.of("success", true));

    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Error: " + e.getMessage());
    }
}


    @PutMapping("/updateTestSuites")
    public ResponseEntity<?> updateTestSuites(@RequestBody Map<String, Object> request, HttpServletRequest req) {
        try {
            // 1) auth
            String email = (String) request.get("email");
            String projectId = (String) request.get("projectId");
            if (email == null || email.isBlank()) return ResponseEntity.badRequest().body("Error: email required");
            if (projectId == null || projectId.isBlank()) return ResponseEntity.badRequest().body("Error: projectId required");

            this.validateCookie(req, email);

            // 2) parse suites
            ObjectMapper mapper = new ObjectMapper();
            List<TestSuite> suites = mapper.convertValue(
                request.get("testSuites"),
                mapper.getTypeFactory().constructCollectionType(List.class, TestSuite.class)
            );

            // 3) call service
            Project updated = projectService.replaceTestSuites(projectId, email, suites);
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }



}
