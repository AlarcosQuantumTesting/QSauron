package edu.uclm.reper.dto;

public class ConvertRequestDTO {
    private String email;
    private String fileName;
    private String format;
    private String code;

    public ConvertRequestDTO() {}

    public ConvertRequestDTO(String email, String fileName, String format, String code) {
        this.email = email;
        this.fileName = fileName;
        this.format = format;
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
