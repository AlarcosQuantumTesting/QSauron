package edu.uclm.reper.http;

import edu.uclm.reper.services.ImportException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Manejador común de ImportException para el import de proyectos y el traductor. */
@RestControllerAdvice(assignableTypes = { ImportController.class, ConverterController.class })
public class ImportExceptionAdvice {

    @ExceptionHandler(ImportException.class)
    public ResponseEntity<Map<String, Object>> handleImportException(ImportException e) {
        Map<String, Object> errorBody = Map.of(
                "errorCode", e.getErrorCode(),
                "error", e.getMessage() != null ? e.getMessage() : ""
        );
        return ResponseEntity.status(e.getHttpStatus()).body(errorBody);
    }
}
