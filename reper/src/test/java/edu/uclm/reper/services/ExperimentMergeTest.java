package edu.uclm.reper.services;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import edu.uclm.reper.dto.MergePreviewDTO;
import edu.uclm.reper.model.Experiment;
import edu.uclm.reper.model.MutantCycle;
import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.User;
import edu.uclm.reper.repository.ExperimentRepository;
import edu.uclm.reper.repository.MutantCycleRepository;
import edu.uclm.reper.repository.MutantRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExperimentMergeTest {

    private ExperimentService service;
    private ExperimentRepository experimentRepository;
    private MutantCycleRepository cycleRepository;
    private MutantRepository mutantRepository;
    private BulkDeleteService bulkDeleteService;
    private BackgroundJobService jobs;
    private DeletionRegistry deletionRegistry;
    private ExperimentRunRegistry registry;

    @BeforeEach
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void setUp() {
        service = new ExperimentService();
        experimentRepository = mock(ExperimentRepository.class);
        cycleRepository = mock(MutantCycleRepository.class);
        mutantRepository = mock(MutantRepository.class);
        bulkDeleteService = mock(BulkDeleteService.class);
        jobs = mock(BackgroundJobService.class);
        deletionRegistry = new DeletionRegistry();
        registry = new ExperimentRunRegistry();
        TransactionTemplate tt = mock(TransactionTemplate.class);
        when(tt.execute(any())).thenAnswer(inv -> ((TransactionCallback) inv.getArgument(0)).doInTransaction(null));
        when(jobs.submit(anyString(), anyString(), any())).thenReturn("job-1");
        ReflectionTestUtils.setField(service, "experimentRepository", experimentRepository);
        ReflectionTestUtils.setField(service, "mutantCycleRepository", cycleRepository);
        ReflectionTestUtils.setField(service, "mutantRepository", mutantRepository);
        ReflectionTestUtils.setField(service, "runRegistry", registry);
        ReflectionTestUtils.setField(service, "transactionTemplate", tt);
        ReflectionTestUtils.setField(service, "bulkDeleteService", bulkDeleteService);
        ReflectionTestUtils.setField(service, "backgroundJobService", jobs);
        ReflectionTestUtils.setField(service, "deletionRegistry", deletionRegistry);
    }

    private Experiment exp(String id, String owner, String status, long createdAt, String error) {
        User u = new User();
        u.setId(owner);
        Experiment e = new Experiment("name-" + id, u);
        e.setId(id);
        e.setStatus(status);
        e.setCreatedAt(new Date(createdAt));
        e.setErrorMessage(error);
        e.setDurationSeconds(10L);
        when(experimentRepository.findByIdWithExecConfig(id)).thenReturn(Optional.of(e));
        when(experimentRepository.findById(id)).thenReturn(Optional.of(e));
        return e;
    }

    private void cycles(String expId, MutantCycle... cs) {
        List<MutantCycle> list = List.of(cs);
        when(cycleRepository.findByExperimentIdWithExecConfig(expId)).thenReturn(list);
    }

    private MutantCycle cycle(String projectId, int id, long total, long pending, boolean gen) {
        Project p = new Project();
        p.setId(projectId);
        MutantCycle mc = new MutantCycle(new Date());
        mc.setId(id);
        mc.setName("c-" + projectId);
        mc.setProject(p);
        mc.setGenerationCompleted(gen);
        when(mutantRepository.countTotalByCycle(projectId, id)).thenReturn(total);
        when(mutantRepository.countPendingByCycle(projectId, id)).thenReturn(pending);
        return mc;
    }

    @Test
    void oldestSurvivesAndIncompleteDuplicateIsDeleted() throws Exception {
        Experiment a = exp("A", "u", "FAILED", 1000, "fallo de A");
        Experiment b = exp("B", "u", "COMPLETED", 2000, null);
        // A: p1 completo, p2 incompleto (pendientes), p3 incompleto sin duplicado en B
        cycles("A", cycle("p1", 1, 5, 0, true), cycle("p2", 1, 5, 3, true), cycle("p3", 1, 5, 2, true));
        // B: p2 completo
        cycles("B", cycle("p2", 2, 5, 0, true));

        // Pasamos los ids al revés: sobrevive igualmente el más antiguo
        MergePreviewDTO prev = service.getMergePreview(List.of("B", "A"), "u");
        assertEquals("A", prev.getSurvivorId());
        assertEquals("B", prev.getDeletedId());
        assertEquals(1, prev.getCyclesToMove());
        assertEquals(1, prev.getDuplicateCyclesToDelete().size());
        assertEquals("p2", prev.getDuplicateCyclesToDelete().get(0).getProjectId());
        assertEquals("FAILED", prev.getResultingStatus()); // p3 sigue incompleto

        Map<String, Object> res = service.mergeExperiments(List.of("B", "A"), "u");
        assertEquals("A", res.get("id"));
        assertEquals("B", res.get("deletedId"));
        assertEquals("job-1", res.get("deletionJobId"));

        // Solo se desvincula y borra el ciclo incompleto duplicado, no los completos
        verify(cycleRepository).detachFromExperiment("p2", 1);
        verify(cycleRepository, never()).detachFromExperiment(eq("p1"), anyInt());
        verify(cycleRepository, never()).detachFromExperiment(eq("p3"), anyInt());
        verify(cycleRepository).moveCycles(b, a);
        verify(experimentRepository).delete(b);
        assertTrue(a.getErrorHistory().contains("fallo de A"));
        assertEquals("FAILED", a.getStatus());
        assertEquals(20L, a.getDurationSeconds());
        assertNotNull(a.getMergedFrom());

        ArgumentCaptor<Callable<Object>> task = ArgumentCaptor.forClass(Callable.class);
        verify(jobs).submit(eq("delete"), eq("u"), task.capture());
        assertTrue(deletionRegistry.isCycleDeleting("p2", 1));
        task.getValue().call();
        verify(bulkDeleteService).deleteCycle("p2", 1);
        assertFalse(deletionRegistry.isCycleDeleting("p2", 1));
    }

    @Test
    void completedWhenBCompletedAndNothingIncompleteRemains() throws Exception {
        Experiment a = exp("A", "u", "FAILED", 1000, "fallo de A");
        exp("B", "u", "COMPLETED", 2000, null);
        cycles("A", cycle("p1", 1, 5, 0, true), cycle("p2", 1, 5, 3, true));
        cycles("B", cycle("p2", 2, 5, 0, true));

        service.mergeExperiments(List.of("A", "B"), "u");
        assertEquals("COMPLETED", a.getStatus());
        assertNull(a.getErrorMessage());
        assertEquals(2, a.getCompletedCircuits());
        assertTrue(a.getErrorHistory().contains("fallo de A"));
    }

    @Test
    void failedWithErrorOfBWhenBFailed() throws Exception {
        Experiment a = exp("A", "u", "FAILED", 1000, "fallo de A");
        exp("B", "u", "FAILED", 2000, "fallo de B");
        cycles("A", cycle("p1", 1, 5, 0, true));
        cycles("B", cycle("p2", 1, 5, 0, true));

        service.mergeExperiments(List.of("A", "B"), "u");
        assertEquals("FAILED", a.getStatus());
        assertEquals("fallo de B", a.getErrorMessage());
        assertTrue(a.getErrorHistory().contains("fallo de A"));
        assertTrue(a.getErrorHistory().contains("fallo de B"));
    }

    @Test
    void rejectedWhenRunningOrOtherUser() {
        exp("A", "u", "RUNNING", 1000, null);
        exp("B", "u", "FAILED", 2000, "x");
        assertThrows(Exception.class, () -> service.mergeExperiments(List.of("A", "B"), "u"));

        exp("C", "otro", "FAILED", 3000, "x");
        exp("D", "u", "FAILED", 4000, "x");
        assertThrows(Exception.class, () -> service.mergeExperiments(List.of("C", "D"), "u"));
        verify(experimentRepository, never()).delete(any());
    }
}
