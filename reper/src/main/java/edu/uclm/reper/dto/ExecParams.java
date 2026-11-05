package edu.uclm.reper.dto;

import edu.uclm.reper.model.ExecConfig;

public record ExecParams(
        String machine,
        String execAlgorithm,
        Double toleratedError,
        Double zombieError,
        Integer shots,
        String inputQubits,
        String outputQubits
) {
    public static ExecParams fromDto(CreateExperimentDTO dto, String inputQubits, String outputQubits) {
        return new ExecParams(
                dto != null ? dto.getMachine() : "AerSimulator",
                dto != null ? dto.getExecAlgorithm() : "AllAgainstAll",
                dto != null ? dto.getToleratedError() : 0.05,
                dto != null ? dto.getZombieError() : 0.05,
                dto != null ? dto.getShots() : 1024,
                inputQubits != null ? inputQubits.trim() : "",
                outputQubits != null ? outputQubits.trim() : ""
        );
    }

    public static ExecParams fromExecConfig(ExecConfig ec, String inputQubits, String outputQubits) {
        return new ExecParams(
                ec != null ? ec.getMachine() : "AerSimulator",
                ec != null ? ec.getExecAlgorithm() : "AllAgainstAll",
                ec != null ? ec.getToleratedError() : 0.05,
                ec != null ? ec.getZombieError() : 0.05,
                ec != null && ec.getShots() != null ? ec.getShots() : 1024,
                inputQubits != null ? inputQubits.trim() : (ec != null && ec.getInputQubits() != null ? ec.getInputQubits().trim() : ""),
                outputQubits != null ? outputQubits.trim() : (ec != null && ec.getOutputQubits() != null ? ec.getOutputQubits().trim() : "")
        );
    }
}
