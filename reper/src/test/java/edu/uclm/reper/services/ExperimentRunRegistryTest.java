package edu.uclm.reper.services;

import edu.uclm.reper.model.Experiment;
import edu.uclm.reper.repository.ExperimentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests para ExperimentRunRegistry y el listener de arranque que marca
 * experimentos RUNNING como FAILED cuando reper se reinicia.
 */
class ExperimentRunRegistryTest {

    // --- Tests del registro en memoria ---

    @Test
    void register_and_isRunning() {
        ExperimentRunRegistry registry = new ExperimentRunRegistry();
        assertFalse(registry.isRunning("exp-1"));
        registry.register("exp-1");
        assertTrue(registry.isRunning("exp-1"));
    }

    @Test
    void unregister_removes_entry() {
        ExperimentRunRegistry registry = new ExperimentRunRegistry();
        registry.register("exp-2");
        registry.unregister("exp-2");
        assertFalse(registry.isRunning("exp-2"));
    }

    @Test
    void unregister_nonexistent_does_not_throw() {
        ExperimentRunRegistry registry = new ExperimentRunRegistry();
        assertDoesNotThrow(() -> registry.unregister("not-registered"));
    }

    // --- Test del listener de startup que marca RUNNING → FAILED ---

    @Test
    void markInterruptedExperimentsOnStartup_marks_running_as_failed() {
        // Preparamos un experimento stub en estado RUNNING
        Experiment runningExp = new Experiment();
        runningExp.setId("exp-interrupted");
        runningExp.setStatus("RUNNING");
        runningExp.setDurationSeconds(0L);

        ExperimentRepository repo = mock(ExperimentRepository.class);
        when(repo.findByStatus("RUNNING")).thenReturn(List.of(runningExp));
        // findByIdWithDetails se usa dentro de markExperimentFinished
        when(repo.findByIdWithDetails("exp-interrupted")).thenReturn(Optional.of(runningExp));

        // Montamos ExperimentService con dependencias mínimas
        ExperimentService service = new ExperimentService();
        injectField(service, "experimentRepository", repo);
        injectField(service, "runRegistry", new ExperimentRunRegistry());
        // Resto de dependencias que markExperimentFinished no usa directamente
        // se dejan null — el método sólo accede a experimentRepository

        // Ejecutamos el listener
        service.markInterruptedExperimentsOnStartup();

        // Verificamos que se llamó save con el experimento marcado FAILED
        ArgumentCaptor<Experiment> captor = ArgumentCaptor.forClass(Experiment.class);
        verify(repo, atLeastOnce()).save(captor.capture());

        Experiment saved = captor.getAllValues().stream()
                .filter(e -> "exp-interrupted".equals(e.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(saved, "Se debe haber guardado el experimento interrumpido");
        assertEquals("FAILED", saved.getStatus());
        assertNotNull(saved.getErrorHistory(), "errorHistory debe contener el mensaje de reinicio");
        assertTrue(saved.getErrorHistory().contains("Interrumpido"),
                "errorHistory debe mencionar que fue interrumpido");
    }

    // Helper de inyección de campos privados para tests unitarios sin Spring
    private static void injectField(Object target, String fieldName, Object value) {
        try {
            var field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo inyectar campo " + fieldName, e);
        }
    }

    private static java.lang.reflect.Field findField(Class<?> clazz, String name) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new RuntimeException("Campo no encontrado: " + name);
    }
}

