package edu.uclm.reper.dto;

import com.fasterxml.jackson.databind.JsonNode;

public class ConvertedCircuitDTO {
    private String format;
    private int qubits;
    private int columns;
    private JsonNode quirk;

    public ConvertedCircuitDTO() {}

    public ConvertedCircuitDTO(String format, int qubits, int columns, JsonNode quirk) {
        this.format = format;
        this.qubits = qubits;
        this.columns = columns;
        this.quirk = quirk;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getQubits() {
        return qubits;
    }

    public void setQubits(int qubits) {
        this.qubits = qubits;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public JsonNode getQuirk() {
        return quirk;
    }

    public void setQuirk(JsonNode quirk) {
        this.quirk = quirk;
    }
}
