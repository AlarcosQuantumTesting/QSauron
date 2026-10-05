package edu.uclm.reper.services;

import java.text.Normalizer;
import java.util.Set;

public class ProjectNameSanitizer {

    private ProjectNameSanitizer() {}

    public static String fromItem(String fileName, String requested) {
        String candidate;
        if (requested != null && !requested.trim().isEmpty()) {
            candidate = requested;
        } else if (fileName != null && !fileName.trim().isEmpty()) {
            int lastSlash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
            String base = lastSlash >= 0 ? fileName.substring(lastSlash + 1) : fileName;
            int lastDot = base.lastIndexOf('.');
            candidate = lastDot > 0 ? base.substring(0, lastDot) : (lastDot == 0 ? base.substring(0, lastDot) : base);
            if (candidate.isEmpty() && lastDot >= 0) {
                // If like ".json" or "....json"
                int extDot = base.lastIndexOf('.');
                candidate = base.substring(0, extDot);
            }
        } else {
            return "Imported project";
        }

        // Normalizar con NFC
        String normalized = Normalizer.normalize(candidate, Normalizer.Form.NFC);

        // Sustituir todo lo que no sea letra, dígito, espacio, -, _, ., ( o ) por _
        String sanitized = normalized.replaceAll("[^\\p{L}\\p{N} \\-_\\.\\(\\)]", "_");

        // Colapsar espacios, trim
        sanitized = sanitized.replaceAll("\\s+", " ").trim();

        // Recortar a 80 caracteres
        if (sanitized.length() > 80) {
            sanitized = sanitized.substring(0, 80).trim();
        }

        // Si queda vacío o sin caracteres útiles
        if (sanitized.isEmpty() || sanitized.replaceAll("[._\\-\\s]", "").isEmpty()) {
            return "Imported project";
        }

        return sanitized;
    }

    public static String makeUnique(String name, Set<String> takenLowercase) {
        if (name == null || name.trim().isEmpty()) {
            name = "Imported project";
        }
        if (!takenLowercase.contains(name.toLowerCase())) {
            takenLowercase.add(name.toLowerCase());
            return name;
        }

        int counter = 2;
        while (true) {
            String candidate = name + " (" + counter + ")";
            if (!takenLowercase.contains(candidate.toLowerCase())) {
                takenLowercase.add(candidate.toLowerCase());
                return candidate;
            }
            counter++;
        }
    }
}
