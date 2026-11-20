package edu.uclm.reper.services;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uclm.reper.http.MutantAnalyticsProxy;
import edu.uclm.reper.model.MutantCycle;
import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.Result;
import edu.uclm.reper.repository.MutantCycleRepository;
import edu.uclm.reper.repository.MutantRepository;
import edu.uclm.reper.repository.MutantResultRepository;
import edu.uclm.reper.repository.ProjectRepository;

@Service
public class CycleStatisticsService {

    private static final Logger log = LoggerFactory.getLogger(CycleStatisticsService.class);

    private final MutantRepository mutantRepository;
    private final MutantResultRepository mutantResultRepository;
    private final MutantCycleRepository mutantCycleRepository;
    private final ProjectRepository projectRepository;
    private final MutantAnalyticsProxy mutantAnalyticsProxy;

    @Autowired
    public CycleStatisticsService(
            MutantRepository mutantRepository,
            MutantResultRepository mutantResultRepository,
            MutantCycleRepository mutantCycleRepository,
            ProjectRepository projectRepository,
            MutantAnalyticsProxy mutantAnalyticsProxy) {
        this.mutantRepository = mutantRepository;
        this.mutantResultRepository = mutantResultRepository;
        this.mutantCycleRepository = mutantCycleRepository;
        this.projectRepository = projectRepository;
        this.mutantAnalyticsProxy = mutantAnalyticsProxy;
    }

    public Path writeKillingMatrixCsv(String projectId, int cycleId, Integer inputBits) throws IOException {
        if (inputBits == null || inputBits <= 0) {
            MutantCycle cycle = mutantCycleRepository.findWithExecConfig(projectId, cycleId).orElse(null);
            if (cycle != null && cycle.getExecConfig() != null && cycle.getExecConfig().getInputQubits() != null
                    && !cycle.getExecConfig().getInputQubits().isBlank()) {
                inputBits = cycle.getExecConfig().getInputQubits().split(",").length;
            } else {
                Project project = projectRepository.findByIdWithQProgram(projectId).orElse(null);
                if (project != null && project.getQProgram() != null && project.getQProgram().getQubits() > 0) {
                    inputBits = project.getQProgram().getQubits();
                }
            }
        }

        List<Integer> inputIds = mutantResultRepository.findDistinctInputIdsByCycle(projectId, cycleId);
        if (inputIds == null) {
            inputIds = Collections.emptyList();
        }

        if (inputBits == null || inputBits <= 0) {
            if (!inputIds.isEmpty()) {
                int maxInput = inputIds.stream().mapToInt(Integer::intValue).max().orElse(0);
                inputBits = Math.max(1, Integer.toBinaryString(maxInput).length());
            } else {
                inputBits = 1;
            }
        }

        int padLength = inputBits;
        List<String> binaryHeaders = new ArrayList<>(inputIds.size());
        for (int inId : inputIds) {
            String bin = Integer.toBinaryString(inId);
            if (bin.length() < padLength) {
                bin = "0".repeat(padLength - bin.length()) + bin;
            }
            binaryHeaders.add(bin);
        }

        Path tempFile = Files.createTempFile("killing_matrix_", ".csv");
        try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
            StringBuilder header = new StringBuilder("Mutant ID,Operator");
            for (String binHeader : binaryHeaders) {
                header.append(",").append(binHeader);
            }
            writer.write(header.toString());
            writer.newLine();

            List<Object[]> mutantRows = mutantRepository.findMatrixRows(projectId, cycleId);
            if (mutantRows != null && !mutantRows.isEmpty()) {
                int batchSize = 500;
                for (int i = 0; i < mutantRows.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, mutantRows.size());
                    List<Object[]> batch = mutantRows.subList(i, end);

                    List<Integer> batchIds = new ArrayList<>(batch.size());
                    for (Object[] row : batch) {
                        batchIds.add(((Number) row[0]).intValue());
                    }

                    List<Object[]> results = mutantResultRepository.findResultsByMutantIds(batchIds);
                    Map<Integer, Map<Integer, Result>> resultMap = new HashMap<>();
                    if (results != null) {
                        for (Object[] res : results) {
                            int mId = ((Number) res[0]).intValue();
                            int inId = ((Number) res[1]).intValue();
                            Result r = (Result) res[2];
                            resultMap.computeIfAbsent(mId, k -> new HashMap<>()).put(inId, r);
                        }
                    }

                    for (Object[] row : batch) {
                        int mId = ((Number) row[0]).intValue();
                        Object idxObj = row[1];
                        String mutantIndex = idxObj != null ? String.valueOf(idxObj) : "N/A";
                        String opName = (row[4] != null && !((String) row[4]).isBlank()) ? (String) row[4] : "N/A";

                        StringBuilder line = new StringBuilder();
                        line.append(mutantIndex).append(",").append(opName);

                        Map<Integer, Result> mResults = resultMap.getOrDefault(mId, Collections.emptyMap());
                        for (int inId : inputIds) {
                            Result r = mResults.get(inId);
                            String cell;
                            if (r == Result.KILLED) {
                                cell = "Killed";
                            } else if (r == Result.ZOMBIE) {
                                cell = "Zombie";
                            } else {
                                cell = "Alive";
                            }
                            line.append(",").append(cell);
                        }
                        writer.write(line.toString());
                        writer.newLine();
                    }
                }
            }
        }

        return tempFile;
    }

    public Map<String, Object> calculateCycleStatistics(String projectId, int cycleId, Double threshold,
            Integer inputBits) {
        Path csv = null;
        try {
            csv = writeKillingMatrixCsv(projectId, cycleId, inputBits);
            return mutantAnalyticsProxy.calculateStatisticsFromFile(csv, threshold).block();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate killing matrix CSV: " + e.getMessage(), e);
        } finally {
            if (csv != null) {
                try {
                    Files.deleteIfExists(csv);
                } catch (IOException e) {
                    log.warn("Failed to delete temp CSV file: {}", csv, e);
                }
            }
        }
    }
}
