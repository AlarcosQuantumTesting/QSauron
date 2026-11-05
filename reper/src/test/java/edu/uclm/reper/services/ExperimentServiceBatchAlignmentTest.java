package edu.uclm.reper.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uclm.reper.model.Mutant;
import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.QCircuit;
import edu.uclm.reper.model.QProgram;
import edu.uclm.reper.model.Result;
import edu.uclm.reper.services.ExperimentService.FormattedBatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExperimentServiceBatchAlignmentTest {

    private ExperimentService experimentService;
    private ProjectService projectService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        experimentService = new ExperimentService();
        projectService = mock(ProjectService.class);
        ReflectionTestUtils.setField(experimentService, "projectService", projectService);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testBatchWithMissingCircuitKeepsResultsAlignedToCorrectMutants() throws Exception {
        Project project = new Project();
        project.setId("proj-align");
        QProgram qp = new QProgram();
        qp.setQubits(2);
        project.setQProgram(qp);

        // Mutant 0: has NO circuit (quirkCircuit is null)
        Mutant m0 = new Mutant();
        m0.setId(10);
        m0.setIndex(0);

        // Mutant 1: has valid circuit
        Mutant m1 = new Mutant();
        m1.setId(11);
        m1.setIndex(1);
        QProgram prog1 = new QProgram();
        QCircuit circ1 = new QCircuit();
        circ1.setCode(objectMapper.readTree("{\"cols\":[[\"H\"]]}"));
        prog1.setQuirkCircuit(circ1);
        m1.setQuirkCircuit(prog1);

        // Mutant 2: has valid circuit
        Mutant m2 = new Mutant();
        m2.setId(12);
        m2.setIndex(2);
        QProgram prog2 = new QProgram();
        QCircuit circ2 = new QCircuit();
        circ2.setCode(objectMapper.readTree("{\"cols\":[[\"X\"]]}"));
        prog2.setQuirkCircuit(circ2);
        m2.setQuirkCircuit(prog2);

        List<Mutant> batch = List.of(m0, m1, m2);

        // formatMutantBatch must filter out m0 and keep m1 and m2
        FormattedBatch formatted = experimentService.formatMutantBatch(project, batch);
        List<Mutant> sentMutants = formatted.sentMutants();
        assertEquals(2, sentMutants.size());
        assertEquals(1, sentMutants.get(0).getIndex());
        assertEquals(2, sentMutants.get(1).getIndex());
        assertEquals(2, formatted.formattedList().size());

        // Strategy results for the 2 sent mutants
        // Mutant 1 gets KILLED, Mutant 2 gets ZOMBIE
        List<List<Map<String, Object>>> stratResults = List.of(
                List.of(Map.of("killed", true, "zombie", false)),
                List.of(Map.of("killed", false, "zombie", true))
        );

        // Save batch results with the filtered sentMutants
        experimentService.processAndSaveBatchResults(project.getId(), 1, sentMutants, stratResults);

        // Verify m1 received KILLED
        assertNotNull(m1.getMutantResults());
        assertEquals(1, m1.getMutantResults().size());
        assertEquals(Result.KILLED, m1.getMutantResults().get(0).getResult());

        // Verify m2 received ZOMBIE
        assertNotNull(m2.getMutantResults());
        assertEquals(1, m2.getMutantResults().size());
        assertEquals(Result.ZOMBIE, m2.getMutantResults().get(0).getResult());

        // Verify m0 was untouched
        assertNull(m0.getMutantResults());

        verify(projectService).saveExperimentBatchResults(sentMutants);
    }
}
