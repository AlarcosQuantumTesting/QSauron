package edu.uclm.reper.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.ProjectNote;
import edu.uclm.reper.services.ProjectService;
import edu.uclm.reper.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:4201" }, allowCredentials = "true")
@RestController
@RequestMapping("/notes")
public class NotesController {

    @Autowired private ProjectService projectService;
    @Autowired private UserService userService;

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

    // LIST: devuelve notas del proyecto
    // Body: { "email": "...", "projectId": "..." }
    @PostMapping("/list")
    public ResponseEntity<?> list(@RequestBody Map<String, String> req, HttpServletRequest httpReq) {
        String email = req.get("email");
        String projectId = req.get("projectId");
        if (email == null || email.isBlank()) return ResponseEntity.badRequest().body("Error: email required");
        if (projectId == null || projectId.isBlank()) return ResponseEntity.badRequest().body("Error: projectId required");
        this.validateCookie(httpReq, email);

        try {
            // reutiliza tu lógica que valida user asociado al proyecto
            Map<String, String> r = new HashMap<>();
            r.put("email", email);
            r.put("projectId", projectId);
            Project p = projectService.getProject(r);

            List<ProjectNote> notes = p.getNotes();
            if (notes == null) notes = new ArrayList<>();

            // opcional: ordenar por timestamp desc
            notes.sort((a,b) -> {
                Date ta = a.getTimestamp();
                Date tb = b.getTimestamp();
                if (ta == null && tb == null) return 0;
                if (ta == null) return 1;
                if (tb == null) return -1;
                return tb.compareTo(ta);
            });

            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    // CREATE: añade nota al proyecto
    // Body: { "email":"...", "projectId":"...", "title":"...", "text":"...", "type":"general" }
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> req, HttpServletRequest httpReq) {
        String email = (String) req.get("email");
        String projectId = (String) req.get("projectId");
        if (email == null || email.isBlank()) return ResponseEntity.badRequest().body("Error: email required");
        if (projectId == null || projectId.isBlank()) return ResponseEntity.badRequest().body("Error: projectId required");
        this.validateCookie(httpReq, email);

        try {
            Map<String, String> r = new HashMap<>();
            r.put("email", email);
            r.put("projectId", projectId);
            Project p = projectService.getProject(r);

            String title = String.valueOf(req.getOrDefault("title", ""));
            String text  = String.valueOf(req.getOrDefault("text", ""));
            String type  = String.valueOf(req.getOrDefault("type", "general"));

            ProjectNote n = new ProjectNote();
            n.setId(UUID.randomUUID().toString());
            n.setTitle(title);
            n.setText(text);
            n.setType(type);
            n.setTimestamp(new Date());

            if (p.getNotes() == null) p.setNotes(new ArrayList<>());
            p.getNotes().add(n);

            Project saved = projectService.saveProject(p);
            return ResponseEntity.ok(saved.getNotes());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    // UPDATE: edita una nota (buscando por id dentro del proyecto)
    // Body: { "email":"...", "projectId":"...", "title":..., "text":..., "type":... }
    @PutMapping("/{noteId}")
    public ResponseEntity<?> update(@PathVariable String noteId, @RequestBody Map<String, Object> req, HttpServletRequest httpReq) {
        String email = (String) req.get("email");
        String projectId = (String) req.get("projectId");
        if (email == null || email.isBlank()) return ResponseEntity.badRequest().body("Error: email required");
        if (projectId == null || projectId.isBlank()) return ResponseEntity.badRequest().body("Error: projectId required");
        this.validateCookie(httpReq, email);

        try {
            Map<String, String> r = new HashMap<>();
            r.put("email", email);
            r.put("projectId", projectId);
            Project p = projectService.getProject(r);

            if (p.getNotes() == null) p.setNotes(new ArrayList<>());

            ProjectNote target = null;
            for (ProjectNote n : p.getNotes()) {
                if (noteId.equals(n.getId())) { target = n; break; }
            }
            if (target == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: note not found");

            if (req.containsKey("title")) target.setTitle(String.valueOf(req.get("title")));
            if (req.containsKey("text"))  target.setText(String.valueOf(req.get("text")));
            if (req.containsKey("type"))  target.setType(String.valueOf(req.get("type")));
            target.setTimestamp(new Date());

            Project saved = projectService.saveProject(p);
            return ResponseEntity.ok(saved.getNotes());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    // DELETE: borra nota del proyecto
    // Body: { "email":"...", "projectId":"..." }
    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> delete(@PathVariable String noteId, @RequestBody Map<String, Object> req, HttpServletRequest httpReq) {
        String email = (String) req.get("email");
        String projectId = (String) req.get("projectId");
        if (email == null || email.isBlank()) return ResponseEntity.badRequest().body("Error: email required");
        if (projectId == null || projectId.isBlank()) return ResponseEntity.badRequest().body("Error: projectId required");
        this.validateCookie(httpReq, email);

        try {
            Map<String, String> r = new HashMap<>();
            r.put("email", email);
            r.put("projectId", projectId);
            Project p = projectService.getProject(r);

            if (p.getNotes() == null) p.setNotes(new ArrayList<>());

            boolean removed = p.getNotes().removeIf(n -> noteId.equals(n.getId()));
            if (!removed) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: note not found");

            Project saved = projectService.saveProject(p);
            return ResponseEntity.ok(saved.getNotes());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }
}
