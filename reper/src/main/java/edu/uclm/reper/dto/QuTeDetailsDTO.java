package edu.uclm.reper.dto;

public class QuTeDetailsDTO {
    private int testCasesCount;
    private String suiteType;

    public QuTeDetailsDTO() {
    }

    public QuTeDetailsDTO(int testCasesCount, String suiteType) {
        this.testCasesCount = testCasesCount;
        this.suiteType = suiteType;
    }

    public int getTestCasesCount() {
        return testCasesCount;
    }

    public void setTestCasesCount(int testCasesCount) {
        this.testCasesCount = testCasesCount;
    }

    public String getSuiteType() {
        return suiteType;
    }

    public void setSuiteType(String suiteType) {
        this.suiteType = suiteType;
    }
}
