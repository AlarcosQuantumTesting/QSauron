package edu.uclm.reper.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uclm.reper.dto.ConvertRequestDTO;
import edu.uclm.reper.dto.ConvertedCircuitDTO;
import edu.uclm.reper.dto.ImportItemDTO;
import edu.uclm.reper.dto.ImportResultDTO;
import edu.uclm.reper.dto.RepresentRequestDTO;
import edu.uclm.reper.dto.RepresentationDTO;
import edu.uclm.reper.http.QConvertProxy;
import edu.uclm.reper.http.QConvertProxy.QConvertResult;
import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.User;
import edu.uclm.reper.repository.ProjectRepository;
import edu.uclm.reper.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProjectImportServiceTest {

    private ProjectImportService service;
    private QConvertProxy proxy;
    private ProjectService projectService;
    private ProjectRepository projectRepository;
    private UserRepository userRepository;
    private ObjectMapper mapper;

    private final String email = "test@example.com";
    private final String bellQuirkOutput = "{\"cols\":[[\"H\"],[\"•\",\"X\"]]}";

    @BeforeEach
    void setUp() {
        ImportContentValidator validator = new ImportContentValidator(524288);
        proxy = mock(QConvertProxy.class);
        projectService = mock(ProjectService.class);
        projectRepository = mock(ProjectRepository.class);
        userRepository = mock(UserRepository.class);
        mapper = new ObjectMapper();

        service = new ProjectImportService(
                validator,
                proxy,
                projectService,
                projectRepository,
                userRepository,
                20,
                5242880,
                524288,
                16
        );

        when(userRepository.findById(email)).thenReturn(Optional.of(new User(email)));
        when(projectService.saveProject(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void testImportBellAndPastedQuirk() {
        ImportItemDTO item1 = new ImportItemDTO("bell.qasm", "qasm", null, "OPENQASM 2.0;\nqreg q[2];");
        ImportItemDTO item2 = new ImportItemDTO(null, "quirk", "pegado", bellQuirkOutput);

        when(proxy.convertBatch(anyList(), anyList(), eq("quirk"))).thenReturn(List.of(
                new QConvertResult(true, bellQuirkOutput, null),
                new QConvertResult(true, bellQuirkOutput, null)
        ));
        when(projectRepository.findProjectsByUserId(email)).thenReturn(Collections.emptyList());

        ImportResultDTO res = service.importProjects(email, List.of(item1, item2));

        assertEquals(2, res.getSuccessCount());
        assertEquals(0, res.getFailureCount());
        verify(projectService, times(2)).saveProject(any());
        verify(projectService, times(2)).associateProjectWithUser(any(), eq(email));

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectService, times(2)).saveProject(projectCaptor.capture());
        List<Project> saved = projectCaptor.getAllValues();
        assertEquals("bell", saved.get(0).getName());
        assertEquals("pegado", saved.get(1).getName());
        assertEquals(2, saved.get(0).getQProgram().getQubits());
        assertEquals("0,1", saved.get(0).getQProgram().getQuirkCircuit().getMutableRows());
    }

    @Test
    void testInvalidExtensionSkipsConversion() {
        ImportItemDTO itemBad = new ImportItemDTO("bell.json", "qasm", "bell", "OPENQASM 2.0;");
        ImportItemDTO itemGood = new ImportItemDTO("ghz.quil", "quil", "ghz", "H 0\nCNOT 0 1");

        when(proxy.convertBatch(anyList(), anyList(), eq("quirk"))).thenReturn(List.of(
                new QConvertResult(true, bellQuirkOutput, null)
        ));

        ImportResultDTO res = service.importProjects(email, List.of(itemBad, itemGood));

        assertEquals(1, res.getSuccessCount());
        assertEquals(1, res.getFailureCount());
        assertEquals(ImportException.INVALID_EXTENSION, res.getItems().get(0).getErrorCode());

        ArgumentCaptor<List<String>> codesCaptor = ArgumentCaptor.forClass(List.class);
        verify(proxy).convertBatch(codesCaptor.capture(), anyList(), eq("quirk"));
        assertEquals(1, codesCaptor.getValue().size());
    }



    @Test
    void testConversionFailedFromProxy() {
        ImportItemDTO item = new ImportItemDTO("bell.qasm", "qasm", "bell", "OPENQASM 2.0;\nqreg q[2];");
        when(proxy.convertBatch(anyList(), anyList(), eq("quirk"))).thenReturn(List.of(
                new QConvertResult(false, null, "Syntax error in QASM")
        ));

        ImportResultDTO res = service.importProjects(email, List.of(item));

        assertEquals(0, res.getSuccessCount());
        assertEquals(1, res.getFailureCount());
        assertEquals(ImportException.CONVERSION_FAILED, res.getItems().get(0).getErrorCode());
        verify(projectService, never()).saveProject(any());
    }

    @Test
    void testTooManyQubits() {
        ImportItemDTO item = new ImportItemDTO("bell.qasm", "qasm", "bell", "OPENQASM 2.0;\nqreg q[2];");
        // 17 qubits circuit
        StringBuilder cols = new StringBuilder("{\"cols\":[[");
        for (int i = 0; i < 17; i++) {
            if (i > 0) cols.append(",");
            cols.append("\"H\"");
        }
        cols.append("]]}");

        when(proxy.convertBatch(anyList(), anyList(), eq("quirk"))).thenReturn(List.of(
                new QConvertResult(true, cols.toString(), null)
        ));

        ImportResultDTO res = service.importProjects(email, List.of(item));

        assertEquals(0, res.getSuccessCount());
        assertEquals(1, res.getFailureCount());
        assertEquals(ImportException.TOO_MANY_QUBITS, res.getItems().get(0).getErrorCode());
    }

    @Test
    void testUniqueNameDisambiguation() {
        ImportItemDTO item = new ImportItemDTO("bell.qasm", "qasm", null, "OPENQASM 2.0;\nqreg q[2];");
        when(proxy.convertBatch(anyList(), anyList(), eq("quirk"))).thenReturn(List.of(
                new QConvertResult(true, bellQuirkOutput, null)
        ));

        Project existing = new Project("Bell");
        when(projectRepository.findProjectsByUserId(email)).thenReturn(List.of(existing));

        ImportResultDTO res = service.importProjects(email, List.of(item));

        assertEquals(1, res.getSuccessCount());
        assertEquals("bell (2)", res.getItems().get(0).getProjectName());
    }

    @Test
    void testTooManyFilesThrows400() {
        List<ImportItemDTO> items = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            items.add(new ImportItemDTO("f" + i + ".qasm", "qasm", "p" + i, "OPENQASM 2.0;\nqreg q[1];"));
        }

        ImportException ex = assertThrows(ImportException.class, () ->
                service.importProjects(email, items));
        assertEquals(ImportException.TOO_MANY_FILES, ex.getErrorCode());
        assertEquals(400, ex.getHttpStatus());
    }

    @Test
    void testConvertOnlyBell() {
        ConvertRequestDTO req = new ConvertRequestDTO(email, "bell.qasm", "qasm", "OPENQASM 2.0;\nqreg q[2];");
        when(proxy.convertBatch(anyList(), anyList(), eq("quirk"))).thenReturn(List.of(
                new QConvertResult(true, bellQuirkOutput, null)
        ));

        ConvertedCircuitDTO res = service.convertOnly(email, req);

        assertEquals(2, res.getQubits());
        assertEquals(2, res.getColumns());
        assertEquals("qasm", res.getFormat());
        verify(projectService, never()).saveProject(any());
    }

    @Test
    void testRepresentQasmSuccessAndIonqError() throws Exception {
        JsonNode quirk = mapper.readTree(bellQuirkOutput);
        RepresentRequestDTO reqQasm = new RepresentRequestDTO(email, quirk, "qasm");
        when(proxy.convertOne(anyString(), eq("quirk"), eq("qasm"))).thenReturn(
                new QConvertResult(true, "OPENQASM 2.0;\nqreg q[2];", null)
        );

        RepresentationDTO rep = service.represent(email, reqQasm);
        assertEquals("qasm", rep.getFormat());
        assertEquals(".qasm", rep.getExtension());
        assertEquals("OPENQASM 2.0;\nqreg q[2];", rep.getCode());

        RepresentRequestDTO reqIonq = new RepresentRequestDTO(email, quirk, "ionq");
        ImportException ex = assertThrows(ImportException.class, () ->
                service.represent(email, reqIonq));
        assertEquals(ImportException.INVALID_FORMAT, ex.getErrorCode());
        assertEquals(400, ex.getHttpStatus());
    }
}
