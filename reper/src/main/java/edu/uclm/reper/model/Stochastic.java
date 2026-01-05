package edu.uclm.reper.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "stochastic_cases")
@DiscriminatorValue("STOCHASTIC")
public class Stochastic extends TestCase {

    @Column(name = "probability_distribution", columnDefinition = "TEXT")
    @Convert(converter = ProbabilityDistributionConverter.class)
    private Map<String, Integer> probabilityDistribution;

    public Stochastic() {
        this.probabilityDistribution = new HashMap<>();
    }

    public Stochastic(Map<String, Integer> probabilityDistribution) {
        this.probabilityDistribution = probabilityDistribution;
    }

    // Getters and Setters
    public Map<String, Integer> getProbabilityDistribution() {
        return probabilityDistribution;
    }

    public void setProbabilityDistribution(Map<String, Integer> probabilityDistribution) {
        this.probabilityDistribution = probabilityDistribution;
    }

    // Compatibility methods for handling int[] keys
    @JsonIgnore
    public void setProbabilityDistributionFromIntArrayMap(Map<int[], Integer> source) {
        if (source == null) {
            this.probabilityDistribution = null;
            return;
        }
        this.probabilityDistribution = new HashMap<>();
        for (Map.Entry<int[], Integer> entry : source.entrySet()) {
            this.probabilityDistribution.put(Arrays.toString(entry.getKey()), entry.getValue());
        }
    }

    @JsonIgnore
    public Map<int[], Integer> getProbabilityDistributionAsIntArrayMap() {
        if (this.probabilityDistribution == null) {
            return null;
        }
        Map<int[], Integer> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : this.probabilityDistribution.entrySet()) {
            String key = entry.getKey();
            // Remove brackets []
            String content = key.substring(1, key.length() - 1);
            if (content.isEmpty()) {
                result.put(new int[0], entry.getValue());
            } else {
                String[] parts = content.split(",");
                int[] intArray = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    intArray[i] = Integer.parseInt(parts[i].trim());
                }
                result.put(intArray, entry.getValue());
            }
        }
        return result;
    }
}
