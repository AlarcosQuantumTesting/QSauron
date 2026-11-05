package edu.uclm.reper.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import edu.uclm.reper.repository.GeneratorRepository;
import edu.uclm.reper.repository.ProjectRepository;
import jakarta.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BulkDeleteServiceTest {

    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;
    private EntityManager entityManager;
    private ProjectRepository projectRepository;
    private GeneratorRepository generatorRepository;
    private BulkDeleteService bulkDeleteService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        transactionTemplate = mock(TransactionTemplate.class);
        entityManager = mock(EntityManager.class);
        projectRepository = mock(ProjectRepository.class);
        generatorRepository = mock(GeneratorRepository.class);

        when(transactionTemplate.execute(any(TransactionCallback.class))).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });

        bulkDeleteService = new BulkDeleteService(
                jdbcTemplate,
                transactionTemplate,
                entityManager,
                projectRepository,
                generatorRepository,
                2000
        );
    }

    @Test
    void testDeleteCycleBatches2001MutantsIntoTwoBatchesAndDeletesInOrder() {
        String projectId = "proj-1";
        int cycleId = 1;

        // Simulate 2001 mutants
        List<Map<String, Object>> mutantsList = new ArrayList<>();
        for (int i = 1; i <= 2001; i++) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", i);
            m.put("quirk_circuit_id", "qp-" + i);
            mutantsList.add(m);
        }

        when(jdbcTemplate.queryForList(
                eq("SELECT id, quirk_circuit_id FROM mutants WHERE mutant_cycle_id = ? AND project_id = ? ORDER BY id"),
                eq(cycleId), eq(projectId)
        )).thenReturn(mutantsList);

        when(jdbcTemplate.queryForList(startsWith("SELECT circuit_id, generator_id FROM q_programs"), any(Object[].class)))
                .thenReturn(List.of());

        when(jdbcTemplate.queryForList(
                eq("SELECT id, killing_matrix_id FROM exec_configs WHERE mutant_cycle_id = ? AND project_id = ?"),
                eq(cycleId), eq(projectId)
        )).thenReturn(List.of(Map.of("id", "ec-1", "killing_matrix_id", "km-1")));

        bulkDeleteService.deleteCycle(projectId, cycleId);

        // Verify transactionTemplate was called 3 times: batch 1 (2000), batch 2 (1), and cycle cleanup
        verify(transactionTemplate, times(3)).execute(any());

        InOrder inOrder = inOrder(jdbcTemplate);

        // Batch 1: mutant_results -> mutants -> q_programs
        inOrder.verify(jdbcTemplate).update(startsWith("DELETE FROM mutant_results WHERE mutant_id IN"), any(Object[].class));
        inOrder.verify(jdbcTemplate).update(startsWith("DELETE FROM mutants WHERE id IN"), any(Object[].class));
        inOrder.verify(jdbcTemplate).update(startsWith("DELETE FROM qcode WHERE q_program_id IN"), any(Object[].class));
        inOrder.verify(jdbcTemplate).update(startsWith("DELETE FROM expressions WHERE q_program_id IN"), any(Object[].class));
        inOrder.verify(jdbcTemplate).update(startsWith("DELETE FROM q_programs WHERE id IN"), any(Object[].class));

        // Batch 2: mutant_results -> mutants -> q_programs
        inOrder.verify(jdbcTemplate).update(startsWith("DELETE FROM mutant_results WHERE mutant_id IN"), any(Object[].class));
        inOrder.verify(jdbcTemplate).update(startsWith("DELETE FROM mutants WHERE id IN"), any(Object[].class));
        inOrder.verify(jdbcTemplate).update(startsWith("DELETE FROM qcode WHERE q_program_id IN"), any(Object[].class));
        inOrder.verify(jdbcTemplate).update(startsWith("DELETE FROM expressions WHERE q_program_id IN"), any(Object[].class));
        inOrder.verify(jdbcTemplate).update(startsWith("DELETE FROM q_programs WHERE id IN"), any(Object[].class));

        // Post-mutants: killing matrices -> exec configs -> mutant cycle
        inOrder.verify(jdbcTemplate).update(eq("DELETE FROM mutant_results WHERE killing_matrix_id = ?"), eq("km-1"));
        inOrder.verify(jdbcTemplate).update(eq("UPDATE exec_configs SET killing_matrix_id = NULL WHERE id = ?"), eq("ec-1"));
        inOrder.verify(jdbcTemplate).update(eq("DELETE FROM killing_matrices WHERE id = ?"), eq("km-1"));
        inOrder.verify(jdbcTemplate).update(eq("UPDATE experiments SET exec_config_id = NULL WHERE exec_config_id = ?"), eq("ec-1"));
        inOrder.verify(jdbcTemplate).update(eq("DELETE FROM exec_configs WHERE id = ?"), eq("ec-1"));
        inOrder.verify(jdbcTemplate).update(eq("DELETE FROM mutant_cycles WHERE id = ? AND project_id = ?"), eq(cycleId), eq(projectId));
    }

    @Test
    void testDeleteProjectDeletesAllCyclesThenClearsAndDeletesProject() {
        String projectId = "proj-1";

        when(jdbcTemplate.queryForList(
                eq("SELECT id FROM mutant_cycles WHERE project_id = ?"),
                eq(Integer.class), eq(projectId)
        )).thenReturn(List.of(1, 2));

        when(jdbcTemplate.queryForList(
                eq("SELECT id, quirk_circuit_id FROM mutants WHERE mutant_cycle_id = ? AND project_id = ? ORDER BY id"),
                anyInt(), eq(projectId)
        )).thenReturn(List.of());

        when(jdbcTemplate.queryForList(
                eq("SELECT id, killing_matrix_id FROM exec_configs WHERE mutant_cycle_id = ? AND project_id = ?"),
                anyInt(), eq(projectId)
        )).thenReturn(List.of());

        bulkDeleteService.deleteProject(projectId);

        InOrder inOrder = inOrder(entityManager, projectRepository);
        inOrder.verify(entityManager).clear();
        inOrder.verify(projectRepository).deleteById(projectId);
    }
}
