package edu.uclm.reper.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CreateExperimentDTO {
    private String name;
    private List<CircuitConfigDTO> circuitConfigs;
    private List<String> operatorNames;
    private String machine = "AerSimulator";
    private String execAlgorithm = "AllAgainstAll";
    private Double toleratedError = 0.05;
    private Double zombieError = 0.05;
    private Integer shots = 1024;
    private String email;
    private NoiseConfigDTO noiseConfig;
    private List<NoiseConfigDTO> noiseConfigs;

    public CreateExperimentDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<CircuitConfigDTO> getCircuitConfigs() { return circuitConfigs; }
    public void setCircuitConfigs(List<CircuitConfigDTO> circuitConfigs) { this.circuitConfigs = circuitConfigs; }

    public List<String> getOperatorNames() { return operatorNames; }
    public void setOperatorNames(List<String> operatorNames) { this.operatorNames = operatorNames; }

    public String getMachine() { return machine; }
    public void setMachine(String machine) { this.machine = machine; }

    public String getExecAlgorithm() { return execAlgorithm; }
    public void setExecAlgorithm(String execAlgorithm) { this.execAlgorithm = execAlgorithm; }

    public Double getToleratedError() { return toleratedError; }
    public void setToleratedError(Double toleratedError) { this.toleratedError = toleratedError; }

    public Double getZombieError() { return zombieError; }
    public void setZombieError(Double zombieError) { this.zombieError = zombieError; }

    public Integer getShots() { return shots; }
    public void setShots(Integer shots) { this.shots = shots; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public NoiseConfigDTO getNoiseConfig() { return noiseConfig; }
    public void setNoiseConfig(NoiseConfigDTO noiseConfig) { this.noiseConfig = noiseConfig; }

    public List<NoiseConfigDTO> getNoiseConfigs() { return noiseConfigs; }
    public void setNoiseConfigs(List<NoiseConfigDTO> noiseConfigs) { this.noiseConfigs = noiseConfigs; }

    @JsonIgnore
    public List<NoiseConfigDTO> effectiveNoiseConfigs() {
        if (this.noiseConfigs != null && !this.noiseConfigs.isEmpty()) {
            return this.noiseConfigs;
        }
        if (this.noiseConfig != null) {
            return List.of(this.noiseConfig);
        }
        return null;
    }
}
