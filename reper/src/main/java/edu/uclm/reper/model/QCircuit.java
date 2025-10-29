package edu.uclm.reper.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "code", columnDefinition = "JSON")
    @JsonProperty("quirkCode")
    private JsonNode code;
    

    
    public QCircuit() {}

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
}
