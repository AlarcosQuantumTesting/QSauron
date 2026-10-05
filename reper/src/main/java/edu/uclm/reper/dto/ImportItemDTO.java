package edu.uclm.reper.dto;

public class ImportItemDTO {
    private String fileName;
    private String format;
    private String projectName;
    private String code;

    public ImportItemDTO() {}

    public ImportItemDTO(String fileName, String format, String projectName, String code) {
        this.fileName = fileName;
        this.format = format;
        this.projectName = projectName;
        this.code = code;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
