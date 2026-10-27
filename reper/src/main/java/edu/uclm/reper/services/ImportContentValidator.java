package edu.uclm.reper.services;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uclm.reper.dto.ImportItemDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class ImportContentValidator {

    public record ValidatedItem(String fileName, ImportFormat format, String code, byte[] utf8) {}

    private final long maxFileBytes;
    private final ObjectMapper jsonMapper;

    public ImportContentValidator(@Value("${app.import.max-file-bytes:2097152}") long maxFileBytes) {
        this.maxFileBytes = maxFileBytes;
        JsonFactory jsonFactory = JsonFactory.builder()
                .streamReadConstraints(StreamReadConstraints.builder().maxNestingDepth(64).build())
                .build();
        this.jsonMapper = new ObjectMapper(jsonFactory);
    }

    public ValidatedItem validate(ImportItemDTO item) {
        if (item == null) {
            throw new ImportException(ImportException.BAD_REQUEST, "Item cannot be null", 400);
        }

        // 1. Lenguaje
        ImportFormat format = ImportFormat.fromId(item.getFormat())
                .orElseThrow(() -> new ImportException(ImportException.INVALID_FORMAT, "Unknown language: " + item.getFormat()));

        // 2. Nombre de fichero
        String rawFileName = item.getFileName();
        String cleanFileName = null;
        if (rawFileName != null && !rawFileName.trim().isEmpty()) {
            int lastSlash = Math.max(rawFileName.lastIndexOf('/'), rawFileName.lastIndexOf('\\'));
            String baseName = lastSlash >= 0 ? rawFileName.substring(lastSlash + 1) : rawFileName;
            baseName = baseName.replaceAll("[\\p{Cntrl}]", "");

            int lastDot = baseName.lastIndexOf('.');
            String ext = lastDot >= 0 ? baseName.substring(lastDot).toLowerCase() : "";
            String nameWithoutExt = lastDot >= 0 ? baseName.substring(0, lastDot) : baseName;

            if (baseName.length() > 100) {
                int maxNameLen = Math.max(0, 100 - ext.length());
                if (nameWithoutExt.length() > maxNameLen) {
                    nameWithoutExt = nameWithoutExt.substring(0, maxNameLen);
                }
                baseName = nameWithoutExt + ext;
            }
            cleanFileName = baseName;

            if (!format.acceptsExtension(ext)) {
                String expected = format.getExtensions().isEmpty() ? "" : format.getExtensions().get(0);
                throw new ImportException(ImportException.INVALID_EXTENSION,
                        cleanFileName + " is not a " + format.getLabel() + " file (expected " + expected + ")");
            }
        }

        // 3. Sin fichero (código pegado)
        if (cleanFileName == null) {
            if (item.getProjectName() == null || item.getProjectName().trim().isEmpty()) {
                throw new ImportException(ImportException.MISSING_NAME, "Project name is required for pasted code");
            }
        }

        // 4. Código
        String code = item.getCode();
        if (code == null || code.strip().isEmpty()) {
            throw new ImportException(ImportException.EMPTY_FILE, "File is empty");
        }
        if (code.startsWith("\uFEFF")) {
            code = code.substring(1);
        }
        byte[] utf8 = code.getBytes(StandardCharsets.UTF_8);
        if (utf8.length > maxFileBytes) {
            String name = cleanFileName != null ? cleanFileName : "File";
            throw new ImportException(ImportException.FILE_TOO_LARGE, name + " exceeds " + formatBytes(maxFileBytes));
        }

        // 5. Solo texto
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c == '\u0000' || c == '\uFFFD' || c == 0x7F || (c < 0x20 && c != '\t' && c != '\n' && c != '\r')) {
                String name = cleanFileName != null ? cleanFileName : "File";
                throw new ImportException(ImportException.NOT_TEXT, name + " is not a plain-text circuit file");
            }
        }

        // 6. Estructura
        if (format == ImportFormat.QASM) {
            String[] lines = code.split("\\R");
            String firstStatement = null;
            for (String line : lines) {
                String stripped = line.strip();
                if (stripped.startsWith("//")) {
                    continue;
                }
                int commentIdx = stripped.indexOf("//");
                if (commentIdx >= 0) {
                    stripped = stripped.substring(0, commentIdx).strip();
                }
                if (!stripped.isEmpty()) {
                    firstStatement = stripped;
                    break;
                }
            }
            if (firstStatement == null) {
                throw new ImportException(ImportException.INVALID_CONTENT, "Missing 'OPENQASM 2.0;' header");
            }
            if (firstStatement.matches("^OPENQASM\\s+3.*")) {
                throw new ImportException(ImportException.INVALID_CONTENT, "Only OpenQASM 2.0 is supported");
            }
            if (!firstStatement.matches("^OPENQASM\\s+2(\\.0)?\\s*;.*")) {
                throw new ImportException(ImportException.INVALID_CONTENT, "Missing 'OPENQASM 2.0;' header");
            }
        } else if (format == ImportFormat.QISKIT || format == ImportFormat.CIRQ) {
            // Scripts Python: q-convert-api los ejecuta; aquí solo se comprueba que usen la librería
            String library = format == ImportFormat.QISKIT ? "qiskit" : "cirq";
            if (!code.matches("(?s).*\\b(import|from)\\s+" + library + "\\b.*")) {
                throw new ImportException(ImportException.INVALID_CONTENT,
                        "Not a " + format.getLabel() + " file: missing '" + library + "' import");
            }
        } else if (format != ImportFormat.QUIL) {
            // Formatos JSON
            JsonNode root;
            try {
                root = jsonMapper.readTree(code);
            } catch (Exception e) {
                throw new ImportException(ImportException.INVALID_CONTENT, "Invalid JSON");
            }
            if (root == null || !root.isObject()) {
                throw new ImportException(ImportException.INVALID_CONTENT, "Invalid JSON");
            }

            String requiredKey = null;
            switch (format) {
                case QUIRK:
                    requiredKey = "cols";
                    break;
                case QOBJ:
                    requiredKey = "experiments";
                    break;
                case IONQ:
                    requiredKey = "circuit";
                    break;
                case QUANTUM_CIRCUIT:
                    requiredKey = "gates";
                    break;
                default:
                    break;
            }

            if (requiredKey != null) {
                JsonNode keyNode = root.get(requiredKey);
                if (keyNode == null || !keyNode.isArray()) {
                    throw new ImportException(ImportException.INVALID_CONTENT,
                            "Not a " + format.getLabel() + " file: missing \"" + requiredKey + "\"");
                }
            }
        }

        // 7. Retorno
        return new ValidatedItem(cleanFileName, format, code, utf8);
    }

    private static String formatBytes(long b) {
        if (b % 1048576 == 0) {
            return (b / 1048576) + " MB";
        }
        if (b % 1024 == 0) {
            return (b / 1024) + " KB";
        }
        return b + " bytes";
    }
}
