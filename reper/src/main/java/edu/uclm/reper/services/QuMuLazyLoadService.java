package edu.uclm.reper.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.reper.dto.OperatorSummaryDTO;
import edu.uclm.reper.dto.ProjectNoteDTO;
import edu.uclm.reper.dto.QuMuCircuitDTO;
import edu.uclm.reper.dto.QuMuCycleMatrixDTO;
import edu.uclm.reper.dto.QuMuCycleSummaryDTO;
import edu.uclm.reper.dto.QuMuExecConfigDTO;
import edu.uclm.reper.dto.QuMuMatrixMutantDTO;
import edu.uclm.reper.dto.QuMuMutantCircuitDTO;
import edu.uclm.reper.dto.QuMuMutantDTO;
import edu.uclm.reper.dto.QuMuProgramDTO;
import edu.uclm.reper.dto.QuMuProjectDTO;
import edu.uclm.reper.dto.QuMuResultDTO;
import edu.uclm.reper.model.ExecConfig;
import edu.uclm.reper.model.Mutant;
import edu.uclm.reper.model.MutantCycle;
import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.ProjectNote;
import edu.uclm.reper.model.QCircuit;
import edu.uclm.reper.model.QProgram;
import edu.uclm.reper.model.Result;
import edu.uclm.reper.model.TestSuite;
import edu.uclm.reper.repository.MutantCycleRepository;
import edu.uclm.reper.repository.MutantRepository;
import edu.uclm.reper.repository.MutantResultRepository;
import edu.uclm.reper.repository.ProjectRepository;

@Service
@Transactional(readOnly = true)
public class QuMuLazyLoadService {

    private static final Logger logger = LoggerFactory.getLogger(QuMuLazyLoadService.class);

    private final ProjectRepository projectRepository;
    private final MutantCycleRepository mutantCycleRepository;
    private final MutantRepository mutantRepository;
    private final MutantResultRepository mutantResultRepository;

    public QuMuLazyLoadService(ProjectRepository projectRepository,
                               MutantCycleRepository mutantCycleRepository,
                               MutantRepository mutantRepository,
                               MutantResultRepository mutantResultRepository) {
        this.projectRepository = projectRepository;
        this.mutantCycleRepository = mutantCycleRepository;
        this.mutantRepository = mutantRepository;
        this.mutantResultRepository = mutantResultRepository;
    }

    private void checkAccess(String email, String projectId) {
        if (!projectRepository.isUserInProject(projectId, email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with the project");
        }
    }

    public QuMuProjectDTO getProject(String email, String projectId) {
        long start = System.currentTimeMillis();
        checkAccess(email, projectId);

        Project project = projectRepository.findByIdWithQProgram(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        QuMuProgramDTO programDTO = toProgramDTO(project.getQProgram());

        List<MutantCycle> cycles = mutantCycleRepository.findWithExecConfigByProjectId(projectId);
        List<Object[]> counts = mutantRepository.countByCycle(projectId);
        Map<Integer, Long> countMap = new HashMap<>();
        if (counts != null) {
            for (Object[] row : counts) {
                if (row != null && row.length >= 2 && row[0] != null) {
                    countMap.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
                }
            }
        }

        List<QuMuCycleSummaryDTO> cycleDTOs = new ArrayList<>();
        if (cycles != null) {
            for (MutantCycle c : cycles) {
                long count = countMap.getOrDefault(c.getId(), 0L);
                cycleDTOs.add(new QuMuCycleSummaryDTO(
                        c.getId(),
                        c.getName(),
                        c.getDate(),
                        toExecConfigDTO(c.getExecConfig()),
                        count
                ));
            }
        }

        List<ProjectNoteDTO> noteDTOs = new ArrayList<>();
        if (project.getNotes() != null) {
            for (ProjectNote n : project.getNotes()) {
                noteDTOs.add(new ProjectNoteDTO(
                        n.getId(), n.getTitle(), n.getText(), n.getType(), n.getTimestamp()
                ));
            }
        }

        List<TestSuite> testSuites = project.getTestSuites();
        if (testSuites != null) {
            for (TestSuite ts : testSuites) {
                if (ts.getTestCases() != null) {
                    ts.getTestCases().size();
                }
            }
        }

        QuMuProjectDTO dto = new QuMuProjectDTO(project.getId(), project.getName(), programDTO, cycleDTOs, noteDTOs, testSuites);
        logger.info("[PERF] qumu getProject project={} cycle=null items={} ms={}", projectId, cycleDTOs.size(), System.currentTimeMillis() - start);
        return dto;
    }

    public QuMuCycleMatrixDTO getCycleMatrix(String email, String projectId, int cycleId) {
        long start = System.currentTimeMillis();
        checkAccess(email, projectId);

        MutantCycle cycle = mutantCycleRepository.findWithExecConfig(projectId, cycleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mutant cycle not found"));

        List<Object[]> rows = mutantRepository.findMatrixRows(projectId, cycleId);
        List<Object[]> results = mutantResultRepository.findMatrixResults(projectId, cycleId);

        Map<Integer, List<QuMuResultDTO>> resultMap = new HashMap<>();
        if (results != null) {
            for (Object[] res : results) {
                if (res != null && res.length >= 3 && res[0] != null) {
                    int mutantId = ((Number) res[0]).intValue();
                    int resId = ((Number) res[1]).intValue();
                    Result rEnum = (Result) res[2];
                    resultMap.computeIfAbsent(mutantId, k -> new ArrayList<>())
                            .add(new QuMuResultDTO(resId, rEnum));
                }
            }
        }

        List<QuMuMatrixMutantDTO> mutants = new ArrayList<>();
        if (rows != null) {
            for (Object[] row : rows) {
                int id = ((Number) row[0]).intValue();
                int mutantIndex = ((Number) row[1]).intValue();
                int mutatedColumn = ((Number) row[2]).intValue();
                int mutatedRow = ((Number) row[3]).intValue();
                String opName = row[4] != null ? (String) row[4] : null;
                OperatorSummaryDTO op = opName != null ? new OperatorSummaryDTO(opName) : null;
                String oracleName = row[5] != null ? (String) row[5] : null;
                String mutationDetail = row[6] != null ? (String) row[6] : null;
                List<QuMuResultDTO> mutantResults = resultMap.getOrDefault(id, Collections.emptyList());

                mutants.add(new QuMuMatrixMutantDTO(id, mutantIndex, mutatedColumn, mutatedRow, op, oracleName, mutationDetail, mutantResults));
            }
        }

        QuMuCycleMatrixDTO dto = new QuMuCycleMatrixDTO(
                cycle.getId(),
                cycle.getName(),
                cycle.getDate(),
                toExecConfigDTO(cycle.getExecConfig()),
                mutants
        );
        logger.info("[PERF] qumu getCycleMatrix project={} cycle={} items={} ms={}", projectId, cycleId, mutants.size(), System.currentTimeMillis() - start);
        return dto;
    }

    public QuMuMutantDTO getMutant(String email, String projectId, int cycleId, int mutantIndex) {
        long start = System.currentTimeMillis();
        checkAccess(email, projectId);

        Mutant mutant = mutantRepository.findWithCircuit(projectId, cycleId, mutantIndex)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mutant not found"));

        List<Object[]> rawResults = mutantResultRepository.findByMutantIdLight(mutant.getId());
        List<QuMuResultDTO> resultDTOs = new ArrayList<>();
        if (rawResults != null) {
            for (Object[] r : rawResults) {
                if (r != null && r.length >= 2) {
                    resultDTOs.add(new QuMuResultDTO(((Number) r[0]).intValue(), (Result) r[1]));
                }
            }
        }

        OperatorSummaryDTO opDto = mutant.getOperator() != null ? new OperatorSummaryDTO(mutant.getOperator().getName()) : null;
        QuMuProgramDTO circuitDto = toProgramDTO(mutant.getQuirkCircuit());

        QuMuMutantDTO dto = new QuMuMutantDTO(
                mutant.getId(),
                mutant.getIndex(),
                mutant.getColumn(),
                mutant.getRow(),
                opDto,
                mutant.getOracleName(),
                mutant.getMutationDetail(),
                resultDTOs,
                circuitDto
        );
        logger.info("[PERF] qumu getMutant project={} cycle={} items=1 ms={}", projectId, cycleId, System.currentTimeMillis() - start);
        return dto;
    }

    public List<QuMuMutantCircuitDTO> getMutantCircuits(String email, String projectId, int cycleId, int fromIndex, int limit) {
        long start = System.currentTimeMillis();
        checkAccess(email, projectId);

        int safeLimit = Math.max(1, Math.min(limit, 200));
        List<Mutant> page = mutantRepository.findCircuitsPage(projectId, cycleId, fromIndex, PageRequest.of(0, safeLimit));

        List<QuMuMutantCircuitDTO> dtos = new ArrayList<>();
        if (page != null) {
            for (Mutant m : page) {
                dtos.add(new QuMuMutantCircuitDTO(m.getIndex(), toProgramDTO(m.getQuirkCircuit())));
            }
        }
        logger.info("[PERF] qumu getMutantCircuits project={} cycle={} items={} ms={}", projectId, cycleId, dtos.size(), System.currentTimeMillis() - start);
        return dtos;
    }

    private QuMuProgramDTO toProgramDTO(QProgram program) {
        if (program == null) {
            return null;
        }
        return new QuMuProgramDTO(
                program.getId(),
                program.getQubits(),
                program.getShots(),
                program.getInputQubits(),
                program.getOutputQubits(),
                toCircuitDTO(program.getQuirkCircuit())
        );
    }

    private QuMuCircuitDTO toCircuitDTO(QCircuit circuit) {
        if (circuit == null) {
            return null;
        }
        return new QuMuCircuitDTO(
                circuit.getId(),
                circuit.getCode(),
                circuit.getMutableColumns(),
                circuit.getMutableRows()
        );
    }

    private QuMuExecConfigDTO toExecConfigDTO(ExecConfig config) {
        if (config == null) {
            return null;
        }
        return new QuMuExecConfigDTO(
                config.getId(),
                config.getExecutionDate(),
                config.getMachine(),
                config.getExecAlgorithm(),
                config.getToleratedError(),
                config.getZombieError(),
                config.getShots()
        );
    }
}
