package edu.uclm.reper.dto;

public class RepresentationDTO {
    private String format;
    private String extension;
    private String code;

    public RepresentationDTO() {}

    public RepresentationDTO(String format, String extension, String code) {
        this.format = format;
        this.extension = extension;
        this.code = code;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
