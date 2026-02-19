package edu.uclm.reper.http;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.TestSuite;
import edu.uclm.reper.model.User;
import edu.uclm.reper.services.ProjectService;
import edu.uclm.reper.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:4201" }, allowCredentials = "true")
@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired private ProjectService projectService;
    @Autowired private UserService userService;

    // ---- DTOs (mínimos) ----
    public static class SaveProjectRequest {
        public Project circuit;
        public User user;
    }

    public static class UpdateCircuitRequest {
        public String email;
        public String updated_content;
    }

    public static class UpdateTestSuitesRequest {
        public String email;
        public String projectId;
        public List<TestSuite> testSuites;
    }

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

    // ✅ IMPORTANTE: /save ya no usa Map + convertValue
    @PutMapping("/save")
    public ResponseEntity<?> saveProject(@RequestBody SaveProjectRequest request, HttpServletRequest req) {
        try {
            if (request == null || request.user == null || request.user.getId() == null) {
                return ResponseEntity.badRequest().body("Error: user.id required");
            }
            if (request.circuit == null) {
                return ResponseEntity.badRequest().body("Error: circuit required");
            }

            // Si quieres validar cookie, hazlo aquí
            // this.validateCookie(req, request.user.getId());

            Project savedProject = projectService.saveProject(request.circuit);
            projectService.associateProjectWithUser(savedProject.getId(), request.user.getId());
            return ResponseEntity.ok(savedProject);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ✅ update solo circuito (cambia CUT)
    @PutMapping("/{projectId}/circuit")
    public ResponseEntity<?> updateCircuit(
            @PathVariable String projectId,
            @RequestBody UpdateCircuitRequest body,
            HttpServletRequest req
    ) {
        try {
            if (body == null || body.email == null || body.email.isBlank()) {
                return ResponseEntity.badRequest().body("Error: email required");
            }
            this.validateCookie(req, body.email);

            if (body.updated_content == null) {
                return ResponseEntity.badRequest().body("Error: updated_content required");
            }

            projectService.updateCircuitContent(projectId, body.updated_content);
            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ✅ update SOLO testSuites (incluye entryValues ya como List<String>)
    @PutMapping("/updateTestSuites")
    public ResponseEntity<?> updateTestSuites(@RequestBody UpdateTestSuitesRequest request, HttpServletRequest req) {
        try {
            if (request == null) return ResponseEntity.badRequest().body("Error: body required");
            if (request.email == null || request.email.isBlank()) return ResponseEntity.badRequest().body("Error: email required");
            if (request.projectId == null || request.projectId.isBlank()) return ResponseEntity.badRequest().body("Error: projectId required");
            if (request.testSuites == null) return ResponseEntity.badRequest().body("Error: testSuites required");

            this.validateCookie(req, request.email);

            Project updated = projectService.replaceTestSuites(request.projectId, request.email, request.testSuites);
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
