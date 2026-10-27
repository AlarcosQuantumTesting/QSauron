package edu.uclm.reper.repository;

import edu.uclm.reper.model.OperatorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperatorTypeRepository extends JpaRepository<OperatorType, Long> {

    // El método findAll() ya está disponible automáticamente
    // Si necesitas métodos adicionales, puedes agregarlos aquí

}