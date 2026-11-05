package edu.uclm.reper.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import edu.uclm.reper.dto.ExecParams;
import edu.uclm.reper.dto.ResumeExperimentDTO;
import edu.uclm.reper.dto.ResumePreviewDTO;
import edu.uclm.reper.model.ExecConfig;
import edu.uclm.reper.model.Experiment;
import edu.uclm.reper.model.MutantCycle;
import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.User;
import edu.uclm.reper.repository.ExecConfigRepository;
import edu.uclm.reper.repository.ExperimentRepository;
import edu.uclm.reper.repository.MutantCycleRepository;
import edu.uclm.reper.repository.MutantRepository;
import edu.uclm.reper.repository.ProjectRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExperimentResumeTest {

    private ExperimentService service;
    private ExperimentRepository experimentRepository;
    private MutantCycleRepository cycleRepository;
    private MutantRepository mutantRepository;
    private ProjectRepository projectRepository;
    private ExecConfigRepository execConfigRepository;
    private ExperimentRunRegistry registry;
    private ExperimentNotificationService notification;

    private static final String DEFINITION = "{\"name\":\"exp\",\"circuitConfigs\":["
            + "{\"projectId\":\"p1\",\"inputQubits\":\"0,1\",\"outputQubits\":\"0,1\"},"
            + "{\"projectId\":\"p2\",\"inputQubits\":\"0\",\"outputQubits\":\"0\"}]}";

    @BeforeEach
    void setUp() {
        service = spy(new ExperimentService());
        experimentRepository = mock(ExperimentRepository.class);
        cycleRepository = mock(MutantCycleRepository.class);
        mutantRepository = mock(MutantRepository.class);
        projectRepository = mock(ProjectRepository.class);
        execConfigRepository = mock(ExecConfigRepository.class);
        notification = mock(ExperimentNotificationService.class);
        registry = new ExperimentRunRegistry();
        ReflectionTestUtils.setField(service, "experimentRepository", experimentRepository);
        ReflectionTestUtils.setField(service, "mutantCycleRepository", cycleRepository);
        ReflectionTestUtils.setField(service, "mutantRepository", mutantRepository);
        ReflectionTestUtils.setField(service, "projectRepository", projectRepository);
        ReflectionTestUtils.setField(service, "execConfigRepository", execConfigRepository);
        ReflectionTestUtils.setField(service, "notificationService", notification);
        ReflectionTestUtils.setField(service, "runRegistry", registry);
        doNothing().when(service).startAsync(any(), anyString());
    }

    private Experiment experiment(String definition, String status) {
        User u = new User();
        u.setId("u@x.com");
        Experiment e = new Experiment("exp", u);
        e.setId("e1");
        e.setStatus(status);
        e.setErrorMessage("fallo previo");
        e.setDefinitionJson(definition);
        e.setTotalCircuits(2);
        e.setCompletedCircuits(1);
        when(experimentRepository.findByIdWithExecConfig("e1")).thenReturn(Optional.of(e));
        when(experimentRepository.findById("e1")).thenReturn(Optional.of(e));
        when(experimentRepository.findByIdWithDetails("e1")).thenReturn(Optional.of(e));
        return e;
    }

    private MutantCycle cycle(String projectId, int id, Integer circuitIndex, Boolean genCompleted, String in,
            String out, long total, long pending) {
        Project p = new Project();
        p.setId(projectId);
        MutantCycle mc = new MutantCycle(new Date());
        mc.setId(id);
        mc.setName("c-" + projectId);
        mc.setProject(p);
        mc.setCircuitIndex(circuitIndex);
        mc.setGenerationCompleted(genCompleted);
        ExecConfig ec = new ExecConfig(new Date(), "AerSimulator", "AllAgainstAll");
        ec.setInputQubits(in);
        ec.setOutputQubits(out);
        mc.setExecConfig(ec);
        when(mutantRepository.countTotalByCycle(projectId, id)).thenReturn(total);
        when(mutantRepository.countPendingByCycle(projectId, id)).thenReturn(pending);
        return mc;
    }

    private ResumeExperimentDTO request() {
        ResumeExperimentDTO r = new ResumeExperimentDTO();
        r.setEmail("u@x.com");
        r.setExperimentId("e1");
        return r;
    }

    @Test
    void a_pendingCycleThenRemainingCircuitAreProcessed() throws Exception {
        Experiment e = experiment(DEFINITION, "FAILED");
        MutantCycle c1 = cycle("p1", 1, 0, true, "0,1", "0,1", 10, 4);
        when(cycleRepository.findByExperimentIdWithExecConfig("e1")).thenReturn(List.of(c1));

        ResumePreviewDTO prev = service.getResumePreview("e1", "u@x.com");
        assertTrue(prev.isResumable());
        assertEquals(1, prev.getPendingCycles().size());
        assertEquals(4, prev.getPendingCycles().get(0).getPendingMutants());
        assertEquals(1, prev.getRemainingCircuits());
        assertTrue(prev.isHasDefinition());

        Map<String, Object> res = service.resumeExperiment(request(), "u@x.com");
        assertEquals("e1", res.get("id"));
        assertEquals("RUNNING", e.getStatus());
        assertNull(e.getErrorMessage());
        assertEquals(1, e.getResumeCount());
        assertEquals(0, e.getCompletedCircuits());
        assertTrue(e.getErrorHistory().contains("fallo previo"));
        assertTrue(registry.isRunning("e1"));

        // Ejecutamos la parte asíncrona directamente
        Project p1 = new Project();
        p1.setId("p1");
        when(projectRepository.findByIdWithQProgram("p1")).thenReturn(Optional.of(p1));
        doNothing().when(service).executeCycleInQiskitExec(anyString(), any(), any(), any(), anyBoolean());
        doNothing().when(service).processSingleCircuit(anyString(), any(), any(), anyInt());

        ExperimentService.CycleState st = service.computeCycleStates(e).get(0);
        List<Integer> remaining = service.remainingCircuitIndices(e, List.of(st));
        service.resumeExperimentAsync("e1", List.of(st), List.of(ExecParams.fromExecConfig(c1.getExecConfig(), "0,1", "0,1")),
                new com.fasterxml.jackson.databind.ObjectMapper().readValue(DEFINITION,
                        edu.uclm.reper.dto.CreateExperimentDTO.class),
                remaining, "u@x.com");

        verify(service).executeCycleInQiskitExec(eq("e1"), eq(p1), eq(c1), any(), eq(true));
        ArgumentCaptor<Integer> idx = ArgumentCaptor.forClass(Integer.class);
        verify(service).processSingleCircuit(eq("e1"), any(), any(), idx.capture());
        assertEquals(1, idx.getValue());
        assertEquals("COMPLETED", e.getStatus());
        assertFalse(registry.isRunning("e1"));
    }

    @Test
    void b_cycleWithIncompleteGenerationIsSkippedAndNoted() throws Exception {
        Experiment e = experiment(DEFINITION, "FAILED");
        MutantCycle c1 = cycle("p1", 1, 0, false, "0,1", "0,1", 3, 3);
        when(cycleRepository.findByExperimentIdWithExecConfig("e1")).thenReturn(List.of(c1));

        ResumePreviewDTO prev = service.getResumePreview("e1", "u@x.com");
        assertEquals(0, prev.getPendingCycles().size());
        assertEquals(1, prev.getSkippedCycles().size());

        service.resumeExperiment(request(), "u@x.com");
        assertTrue(e.getErrorHistory().contains("Ciclo c-p1 saltado: generación incompleta"));
        assertEquals(1, e.getCompletedCircuits());
        verify(service, never()).executeCycleInQiskitExec(any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void c_oldExperimentWithoutQubitsRequestsThem() throws Exception {
        experiment(null, "FAILED");
        MutantCycle c1 = cycle("p1", 1, null, null, null, null, 10, 4);
        when(cycleRepository.findByExperimentIdWithExecConfig("e1")).thenReturn(List.of(c1));

        ResumePreviewDTO prev = service.getResumePreview("e1", "u@x.com");
        assertFalse(prev.isHasDefinition());
        assertTrue(prev.getPendingCycles().get(0).isNeedsQubits());

        ExperimentService.MissingQubitsException ex = assertThrows(ExperimentService.MissingQubitsException.class,
                () -> service.resumeExperiment(request(), "u@x.com"));
        assertEquals(1, ex.getMissing().size());
        assertFalse(registry.isRunning("e1"));

        // Con los qubits informados se puede reanudar y se guardan en el ExecConfig
        ResumeExperimentDTO r = request();
        ResumeExperimentDTO.CycleQubits q = new ResumeExperimentDTO.CycleQubits();
        q.setProjectId("p1");
        q.setCycleId(1);
        q.setInputQubits("0,1");
        q.setOutputQubits("2");
        r.setCycles(new ArrayList<>(List.of(q)));
        service.resumeExperiment(r, "u@x.com");
        assertEquals("0,1", c1.getExecConfig().getInputQubits());
        assertEquals("2", c1.getExecConfig().getOutputQubits());
        verify(execConfigRepository).save(c1.getExecConfig());
    }

    @Test
    void d_experimentAlreadyRunningIsRejected() throws Exception {
        experiment(DEFINITION, "RUNNING");
        when(cycleRepository.findByExperimentIdWithExecConfig("e1")).thenReturn(List.of());
        registry.register("e1");

        assertThrows(ExperimentService.ExperimentConflictException.class,
                () -> service.resumeExperiment(request(), "u@x.com"));
        assertFalse(service.getResumePreview("e1", "u@x.com").isResumable());
    }

    @Test
    void e_suggestInputQubits() {
        assertEquals("", ExperimentService.suggestInputQubits(List.of(), 5));
        assertEquals("", ExperimentService.suggestInputQubits(List.of(0), 5));
        assertEquals("0,1", ExperimentService.suggestInputQubits(List.of(0, 1, 2, 3), 5));
        assertEquals("0,1", ExperimentService.suggestInputQubits(List.of(0, 1, 2, 3, 4, 5, 6, 7), 2));
    }

    @Test
    void f_previewOfCycleWithoutQubitsCarriesSelectorData() throws Exception {
        experiment(null, "FAILED");
        MutantCycle c1 = cycle("p1", 1, null, null, null, null, 10, 4);
        when(cycleRepository.findByExperimentIdWithExecConfig("e1")).thenReturn(List.of(c1));
        Project p = new Project();
        p.setId("p1");
        edu.uclm.reper.model.QProgram qp = new edu.uclm.reper.model.QProgram();
        qp.setQubits(3);
        p.setQProgram(qp);
        when(projectRepository.findByIdWithQProgram("p1")).thenReturn(Optional.of(p));
        edu.uclm.reper.repository.MutantResultRepository resRepo = mock(
                edu.uclm.reper.repository.MutantResultRepository.class);
        when(resRepo.findDistinctInputIdsByCycle("p1", 1)).thenReturn(List.of(0, 1, 2, 3));
        ReflectionTestUtils.setField(service, "mutantResultRepository", resRepo);

        ResumePreviewDTO.PendingCycle pc = service.getResumePreview("e1", "u@x.com").getPendingCycles().get(0);
        assertEquals(3, pc.getQubits());
        assertEquals("0,1,2", pc.getSuggestedOutputQubits());
        assertEquals("0,1", pc.getSuggestedInputQubits());
        assertTrue(pc.isSuggestedFromResults());
    }
}
