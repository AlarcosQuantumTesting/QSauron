package edu.uclm.reper.http;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.reper.model.OperatorType;
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

}
