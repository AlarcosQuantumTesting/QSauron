package edu.uclm.reper.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import edu.uclm.reper.repository.GeneratorRepository;
import edu.uclm.reper.repository.ProjectRepository;
import jakarta.persistence.EntityManager;

@Service
public class BulkDeleteService {

    private static final Logger log = LoggerFactory.getLogger(BulkDeleteService.class);

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final EntityManager entityManager;
    private final ProjectRepository projectRepository;
    private final GeneratorRepository generatorRepository;
    private int batchSize;

    @Autowired
    public BulkDeleteService(
            JdbcTemplate jdbcTemplate,
            TransactionTemplate transactionTemplate,
            EntityManager entityManager,
            ProjectRepository projectRepository,
            GeneratorRepository generatorRepository,
            @Value("${app.delete.batch-mutants:2000}") int batchSize) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.entityManager = entityManager;
        this.projectRepository = projectRepository;
        this.generatorRepository = generatorRepository;
        this.batchSize = batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void deleteCycle(String projectId, int cycleId) {
        long start = System.currentTimeMillis();

        List<Map<String, Object>> mutants = jdbcTemplate.queryForList(
                "SELECT id, quirk_circuit_id FROM mutants WHERE mutant_cycle_id = ? AND project_id = ? ORDER BY id",
                cycleId, projectId);
        int totalMutants = mutants.size();

        if (totalMutants > 0) {
            for (int i = 0; i < totalMutants; i += batchSize) {
                int end = Math.min(i + batchSize, totalMutants);
                List<Map<String, Object>> batch = mutants.subList(i, end);

                transactionTemplate.execute(status -> {
                    List<Integer> mutantIds = new ArrayList<>(batch.size());
                    List<String> qProgramIds = new ArrayList<>();
                    for (Map<String, Object> m : batch) {
                        mutantIds.add(((Number) m.get("id")).intValue());
                        Object qpId = m.get("quirk_circuit_id");
                        if (qpId != null) {
                            qProgramIds.add((String) qpId);
                        }
                    }

                    String inMutants = inPlaceholders(mutantIds.size());
                    Object[] mutantIdArgs = mutantIds.toArray();

                    jdbcTemplate.update("DELETE FROM mutant_results WHERE mutant_id IN (" + inMutants + ")", mutantIdArgs);
                    jdbcTemplate.update("DELETE FROM mutants WHERE id IN (" + inMutants + ")", mutantIdArgs);

                    if (!qProgramIds.isEmpty()) {
                        String inProgs = inPlaceholders(qProgramIds.size());
                        Object[] progIdArgs = qProgramIds.toArray();

                        List<Map<String, Object>> progs = jdbcTemplate.queryForList(
                                "SELECT circuit_id, generator_id FROM q_programs WHERE id IN (" + inProgs + ")", progIdArgs);

                        List<String> circuitIds = new ArrayList<>();
                        List<Integer> genIds = new ArrayList<>();
                        for (Map<String, Object> p : progs) {
                            Object cId = p.get("circuit_id");
                            if (cId != null) {
                                circuitIds.add((String) cId);
                            }
                            Object gId = p.get("generator_id");
                            if (gId != null) {
                                genIds.add(((Number) gId).intValue());
                            }
                        }

                        jdbcTemplate.update("DELETE FROM qcode WHERE q_program_id IN (" + inProgs + ")", progIdArgs);
                        jdbcTemplate.update("DELETE FROM expressions WHERE q_program_id IN (" + inProgs + ")", progIdArgs);
                        jdbcTemplate.update("DELETE FROM q_programs WHERE id IN (" + inProgs + ")", progIdArgs);

                        if (!circuitIds.isEmpty()) {
                            String inCirc = inPlaceholders(circuitIds.size());
                            jdbcTemplate.update("DELETE FROM qcircuits WHERE id IN (" + inCirc + ")", circuitIds.toArray());
                        }

                        for (Integer genId : genIds) {
                            try {
                                generatorRepository.deleteById(genId.longValue());
                            } catch (Exception e) {
                                log.warn("Could not delete generator {}: {}", genId, e.getMessage());
                            }
                        }
                    }
                    return null;
                });
            }
        }

        transactionTemplate.execute(status -> {
            List<Map<String, Object>> configs = jdbcTemplate.queryForList(
                    "SELECT id, killing_matrix_id FROM exec_configs WHERE mutant_cycle_id = ? AND project_id = ?",
                    cycleId, projectId);

            for (Map<String, Object> ec : configs) {
                String ecId = (String) ec.get("id");
                String kmId = (String) ec.get("killing_matrix_id");
                if (kmId != null) {
                    jdbcTemplate.update("DELETE FROM mutant_results WHERE killing_matrix_id = ?", kmId);
                    jdbcTemplate.update("UPDATE exec_configs SET killing_matrix_id = NULL WHERE id = ?", ecId);
                    jdbcTemplate.update("DELETE FROM killing_matrices WHERE id = ?", kmId);
                }
                jdbcTemplate.update("UPDATE experiments SET exec_config_id = NULL WHERE exec_config_id = ?", ecId);
                jdbcTemplate.update("DELETE FROM exec_configs WHERE id = ?", ecId);
            }

            jdbcTemplate.update("DELETE FROM mutant_cycles WHERE id = ? AND project_id = ?", cycleId, projectId);
            return null;
        });

        log.info("[PERF] bulkDelete cycle={}:{} mutants={} ms={}", projectId, cycleId, totalMutants, System.currentTimeMillis() - start);
    }

    public void deleteProject(String projectId) {
        long start = System.currentTimeMillis();

        List<Integer> cycleIds = jdbcTemplate.queryForList(
                "SELECT id FROM mutant_cycles WHERE project_id = ?", Integer.class, projectId);

        for (Integer cycleId : cycleIds) {
            deleteCycle(projectId, cycleId);
        }

        transactionTemplate.execute(status -> {
            entityManager.clear();
            projectRepository.deleteById(projectId);
            return null;
        });

        log.info("[PERF] bulkDelete project={} ms={}", projectId, System.currentTimeMillis() - start);
    }

    private String inPlaceholders(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");
            sb.append("?");
        }
        return sb.toString();
    }
}
