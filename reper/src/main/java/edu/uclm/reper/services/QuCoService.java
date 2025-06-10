package edu.uclm.reper.services;

import org.springframework.beans.factory.annotation.Autowired;
import edu.uclm.reper.model.Generator;
import edu.uclm.reper.repository.GeneratorRepository;

public class QuCoService {

    @Autowired
    private GeneratorRepository generatorRepository;

    public Generator getCircuit(int id) {

        Generator g = generatorRepository.findById(id);
        
        if (g == null) {
            throw new RuntimeException("Circuit not found for id: " + id);
        }

        return g;

    }
    
   

}
