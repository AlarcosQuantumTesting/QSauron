package edu.uclm.reper.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "blocks_generators")
@DiscriminatorValue("BLOCKS")
public class Blocks extends Generator {

    @Column(name = "minNumberColumns")
    private int minNumberColumns;

    @Column(name = "maxNumberColumns")
    private int maxNumberColumns;

    @Column(name = "populationSize")
    private int populationSize;

    @Column(name = "desiredError")
    private Double desiredError;

    @Column(name = "fallsThreshold")
    private int fallsThreshold;

    @Column(name = "applicableWhenBestFitnessFalls")
    private boolean applicableWhenBestFitnessFalls;

    @Column(name = "applicableWhenMeanFitnessFalls")
    private boolean applicableWhenMeanFitnessFalls;

    @Column(name = "fitnessPercentage")
    private Double fitnessPercentage;

    @Column(name = "maxConsecutiveApplications")
    private int maxConsecutiveApplications;

    @ElementCollection(fetch = FetchType.EAGER)
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
    private List<String> gates;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "block_circuit_id")
    private BlockCircuit blockCircuit;

    public Blocks() {
    }

    public int getMinNumberColumns() {
        return minNumberColumns;
    }

    public void setMinNumberColumns(int minNumberColumns) {
        this.minNumberColumns = minNumberColumns;
    }

    public int getMaxNumberColumns() {
        return maxNumberColumns;
    }

    public void setMaxNumberColumns(int maxNumberColumns) {
        this.maxNumberColumns = maxNumberColumns;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public Double getDesiredError() {
        return desiredError;
    }

    public void setDesiredError(Double desiredError) {
        this.desiredError = desiredError;
    }

    public int getFallsThreshold() {
        return fallsThreshold;
    }

    public void setFallsThreshold(int fallsThreshold) {
        this.fallsThreshold = fallsThreshold;
    }

    public boolean isApplicableWhenBestFitnessFalls() {
        return applicableWhenBestFitnessFalls;
    }

    public void setApplicableWhenBestFitnessFalls(boolean applicableWhenBestFitnessFalls) {
        this.applicableWhenBestFitnessFalls = applicableWhenBestFitnessFalls;
    }

    public boolean isApplicableWhenMeanFitnessFalls() {
        return applicableWhenMeanFitnessFalls;
    }

    public void setApplicableWhenMeanFitnessFalls(boolean applicableWhenMeanFitnessFalls) {
        this.applicableWhenMeanFitnessFalls = applicableWhenMeanFitnessFalls;
    }

    public Double getFitnessPercentage() {
        return fitnessPercentage;
    }

    public void setFitnessPercentage(Double fitnessPercentage) {
        this.fitnessPercentage = fitnessPercentage;
    }

    public int getMaxConsecutiveApplications() {
        return maxConsecutiveApplications;
    }

    public void setMaxConsecutiveApplications(int maxConsecutiveApplications) {
        this.maxConsecutiveApplications = maxConsecutiveApplications;
    }

    public List<String> getGates() {
        return gates;
    }

    public void setGates(List<String> gates) {
        this.gates = gates;
    }

    public BlockCircuit getBlockCircuit() {
        return blockCircuit;
    }

    public void setBlockCircuit(BlockCircuit blockCircuit) {
        this.blockCircuit = blockCircuit;
    }

}
