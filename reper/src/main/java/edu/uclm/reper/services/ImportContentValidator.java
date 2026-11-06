package edu.uclm.reper.services;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uclm.reper.dto.ImportItemDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class ImportContentValidator {

    public record ValidatedItem(String fileName, ConverterFormat format, String code, byte[] utf8) {}

    // Reglas de negocio del import por id de formato (no es una lista de formatos: un formato
    // nuevo sin regla solo pasa las comprobaciones genéricas)
    private static final Map<String, String> REQUIRED_JSON_KEY = Map.of(
            "quirk", "cols", "qobj", "experiments", "ionq", "circuit", "quantum-circuit", "gates");

    private final long maxFileBytes;
    private final ConverterCatalogService catalog;
    private final ObjectMapper jsonMapper;

    public ImportContentValidator(@Value("${app.import.max-file-bytes:2097152}") long maxFileBytes,
            ConverterCatalogService catalog) {
        this.maxFileBytes = maxFileBytes;
        this.catalog = catalog;
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
        ConverterFormat format = catalog.find(item.getFormat())
                .filter(ConverterFormat::source)
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
                String expected = format.extensions().isEmpty() ? "" : format.extensions().get(0);
                throw new ImportException(ImportException.INVALID_EXTENSION,
                        cleanFileName + " is not a " + format.label() + " file (expected " + expected + ")");
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

        // 6. Estructura (reglas por id de formato)
        String id = format.id();
        if (id.equals("qasm") || id.equals("qasm3")) {
            boolean v3Only = id.equals("qasm3");
            String firstStatement = firstStatement(code);
            String missing = v3Only ? "Missing 'OPENQASM 3.0;' header" : "Missing 'OPENQASM 2.0;' or 'OPENQASM 3.0;' header";
            String header = v3Only ? "^OPENQASM\\s+3(\\.\\d+)?\\s*;.*" : "^OPENQASM\\s+(2(\\.0)?|3(\\.\\d+)?)\\s*;.*";
            if (firstStatement == null || !firstStatement.matches(header)) {
                throw new ImportException(ImportException.INVALID_CONTENT, missing);
            }
        } else if (id.equals("qiskit") || id.equals("cirq")) {
            // Scripts Python: q-convert-api los ejecuta; aquí solo se comprueba que usen la librería
            if (!code.matches("(?s).*\\b(import|from)\\s+" + id + "\\b.*")) {
                throw new ImportException(ImportException.INVALID_CONTENT,
                        "Not a " + format.label() + " file: missing '" + id + "' import");
            }
        } else if (format.extensions().contains(".json")) {
            JsonNode root;
            try {
                root = jsonMapper.readTree(code);
            } catch (Exception e) {
                throw new ImportException(ImportException.INVALID_CONTENT, "Invalid JSON");
            }
            if (root == null || !root.isObject()) {
                throw new ImportException(ImportException.INVALID_CONTENT, "Invalid JSON");
            }

            String requiredKey = REQUIRED_JSON_KEY.get(id);
            if (requiredKey != null) {
                JsonNode keyNode = root.get(requiredKey);
                if (keyNode == null || !keyNode.isArray()) {
                    throw new ImportException(ImportException.INVALID_CONTENT,
                            "Not a " + format.label() + " file: missing \"" + requiredKey + "\"");
                }
            }
        }

        // 7. Retorno
        return new ValidatedItem(cleanFileName, format, code, utf8);
    }

    /** Primera sentencia del fichero sin comentarios de línea, o null si está vacío. */
    private static String firstStatement(String code) {
        for (String line : code.split("\\R")) {
            String stripped = line.strip();
            if (stripped.startsWith("//")) {
                continue;
            }
            int commentIdx = stripped.indexOf("//");
            if (commentIdx >= 0) {
                stripped = stripped.substring(0, commentIdx).strip();
            }
            if (!stripped.isEmpty()) {
                return stripped;
            }
        }
        return null;
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
