package edu.uclm.reper.services;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uclm.reper.dto.CircuitConfigDTO;
import edu.uclm.reper.dto.CreateExperimentDTO;
import edu.uclm.reper.dto.ExperimentProgressDTO;
import edu.uclm.reper.dto.ExperimentSummaryDTO;
import edu.uclm.reper.dto.MutantCycleSummaryDTO;
import edu.uclm.reper.dto.MutantSummaryDTO;
import edu.uclm.reper.dto.OperatorSummaryDTO;
import edu.uclm.reper.model.ExecConfig;
import edu.uclm.reper.model.Experiment;
import edu.uclm.reper.model.Mutant;
import edu.uclm.reper.model.MutantCycle;
import edu.uclm.reper.model.MutantResult;
import edu.uclm.reper.model.Operator;
import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.QCircuit;
import edu.uclm.reper.model.QProgram;
import edu.uclm.reper.model.Result;
import edu.uclm.reper.model.User;
import edu.uclm.reper.repository.ExperimentRepository;
import edu.uclm.reper.repository.MutantCycleRepository;
import edu.uclm.reper.repository.OperatorRepository;
import edu.uclm.reper.repository.ProjectRepository;
import edu.uclm.reper.repository.UserRepository;

@Service
public class ExperimentService {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentService.class);

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MutantCycleRepository mutantCycleRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ExperimentNotificationService notificationService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.qumugen.url:http://localhost:8500}")
    private String qumugenUrl;

    @Value("${app.qiskitexec.url:http://localhost:8502}")
    private String qiskitexecUrl;

    @Value("${app.qiskitexec.batch-size:32}")
    private int batchSize = 32;

    @Value("${app.qiskitexec.max-programs-per-request:128}")
    private int maxProgramsPerRequest;

    @Value("${app.qiskitexec.read-timeout-seconds:900}")
    private int readTimeoutSeconds;

    private RestTemplate restTemplate;

    @PostConstruct
    public void initRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(this.readTimeoutSeconds * 1000);
        this.restTemplate = new RestTemplate(factory);
    }

    public ExperimentService() {
    }

    public Experiment createExperiment(CreateExperimentDTO dto, String userId) throws Exception {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new Exception("User not found: " + userId);
        }

        User user = userOpt.get();

        Experiment experiment = new Experiment(dto.getName(), user);
        experiment.setTotalCircuits(dto.getCircuitConfigs() != null ? dto.getCircuitConfigs().size() : 0);
        experiment.setCompletedCircuits(0);
        experiment.setStatus("RUNNING");

        ExecConfig execConfig = new ExecConfig(new Date(), dto.getMachine(), dto.getExecAlgorithm());
        execConfig.setId(UUID.randomUUID().toString());
        execConfig.setToleratedError(dto.getToleratedError());
        execConfig.setZombieError(dto.getZombieError());
        execConfig.setShots(dto.getShots());
        experiment.setExecConfig(execConfig);

        experiment = experimentRepository.save(experiment);

        final String experimentId = experiment.getId();
        final String userEmail = userId;

        CompletableFuture.runAsync(() -> {
            runExperimentAsync(experimentId, dto, userEmail);
        });

        return experiment;
    }

    private void runExperimentAsync(String experimentId, CreateExperimentDTO dto, String userEmail) {
        long startTime = System.currentTimeMillis();
        try {
            List<CircuitConfigDTO> configs = dto.getCircuitConfigs();
            if (configs == null || configs.isEmpty()) {
                markExperimentFinished(experimentId, "COMPLETED", null, startTime);
                return;
            }

            for (CircuitConfigDTO cc : configs) {
                long tCircuitStart = System.nanoTime();
                processSingleCircuit(experimentId, cc, dto);
                long circuitTotalMs = (System.nanoTime() - tCircuitStart) / 1_000_000;
                logger.info("[PERF] exp={} circuit={} totalMs={}", experimentId, cc.getProjectId(), circuitTotalMs);

                // Use simple findById without heavy JOIN FETCH
                Optional<Experiment> expOpt = experimentRepository.findById(experimentId);
                if (expOpt.isPresent()) {
                    Experiment exp = expOpt.get();
                    exp.setCompletedCircuits(exp.getCompletedCircuits() + 1);
                    experimentRepository.save(exp);
                }
            }

            markExperimentFinished(experimentId, "COMPLETED", null, startTime);

            Optional<Experiment> expOpt = experimentRepository.findByIdWithDetails(experimentId);
            if (expOpt.isPresent()) {
                notificationService.sendExperimentCompletedEmail(expOpt.get(), userEmail);
            }

        } catch (Exception e) {
            logger.error("Error executing experiment {}: {}", experimentId, e.getMessage(), e);
            markExperimentFinished(experimentId, "FAILED", e.getMessage(), startTime);
            Optional<Experiment> expOpt = experimentRepository.findByIdWithDetails(experimentId);
            if (expOpt.isPresent()) {
                notificationService.sendExperimentFailedEmail(expOpt.get(), userEmail, e.getMessage());
            }
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public void processSingleCircuit(String experimentId, CircuitConfigDTO cc, CreateExperimentDTO dto)
            throws Exception {
        Project project = projectRepository.findByIdWithQProgram(cc.getProjectId())
                .or(() -> projectRepository.findById(cc.getProjectId()))
                .orElseThrow(() -> new Exception("Project not found: " + cc.getProjectId()));

        QProgram qProgram = project.getQProgram();
        if (qProgram == null || qProgram.getQuirkCircuit() == null) {
            throw new Exception("Project " + project.getName() + " has no valid quantum program");
        }

        String inputQubits = cc.getInputQubits() != null ? cc.getInputQubits().trim() : "";
        String outputQubits = cc.getOutputQubits() != null ? cc.getOutputQubits().trim() : "";

        // 1. Generate mutants in qumugen
        long tGenStart = System.nanoTime();
        List<Map<String, Object>> generatedMutantCircuits = generateMutantsInQumugen(qProgram, dto.getOperatorNames(),
                inputQubits);
        long generateMs = (System.nanoTime() - tGenStart) / 1_000_000;

        // 2. Build and save new MutantCycle
        long tCycleStart = System.nanoTime();
        MutantCycle newCycle = createAndSaveMutantCycle(project, experimentId, dto, generatedMutantCircuits);
        long createCycleMs = (System.nanoTime() - tCycleStart) / 1_000_000;

        int mutantCount = generatedMutantCircuits != null ? generatedMutantCircuits.size() : 0;
        logger.info("[PERF] exp={} circuit={} generateMs={} createCycleMs={} mutants={}",
                experimentId, cc.getProjectId(), generateMs, createCycleMs, mutantCount);

        // 3. Execute original circuit & mutants in qiskitexec
        executeCycleInQiskitExec(experimentId, project, newCycle, inputQubits, outputQubits, dto);
    }

    private MutantCycle createAndSaveMutantCycle(Project project, String experimentId, CreateExperimentDTO dto,
            List<Map<String, Object>> generatedMutants) {
        long tCyclesStart = System.nanoTime();
        List<MutantCycle> existingCycles = mutantCycleRepository.findByProjectId(project.getId());
        int nextCycleId = 1;
        if (existingCycles != null && !existingCycles.isEmpty()) {
            for (MutantCycle mc : existingCycles) {
                if (mc.getId() >= nextCycleId) {
                    nextCycleId = mc.getId() + 1;
                }
            }
        }
        long findCyclesMs = (System.nanoTime() - tCyclesStart) / 1_000_000;

        MutantCycle newCycle = new MutantCycle(new Date());
        newCycle.setId(nextCycleId);
        newCycle.setName(dto.getName() + " - " + project.getName());
        newCycle.setProject(project);

        long tExpStart = System.nanoTime();
        newCycle.setExperiment(experimentRepository.getReferenceById(experimentId));
        long findExpMs = (System.nanoTime() - tExpStart) / 1_000_000;

        ExecConfig cycleExecConfig = new ExecConfig(new Date(), dto.getMachine(), dto.getExecAlgorithm());
        cycleExecConfig.setId(UUID.randomUUID().toString());
        cycleExecConfig.setToleratedError(dto.getToleratedError());
        cycleExecConfig.setZombieError(dto.getZombieError());
        cycleExecConfig.setShots(dto.getShots());
        cycleExecConfig.setMutantCycle(newCycle);
        newCycle.setExecConfig(cycleExecConfig);

        long tBuildStart = System.nanoTime();
        Map<String, Operator> opsByName = operatorRepository.findAll().stream()
                .filter(o -> o.getName() != null)
                .collect(Collectors.toMap(Operator::getName, o -> o, (a, b) -> a));

        QProgram qProgram = project.getQProgram();
        List<Mutant> mutantsList = new ArrayList<>();
        for (int i = 0; i < generatedMutants.size(); i++) {
            Map<String, Object> mcData = generatedMutants.get(i);
            Mutant m = new Mutant();
            m.setIndex(i);
            m.setMutantCycle(newCycle);

            String opName = (String) mcData.get("mutationOperator");
            if (opName != null) {
                Operator op = opsByName.get(opName);
                if (op != null) {
                    m.setOperator(op);
                }
            }

            if (mcData.get("mutatedColumn") != null) {
                m.setColumn(((Number) mcData.get("mutatedColumn")).intValue());
            }
            if (mcData.get("mutatedRow") != null) {
                m.setRow(((Number) mcData.get("mutatedRow")).intValue());
            }

            Map<String, Object> circuitMap = null;
            if (mcData.get("quirk") instanceof Map) {
                circuitMap = (Map<String, Object>) mcData.get("quirk");
            } else if (mcData.get("qCircuit") instanceof Map) {
                circuitMap = (Map<String, Object>) mcData.get("qCircuit");
            } else if (mcData.get("circuit") instanceof Map) {
                circuitMap = (Map<String, Object>) mcData.get("circuit");
            } else if (mcData.containsKey("cols")) {
                circuitMap = mcData;
            }

            if (circuitMap != null && qProgram != null) {
                QProgram mq = new QProgram();
                mq.setId(UUID.randomUUID().toString());
                mq.setQubits(qProgram.getQubits());
                QCircuit qc = new QCircuit();
                qc.setId(UUID.randomUUID().toString());
                qc.setCode(objectMapper.convertValue(circuitMap, JsonNode.class));
                qc.setQbits(qProgram.getQubits());
                mq.setQuirkCircuit(qc);
                m.setQuirkCircuit(mq);
            }
            mutantsList.add(m);
        }
        newCycle.setMutants(mutantsList);
        long buildMs = (System.nanoTime() - tBuildStart) / 1_000_000;

        long tSaveStart = System.nanoTime();
        MutantCycle saved = mutantCycleRepository.save(newCycle);
        long saveMs = (System.nanoTime() - tSaveStart) / 1_000_000;

        logger.info("[PERF] createCycle findCyclesMs={} findExpMs={} buildMs={} saveMs={}", findCyclesMs, findExpMs, buildMs, saveMs);

        return saved;
    }

    private Map<String, Object> extractQuirkCode(JsonNode codeNode) {
        if (codeNode == null || codeNode.isNull()) {
            return Map.of("cols", List.of());
        }
        Map<String, Object> map = objectMapper.convertValue(codeNode, Map.class);
        if (map.containsKey("cols")) {
            return map;
        }
        if (map.containsKey("circuit") && map.get("circuit") instanceof Map) {
            Map<String, Object> inner = (Map<String, Object>) map.get("circuit");
            if (inner.containsKey("cols"))
                return inner;
        }
        if (map.containsKey("quirkCode") && map.get("quirkCode") instanceof Map) {
            Map<String, Object> inner = (Map<String, Object>) map.get("quirkCode");
            if (inner.containsKey("cols"))
                return inner;
            if (inner.containsKey("circuit") && inner.get("circuit") instanceof Map) {
                return (Map<String, Object>) inner.get("circuit");
            }
        }
        return map;
    }

    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private List<Map<String, Object>> generateMutantsInQumugen(QProgram qProgram, List<String> operatorNames,
            String inputQubits) throws Exception {
        try {
            Map<String, Object> quirkMap = extractQuirkCode(qProgram.getQuirkCircuit().getCode());
            Map<String, Object> body = new HashMap<>();
            body.put("circuit", quirkMap);
            body.put("operatorNames", operatorNames);
            body.put("mutableColumns",
                    qProgram.getQuirkCircuit().getMutableColumns() != null
                            ? qProgram.getQuirkCircuit().getMutableColumns()
                            : "-1");
            body.put("mutableRows",
                    qProgram.getQuirkCircuit().getMutableRows() != null ? qProgram.getQuirkCircuit().getMutableRows()
                            : "-1");
            body.put("generateWithAllInputs", false);
            body.put("inputQubits", inputQubits != null ? inputQubits : "");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, createJsonHeaders());

            ResponseEntity<List> response = restTemplate.exchange(
                    qumugenUrl + "/qumugen/generateQuirkMutants",
                    HttpMethod.PUT,
                    request,
                    List.class);

            return response.getBody() != null ? (List<Map<String, Object>>) response.getBody() : new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error calling qumugen generateQuirkMutants: {}", e.getMessage(), e);
            throw new Exception("Error al generar mutantes en qumugen: " + e.getMessage(), e);
        }
    }

    private void executeCycleInQiskitExec(String experimentId, Project project, MutantCycle cycle, String inputQubits, String outputQubits,
            CreateExperimentDTO dto) throws Exception {
        try {
            long tOrigCodeStart = System.nanoTime();
            String originalWholeCode = getOriginalQiskitCode(project, outputQubits, dto.getShots());
            long originalCodeMs = (System.nanoTime() - tOrigCodeStart) / 1_000_000;

            long tOrigExecStart = System.nanoTime();
            List<?> originalResults = executeOriginalInQiskitExec(project, originalWholeCode, inputQubits, outputQubits,
                    dto.getExecAlgorithm());
            long originalExecMs = (System.nanoTime() - tOrigExecStart) / 1_000_000;

            List<Mutant> mutants = cycle.getMutants();
            int chunkSize = calculateChunkSize(inputQubits);
            logger.info("[PERF] exp={} circuit={} originalCodeMs={} originalExecMs={} chunkSize={}",
                    experimentId, project.getId(), originalCodeMs, originalExecMs, chunkSize);

            if (mutants == null || mutants.isEmpty()) {
                return;
            }

            for (int start = 0; start < mutants.size(); start += chunkSize) {
                int end = Math.min(start + chunkSize, mutants.size());
                List<Mutant> batch = mutants.subList(start, end);

                executeAndProcessMutantBatch(experimentId, project, cycle, batch, originalResults, inputQubits, outputQubits, dto);
            }
        } catch (Exception e) {
            logger.error("Error executing cycle in qiskitexec: {}", e.getMessage(), e);
            throw new Exception("Fallo al ejecutar mutantes en qiskitexec: " + e.getMessage(), e);
        }
    }

    private String getOriginalQiskitCode(Project project, String outputQubits, int shots) throws Exception {
        Map<String, Object> quirkMap = extractQuirkCode(project.getQProgram().getQuirkCircuit().getCode());
        Map<String, Object> origBody = new HashMap<>();
        origBody.put("qCircuit", Map.of("quirkCode", quirkMap));
        origBody.put("qubits", project.getQProgram().getQubits());
        origBody.put("outputQubits", outputQubits);

        HttpEntity<Map<String, Object>> origReq = new HttpEntity<>(origBody, createJsonHeaders());
        String qumugenCodeUrl = qumugenUrl + "/qumugen/getQiskitCode?useTemplate=true&shots=" + shots
                + "&qiskitTemplate=qiskitTemplateWindows.txt";

        ResponseEntity<Map> origCodeResp = restTemplate.exchange(qumugenCodeUrl, HttpMethod.PUT, origReq, Map.class);
        Map<?, ?> body = origCodeResp.getBody();
        if (body == null || !body.containsKey("wholeCode")) {
            throw new Exception("No se pudo obtener el código Qiskit del circuito original");
        }
        return (String) body.get("wholeCode");
    }

    private List<?> executeOriginalInQiskitExec(Project project, String originalWholeCode, String inputQubits,
            String outputQubits, String algorithm) {
        Map<String, Object> programMap = new HashMap<>();
        programMap.put("id", project.getId());
        programMap.put("qiskitCode", Arrays.asList(originalWholeCode.split("\n")));
        programMap.put("inputQubits", inputQubits);
        programMap.put("outputQubits", outputQubits);
        programMap.put("qubits", project.getQProgram().getQubits());

        Map<String, Object> execOneBody = new HashMap<>();
        execOneBody.put("program", programMap);
        execOneBody.put("outputQubits", outputQubits);
        execOneBody.put("qubits", project.getQProgram().getQubits());
        execOneBody.put("algorithm", algorithm);
        execOneBody.put("reduceTable", false);
        execOneBody.put("inputs", new ArrayList<>());

        HttpEntity<Map<String, Object>> execOneReq = new HttpEntity<>(execOneBody, createJsonHeaders());
        ResponseEntity<List> origExecResp = restTemplate.exchange(
                qiskitexecUrl + "/strategy/executeOne",
                HttpMethod.PUT,
                execOneReq,
                List.class);
        return origExecResp.getBody();
    }

    private int calculateChunkSize(String inputQubits) {
        int inputCount = 0;
        if (inputQubits != null && !inputQubits.trim().isEmpty()) {
            for (String part : inputQubits.split(",")) {
                if (!part.trim().isEmpty()) {
                    inputCount++;
                }
            }
        }
        int variants = 1 << Math.min(inputCount, 20);
        return Math.max(1, Math.min(this.batchSize, this.maxProgramsPerRequest / variants));
    }

    private void executeAndProcessMutantBatch(String experimentId, Project project, MutantCycle cycle, List<Mutant> batch,
            List<?> originalResults, String inputQubits, String outputQubits, CreateExperimentDTO dto) throws Exception {
        List<Map<String, Object>> batchFormatted = formatMutantBatch(project, batch);

        Map<String, Object> multCodeBody = new HashMap<>();
        multCodeBody.put("mutants", batchFormatted);
        multCodeBody.put("outputQubits", outputQubits);

        HttpEntity<Map<String, Object>> multCodeReq = new HttpEntity<>(multCodeBody, createJsonHeaders());
        String multCodeUrl = qumugenUrl + "/qumugen/getMultipleQiskitCode?shots=" + dto.getShots()
                + "&qiskitTemplate=qiskitTemplateWindows.txt";
        long tCodeGenStart = System.nanoTime();
        ResponseEntity<List> multCodeResp = restTemplate.exchange(multCodeUrl, HttpMethod.PUT, multCodeReq, List.class);
        long codeGenMs = (System.nanoTime() - tCodeGenStart) / 1_000_000;
        List<?> mutantCodes = multCodeResp.getBody();

        // Execute batch with strategy
        Map<String, Object> stratBody = new HashMap<>();
        stratBody.put("mutants", mutantCodes);
        stratBody.put("originalResults", originalResults);
        stratBody.put("algorithm", dto.getExecAlgorithm());
        stratBody.put("toleratedError", dto.getToleratedError());
        stratBody.put("zombieError", dto.getZombieError());
        stratBody.put("inputQubits", inputQubits);
        stratBody.put("outputQubits", outputQubits);
        stratBody.put("qubits", project.getQProgram().getQubits());
        stratBody.put("reduceTable", false);

        HttpEntity<Map<String, Object>> stratReq = new HttpEntity<>(stratBody, createJsonHeaders());
        ResponseEntity<List> stratResp = null;
        int attempts = 0;
        long tStratStart = System.nanoTime();
        while (attempts < 2) {
            try {
                attempts++;
                stratResp = restTemplate.exchange(
                        qiskitexecUrl + "/strategy/executeWithStrategy",
                        HttpMethod.PUT,
                        stratReq,
                        List.class);
                break;
            } catch (ResourceAccessException ex) {
                Throwable cause = ex;
                boolean isConnectException = false;
                while (cause != null) {
                    if (cause instanceof ConnectException) {
                        isConnectException = true;
                        break;
                    }
                    cause = cause.getCause();
                }
                if (!isConnectException || attempts >= 2) {
                    throw ex;
                }
                logger.warn("Advertencia: error de conexión al conectar con qiskitexec ({}). Reintentando...", ex.getMessage());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        long strategyMs = (System.nanoTime() - tStratStart) / 1_000_000;

        long saveMs = 0;
        List<List<Map<String, Object>>> stratResults = stratResp != null ? stratResp.getBody() : null;
        if (stratResults != null) {
            long tSaveStart = System.nanoTime();
            processAndSaveBatchResults(project.getId(), cycle.getId(), batch, stratResults);
            saveMs = (System.nanoTime() - tSaveStart) / 1_000_000;
        }

        List<Integer> mutantIndices = batch.stream().map(Mutant::getIndex).collect(Collectors.toList());
        logger.info("[PERF] exp={} mutants={} codeGenMs={} strategyMs={} attempts={} saveMs={}",
                experimentId, mutantIndices, codeGenMs, strategyMs, attempts, saveMs);
    }

    private List<Map<String, Object>> formatMutantBatch(Project project, List<Mutant> batch) {
        List<Map<String, Object>> batchFormatted = new ArrayList<>();
        int qubits = project.getQProgram() != null ? project.getQProgram().getQubits() : 1;
        for (Mutant m : batch) {
            if (m.getQuirkCircuit() != null && m.getQuirkCircuit().getQuirkCircuit() != null) {
                Map<String, Object> mQuirkMap = extractQuirkCode(m.getQuirkCircuit().getQuirkCircuit().getCode());
                Map<String, Object> item = new HashMap<>();
                item.put("mutantIndex", m.getIndex());
                item.put("circuit", Map.of(
                        "quirkCode", mQuirkMap,
                        "qubits", qubits));
                batchFormatted.add(item);
            }
        }
        return batchFormatted;
    }

    private void processAndSaveBatchResults(String projectId, int cycleId, List<Mutant> batch,
            List<List<Map<String, Object>>> stratResults) throws Exception {
        for (int mIdx = 0; mIdx < Math.min(batch.size(), stratResults.size()); mIdx++) {
            Mutant mutant = batch.get(mIdx);
            List<Map<String, Object>> resultsList = stratResults.get(mIdx);
            if (mutant.getMutantResults() == null) {
                mutant.setMutantResults(new ArrayList<>());
            }
            mutant.getMutantResults().clear();

            for (int rIdx = 0; rIdx < resultsList.size(); rIdx++) {
                Map<String, Object> rMap = resultsList.get(rIdx);
                boolean killed = Boolean.TRUE.equals(rMap.get("killed"));
                boolean zombie = Boolean.TRUE.equals(rMap.get("zombie"));

                MutantResult mr = new MutantResult();
                mr.setId(rIdx);
                mr.setMutant(mutant);
                mr.setResult(killed ? Result.KILLED : (zombie ? Result.ZOMBIE : Result.ALIVE));
                mutant.getMutantResults().add(mr);
            }
        }
        projectService.saveExecutionsBatch(projectId, cycleId, batch);
    }

    private void markExperimentFinished(String experimentId, String status, String errorMessage, long startTime) {
        Optional<Experiment> expOpt = experimentRepository.findByIdWithDetails(experimentId);
        if (expOpt.isPresent()) {
            Experiment exp = expOpt.get();
            exp.setStatus(status);
            exp.setFinishedAt(new Date());
            exp.setDurationSeconds((System.currentTimeMillis() - startTime) / 1000);
            if (errorMessage != null) {
                if (errorMessage.length() > 500) {
                    errorMessage = errorMessage.substring(0, 500);
                }
                exp.setErrorMessage(errorMessage);
            }
            experimentRepository.save(exp);
        }
    }

    public List<ExperimentProgressDTO> getActiveProgress(String userId) {
        List<Experiment> active = experimentRepository.findActiveByUserId(userId);
        return active.stream().map(e -> new ExperimentProgressDTO(
                e.getId(),
                e.getName(),
                e.getStatus(),
                e.getTotalCircuits() != null ? e.getTotalCircuits() : 0,
                e.getCompletedCircuits() != null ? e.getCompletedCircuits() : 0,
                e.getCreatedAt())).collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<ExperimentSummaryDTO> getUserExperiments(String userId) {
        List<Experiment> list = experimentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return list.stream().map(this::toSummaryDTO).collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ExperimentSummaryDTO getExperimentDetails(String experimentId) throws Exception {
        Optional<Experiment> expOpt = experimentRepository.findByIdWithDetails(experimentId);
        if (expOpt.isEmpty())
            throw new Exception("Experiment not found: " + experimentId);
        return toSummaryDTO(expOpt.get());
    }

    private ExperimentSummaryDTO toSummaryDTO(Experiment e) {
        ExperimentSummaryDTO dto = new ExperimentSummaryDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setStatus(e.getStatus());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setFinishedAt(e.getFinishedAt());
        dto.setDurationSeconds(e.getDurationSeconds());
        dto.setTotalCircuits(e.getTotalCircuits());
        dto.setCompletedCircuits(e.getCompletedCircuits());
        dto.setErrorMessage(e.getErrorMessage());
        if (e.getExecConfig() != null) {
            dto.setMachine(e.getExecConfig().getMachine());
            dto.setExecAlgorithm(e.getExecConfig().getExecAlgorithm());
        }

        if (e.getMutantCycles() != null) {
            List<MutantCycleSummaryDTO> cycleDTOs = e.getMutantCycles().stream().map(mc -> {
                List<MutantSummaryDTO> mDTOs = new ArrayList<>();
                int killed = 0;
                int zombie = 0;
                int alive = 0;
                if (mc.getMutants() != null) {
                    for (Mutant m : mc.getMutants()) {
                        OperatorSummaryDTO opDto = m.getOperator() != null
                                ? new OperatorSummaryDTO(m.getOperator().getName())
                                : null;
                        mDTOs.add(new MutantSummaryDTO(m.getIndex(), opDto, m.getOracleName()));

                        if (m.getMutantResults() != null && !m.getMutantResults().isEmpty()) {
                            if (m.isKilled()) {
                                killed++;
                            } else if (m.isZombie()) {
                                zombie++;
                            } else {
                                alive++;
                            }
                        }
                    }
                }
                MutantCycleSummaryDTO cDto = new MutantCycleSummaryDTO(mc.getId(), mc.getName(), mDTOs);
                if (mc.getProject() != null) {
                    cDto.setProjectId(mc.getProject().getId());
                    cDto.setProjectName(mc.getProject().getName());
                }
                cDto.setKilledCount(killed);
                cDto.setZombieCount(zombie);
                cDto.setAliveCount(alive);
                int totalWithResults = killed + zombie + alive;
                if (totalWithResults > 0) {
                    cDto.setMutationScore(Math.round((killed * 100.0 / totalWithResults) * 100.0) / 100.0);
                }
                return cDto;
            }).collect(Collectors.toList());
            dto.setMutantCycles(cycleDTOs);
        }
        return dto;
    }


    @org.springframework.transaction.annotation.Transactional
    public void deleteExperiment(String experimentId, String userId) throws Exception {
        Optional<Experiment> expOpt = experimentRepository.findByIdWithDetails(experimentId);
        if (expOpt.isEmpty()) {
            throw new Exception("Experiment not found: " + experimentId);
        }
        Experiment exp = expOpt.get();
        if (exp.getUser() != null && !exp.getUser().getId().equals(userId)) {
            throw new Exception("Unauthorized to delete this experiment");
        }
        if (exp.getMutantCycles() != null) {
            for (MutantCycle mc : exp.getMutantCycles()) {
                mc.setExperiment(null);
                mutantCycleRepository.save(mc);
            }
        }
        experimentRepository.delete(exp);
    }
}
