package edu.uclm.reper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;

public class QuMuCircuitDTO {
    private String id;

    @JsonProperty("quirkCode")
    @JsonRawValue
    private JsonNode quirkCode;

    private String mutableColumns;
    private String mutableRows;

    public QuMuCircuitDTO() {
    }

    public QuMuCircuitDTO(String id, JsonNode quirkCode, String mutableColumns, String mutableRows) {
        this.id = id;
        this.quirkCode = quirkCode;
        this.mutableColumns = mutableColumns;
        this.mutableRows = mutableRows;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JsonNode getQuirkCode() {
        return quirkCode;
    }

    public void setQuirkCode(JsonNode quirkCode) {
        this.quirkCode = quirkCode;
    }

    public String getMutableColumns() {
        return mutableColumns;
    }

    public void setMutableColumns(String mutableColumns) {
        this.mutableColumns = mutableColumns;
    }

    public String getMutableRows() {
        return mutableRows;
    }

    public void setMutableRows(String mutableRows) {
        this.mutableRows = mutableRows;
    }
}
