package edu.uclm.reper.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uclm.reper.model.OperatorType;
import edu.uclm.reper.model.MutantCycleId;
import edu.uclm.reper.repository.OperatorTypeRepository;
import edu.uclm.reper.repository.MutantCycleRepository;

@Service
public class QumuService {

    @Autowired
    OperatorTypeRepository operatorTypeRepository;

    @Autowired
    MutantCycleRepository mutantCycleRepository;

    public List<OperatorType> getOperatorsByFamily() {
        List<OperatorType> operators = operatorTypeRepository.findAll();
        return operators;
    }

    public void deleteMutantCycle(String projectId, int cycleId) {
        mutantCycleRepository.deleteById(new MutantCycleId(projectId, cycleId));
    }

}
