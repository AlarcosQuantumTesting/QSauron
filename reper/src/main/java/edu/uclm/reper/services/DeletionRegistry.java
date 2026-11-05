package edu.uclm.reper.services;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class DeletionRegistry {

    private final Set<String> deletingKeys = ConcurrentHashMap.newKeySet();

    public boolean registerProject(String projectId) {
        if (projectId == null) return false;
        return deletingKeys.add("project:" + projectId);
    }

    public void unregisterProject(String projectId) {
        if (projectId != null) {
            deletingKeys.remove("project:" + projectId);
        }
    }

    public boolean isProjectDeleting(String projectId) {
        if (projectId == null) return false;
        return deletingKeys.contains("project:" + projectId);
    }

    public boolean registerCycle(String projectId, int cycleId) {
        if (projectId == null) return false;
        return deletingKeys.add("cycle:" + projectId + ":" + cycleId);
    }

    public void unregisterCycle(String projectId, int cycleId) {
        if (projectId != null) {
            deletingKeys.remove("cycle:" + projectId + ":" + cycleId);
        }
    }

    public boolean isCycleDeleting(String projectId, int cycleId) {
        if (projectId == null) return false;
        return deletingKeys.contains("project:" + projectId) || deletingKeys.contains("cycle:" + projectId + ":" + cycleId);
    }
}
