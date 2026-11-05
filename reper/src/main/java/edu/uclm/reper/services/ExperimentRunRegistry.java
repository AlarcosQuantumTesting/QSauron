package edu.uclm.reper.services;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro de experimentos que están actualmente en ejecución en esta JVM.
 * Se usa para detectar experimentos que quedaron RUNNING tras un reinicio.
 */
@Component
public class ExperimentRunRegistry {

    private final Set<String> running = ConcurrentHashMap.newKeySet();

    public void register(String experimentId) {
        running.add(experimentId);
    }

    public void unregister(String experimentId) {
        running.remove(experimentId);
    }

    public boolean isRunning(String experimentId) {
        return running.contains(experimentId);
    }
}

