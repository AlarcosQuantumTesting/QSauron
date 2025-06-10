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
@CrossOrigin("*")
public class QuCoController {
    
     @Autowired
    private QuCoService qucoService;


    @PostMapping("/circuit")
    public Generator getCircuit(@RequestBody int id) {
        try{

            return qucoService.getCircuit(id);

        } catch (Exception e) {

            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());

        }
    }

}
