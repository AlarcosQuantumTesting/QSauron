package edu.uclm.reper.dto;

import java.util.List;

public class ImportResultDTO {
    private int total;
    private int successCount;
    private int failureCount;
    private List<ImportItemResultDTO> items;

    public ImportResultDTO() {}

    public ImportResultDTO(int total, int successCount, int failureCount, List<ImportItemResultDTO> items) {
        this.total = total;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.items = items;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public List<ImportItemResultDTO> getItems() {
        return items;
    }

    public void setItems(List<ImportItemResultDTO> items) {
        this.items = items;
    }
}
