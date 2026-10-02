package edu.uclm.reper.dto;

public class NoiseConfigDTO {
    private String axis = "X";
    private Double angle = 0.1;

    public NoiseConfigDTO() {}

    public NoiseConfigDTO(String axis, Double angle) {
        this.axis = axis;
        this.angle = angle;
    }

    public String getAxis() {
        return axis;
    }

    public void setAxis(String axis) {
        this.axis = axis;
    }

    public Double getAngle() {
        return angle;
    }

    public void setAngle(Double angle) {
        this.angle = angle;
    }
}
