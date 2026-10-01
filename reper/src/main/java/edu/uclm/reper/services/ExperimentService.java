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

import java.nio.charset.StandardCharsets;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uclm.reper.dto.CircuitConfigDTO;
import edu.uclm.reper.dto.CreateExperimentDTO;
import edu.uclm.reper.dto.ExperimentProgressDTO;
import edu.uclm.reper.dto.CycleStatsDTO;
import edu.uclm.reper.dto.ExperimentDetailDTO;
import edu.uclm.reper.dto.ExperimentListItemDTO;
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

    @Value("${app.qiskitexec.runner-workers:8}")
    private int runnerWorkers;

    @Value("${app.qiskitexec.target-request-seconds:300}")
    private int targetRequestSeconds;

    @Value("${app.qiskitexec.read-timeout-seconds:3600}")
    private int readTimeoutSeconds;

    @Value("${app.qiskitexec.poll-interval-ms:3000}")
    private long pollIntervalMs;

    private RestTemplate restTemplate;
    private RestTemplate pollRestTemplate;

    @PostConstruct
    public void initRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(this.readTimeoutSeconds * 1000);
        this.restTemplate = new RestTemplate(factory);

        SimpleClientHttpRequestFactory pollFactory = new SimpleClientHttpRequestFactory();
        pollFactory.setConnectTimeout(30000);
        pollFactory.setReadTimeout(30000);
        this.pollRestTemplate = new RestTemplate(pollFactory);
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
            int chunkSize = calculateChunkSize(inputQubits, originalExecMs);
            logger.info("[PERF] exp={} circuit={} originalCodeMs={} originalExecMs={} chunkSize={}",
                    experimentId, project.getId(), originalCodeMs, originalExecMs, chunkSize);

            if (mutants == null || mutants.isEmpty()) {
                return;
            }

            for (int start = 0; start < mutants.size(); start += chunkSize) {
                int end = Math.min(start + chunkSize, mutants.size());
                List<Mutant> batch = mutants.subList(start, end);

                executeBatchWithSplit(experimentId, project, cycle, batch, originalResults, inputQubits, outputQubits, dto);
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

        return runQiskitexecJob("executeOne", execOneBody);
    }

    private List<?> runQiskitexecJob(String kind, Object body) {
        ResponseEntity<Map> resp = restTemplate.exchange(
                qiskitexecUrl + "/strategy/jobs/" + kind,
                HttpMethod.POST,
                new HttpEntity<>(body, createJsonHeaders()),
                Map.class);
        String jobId = (String) resp.getBody().get("jobId");

        long start = System.currentTimeMillis();
        int consecutiveErrors = 0;
        int polls = 0;
        boolean done = false;
        try {
            while (true) {
                try {
                    Thread.sleep(pollIntervalMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Interrumpido",
                            new HttpHeaders(), new byte[0], StandardCharsets.UTF_8);
                }
                polls++;
                ResponseEntity<Map> stResp;
                try {
                    stResp = pollRestTemplate.getForEntity(qiskitexecUrl + "/strategy/jobs/" + jobId, Map.class);
                } catch (ResourceAccessException ex) {
                    consecutiveErrors++;
                    logger.warn("[PERF] fallo sondeando job {} ({}): {}", jobId, consecutiveErrors, ex.getMessage());
                    if (consecutiveErrors >= 10) {
                        throw HttpServerErrorException.create(HttpStatus.BAD_GATEWAY, "No se pudo consultar el job " + jobId,
                                new HttpHeaders(), new byte[0], StandardCharsets.UTF_8);
                    }
                    continue;
                } catch (HttpStatusCodeException ex) {
                    int sc = ex.getStatusCode().value();
                    if (sc == 404) {
                        throw HttpServerErrorException.create(HttpStatus.BAD_GATEWAY, "qiskitexec perdió el job " + jobId,
                                new HttpHeaders(), new byte[0], StandardCharsets.UTF_8);
                    } else if (sc == 502 || sc == 503 || sc == 504) {
                        consecutiveErrors++;
                        logger.warn("[PERF] fallo sondeando job {} ({}): {}", jobId, consecutiveErrors, ex.getMessage());
                        if (consecutiveErrors >= 10) {
                            throw HttpServerErrorException.create(HttpStatus.BAD_GATEWAY, "No se pudo consultar el job " + jobId,
                                    new HttpHeaders(), new byte[0], StandardCharsets.UTF_8);
                        }
                        continue;
                    } else {
                        throw ex;
                    }
                }

                consecutiveErrors = 0;
                Map<?, ?> st = stResp.getBody();
                String state = st != null ? (String) st.get("state") : null;

                if ("finished".equals(state)) {
                    List<?> r = pollRestTemplate.getForEntity(qiskitexecUrl + "/strategy/jobs/" + jobId + "/result", List.class).getBody();
                    done = true;
                    long waitMs = System.currentTimeMillis() - start;
                    logger.info("[PERF] qiskitexec job={} kind={} waitMs={} polls={}", jobId, kind, waitMs, polls);
                    return r;
                } else if ("failed".equals(state)) {
                    done = true;
                    Object statusVal = st != null ? st.get("httpStatus") : null;
                    int code = (statusVal instanceof Number n) ? n.intValue() : 500;
                    String err = String.valueOf(st != null ? st.get("error") : null);
                    if (code >= 500) {
                        throw HttpServerErrorException.create(HttpStatusCode.valueOf(code), err, new HttpHeaders(),
                                err.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                    } else {
                        throw HttpClientErrorException.create(HttpStatusCode.valueOf(code), err, new HttpHeaders(),
                                err.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                    }
                } else if ("cancelled".equals(state)) {
                    throw HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Job " + jobId + " cancelado en qiskitexec",
                            new HttpHeaders(), new byte[0], StandardCharsets.UTF_8);
                }

                if (System.currentTimeMillis() - start > readTimeoutSeconds * 1000L) {
                    throw HttpServerErrorException.create(HttpStatus.GATEWAY_TIMEOUT, "Job " + jobId + " superó " + readTimeoutSeconds + " s",
                            new HttpHeaders(), new byte[0], StandardCharsets.UTF_8);
                }
            }
        } finally {
            if (!done) {
                cancelQiskitexecJob(jobId);
            }
        }
    }

    private void cancelQiskitexecJob(String jobId) {
        try {
            pollRestTemplate.exchange(qiskitexecUrl + "/strategy/jobs/" + jobId, HttpMethod.DELETE, null, Map.class);
        } catch (Exception e) {
            logger.warn("No se pudo cancelar el job {}: {}", jobId, e.getMessage());
        }
    }

    private void executeBatchWithSplit(String experimentId, Project project, MutantCycle cycle, List<Mutant> batch,
            List<?> originalResults, String inputQubits, String outputQubits, CreateExperimentDTO dto) throws Exception {
        try {
            executeAndProcessMutantBatch(experimentId, project, cycle, batch, originalResults, inputQubits, outputQubits, dto);
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            if (ex.getStatusCode().value() == 504 && batch.size() > 1) {
                logger.warn("[PERF] exp={} 504 en lote de {} mutantes; se divide en dos", experimentId, batch.size());
                int mid = batch.size() / 2;
                executeBatchWithSplit(experimentId, project, cycle, new ArrayList<>(batch.subList(0, mid)), originalResults, inputQubits, outputQubits, dto);
                executeBatchWithSplit(experimentId, project, cycle, new ArrayList<>(batch.subList(mid, batch.size())), originalResults, inputQubits, outputQubits, dto);
            } else {
                throw ex;
            }
        }
    }

    private int calculateChunkSize(String inputQubits, long originalExecMs) {
        int inputCount = 0;
        if (inputQubits != null && !inputQubits.trim().isEmpty()) {
            for (String part : inputQubits.split(",")) {
                if (!part.trim().isEmpty()) {
                    inputCount++;
                }
            }
        }
        int variants = 1 << Math.min(inputCount, 20);
        int byInputs = Math.max(1, Math.min(this.batchSize, this.maxProgramsPerRequest / variants));
        long perProgramMs = Math.max(1L, originalExecMs);
        int rounds = (int) Math.max(1L, (this.targetRequestSeconds * 1000L) / perProgramMs);
        int byCost = Math.max(1, (Math.max(1, this.runnerWorkers) * rounds) / variants);
        // cada petición ≈ ceil(chunk·variantes / W) rondas de ≈ originalExecMs
        return Math.min(byInputs, byCost);
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

        List<List<Map<String, Object>>> stratResults = null;
        int attempts = 0;
        long tStratStart = System.nanoTime();
        while (attempts < 2) {
            try {
                attempts++;
                stratResults = (List<List<Map<String, Object>>>) (List<?>) runQiskitexecJob("executeWithStrategy", stratBody);
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
    public List<ExperimentListItemDTO> getUserExperiments(String userId) {
        return experimentRepository.findListItemsByUserId(userId);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ExperimentDetailDTO getExperimentDetails(String experimentId, String userId) throws Exception {
        Experiment e = experimentRepository.findByIdWithExecConfig(experimentId)
                .orElseThrow(() -> new Exception("Experiment not found: " + experimentId));
        if (e.getUser() != null && !e.getUser().getId().equals(userId)) {
            throw new Exception("Unauthorized to view this experiment");
        }

        ExperimentDetailDTO dto = new ExperimentDetailDTO();
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

        List<Object[]> headers = mutantCycleRepository.findCycleHeadersByExperimentId(experimentId);
        List<Object[]> counts = mutantCycleRepository.countMutantStatesByExperimentId(experimentId);

        Map<String, Object[]> countsMap = new HashMap<>();
        if (counts != null) {
            for (Object[] row : counts) {
                if (row != null && row.length >= 6 && row[0] != null) {
                    countsMap.put(((Number) row[0]).intValue() + "|" + row[1], row);
                }
            }
        }

        List<CycleStatsDTO> cycles = new ArrayList<>();
        if (headers != null) {
            for (Object[] h : headers) {
                CycleStatsDTO cDto = new CycleStatsDTO();
                int id = ((Number) h[0]).intValue();
                String projectId = (String) h[1];
                String projectName = (String) h[2];
                String name = h[3] != null ? (String) h[3] : "Cycle " + id;

                cDto.setId(id);
                cDto.setProjectId(projectId);
                cDto.setProjectName(projectName);
                cDto.setName(name);

                Object[] c = countsMap.get(id + "|" + projectId);
                long totalMutants = 0;
                long killed = 0;
                long zombie = 0;
                long alive = 0;
                if (c != null) {
                    totalMutants = c[2] != null ? ((Number) c[2]).longValue() : 0;
                    killed = c[3] != null ? ((Number) c[3]).longValue() : 0;
                    zombie = c[4] != null ? ((Number) c[4]).longValue() : 0;
                    alive = c[5] != null ? ((Number) c[5]).longValue() : 0;
                }
                long notExecuted = totalMutants - killed - zombie - alive;
                cDto.setTotalMutants(totalMutants);
                cDto.setKilledCount(killed);
                cDto.setZombieCount(zombie);
                cDto.setAliveCount(alive);
                cDto.setNotExecutedCount(notExecuted >= 0 ? notExecuted : 0);

                long totalWithResults = killed + zombie + alive;
                if (totalWithResults > 0) {
                    cDto.setMutationScore(Math.round((killed * 100.0 / totalWithResults) * 100.0) / 100.0);
                } else {
                    cDto.setMutationScore(null);
                }

                cycles.add(cDto);
            }
        }
        dto.setCycles(cycles);
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
