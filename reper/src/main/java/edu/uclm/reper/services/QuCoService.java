package edu.uclm.reper.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uclm.reper.model.Generator;
import edu.uclm.reper.repository.GeneratorRepository;

@Service
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

    public void saveCircuit(Generator generator) {
        if (generator == null) {
            throw new IllegalArgumentException("Generator or its ID cannot be null");
        }

        generatorRepository.save(generator);
    }
    
   

}
