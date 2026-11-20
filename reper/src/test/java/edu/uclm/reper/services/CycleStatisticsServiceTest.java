package edu.uclm.reper.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.uclm.reper.http.MutantAnalyticsProxy;
import edu.uclm.reper.model.Result;
import edu.uclm.reper.repository.MutantCycleRepository;
import edu.uclm.reper.repository.MutantRepository;
import edu.uclm.reper.repository.MutantResultRepository;
import edu.uclm.reper.repository.ProjectRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CycleStatisticsServiceTest {

    private MutantRepository mutantRepository;
    private MutantResultRepository mutantResultRepository;
    private MutantCycleRepository mutantCycleRepository;
    private ProjectRepository projectRepository;
    private MutantAnalyticsProxy mutantAnalyticsProxy;
    private CycleStatisticsService service;

    @BeforeEach
    void setUp() {
        mutantRepository = mock(MutantRepository.class);
        mutantResultRepository = mock(MutantResultRepository.class);
        mutantCycleRepository = mock(MutantCycleRepository.class);
        projectRepository = mock(ProjectRepository.class);
        mutantAnalyticsProxy = mock(MutantAnalyticsProxy.class);

        service = new CycleStatisticsService(
                mutantRepository,
                mutantResultRepository,
                mutantCycleRepository,
                projectRepository,
                mutantAnalyticsProxy
        );
    }

    @Test
    void testWriteKillingMatrixCsvMatchesExpectedFormat() throws Exception {
        String projectId = "proj-1";
        int cycleId = 1;

        // Inputs 0..3 with gaps: 0, 1, 3 (missing 2)
        when(mutantResultRepository.findDistinctInputIdsByCycle(projectId, cycleId))
                .thenReturn(List.of(0, 1, 3));

        // 3 mutants: mutant 1 has operator, mutant 2 has null operator, mutant 3 has operator
        List<Object[]> mutantRows = new ArrayList<>();
        mutantRows.add(new Object[]{101, 0, 1, 0, "H_X", "oracle1", ""});
        mutantRows.add(new Object[]{102, 1, 2, 0, null, null, null});
        mutantRows.add(new Object[]{103, 2, 3, 0, "RZ", null, null});
        when(mutantRepository.findMatrixRows(projectId, cycleId)).thenReturn(mutantRows);

        // Results:
        // mutant 101: input 0 -> KILLED, input 1 -> ZOMBIE, input 3 missing -> defaults to Alive
        // mutant 102: input 3 -> KILLED, input 0,1 missing -> defaults to Alive
        // mutant 103: no results -> all Alive
        List<Object[]> results = new ArrayList<>();
        results.add(new Object[]{101, 0, Result.KILLED});
        results.add(new Object[]{101, 1, Result.ZOMBIE});
        results.add(new Object[]{102, 3, Result.KILLED});
        when(mutantResultRepository.findResultsByMutantIds(anyCollection())).thenReturn(results);

        // Explicit inputBits = 2 (so binary strings are 00, 01, 11)
        Path csvPath = service.writeKillingMatrixCsv(projectId, cycleId, 2);
        assertNotNull(csvPath);
        assertTrue(Files.exists(csvPath));

        String content = Files.readString(csvPath).replace("\r\n", "\n");
        Files.deleteIfExists(csvPath);

        String expected = "Mutant ID,Operator,00,01,11\n" +
                "0,H_X,Killed,Zombie,Alive\n" +
                "1,N/A,Alive,Alive,Killed\n" +
                "2,RZ,Alive,Alive,Alive\n";

        assertEquals(expected, content);
    }
}

