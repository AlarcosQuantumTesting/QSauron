package edu.uclm.reper.dto;

import com.fasterxml.jackson.databind.JsonNode;

public class RepresentRequestDTO {
    private String email;
    private JsonNode quirk;
    private String targetFormat;

    public RepresentRequestDTO() {}

    public RepresentRequestDTO(String email, JsonNode quirk, String targetFormat) {
        this.email = email;
        this.quirk = quirk;
        this.targetFormat = targetFormat;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public JsonNode getQuirk() {
        return quirk;
    }

    public void setQuirk(JsonNode quirk) {
        this.quirk = quirk;
    }

    public String getTargetFormat() {
        return targetFormat;
    }

    public void setTargetFormat(String targetFormat) {
        this.targetFormat = targetFormat;
    }
}
