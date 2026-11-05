package edu.uclm.reper.services;

import java.io.InputStream;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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
import org.springframework.data.domain.PageRequest;
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
import edu.uclm.reper.dto.ExecParams;
import edu.uclm.reper.dto.ExperimentProgressDTO;
import edu.uclm.reper.dto.CycleStatsDTO;
import edu.uclm.reper.dto.ExperimentDetailDTO;
import edu.uclm.reper.dto.ExperimentListItemDTO;
import edu.uclm.reper.dto.NoiseConfigDTO;
import edu.uclm.reper.dto.MergePreviewDTO;
import edu.uclm.reper.dto.ResumeExperimentDTO;
import edu.uclm.reper.dto.ResumePreviewDTO;
import edu.uclm.reper.repository.ExecConfigRepository;
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
import edu.uclm.reper.repository.MutantRepository;
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
    private MutantRepository mutantRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ExperimentNotificationService notificationService;

    @Autowired
    private ExperimentRunRegistry runRegistry;

    @Autowired
    private ExecConfigRepository execConfigRepository;

    @Autowired
    private org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    @Autowired
    private BulkDeleteService bulkDeleteService;

    @Autowired
    private BackgroundJobService backgroundJobService;

    @Autowired
    private DeletionRegistry deletionRegistry;

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

    private String serializeDefinition(CreateExperimentDTO dto) {
        if (dto == null) return null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            String origEmail = dto.getEmail();
            dto.setEmail(null);
            String json = mapper.writeValueAsString(dto);
            dto.setEmail(origEmail);
            return json;
        } catch (Exception e) {
            logger.warn("Could not serialize experiment definition", e);
            return null;
        }
    }

    public Experiment createExperiment(CreateExperimentDTO dto, String userId) throws Exception {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new Exception("User not found: " + userId);
        }

        validateNoiseConfigs(dto.effectiveNoiseConfigs());

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
        experiment.setDefinitionJson(serializeDefinition(dto));

        experiment = experimentRepository.save(experiment);

        final String experimentId = experiment.getId();
        final String userEmail = userId;

        CompletableFuture.runAsync(() -> {
            runExperimentAsync(experimentId, dto, userEmail);
        }).exceptionally(t -> {
            logger.error("Tarea de experimento {} terminó con error no capturado", experimentId, t);
            return null;
        });

        return experiment;
    }

    private void runExperimentAsync(String experimentId, CreateExperimentDTO dto, String userEmail) {
        long startTime = System.currentTimeMillis();
        runRegistry.register(experimentId);
        List<NoiseConfigDTO> effNoise = dto.effectiveNoiseConfigs();
        if (effNoise != null && !effNoise.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (NoiseConfigDTO nc : effNoise) {
                sb.append(" ").append(nc.getAxis() != null ? nc.getAxis().trim().toUpperCase() : "?")
                        .append("/").append(nc.getAngle());
            }
            logger.info("exp={} noiseConfigs count={}:{}", experimentId, effNoise.size(), sb.toString());
        }
        try {
            List<CircuitConfigDTO> configs = dto.getCircuitConfigs();
            if (configs == null || configs.isEmpty()) {
                markExperimentFinished(experimentId, "COMPLETED", null, startTime);
                return;
            }

            for (int i = 0; i < configs.size(); i++) {
                CircuitConfigDTO cc = configs.get(i);
                long tCircuitStart = System.nanoTime();
                processSingleCircuit(experimentId, cc, dto, i);
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
        } catch (Throwable t) {
            logger.error("Error grave ejecutando experimento {}: {}", experimentId, t.toString(), t);
            // Con la memoria agotada guardar en BD tambien puede fallar: no debe tapar el log
            try {
                String message = t instanceof OutOfMemoryError ? "reper se quedó sin memoria (OutOfMemoryError)"
                        : "Error interno: " + t;
                markExperimentFinished(experimentId, "FAILED", message, startTime);
                Optional<Experiment> expOpt = experimentRepository.findByIdWithDetails(experimentId);
                if (expOpt.isPresent()) {
                    notificationService.sendExperimentFailedEmail(expOpt.get(), userEmail, message);
                }
            } catch (Throwable ignored) {
            }
        } finally {
            runRegistry.unregister(experimentId);
        }
    }

    /**
     * Al arrancar la aplicación, marca como FAILED cualquier experimento que quedó
     * en estado RUNNING (porque reper se reinició durante su ejecución).
     */
    @EventListener(ApplicationReadyEvent.class)
    public void markInterruptedExperimentsOnStartup() {
        List<Experiment> running = experimentRepository.findByStatus("RUNNING");
        if (running.isEmpty()) {
            return;
        }
        logger.warn("Encontrados {} experimentos en estado RUNNING al arrancar — marcando como FAILED", running.size());
        long now = System.currentTimeMillis();
        for (Experiment exp : running) {
            String msg = "Interrumpido: reper se reinició durante la ejecución";
            markExperimentFinished(exp.getId(), "FAILED", msg, now);
            logger.warn("exp={} marcado FAILED por reinicio de reper", exp.getId());
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public void processSingleCircuit(String experimentId, CircuitConfigDTO cc, CreateExperimentDTO dto)
            throws Exception {
        processSingleCircuit(experimentId, cc, dto, 0);
    }

    @org.springframework.transaction.annotation.Transactional
    public void processSingleCircuit(String experimentId, CircuitConfigDTO cc, CreateExperimentDTO dto, int circuitIndex)
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

        // 1. Create the empty MutantCycle
        long tCycleStart = System.nanoTime();
        MutantCycle newCycle = createEmptyMutantCycle(project, experimentId, dto, circuitIndex, inputQubits, outputQubits);
        long createCycleMs = (System.nanoTime() - tCycleStart) / 1_000_000;

        // 2. Generate mutants in qumugen and save them in batches
        long tGenStart = System.nanoTime();
        int mutantCount = generateAndSaveMutants(qProgram, dto.getOperatorNames(), inputQubits,
                dto.effectiveNoiseConfigs(), newCycle, experimentId);
        long generateMs = (System.nanoTime() - tGenStart) / 1_000_000;

        newCycle.setGenerationCompleted(true);
        mutantCycleRepository.save(newCycle);

        logger.info("[PERF] exp={} circuit={} generateMs={} createCycleMs={} mutants={}",
                experimentId, cc.getProjectId(), generateMs, createCycleMs, mutantCount);

        // 3. Execute original circuit & mutants in qiskitexec
        ExecParams params = ExecParams.fromDto(dto, inputQubits, outputQubits);
        executeCycleInQiskitExec(experimentId, project, newCycle, params, false);
    }

    private MutantCycle createEmptyMutantCycle(Project project, String experimentId, CreateExperimentDTO dto,
            int circuitIndex, String inputQubits, String outputQubits) {
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
        newCycle.setCircuitIndex(circuitIndex);
        newCycle.setGenerationCompleted(false);

        long tExpStart = System.nanoTime();
        newCycle.setExperiment(experimentRepository.getReferenceById(experimentId));
        long findExpMs = (System.nanoTime() - tExpStart) / 1_000_000;

        ExecConfig cycleExecConfig = new ExecConfig(new Date(), dto.getMachine(), dto.getExecAlgorithm());
        cycleExecConfig.setId(UUID.randomUUID().toString());
        cycleExecConfig.setToleratedError(dto.getToleratedError());
        cycleExecConfig.setZombieError(dto.getZombieError());
        cycleExecConfig.setShots(dto.getShots());
        cycleExecConfig.setInputQubits(inputQubits);
        cycleExecConfig.setOutputQubits(outputQubits);
        cycleExecConfig.setMutantCycle(newCycle);
        newCycle.setExecConfig(cycleExecConfig);

        newCycle.setMutants(new ArrayList<>());

        long tSaveStart = System.nanoTime();
        MutantCycle saved = mutantCycleRepository.save(newCycle);
        long saveMs = (System.nanoTime() - tSaveStart) / 1_000_000;

        logger.info("[PERF] createCycle findCyclesMs={} findExpMs={} saveMs={}", findCyclesMs, findExpMs, saveMs);

        return saved;
    }

    private Mutant buildMutant(JsonNode data, int index, MutantCycle cycle, Map<String, Operator> opsByName,
            QProgram qProgram) {
        Mutant m = new Mutant();
        m.setIndex(index);
        m.setMutantCycle(cycle);

        JsonNode opNode = data.path("mutationOperator");
        if (opNode.isTextual()) {
            Operator op = opsByName.get(opNode.asText());
            if (op != null) {
                m.setOperator(op);
            }
        }

        if (data.path("mutatedColumn").isNumber()) {
            m.setColumn(data.get("mutatedColumn").asInt());
        }
        if (data.path("mutatedRow").isNumber()) {
            m.setRow(data.get("mutatedRow").asInt());
        }

        if (!data.path("oracleName").isMissingNode() && !data.get("oracleName").isNull())
            m.setOracleName(data.get("oracleName").asText());
        if (!data.path("mutationDetail").isMissingNode() && !data.get("mutationDetail").isNull())
            m.setMutationDetail(data.get("mutationDetail").asText());

        JsonNode circuit = QumugenMutantReader.circuitOf(data);
        if (circuit != null && qProgram != null) {
            QProgram mq = new QProgram();
            mq.setId(UUID.randomUUID().toString());
            mq.setQubits(qProgram.getQubits());
            QCircuit qc = new QCircuit();
            qc.setId(UUID.randomUUID().toString());
            qc.setCode(circuit);
            qc.setQbits(qProgram.getQubits());
            mq.setQuirkCircuit(qc);
            m.setQuirkCircuit(mq);
        }
        return m;
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

    private int generateAndSaveMutants(QProgram qProgram, List<String> operatorNames, String inputQubits,
            List<NoiseConfigDTO> noiseConfigs, MutantCycle cycle, String experimentId) throws Exception {
        try {
            Map<String, Operator> opsByName = operatorRepository.findAll().stream()
                    .filter(o -> o.getName() != null)
                    .collect(Collectors.toMap(Operator::getName, o -> o, (a, b) -> a));

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
            if (noiseConfigs != null && !noiseConfigs.isEmpty()) {
                body.put("noiseConfigs", noiseConfigs.stream()
                        .map(nc -> Map.of("axis", nc.getAxis().trim().toUpperCase(), "angle", nc.getAngle()))
                        .toList());
            }

            HttpHeaders headers = createJsonHeaders();
            headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip");
            Path tmp = Files.createTempFile("qumugen-mutants-", ".json");
            try {
                // Primero se descarga la respuesta entera (sin pausas de lectura) y despues se guarda en BD
                long t0 = System.nanoTime();
                Boolean gzip = restTemplate.execute(qumugenUrl + "/qumugen/generateQuirkMutants", HttpMethod.PUT,
                        restTemplate.httpEntityCallback(new HttpEntity<>(body, headers)),
                        response -> {
                            QumugenMutantReader.download(response.getBody(), tmp);
                            return "gzip".equalsIgnoreCase(
                                    response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING));
                        });
                logger.info("exp={} respuesta de qumugen descargada: {} MB en {} ms (gzip={})", experimentId,
                        Files.size(tmp) / (1024 * 1024), (System.nanoTime() - t0) / 1_000_000, gzip);

                List<Mutant> lote = new ArrayList<>(200);
                int[] index = { 0 };
                try (InputStream in = QumugenMutantReader.open(tmp, Boolean.TRUE.equals(gzip))) {
                    QumugenMutantReader.forEachMutant(in, objectMapper, data -> {
                        lote.add(buildMutant(data, index[0]++, cycle, opsByName, qProgram));
                        if (lote.size() == 200) {
                            mutantRepository.saveAll(lote);
                            lote.clear();
                        }
                        if (index[0] % 1000 == 0) {
                            logger.info("exp={} mutantes guardados={}", experimentId, index[0]);
                        }
                    });
                }
                if (!lote.isEmpty()) {
                    mutantRepository.saveAll(lote);
                }
                return index[0];
            } finally {
                try {
                    Files.deleteIfExists(tmp);
                } catch (java.io.IOException e) {
                    logger.warn("No se pudo borrar el fichero temporal {}: {}", tmp, e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Error calling qumugen generateQuirkMutants: {}", e.getMessage(), e);
            String message = "Error al generar mutantes en qumugen: " + e.getMessage();
            Throwable root = org.springframework.core.NestedExceptionUtils.getMostSpecificCause(e);
            if (root != e) {
                String rootMessage = root.getMessage() != null ? root.getMessage().split("\\R", 2)[0] : "";
                String cause = " | causa: " + root.getClass().getSimpleName() + ": " + rootMessage;
                message += cause.length() > 300 ? cause.substring(0, 300) : cause;
            }
            throw new Exception(message, e);
        }
    }

    private void validateNoiseConfigs(List<NoiseConfigDTO> list) throws Exception {
        if (list == null) {
            return;
        }
        if (list.size() > 6) {
            throw new Exception("Invalid noise configuration: at most 6 rotations are allowed");
        }
        for (int i = 0; i < list.size(); i++) {
            NoiseConfigDTO nc = list.get(i);
            validateNoiseConfig(nc);
            for (int j = 0; j < i; j++) {
                NoiseConfigDTO prev = list.get(j);
                if (nc.getAxis() != null && prev.getAxis() != null
                        && nc.getAxis().trim().equalsIgnoreCase(prev.getAxis().trim())
                        && nc.getAngle() != null && prev.getAngle() != null
                        && Math.abs(nc.getAngle() - prev.getAngle()) < 1e-9) {
                    throw new Exception("Invalid noise configuration: duplicated rotation "
                            + nc.getAxis().trim().toUpperCase() + "(" + nc.getAngle() + ")");
                }
            }
        }
    }

    private void validateNoiseConfig(NoiseConfigDTO nc) throws Exception {
        if (nc == null) {
            return;
        }
        String axis = nc.getAxis();
        if (axis == null || (!axis.trim().equalsIgnoreCase("X") && !axis.trim().equalsIgnoreCase("Y")
                && !axis.trim().equalsIgnoreCase("Z"))) {
            throw new Exception("Invalid noise configuration: unknown axis '" + axis + "'");
        }
        Double angle = nc.getAngle();
        if (angle == null || !Double.isFinite(angle) || angle <= 0 || angle > 2 * Math.PI + 1e-9) {
            throw new Exception("Invalid noise configuration: angle must be in (0, 2pi] radians, got " + angle);
        }
    }

    public void executeCycleInQiskitExec(String experimentId, Project project, MutantCycle cycle,
            ExecParams params, boolean onlyPending) throws Exception {
        try {
            long tOrigCodeStart = System.nanoTime();
            String originalWholeCode = getOriginalQiskitCode(project, params.outputQubits(), params.shots());
            long originalCodeMs = (System.nanoTime() - tOrigCodeStart) / 1_000_000;

            long tOrigExecStart = System.nanoTime();
            List<?> originalResults = executeOriginalInQiskitExec(project, originalWholeCode, params.inputQubits(),
                    params.outputQubits(), params.execAlgorithm());
            long originalExecMs = (System.nanoTime() - tOrigExecStart) / 1_000_000;

            int chunkSize = calculateChunkSize(params.inputQubits(), originalExecMs);
            logger.info("[PERF] exp={} circuit={} originalCodeMs={} originalExecMs={} chunkSize={}",
                    experimentId, project.getId(), originalCodeMs, originalExecMs, chunkSize);

            int fromIndex = 0;
            while (true) {
                List<Mutant> batch = onlyPending
                        ? mutantRepository.findPendingCircuitsPage(project.getId(), cycle.getId(), fromIndex,
                                PageRequest.of(0, chunkSize))
                        : mutantRepository.findCircuitsPage(project.getId(), cycle.getId(), fromIndex,
                                PageRequest.of(0, chunkSize));
                if (batch.isEmpty()) {
                    break;
                }
                executeBatchWithSplit(experimentId, project, cycle, new ArrayList<>(batch), originalResults, params);
                fromIndex = batch.get(batch.size() - 1).getIndex() + 1;
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
                        throw HttpServerErrorException.create(HttpStatus.BAD_GATEWAY,
                                "No se pudo consultar el job " + jobId,
                                new HttpHeaders(), new byte[0], StandardCharsets.UTF_8);
                    }
                    continue;
                } catch (HttpStatusCodeException ex) {
                    int sc = ex.getStatusCode().value();
                    if (sc == 404) {
                        throw HttpServerErrorException.create(HttpStatus.BAD_GATEWAY,
                                "qiskitexec perdió el job " + jobId,
                                new HttpHeaders(), new byte[0], StandardCharsets.UTF_8);
                    } else if (sc == 502 || sc == 503 || sc == 504) {
                        consecutiveErrors++;
                        logger.warn("[PERF] fallo sondeando job {} ({}): {}", jobId, consecutiveErrors,
                                ex.getMessage());
                        if (consecutiveErrors >= 10) {
                            throw HttpServerErrorException.create(HttpStatus.BAD_GATEWAY,
                                    "No se pudo consultar el job " + jobId,
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
                    List<?> r = pollRestTemplate
                            .getForEntity(qiskitexecUrl + "/strategy/jobs/" + jobId + "/result", List.class).getBody();
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
                    throw HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Job " + jobId + " cancelado en qiskitexec",
                            new HttpHeaders(), new byte[0], StandardCharsets.UTF_8);
                }

                if (System.currentTimeMillis() - start > readTimeoutSeconds * 1000L) {
                    throw HttpServerErrorException.create(HttpStatus.GATEWAY_TIMEOUT,
                            "Job " + jobId + " superó " + readTimeoutSeconds + " s",
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
            List<?> originalResults, ExecParams params)
            throws Exception {
        try {
            executeAndProcessMutantBatch(experimentId, project, cycle, batch, originalResults, params);
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            if (ex.getStatusCode().value() == 504 && batch.size() > 1) {
                logger.warn("[PERF] exp={} 504 en lote de {} mutantes; se divide en dos", experimentId, batch.size());
                int mid = batch.size() / 2;
                executeBatchWithSplit(experimentId, project, cycle, new ArrayList<>(batch.subList(0, mid)),
                        originalResults, params);
                executeBatchWithSplit(experimentId, project, cycle, new ArrayList<>(batch.subList(mid, batch.size())),
                        originalResults, params);
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

    public record FormattedBatch(List<Mutant> sentMutants, List<Map<String, Object>> formattedList) {}

    public FormattedBatch formatMutantBatch(Project project, List<Mutant> batch) {
        List<Map<String, Object>> batchFormatted = new ArrayList<>();
        List<Mutant> sentMutants = new ArrayList<>();
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
                sentMutants.add(m);
            }
        }
        return new FormattedBatch(sentMutants, batchFormatted);
    }

    private void executeAndProcessMutantBatch(String experimentId, Project project, MutantCycle cycle,
            List<Mutant> batch,
            List<?> originalResults, ExecParams params)
            throws Exception {
        FormattedBatch formatted = formatMutantBatch(project, batch);
        List<Mutant> sentMutants = formatted.sentMutants();
        List<Map<String, Object>> batchFormatted = formatted.formattedList();

        if (sentMutants.isEmpty()) {
            return;
        }

        Map<String, Object> multCodeBody = new HashMap<>();
        multCodeBody.put("mutants", batchFormatted);
        multCodeBody.put("outputQubits", params.outputQubits());

        HttpEntity<Map<String, Object>> multCodeReq = new HttpEntity<>(multCodeBody, createJsonHeaders());
        String multCodeUrl = qumugenUrl + "/qumugen/getMultipleQiskitCode?shots=" + params.shots()
                + "&qiskitTemplate=qiskitTemplateWindows.txt";
        long tCodeGenStart = System.nanoTime();
        ResponseEntity<List> multCodeResp = restTemplate.exchange(multCodeUrl, HttpMethod.PUT, multCodeReq, List.class);
        long codeGenMs = (System.nanoTime() - tCodeGenStart) / 1_000_000;
        List<?> mutantCodes = multCodeResp.getBody();

        // Execute batch with strategy
        Map<String, Object> stratBody = new HashMap<>();
        stratBody.put("mutants", mutantCodes);
        stratBody.put("originalResults", originalResults);
        stratBody.put("algorithm", params.execAlgorithm());
        stratBody.put("toleratedError", params.toleratedError());
        stratBody.put("zombieError", params.zombieError());
        stratBody.put("inputQubits", params.inputQubits());
        stratBody.put("outputQubits", params.outputQubits());
        stratBody.put("qubits", project.getQProgram().getQubits());
        stratBody.put("reduceTable", false);

        List<List<Map<String, Object>>> stratResults = null;
        int attempts = 0;
        long tStratStart = System.nanoTime();
        while (attempts < 2) {
            try {
                attempts++;
                stratResults = (List<List<Map<String, Object>>>) (List<?>) runQiskitexecJob("executeWithStrategy",
                        stratBody);
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
                logger.warn("Advertencia: error de conexión al conectar con qiskitexec ({}). Reintentando...",
                        ex.getMessage());
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
            processAndSaveBatchResults(project.getId(), cycle.getId(), sentMutants, stratResults);
            saveMs = (System.nanoTime() - tSaveStart) / 1_000_000;
        }

        List<Integer> mutantIndices = sentMutants.stream().map(Mutant::getIndex).collect(Collectors.toList());
        logger.info("[PERF] exp={} mutants={} codeGenMs={} strategyMs={} attempts={} saveMs={}",
                experimentId, mutantIndices, codeGenMs, strategyMs, attempts, saveMs);
    }


    void processAndSaveBatchResults(String projectId, int cycleId, List<Mutant> batch,
            List<List<Map<String, Object>>> stratResults) throws Exception {
        for (int mIdx = 0; mIdx < Math.min(batch.size(), stratResults.size()); mIdx++) {
            Mutant mutant = batch.get(mIdx);
            List<Map<String, Object>> resultsList = stratResults.get(mIdx);
            // El mutante viene de BD sin sesion: su coleccion perezosa no se puede tocar, se sustituye por una lista nueva
            mutant.setMutantResults(new ArrayList<>());

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
        projectService.saveExperimentBatchResults(batch);
    }

    private static final java.time.format.DateTimeFormatter ERROR_DATE_FORMAT =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(java.time.ZoneId.systemDefault());

    private void markExperimentFinished(String experimentId, String status, String errorMessage, long startTime) {
        Optional<Experiment> expOpt = experimentRepository.findByIdWithDetails(experimentId);
        if (expOpt.isPresent()) {
            Experiment exp = expOpt.get();
            exp.setStatus(status);
            exp.setFinishedAt(new Date());
            long prevDuration = exp.getDurationSeconds() != null ? exp.getDurationSeconds() : 0L;
            exp.setDurationSeconds(prevDuration + (System.currentTimeMillis() - startTime) / 1000);
            if (errorMessage != null) {
                if (errorMessage.length() > 500) {
                    errorMessage = errorMessage.substring(0, 500);
                }
                exp.setErrorMessage(errorMessage);
                if ("FAILED".equals(status)) {
                    String timestamp = ERROR_DATE_FORMAT.format(java.time.Instant.now());
                    String line = "[" + timestamp + "] " + errorMessage;
                    String history = exp.getErrorHistory();
                    if (history == null || history.isBlank()) {
                        exp.setErrorHistory(line);
                    } else {
                        exp.setErrorHistory(history + "\n" + line);
                    }
                }
            }
            experimentRepository.save(exp);
        }
    }


    // ======================================================================
    // Reanudar experimentos
    // ======================================================================

    public static class ExperimentConflictException extends Exception {
        public ExperimentConflictException(String message) {
            super(message);
        }
    }

    public static class MissingQubitsException extends Exception {
        private final List<Map<String, Object>> missing;

        public MissingQubitsException(List<Map<String, Object>> missing) {
            super("Faltan los qubits de entrada/salida de " + missing.size() + " ciclo(s)");
            this.missing = missing;
        }

        public List<Map<String, Object>> getMissing() {
            return missing;
        }
    }

    private static final String GENERATION_ERROR_PREFIX = "Error al generar mutantes";

    /** Estado calculado de un ciclo de un experimento (para reanudar y fusionar). */
    static class CycleState {
        MutantCycle cycle;
        String projectId;
        long total;
        long pending;
        boolean generationComplete;
        String skipReason; // null si el ciclo se puede ejecutar

        boolean isPending() {
            return skipReason == null && pending > 0;
        }

        boolean isIncomplete() {
            return skipReason != null || pending > 0;
        }

        String key() {
            return projectId + "|" + cycle.getId();
        }
    }

    private Experiment loadOwned(String experimentId, String userId) throws Exception {
        Experiment exp = experimentRepository.findByIdWithExecConfig(experimentId)
                .orElseThrow(() -> new Exception("Experiment not found: " + experimentId));
        if (exp.getUser() != null && !exp.getUser().getId().equals(userId)) {
            throw new Exception("Unauthorized to access this experiment");
        }
        return exp;
    }

    List<CycleState> computeCycleStates(Experiment exp) {
        List<MutantCycle> cycles = mutantCycleRepository.findByExperimentIdWithExecConfig(exp.getId());
        List<CycleState> result = new ArrayList<>();
        if (cycles == null) {
            return result;
        }
        for (int i = 0; i < cycles.size(); i++) {
            MutantCycle mc = cycles.get(i);
            CycleState st = new CycleState();
            st.cycle = mc;
            st.projectId = mc.getProject().getId();
            st.total = mutantRepository.countTotalByCycle(st.projectId, mc.getId());
            st.pending = mutantRepository.countPendingByCycle(st.projectId, mc.getId());
            Boolean gc = mc.getGenerationCompleted();
            if (gc != null) {
                st.generationComplete = gc;
            } else if (st.total > 0) {
                // Ciclo antiguo con mutantes: se considera generado
                st.generationComplete = true;
            } else {
                boolean last = i == cycles.size() - 1;
                String msg = exp.getErrorMessage();
                st.generationComplete = !(last && msg != null && msg.startsWith(GENERATION_ERROR_PREFIX));
            }
            if (!st.generationComplete) {
                st.skipReason = "generación incompleta";
            } else if (st.total == 0) {
                st.skipReason = "sin mutantes";
            }
            result.add(st);
        }
        return result;
    }

    private CreateExperimentDTO parseDefinition(Experiment exp) {
        String json = exp.getDefinitionJson();
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper()
                    .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(json, CreateExperimentDTO.class);
        } catch (Exception e) {
            logger.warn("exp={} no se pudo leer definitionJson: {}", exp.getId(), e.getMessage());
            return null;
        }
    }

    /** Índices de circuitoConfigs de la definición que aún no tienen ciclo con ese circuitIndex. */
    List<Integer> remainingCircuitIndices(Experiment exp, List<CycleState> states) {
        List<Integer> remaining = new ArrayList<>();
        CreateExperimentDTO def = parseDefinition(exp);
        if (def == null || def.getCircuitConfigs() == null) {
            return remaining;
        }
        java.util.Set<Integer> done = new java.util.HashSet<>();
        for (CycleState st : states) {
            if (st.cycle.getCircuitIndex() != null) {
                done.add(st.cycle.getCircuitIndex());
            }
        }
        for (int i = 0; i < def.getCircuitConfigs().size(); i++) {
            if (!done.contains(i)) {
                remaining.add(i);
            }
        }
        return remaining;
    }

    boolean isResumableStatus(Experiment exp) {
        return "FAILED".equals(exp.getStatus())
                || ("RUNNING".equals(exp.getStatus()) && !runRegistry.isRunning(exp.getId()));
    }

    ResumePreviewDTO buildResumePreview(Experiment exp, List<CycleState> states) {
        ResumePreviewDTO dto = new ResumePreviewDTO();
        dto.setResumable(isResumableStatus(exp));
        if (!dto.isResumable()) {
            dto.setReason("RUNNING".equals(exp.getStatus())
                    ? "El experimento se está ejecutando"
                    : "Solo se pueden reanudar experimentos fallidos o interrumpidos (estado actual: "
                            + exp.getStatus() + ")");
        }
        dto.setHasDefinition(parseDefinition(exp) != null);
        dto.setRemainingCircuits(remainingCircuitIndices(exp, states).size());
        for (CycleState st : states) {
            String name = st.cycle.getName() != null ? st.cycle.getName() : "Cycle " + st.cycle.getId();
            if (st.skipReason != null) {
                ResumePreviewDTO.SkippedCycle sk = new ResumePreviewDTO.SkippedCycle();
                sk.setProjectId(st.projectId);
                sk.setCycleId(st.cycle.getId());
                sk.setName(name);
                sk.setReason(st.skipReason);
                dto.getSkippedCycles().add(sk);
            } else if (st.pending > 0) {
                ExecConfig ec = st.cycle.getExecConfig();
                ResumePreviewDTO.PendingCycle pc = new ResumePreviewDTO.PendingCycle();
                pc.setProjectId(st.projectId);
                pc.setCycleId(st.cycle.getId());
                pc.setName(name);
                pc.setPendingMutants(st.pending);
                pc.setInputQubits(ec != null ? ec.getInputQubits() : null);
                pc.setOutputQubits(ec != null ? ec.getOutputQubits() : null);
                pc.setNeedsQubits(pc.getInputQubits() == null || pc.getOutputQubits() == null);
                dto.getPendingCycles().add(pc);
            }
        }
        return dto;
    }

    public ResumePreviewDTO getResumePreview(String experimentId, String userId) throws Exception {
        Experiment exp = loadOwned(experimentId, userId);
        return buildResumePreview(exp, computeCycleStates(exp));
    }

    /** Punto de lanzamiento asíncrono (sobrescribible en tests). */
    void startAsync(Runnable task, String experimentId) {
        CompletableFuture.runAsync(task).exceptionally(t -> {
            logger.error("Tarea de reanudación {} terminó con error no capturado", experimentId, t);
            return null;
        });
    }

    public Map<String, Object> resumeExperiment(ResumeExperimentDTO req, String userId) throws Exception {
        Experiment exp = loadOwned(req.getExperimentId(), userId);
        if (runRegistry.isRunning(exp.getId())) {
            throw new ExperimentConflictException("El experimento ya está en ejecución");
        }
        List<CycleState> states = computeCycleStates(exp);
        ResumePreviewDTO preview = buildResumePreview(exp, states);
        if (!preview.isResumable()) {
            throw new Exception(preview.getReason());
        }

        Map<String, ResumeExperimentDTO.CycleQubits> provided = new HashMap<>();
        if (req.getCycles() != null) {
            for (ResumeExperimentDTO.CycleQubits q : req.getCycles()) {
                provided.put(q.getProjectId() + "|" + q.getCycleId(), q);
            }
        }

        // Qubits resueltos por ciclo pendiente
        List<Map<String, Object>> missing = new ArrayList<>();
        Map<String, String[]> qubits = new HashMap<>();
        for (CycleState st : states) {
            if (!st.isPending()) {
                continue;
            }
            ExecConfig ec = st.cycle.getExecConfig();
            ResumeExperimentDTO.CycleQubits q = provided.get(st.key());
            String in = q != null && q.getInputQubits() != null ? q.getInputQubits().trim()
                    : (ec != null ? ec.getInputQubits() : null);
            String out = q != null && q.getOutputQubits() != null ? q.getOutputQubits().trim()
                    : (ec != null ? ec.getOutputQubits() : null);
            if (in == null || out == null) {
                Map<String, Object> m = new HashMap<>();
                m.put("projectId", st.projectId);
                m.put("cycleId", st.cycle.getId());
                m.put("name", st.cycle.getName());
                missing.add(m);
            } else {
                qubits.put(st.key(), new String[] { in, out });
            }
        }
        if (!missing.isEmpty()) {
            throw new MissingQubitsException(missing);
        }

        if (!runRegistry.tryRegister(exp.getId())) {
            throw new ExperimentConflictException("El experimento ya está en ejecución");
        }
        List<ExecParams> pendingParams = new ArrayList<>();
        List<CycleState> pendingStates = new ArrayList<>();
        List<Integer> remaining;
        try {
            // Guardar los qubits en el ExecConfig del ciclo
            for (CycleState st : states) {
                if (!st.isPending()) {
                    continue;
                }
                String[] q = qubits.get(st.key());
                ExecConfig ec = st.cycle.getExecConfig();
                if (ec == null) {
                    ec = new ExecConfig(new Date(), exp.getExecConfig() != null ? exp.getExecConfig().getMachine() : "AerSimulator",
                            exp.getExecConfig() != null ? exp.getExecConfig().getExecAlgorithm() : "AllAgainstAll");
                    ec.setId(UUID.randomUUID().toString());
                    ec.setMutantCycle(st.cycle);
                    st.cycle.setExecConfig(ec);
                }
                if (!q[0].equals(ec.getInputQubits()) || !q[1].equals(ec.getOutputQubits())) {
                    ec.setInputQubits(q[0]);
                    ec.setOutputQubits(q[1]);
                    execConfigRepository.save(ec);
                }
                pendingStates.add(st);
                pendingParams.add(ExecParams.fromExecConfig(ec, q[0], q[1]));
            }
            remaining = remainingCircuitIndices(exp, states);

            // Actualizar el experimento
            String history = exp.getErrorHistory() != null ? exp.getErrorHistory() : "";
            String err = exp.getErrorMessage();
            if (err != null && !err.isBlank() && !history.contains(err)) {
                history = appendLine(history, "[" + ERROR_DATE_FORMAT.format(java.time.Instant.now()) + "] " + err);
            }
            for (ResumePreviewDTO.SkippedCycle sk : preview.getSkippedCycles()) {
                history = appendLine(history, "Ciclo " + sk.getName() + " saltado: " + sk.getReason());
            }
            exp.setErrorHistory(history.isBlank() ? null : history);
            exp.setErrorMessage(null);
            exp.setStatus("RUNNING");
            exp.setFinishedAt(null);
            exp.setResumeCount((exp.getResumeCount() != null ? exp.getResumeCount() : 0) + 1);
            exp.setCompletedCircuits(states.size() - pendingStates.size());
            experimentRepository.save(exp);
        } catch (RuntimeException e) {
            runRegistry.unregister(exp.getId());
            throw e;
        }

        final String experimentId = exp.getId();
        final CreateExperimentDTO def = parseDefinition(exp);
        final List<Integer> remainingFinal = remaining;
        startAsync(() -> resumeExperimentAsync(experimentId, pendingStates, pendingParams, def, remainingFinal,
                userId), experimentId);

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Experiment resumed");
        res.put("id", experimentId);
        res.put("pendingCycles", pendingStates.size());
        res.put("skippedCycles", preview.getSkippedCycles().size());
        res.put("remainingCircuits", remaining.size());
        return res;
    }

    private static String appendLine(String history, String line) {
        return history == null || history.isBlank() ? line : history + "\n" + line;
    }

    void resumeExperimentAsync(String experimentId, List<CycleState> pendingStates, List<ExecParams> params,
            CreateExperimentDTO def, List<Integer> remainingIndices, String userEmail) {
        long startTime = System.currentTimeMillis();
        runRegistry.register(experimentId);
        try {
            for (int i = 0; i < pendingStates.size(); i++) {
                CycleState st = pendingStates.get(i);
                Project project = projectRepository.findByIdWithQProgram(st.projectId)
                        .or(() -> projectRepository.findById(st.projectId))
                        .orElseThrow(() -> new Exception("Project not found: " + st.projectId));
                long t0 = System.nanoTime();
                executeCycleInQiskitExec(experimentId, project, st.cycle, params.get(i), true);
                logger.info("[PERF] exp={} resume cycle={}/{} totalMs={}", experimentId, st.projectId,
                        st.cycle.getId(), (System.nanoTime() - t0) / 1_000_000);
                incrementCompleted(experimentId);
            }

            if (def != null && def.getCircuitConfigs() != null) {
                for (int idx : remainingIndices) {
                    if (idx < 0 || idx >= def.getCircuitConfigs().size()) {
                        continue;
                    }
                    processSingleCircuit(experimentId, def.getCircuitConfigs().get(idx), def, idx);
                    incrementCompleted(experimentId);
                }
            }

            markExperimentFinished(experimentId, "COMPLETED", null, startTime);
            Optional<Experiment> expOpt = experimentRepository.findByIdWithDetails(experimentId);
            if (expOpt.isPresent()) {
                notificationService.sendExperimentCompletedEmail(expOpt.get(), userEmail);
            }
        } catch (Exception e) {
            logger.error("Error resuming experiment {}: {}", experimentId, e.getMessage(), e);
            markExperimentFinished(experimentId, "FAILED", e.getMessage(), startTime);
            Optional<Experiment> expOpt = experimentRepository.findByIdWithDetails(experimentId);
            if (expOpt.isPresent()) {
                notificationService.sendExperimentFailedEmail(expOpt.get(), userEmail, e.getMessage());
            }
        } catch (Throwable t) {
            logger.error("Error grave reanudando experimento {}: {}", experimentId, t.toString(), t);
            try {
                String message = t instanceof OutOfMemoryError ? "reper se quedó sin memoria (OutOfMemoryError)"
                        : "Error interno: " + t;
                markExperimentFinished(experimentId, "FAILED", message, startTime);
                Optional<Experiment> expOpt = experimentRepository.findByIdWithDetails(experimentId);
                if (expOpt.isPresent()) {
                    notificationService.sendExperimentFailedEmail(expOpt.get(), userEmail, message);
                }
            } catch (Throwable ignored) {
            }
        } finally {
            runRegistry.unregister(experimentId);
        }
    }

    private void incrementCompleted(String experimentId) {
        Optional<Experiment> expOpt = experimentRepository.findById(experimentId);
        if (expOpt.isPresent()) {
            Experiment exp = expOpt.get();
            exp.setCompletedCircuits((exp.getCompletedCircuits() != null ? exp.getCompletedCircuits() : 0) + 1);
            experimentRepository.save(exp);
        }
    }


    // ======================================================================
    // Fusionar experimentos
    // ======================================================================

    static class MergePlan {
        Experiment survivor;
        Experiment deleted;
        List<CycleState> survivorStates;
        List<CycleState> deletedStates;
        List<CycleState> duplicates = new ArrayList<>();
        List<CycleState> survivorKept = new ArrayList<>();
        List<Integer> deletedRemaining;
        String resultingStatus;
    }

    MergePlan planMerge(List<String> ids, String userId) throws Exception {
        if (ids == null || ids.size() != 2 || ids.get(0) == null || ids.get(0).equals(ids.get(1))) {
            throw new Exception("Se necesitan dos experimentos distintos para fusionar");
        }
        Experiment e1 = loadOwned(ids.get(0), userId);
        Experiment e2 = loadOwned(ids.get(1), userId);
        for (Experiment e : List.of(e1, e2)) {
            if ("RUNNING".equals(e.getStatus()) || runRegistry.isRunning(e.getId())) {
                throw new Exception("No se pueden fusionar experimentos en ejecución: " + e.getName());
            }
        }
        boolean firstIsOlder = e1.getCreatedAt() == null || e2.getCreatedAt() == null
                ? e1.getId().compareTo(e2.getId()) <= 0
                : !e1.getCreatedAt().after(e2.getCreatedAt());
        MergePlan plan = new MergePlan();
        plan.survivor = firstIsOlder ? e1 : e2;
        plan.deleted = firstIsOlder ? e2 : e1;
        plan.survivorStates = computeCycleStates(plan.survivor);
        plan.deletedStates = computeCycleStates(plan.deleted);

        java.util.Set<String> deletedProjects = new java.util.HashSet<>();
        for (CycleState st : plan.deletedStates) {
            deletedProjects.add(st.projectId);
        }
        for (CycleState st : plan.survivorStates) {
            if (st.isIncomplete() && deletedProjects.contains(st.projectId)) {
                plan.duplicates.add(st);
            } else {
                plan.survivorKept.add(st);
            }
        }
        plan.deletedRemaining = remainingCircuitIndices(plan.deleted, plan.deletedStates);

        boolean anyIncomplete = false;
        for (CycleState st : plan.survivorKept) {
            anyIncomplete |= st.isIncomplete();
        }
        for (CycleState st : plan.deletedStates) {
            anyIncomplete |= st.isIncomplete();
        }
        plan.resultingStatus = "COMPLETED".equals(plan.deleted.getStatus()) && !anyIncomplete
                && plan.deletedRemaining.isEmpty() ? "COMPLETED" : "FAILED";
        return plan;
    }

    public MergePreviewDTO getMergePreview(List<String> ids, String userId) throws Exception {
        MergePlan plan = planMerge(ids, userId);
        MergePreviewDTO dto = new MergePreviewDTO();
        dto.setSurvivorId(plan.survivor.getId());
        dto.setSurvivorName(plan.survivor.getName());
        dto.setDeletedId(plan.deleted.getId());
        dto.setDeletedName(plan.deleted.getName());
        dto.setCyclesToMove(plan.deletedStates.size());
        for (CycleState st : plan.duplicates) {
            dto.getDuplicateCyclesToDelete()
                    .add(new MergePreviewDTO.DuplicateCycle(st.projectId, st.cycle.getId(), st.cycle.getName()));
        }
        dto.setResultingStatus(plan.resultingStatus);
        return dto;
    }

    public Map<String, Object> mergeExperiments(List<String> ids, String userId) throws Exception {
        MergePlan plan = planMerge(ids, userId);
        String aId = plan.survivor.getId();
        String bId = plan.deleted.getId();
        if (!runRegistry.tryRegister(aId)) {
            throw new ExperimentConflictException("El experimento " + plan.survivor.getName() + " está en ejecución");
        }
        if (!runRegistry.tryRegister(bId)) {
            runRegistry.unregister(aId);
            throw new ExperimentConflictException("El experimento " + plan.deleted.getName() + " está en ejecución");
        }
        try {
            transactionTemplate.execute(status -> {
                for (CycleState st : plan.duplicates) {
                    mutantCycleRepository.detachFromExperiment(st.projectId, st.cycle.getId());
                }
                mutantCycleRepository.moveCycles(plan.deleted, plan.survivor);

                Experiment a = experimentRepository.findByIdWithExecConfig(aId).orElseThrow();
                Experiment b = experimentRepository.findById(bId).orElseThrow();

                String history = a.getErrorHistory() != null ? a.getErrorHistory() : "";
                history = addErrorToHistory(history, a.getErrorMessage());
                if (b.getErrorHistory() != null && !b.getErrorHistory().isBlank()) {
                    history = appendLine(history, b.getErrorHistory());
                }
                history = addErrorToHistory(history, b.getErrorMessage());
                a.setErrorHistory(history.isBlank() ? null : history);

                String bDate = b.getCreatedAt() != null
                        ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(b.getCreatedAt())
                        : "?";
                String mergedEntry = b.getName() + " (" + b.getId() + ", " + bDate + ")";
                a.setMergedFrom(a.getMergedFrom() == null || a.getMergedFrom().isBlank() ? mergedEntry
                        : a.getMergedFrom() + "; " + mergedEntry);
                if (b.getDefinitionJson() != null) {
                    a.setDefinitionJson(b.getDefinitionJson());
                }
                a.setDurationSeconds((a.getDurationSeconds() != null ? a.getDurationSeconds() : 0L)
                        + (b.getDurationSeconds() != null ? b.getDurationSeconds() : 0L));
                if (b.getFinishedAt() != null) {
                    a.setFinishedAt(b.getFinishedAt());
                }
                int totalCycles = plan.survivorKept.size() + plan.deletedStates.size();
                a.setTotalCircuits(totalCycles + plan.deletedRemaining.size());
                int completed = 0;
                for (CycleState st : plan.survivorKept) {
                    completed += st.isIncomplete() ? 0 : 1;
                }
                for (CycleState st : plan.deletedStates) {
                    completed += st.isIncomplete() ? 0 : 1;
                }
                a.setCompletedCircuits(completed);
                a.setStatus(plan.resultingStatus);
                if ("COMPLETED".equals(plan.resultingStatus)) {
                    a.setErrorMessage(null);
                } else {
                    a.setErrorMessage(b.getErrorMessage() != null ? b.getErrorMessage()
                            : "Fusionado con ciclos incompletos");
                }
                experimentRepository.save(a);
                experimentRepository.delete(b);
                return null;
            });
        } finally {
            runRegistry.unregister(aId);
            runRegistry.unregister(bId);
        }

        List<String> jobIds = new ArrayList<>();
        for (CycleState st : plan.duplicates) {
            final String projectId = st.projectId;
            final int cycleId = st.cycle.getId();
            if (!deletionRegistry.registerCycle(projectId, cycleId)) {
                continue;
            }
            try {
                jobIds.add(backgroundJobService.submit("delete", userId, () -> {
                    try {
                        bulkDeleteService.deleteCycle(projectId, cycleId);
                        return Map.of("projectId", projectId, "cycleId", cycleId);
                    } finally {
                        deletionRegistry.unregisterCycle(projectId, cycleId);
                    }
                }));
            } catch (RuntimeException e) {
                deletionRegistry.unregisterCycle(projectId, cycleId);
                throw e;
            }
        }
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Experiments merged");
        res.put("id", aId);
        res.put("deletedId", bId);
        res.put("status", plan.resultingStatus);
        res.put("deletionJobIds", jobIds);
        if (!jobIds.isEmpty()) {
            res.put("deletionJobId", jobIds.get(0));
        }
        return res;
    }

    private String addErrorToHistory(String history, String error) {
        if (error == null || error.isBlank() || history.contains(error)) {
            return history;
        }
        return appendLine(history, "[" + ERROR_DATE_FORMAT.format(java.time.Instant.now()) + "] " + error);
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
