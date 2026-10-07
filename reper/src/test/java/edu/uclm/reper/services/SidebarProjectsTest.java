package edu.uclm.reper.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import edu.uclm.reper.dto.MutantCycleSummaryDTO;
import edu.uclm.reper.dto.ProjectSummaryDTO;
import edu.uclm.reper.repository.MutantCycleRepository;
import edu.uclm.reper.repository.MutantRepository;
import edu.uclm.reper.repository.ProjectRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SidebarProjectsTest {

    private ProjectRepository projectRepository;
    private MutantCycleRepository mutantCycleRepository;
    private MutantRepository mutantRepository;
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectRepository = mock(ProjectRepository.class);
        mutantCycleRepository = mock(MutantCycleRepository.class);
        mutantRepository = mock(MutantRepository.class);

        projectService = new ProjectService();
        ReflectionTestUtils.setField(projectService, "projectRepository", projectRepository);
        ReflectionTestUtils.setField(projectService, "mutantCycleRepository", mutantCycleRepository);
        ReflectionTestUtils.setField(projectService, "mutantRepository", mutantRepository);
    }

    @Test
    void testSidebarProjects_withProjectsCyclesAndMutants() throws Exception {
        String email = "test@example.com";

        List<Object[]> projectRows = new ArrayList<>();
        projectRows.add(new Object[]{"p1", "Project 1", 2, new int[]{0, 1}, new int[]{0}});
        projectRows.add(new Object[]{"p2", "Project 2", 3, new int[]{0, 1, 2}, null});
        when(projectRepository.findSidebarRows(email)).thenReturn(projectRows);

        List<Object[]> cycleRows = new ArrayList<>();
        cycleRows.add(new Object[]{"p1", 1, "Cycle 1-1"});
        cycleRows.add(new Object[]{"p1", 2, "Cycle 1-2"});
        cycleRows.add(new Object[]{"p2", 3, "Cycle 2-1"});
        when(mutantCycleRepository.findSidebarCycles(List.of("p1", "p2"))).thenReturn(cycleRows);

        List<Object[]> mutantRows = new ArrayList<>();
        mutantRows.add(new Object[]{"p1", 1, 0, "Op1", "or1", "det1"});
        mutantRows.add(new Object[]{"p1", 1, 1, "Op2", "or2", "det2"});
        mutantRows.add(new Object[]{"p1", 2, 0, "Op1", "or3", "det3"});
        mutantRows.add(new Object[]{"p2", 3, 0, "Op3", "or4", "det4"});
        mutantRows.add(new Object[]{"p2", 3, 1, "Op4", "or5", "det5"});
        when(mutantRepository.findSidebarMutants(List.of("p1", "p2"))).thenReturn(mutantRows);

        List<ProjectSummaryDTO> result = projectService.getSidebarProjectsByUser(email);

        assertNotNull(result);
        assertEquals(2, result.size());

        // Project 1
        ProjectSummaryDTO p1 = result.get(0);
        assertEquals("p1", p1.getId());
        assertEquals("Project 1", p1.getName());
        assertEquals(2, p1.getQubits());
        assertEquals("0,1", p1.getInputQubits());
        assertEquals("0", p1.getOutputQubits());
        assertEquals(2, p1.getMutantCycles().size());

        MutantCycleSummaryDTO p1c1 = p1.getMutantCycles().get(0);
        assertEquals(1, p1c1.getId());
        assertEquals(2, p1c1.getMutants().size());
        assertEquals(0, p1c1.getMutants().get(0).getMutantIndex());
        assertEquals("Op1", p1c1.getMutants().get(0).getOperator().getName());
        assertEquals(1, p1c1.getMutants().get(1).getMutantIndex());

        MutantCycleSummaryDTO p1c2 = p1.getMutantCycles().get(1);
        assertEquals(2, p1c2.getId());
        assertEquals(1, p1c2.getMutants().size());
        assertEquals(0, p1c2.getMutants().get(0).getMutantIndex());

        // Project 2
        ProjectSummaryDTO p2 = result.get(1);
        assertEquals("p2", p2.getId());
        assertEquals("Project 2", p2.getName());
        assertEquals(3, p2.getQubits());
        assertEquals("0,1,2", p2.getInputQubits());
        assertEquals(1, p2.getMutantCycles().size());

        MutantCycleSummaryDTO p2c1 = p2.getMutantCycles().get(0);
        assertEquals(3, p2c1.getId());
        assertEquals(2, p2c1.getMutants().size());
    }

    @Test
    void testSidebarProjects_userWithoutProjects_returnsEmptyWithoutCallingCyclesOrMutants() throws Exception {
        String email = "empty@example.com";
        when(projectRepository.findSidebarRows(email)).thenReturn(Collections.emptyList());

        List<ProjectSummaryDTO> result = projectService.getSidebarProjectsByUser(email);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(mutantCycleRepository, never()).findSidebarCycles(any());
        verify(mutantRepository, never()).findSidebarMutants(any());
    }
}
