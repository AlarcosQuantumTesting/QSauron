package edu.uclm.reper.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uclm.reper.dto.QuMuCycleMatrixDTO;
import edu.uclm.reper.dto.QuMuProjectDTO;
import edu.uclm.reper.model.MutantCycle;
import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.QCircuit;
import edu.uclm.reper.model.QProgram;
import edu.uclm.reper.model.Result;
import edu.uclm.reper.repository.MutantCycleRepository;
import edu.uclm.reper.repository.MutantRepository;
import edu.uclm.reper.repository.MutantResultRepository;
import edu.uclm.reper.repository.ProjectRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuMuLazyLoadServiceTest {

    private ProjectRepository projectRepository;
    private MutantCycleRepository mutantCycleRepository;
    private MutantRepository mutantRepository;
    private MutantResultRepository mutantResultRepository;
    private QuMuLazyLoadService service;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        projectRepository = mock(ProjectRepository.class);
        mutantCycleRepository = mock(MutantCycleRepository.class);
        mutantRepository = mock(MutantRepository.class);
        mutantResultRepository = mock(MutantResultRepository.class);

        service = new QuMuLazyLoadService(projectRepository, mutantCycleRepository, mutantRepository, mutantResultRepository);
    }

    @Test
    void test1_getProject_loadsSummaryAndNeverCallsFindMatrixRows() throws Exception {
        when(projectRepository.isUserInProject("p1", "test@example.com")).thenReturn(true);

        Project project = new Project("Test Project");
        project.setId("p1");

        JsonNode quirkCodeNode = mapper.readTree("{\"cols\":[[\"H\"]]}");
        QCircuit circuit = new QCircuit(1, quirkCodeNode);
        circuit.setId("circ1");

        QProgram program = new QProgram();
        program.setId("prog1");
        program.setQubits(1);
        program.setQuirkCircuit(circuit);
        project.setQProgram(program);

        when(projectRepository.findByIdWithQProgram("p1")).thenReturn(Optional.of(project));

        MutantCycle c1 = new MutantCycle();
        c1.setId(1);
        c1.setName("Cycle 1");
        MutantCycle c2 = new MutantCycle();
        c2.setId(2);
        c2.setName("Cycle 2");
        when(mutantCycleRepository.findWithExecConfigByProjectId("p1")).thenReturn(List.of(c1, c2));

        List<Object[]> counts = new ArrayList<>();
        counts.add(new Object[]{1, 5L});
        counts.add(new Object[]{2, 0L});
        when(mutantRepository.countByCycle("p1")).thenReturn(counts);

        QuMuProjectDTO dto = service.getProject("test@example.com", "p1");

        assertNotNull(dto);
        assertEquals(2, dto.getMutantCycles().size());
        assertEquals(5L, dto.getMutantCycles().get(0).getMutantsCount());
        assertEquals(0L, dto.getMutantCycles().get(1).getMutantsCount());
        assertNotNull(dto.getQProgram());
        assertNotNull(dto.getQProgram().getQCircuit());
        assertEquals(quirkCodeNode, dto.getQProgram().getQCircuit().getQuirkCode());

        verify(mutantRepository, never()).findMatrixRows(anyString(), anyInt());
    }

    @Test
    void test2_getCycleMatrix_loadsRowsAndResultsWithCorrectGroupingAndCallCounts() {
        when(projectRepository.isUserInProject("p1", "test@example.com")).thenReturn(true);

        MutantCycle mc = new MutantCycle();
        mc.setId(1);
        mc.setName("Cycle 1");
        when(mutantCycleRepository.findWithExecConfig("p1", 1)).thenReturn(Optional.of(mc));

        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{10, 0, 1, 0, "GateReplacement", "oracle1", "H->X"});
        rows.add(new Object[]{11, 1, 1, 1, "GateDeletion", "oracle2", "del H"});
        rows.add(new Object[]{12, 2, 2, 0, null, "oracle3", "det3"});
        when(mutantRepository.findMatrixRows("p1", 1)).thenReturn(rows);

        List<Object[]> results = new ArrayList<>();
        results.add(new Object[]{10, 101, Result.KILLED});
        results.add(new Object[]{10, 102, Result.ALIVE});
        results.add(new Object[]{11, 103, Result.ZOMBIE});
        results.add(new Object[]{12, 104, Result.KILLED});
        when(mutantResultRepository.findMatrixResults("p1", 1)).thenReturn(results);

        QuMuCycleMatrixDTO dto = service.getCycleMatrix("test@example.com", "p1", 1);

        assertNotNull(dto);
        assertEquals(3, dto.getMutants().size());

        // Mutant 10
        assertEquals(10, dto.getMutants().get(0).getId());
        assertEquals(2, dto.getMutants().get(0).getMutantResults().size());
        assertEquals("GateReplacement", dto.getMutants().get(0).getOperator().getName());

        // Mutant 11
        assertEquals(11, dto.getMutants().get(1).getId());
        assertEquals(1, dto.getMutants().get(1).getMutantResults().size());
        assertEquals("GateDeletion", dto.getMutants().get(1).getOperator().getName());

        // Mutant 12
        assertEquals(12, dto.getMutants().get(2).getId());
        assertEquals(1, dto.getMutants().get(2).getMutantResults().size());
        assertNull(dto.getMutants().get(2).getOperator());

        verify(mutantCycleRepository, times(1)).findWithExecConfig("p1", 1);
        verify(mutantRepository, times(1)).findMatrixRows("p1", 1);
        verify(mutantResultRepository, times(1)).findMatrixResults("p1", 1);
    }

    @Test
    void test3_mutantWithoutResults_returnsEmptyListNotNull() {
        when(projectRepository.isUserInProject("p1", "test@example.com")).thenReturn(true);

        MutantCycle mc = new MutantCycle();
        mc.setId(1);
        when(mutantCycleRepository.findWithExecConfig("p1", 1)).thenReturn(Optional.of(mc));

        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{10, 0, 1, 0, "Op", "or", "det"});
        when(mutantRepository.findMatrixRows("p1", 1)).thenReturn(rows);
        when(mutantResultRepository.findMatrixResults("p1", 1)).thenReturn(Collections.emptyList());

        QuMuCycleMatrixDTO dto = service.getCycleMatrix("test@example.com", "p1", 1);

        assertNotNull(dto);
        assertNotNull(dto.getMutants().get(0).getMutantResults());
        assertTrue(dto.getMutants().get(0).getMutantResults().isEmpty());
    }

    @Test
    void test4_checkAccessFalse_throws403ForAllMethods() {
        when(projectRepository.isUserInProject("p1", "test@example.com")).thenReturn(false);

        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class,
                () -> service.getProject("test@example.com", "p1"));
        assertEquals(HttpStatus.FORBIDDEN, ex1.getStatusCode());

        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class,
                () -> service.getCycleMatrix("test@example.com", "p1", 1));
        assertEquals(HttpStatus.FORBIDDEN, ex2.getStatusCode());

        ResponseStatusException ex3 = assertThrows(ResponseStatusException.class,
                () -> service.getMutant("test@example.com", "p1", 1, 0));
        assertEquals(HttpStatus.FORBIDDEN, ex3.getStatusCode());

        ResponseStatusException ex4 = assertThrows(ResponseStatusException.class,
                () -> service.getMutantCircuits("test@example.com", "p1", 1, 0, 10));
        assertEquals(HttpStatus.FORBIDDEN, ex4.getStatusCode());
    }

    @Test
    void test5_getMutantCircuits_limitClampedTo200() {
        when(projectRepository.isUserInProject("p1", "test@example.com")).thenReturn(true);
        when(mutantRepository.findCircuitsPage(eq("p1"), eq(1), eq(0), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        service.getMutantCircuits("test@example.com", "p1", 1, 0, 1000);

        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        verify(mutantRepository).findCircuitsPage(eq("p1"), eq(1), eq(0), captor.capture());
        assertEquals(PageRequest.of(0, 200), captor.getValue());
    }

    @Test
    void test6_getMutantNotFound_throws404() {
        when(projectRepository.isUserInProject("p1", "test@example.com")).thenReturn(true);
        when(mutantRepository.findWithCircuit("p1", 1, 99)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getMutant("test@example.com", "p1", 1, 99));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
