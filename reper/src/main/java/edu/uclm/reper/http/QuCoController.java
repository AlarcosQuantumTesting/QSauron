package edu.uclm.reper.http;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.reper.model.Generator;
import edu.uclm.reper.services.ProjectService;
import edu.uclm.reper.services.QuCoService;
import jakarta.servlet.http.HttpServletRequest;

@RestController()
@RequestMapping("qucoreper")
public class QuCoController extends BaseController {

    @Autowired
    private QuCoService qucoService;

    @Autowired
    private ProjectService projectService;

    @PostMapping("/getcircuit")
    public Generator getCircuit(@RequestBody int id) {
        try {
            return qucoService.getCircuit(id);
        } catch (Exception e) {

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/savecircuit")
    public void saveCircuit(@RequestBody Generator generator) {
        try {
            qucoService.saveCircuit(generator);
        } catch (Exception e) {

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
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

}
