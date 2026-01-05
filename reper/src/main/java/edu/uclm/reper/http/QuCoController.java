package edu.uclm.reper.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.reper.model.Generator;
import edu.uclm.reper.services.QuCoService;

@RestController()
@RequestMapping("qucoreper")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:4201" }, allowCredentials = "true")
public class QuCoController {

    @Autowired
    private QuCoService qucoService;

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

}
