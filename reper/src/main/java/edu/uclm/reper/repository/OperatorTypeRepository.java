package edu.uclm.reper.repository;

import edu.uclm.reper.model.OperatorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperatorTypeRepository extends JpaRepository<OperatorType, Long> {
    
    // El método findAll() ya está disponible automáticamente
    // Si necesitas métodos adicionales, puedes agregarlos aquí
    
}