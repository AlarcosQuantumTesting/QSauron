package edu.uclm.reper.dto;

import java.util.ArrayList;
import java.util.List;

public class MergePreviewDTO {

    public static class DuplicateCycle {
        private String projectId;
        private int cycleId;
        private String name;

        public DuplicateCycle() {}

        public DuplicateCycle(String projectId, int cycleId, String name) {
            this.projectId = projectId;
            this.cycleId = cycleId;
            this.name = name;
        }

        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }
        public int getCycleId() { return cycleId; }
        public void setCycleId(int cycleId) { this.cycleId = cycleId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    private String survivorId;
    private String survivorName;
    private String deletedId;
    private String deletedName;
    private int cyclesToMove;
    private List<DuplicateCycle> duplicateCyclesToDelete = new ArrayList<>();
    private String resultingStatus;

    public String getSurvivorId() { return survivorId; }
    public void setSurvivorId(String survivorId) { this.survivorId = survivorId; }
    public String getSurvivorName() { return survivorName; }
    public void setSurvivorName(String survivorName) { this.survivorName = survivorName; }
    public String getDeletedId() { return deletedId; }
    public void setDeletedId(String deletedId) { this.deletedId = deletedId; }
    public String getDeletedName() { return deletedName; }
    public void setDeletedName(String deletedName) { this.deletedName = deletedName; }
    public int getCyclesToMove() { return cyclesToMove; }
    public void setCyclesToMove(int cyclesToMove) { this.cyclesToMove = cyclesToMove; }
    public List<DuplicateCycle> getDuplicateCyclesToDelete() { return duplicateCyclesToDelete; }
    public void setDuplicateCyclesToDelete(List<DuplicateCycle> d) { this.duplicateCyclesToDelete = d; }
    public String getResultingStatus() { return resultingStatus; }
    public void setResultingStatus(String resultingStatus) { this.resultingStatus = resultingStatus; }
}
