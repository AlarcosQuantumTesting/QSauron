package edu.uclm.reper.http;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.reper.model.MutantCycle;
import edu.uclm.reper.model.OperatorType;
import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.User;
import edu.uclm.reper.services.QumuService;

@RestController()
@RequestMapping("qumureper")
@CrossOrigin("*")
public class QuMuController {

    @Autowired
    private QumuService qumuService;

    @GetMapping("/getoperatorsByFamily")
    public List<OperatorType> getOperatorsByFamily() {
        try {
            return qumuService.getOperatorsByFamily();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

/*  @PostMapping("/saveMutantCycle")
    public ResponseEntity<String> saveMutantCycle(@RequestBody MutantCycle mutantCycle) {
        try {
            qumuService.saveMutantCycle(mutantCycle);
            return ResponseEntity.ok("Mutant Cycle saved successfully");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    @PostMapping("/addMutantCycleToProject")
    public ResponseEntity<String> addMutantCycleToProject(@RequestBody Map<String, String> request) {
        try {
            qumuService.addMutantCycleToProject(mutantCycle, projectId);
            return ResponseEntity.ok("Mutant Cycle added to Project successfully");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
*/
}
