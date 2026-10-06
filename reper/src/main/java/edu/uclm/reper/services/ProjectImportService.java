package edu.uclm.reper.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uclm.reper.dto.ConvertRequestDTO;
import edu.uclm.reper.dto.ConvertedCircuitDTO;
import edu.uclm.reper.dto.ImportItemDTO;
import edu.uclm.reper.dto.ImportItemResultDTO;
import edu.uclm.reper.dto.ImportRequestDTO;
import edu.uclm.reper.dto.ImportResultDTO;
import edu.uclm.reper.dto.RepresentRequestDTO;
import edu.uclm.reper.dto.RepresentationDTO;
import edu.uclm.reper.http.QConvertProxy;
import edu.uclm.reper.http.QConvertProxy.QConvertResult;
import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.QCircuit;
import edu.uclm.reper.model.QProgram;
import edu.uclm.reper.repository.ProjectRepository;
import edu.uclm.reper.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectImportService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectImportService.class);

    private final ImportContentValidator validator;
    private final QConvertProxy proxy;
    private final ProjectService projectService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ObjectMapper jsonMapper;

    private final int maxFiles;
    private final long maxTotalBytes;
    private final long maxFileBytes;
    private final int maxQubits;

    public ProjectImportService(
            ImportContentValidator validator,
            QConvertProxy proxy,
            ProjectService projectService,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            @Value("${app.import.max-files:20}") int maxFiles,
            @Value("${app.import.max-total-bytes:5242880}") long maxTotalBytes,
            @Value("${app.import.max-file-bytes:524288}") long maxFileBytes,
            @Value("${app.import.max-qubits:16}") int maxQubits) {
        this.validator = validator;
        this.proxy = proxy;
        this.projectService = projectService;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.maxFiles = maxFiles;
        this.maxTotalBytes = maxTotalBytes;
        this.maxFileBytes = maxFileBytes;
        this.maxQubits = maxQubits;
        this.jsonMapper = new ObjectMapper();
    }

    public ImportResultDTO importProjects(String email, List<ImportItemDTO> items) {
        // 1. Comprobaciones de petición
        if (items == null || items.isEmpty()) {
            throw new ImportException(ImportException.BAD_REQUEST, "No items provided", 400);
        }
        if (items.size() > maxFiles) {
            throw new ImportException(ImportException.TOO_MANY_FILES, "Too many files (maximum is " + maxFiles + ")", 400);
        }

        long totalBytes = 0;
        for (ImportItemDTO it : items) {
            if (it != null && it.getCode() != null) {
                totalBytes += it.getCode().getBytes(StandardCharsets.UTF_8).length;
            }
        }
        if (totalBytes > maxTotalBytes) {
            throw new ImportException(ImportException.TOTAL_TOO_LARGE, "Total size exceeds limit", 400);
        }

        if (email == null || userRepository.findById(email).isEmpty()) {
            throw new ImportException(ImportException.BAD_REQUEST, "User does not exist", 400);
        }

        List<ImportItemResultDTO> itemResults = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            ImportItemDTO it = items.get(i);
            ImportItemResultDTO res = new ImportItemResultDTO();
            res.setIndex(i);
            if (it != null) {
                res.setFileName(it.getFileName());
                res.setFormat(it.getFormat());
                res.setProjectName(it.getProjectName());
            }
            res.setSuccess(false);
            itemResults.add(res);
        }

        // 2. Validación y escaneo elemento a elemento
        List<Integer> validIndices = new ArrayList<>();
        List<ImportContentValidator.ValidatedItem> validatedItems = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            ImportItemDTO item = items.get(i);
            ImportItemResultDTO res = itemResults.get(i);
            try {
                ImportContentValidator.ValidatedItem v = validator.validate(item);
                validIndices.add(i);
                validatedItems.add(v);
            } catch (ImportException e) {
                res.setSuccess(false);
                res.setErrorCode(e.getErrorCode());
                res.setError(e.getMessage());
            }
        }

        // 3. Conversión por lotes de los válidos
        if (!validIndices.isEmpty()) {
            List<String> codes = validatedItems.stream().map(ImportContentValidator.ValidatedItem::code).toList();
            List<String> formats = validatedItems.stream().map(v -> v.format().getId()).toList();
            List<QConvertResult> convertResults = proxy.convertBatch(codes, formats, "quirk");

            // Nombres tomados del usuario
            Set<String> taken = projectRepository.findProjectsByUserId(email).stream()
                    .map(p -> p.getName() != null ? p.getName().toLowerCase() : "")
                    .collect(Collectors.toCollection(HashSet::new));

            for (int k = 0; k < validIndices.size(); k++) {
                int originalIndex = validIndices.get(k);
                ImportItemResultDTO res = itemResults.get(originalIndex);
                ImportItemDTO item = items.get(originalIndex);
                ImportContentValidator.ValidatedItem v = validatedItems.get(k);
                QConvertResult cr = k < convertResults.size() ? convertResults.get(k) :
                        new QConvertResult(false, null, "Conversion failed");

                if (!cr.success()) {
                    String err = cr.error() != null ? cr.error() : "Conversion failed";
                    if (err.length() > 300) {
                        err = err.substring(0, 300);
                    }
                    res.setSuccess(false);
                    res.setErrorCode(ImportException.CONVERSION_FAILED);
                    res.setError(err);
                    continue;
                }

                JsonNode quirk;
                try {
                    quirk = jsonMapper.readTree(cr.output());
                } catch (Exception e) {
                    res.setSuccess(false);
                    res.setErrorCode(ImportException.INVALID_CIRCUIT);
                    res.setError("Invalid circuit JSON returned");
                    continue;
                }

                if (quirk == null || !quirk.has("cols") || !quirk.get("cols").isArray()) {
                    res.setSuccess(false);
                    res.setErrorCode(ImportException.INVALID_CIRCUIT);
                    res.setError("Invalid circuit: missing cols");
                    continue;
                }

                int qubits = QuirkCircuitUtils.countQubits(quirk);
                int columns = QuirkCircuitUtils.countColumns(quirk);

                if (qubits == 0 || columns == 0) {
                    res.setSuccess(false);
                    res.setErrorCode(ImportException.INVALID_CIRCUIT);
                    res.setError("The converted circuit is empty");
                    continue;
                }

                if (qubits > maxQubits) {
                    res.setSuccess(false);
                    res.setErrorCode(ImportException.TOO_MANY_QUBITS);
                    res.setError("Circuit has " + qubits + " qubits; the maximum is 16");
                    continue;
                }

                // 4. Nombre único
                String baseName = ProjectNameSanitizer.fromItem(v.fileName(), item.getProjectName());
                String uniqueName = ProjectNameSanitizer.makeUnique(baseName, taken);

                // 5. Crear proyecto
                try {
                    QCircuit qc = new QCircuit(qubits, quirk);
                    qc.setId(UUID.randomUUID().toString());
                    qc.setMutableColumns(QuirkCircuitUtils.rangeCsv(columns));
                    qc.setMutableRows(QuirkCircuitUtils.rangeCsv(qubits));

                    QProgram qp = new QProgram();
                    qp.setId(UUID.randomUUID().toString());
                    qp.setQubits(qubits);
                    qp.setQuirkCircuit(qc);

                    Project p = new Project(uniqueName);
                    p.setId(UUID.randomUUID().toString());
                    p.setQProgram(qp);

                    Project saved = projectService.saveProject(p);
                    projectService.associateProjectWithUser(saved.getId(), email);

                    res.setSuccess(true);
                    res.setProjectId(saved.getId());
                    res.setProjectName(uniqueName);
                    res.setQubits(qubits);
                    res.setColumns(columns);
                } catch (Exception e) {
                    logger.error("Error creating project {}: {}", uniqueName, e.getMessage(), e);
                    res.setSuccess(false);
                    res.setErrorCode(ImportException.INTERNAL_ERROR);
                    res.setError("Internal error creating project");
                }
            }
        }

        int successCount = (int) itemResults.stream().filter(ImportItemResultDTO::isSuccess).count();
        int failureCount = itemResults.size() - successCount;

        logger.info("import email={} total={} ok={} ko={}", email, items.size(), successCount, failureCount);
        return new ImportResultDTO(items.size(), successCount, failureCount, itemResults);
    }

    public ConvertedCircuitDTO convertOnly(String email, ConvertRequestDTO req) {
        if (email == null || userRepository.findById(email).isEmpty()) {
            throw new ImportException(ImportException.BAD_REQUEST, "User does not exist", 400);
        }
        if (req == null) {
            throw new ImportException(ImportException.BAD_REQUEST, "Request cannot be null", 400);
        }

        ImportItemDTO item = new ImportItemDTO(req.getFileName(), req.getFormat(), "temp", req.getCode());
        ImportContentValidator.ValidatedItem v = validator.validate(item);

        List<QConvertResult> results = proxy.convertBatch(List.of(v.code()), List.of(v.format().getId()), "quirk");
        if (results.isEmpty() || !results.get(0).success()) {
            String err = results.isEmpty() ? "Conversion failed" : results.get(0).error();
            throw new ImportException(ImportException.CONVERSION_FAILED, err != null ? err : "Conversion failed");
        }

        JsonNode quirk;
        try {
            quirk = jsonMapper.readTree(results.get(0).output());
        } catch (Exception e) {
            throw new ImportException(ImportException.INVALID_CIRCUIT, "Invalid circuit JSON returned");
        }

        if (quirk == null || !quirk.has("cols") || !quirk.get("cols").isArray()) {
            throw new ImportException(ImportException.INVALID_CIRCUIT, "Invalid circuit: missing cols");
        }

        int qubits = QuirkCircuitUtils.countQubits(quirk);
        int columns = QuirkCircuitUtils.countColumns(quirk);

        if (qubits == 0 || columns == 0) {
            throw new ImportException(ImportException.INVALID_CIRCUIT, "The converted circuit is empty");
        }

        if (qubits > maxQubits) {
            throw new ImportException(ImportException.TOO_MANY_QUBITS,
                    "Circuit has " + qubits + " qubits; the maximum is 16");
        }

        return new ConvertedCircuitDTO(v.format().getId(), qubits, columns, quirk);
    }

    public RepresentationDTO represent(String email, RepresentRequestDTO req) {
        if (email == null || userRepository.findById(email).isEmpty()) {
            throw new ImportException(ImportException.BAD_REQUEST, "User does not exist", 400);
        }
        if (req == null || req.getTargetFormat() == null) {
            throw new ImportException(ImportException.BAD_REQUEST, "Target format is required", 400);
        }

        ImportFormat format = ImportFormat.fromId(req.getTargetFormat())
                .orElseThrow(() -> new ImportException(ImportException.INVALID_FORMAT,
                        "Unknown format: " + req.getTargetFormat(), 400));

        if (!format.isRepresentable()) {
            throw new ImportException(ImportException.INVALID_FORMAT,
                    "Format " + req.getTargetFormat() + " is not representable", 400);
        }

        JsonNode quirk = req.getQuirk();
        if (quirk == null || !quirk.isObject() || !quirk.has("cols") || !quirk.get("cols").isArray()) {
            throw new ImportException(ImportException.INVALID_CIRCUIT, "Invalid Quirk circuit: missing cols", 400);
        }

        byte[] quirkBytes = quirk.toString().getBytes(StandardCharsets.UTF_8);
        if (quirkBytes.length > maxFileBytes) {
            throw new ImportException(ImportException.FILE_TOO_LARGE, "Circuit exceeds size limit", 400);
        }

        QConvertResult cr = proxy.convertOne(quirk.toString(), "quirk", format.getId());
        if (!cr.success()) {
            String err = cr.error() != null ? cr.error() : "Conversion failed";
            throw new ImportException(ImportException.CONVERSION_FAILED, err, 422);
        }

        String extension = format.getExtensions().isEmpty() ? "" : format.getExtensions().get(0);
        return new RepresentationDTO(format.getId(), extension, cr.output());
    }
}
