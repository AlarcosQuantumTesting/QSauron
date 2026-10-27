package edu.uclm.reper.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonRawValue;

import jakarta.persistence.Column;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Qcircuits")
@JsonIgnoreProperties(ignoreUnknown = true)
public class QCircuit {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "qbits")
    private int qbits;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "code", columnDefinition = "JSON")
    @JsonProperty("quirkCode")
    @JsonRawValue
    private JsonNode code;

    @Column(name = "mutableColumns", columnDefinition = "TEXT")
    @JsonProperty("mutableColumns")
    private String mutableColumns;

    @Column(name = "mutableRows")
    @JsonProperty("mutableRows")
    private String mutableRows;

    public QCircuit() {
    }

    public QCircuit(int qbits, JsonNode code) {
        this.qbits = qbits;
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JsonNode getCode() {
        return code;
    }

    public void setCode(JsonNode code) {
        this.code = code;
    }

    public int getQbits() {
        return qbits;
    }

    public void setQbits(int qbits) {
        this.qbits = qbits;
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
