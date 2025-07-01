package edu.uclm.reper.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uclm.reper.model.OperatorType;
import edu.uclm.reper.repository.OperatorTypeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QumuService {
    
@Autowired
OperatorTypeRepository operatorTypeRepository;

public List<OperatorType> getOperatorsByFamily(){
    List<OperatorType> operators = operatorTypeRepository.findAll();
    return operators;
}

}
