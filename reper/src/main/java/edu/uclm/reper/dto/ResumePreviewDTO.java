package edu.uclm.reper.dto;

import java.util.ArrayList;
import java.util.List;

public class ResumePreviewDTO {

    public static class PendingCycle {
        private String projectId;
        private int cycleId;
        private String name;
        private long pendingMutants;
        private String inputQubits;
        private String outputQubits;
        private boolean needsQubits;
        private int qubits;
        private String suggestedInputQubits;
        private String suggestedOutputQubits;
        private boolean suggestedFromResults;

        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }
        public int getCycleId() { return cycleId; }
        public void setCycleId(int cycleId) { this.cycleId = cycleId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public long getPendingMutants() { return pendingMutants; }
        public void setPendingMutants(long pendingMutants) { this.pendingMutants = pendingMutants; }
        public String getInputQubits() { return inputQubits; }
        public void setInputQubits(String inputQubits) { this.inputQubits = inputQubits; }
        public String getOutputQubits() { return outputQubits; }
        public void setOutputQubits(String outputQubits) { this.outputQubits = outputQubits; }
        public boolean isNeedsQubits() { return needsQubits; }
        public void setNeedsQubits(boolean needsQubits) { this.needsQubits = needsQubits; }
        public int getQubits() { return qubits; }
        public void setQubits(int qubits) { this.qubits = qubits; }
        public String getSuggestedInputQubits() { return suggestedInputQubits; }
        public void setSuggestedInputQubits(String v) { this.suggestedInputQubits = v; }
        public String getSuggestedOutputQubits() { return suggestedOutputQubits; }
        public void setSuggestedOutputQubits(String v) { this.suggestedOutputQubits = v; }
        public boolean isSuggestedFromResults() { return suggestedFromResults; }
        public void setSuggestedFromResults(boolean v) { this.suggestedFromResults = v; }
    }

    public static class SkippedCycle {
        private String projectId;
        private int cycleId;
        private String name;
        private String reason;

        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }
        public int getCycleId() { return cycleId; }
        public void setCycleId(int cycleId) { this.cycleId = cycleId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    private boolean resumable;
    private String reason;
    private List<PendingCycle> pendingCycles = new ArrayList<>();
    private List<SkippedCycle> skippedCycles = new ArrayList<>();
    private int remainingCircuits;
    private boolean hasDefinition;

    public boolean isResumable() { return resumable; }
    public void setResumable(boolean resumable) { this.resumable = resumable; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public List<PendingCycle> getPendingCycles() { return pendingCycles; }
    public void setPendingCycles(List<PendingCycle> pendingCycles) { this.pendingCycles = pendingCycles; }
    public List<SkippedCycle> getSkippedCycles() { return skippedCycles; }
    public void setSkippedCycles(List<SkippedCycle> skippedCycles) { this.skippedCycles = skippedCycles; }
    public int getRemainingCircuits() { return remainingCircuits; }
    public void setRemainingCircuits(int remainingCircuits) { this.remainingCircuits = remainingCircuits; }
    public boolean isHasDefinition() { return hasDefinition; }
    public void setHasDefinition(boolean hasDefinition) { this.hasDefinition = hasDefinition; }
}
