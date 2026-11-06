package edu.uclm.reper.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Formato del catálogo de q-convert-api (única fuente de formatos). No hay
 * ninguna lista de ids en reper: todo viene de GET /api/formats.
 */
public record ConverterFormat(String id, String label, List<String> extensions,
                              boolean source, boolean dest, boolean jupyter) {

    /** Se puede generar en este formato (es destino de q-convert-api). */
    public boolean representable() {
        return dest;
    }

    public boolean acceptsExtension(String ext) {
        if (ext == null) {
            return false;
        }
        return extensions.contains(ext.toLowerCase().trim());
    }

    /** Forma que consume el import: id, label, extensions, representable. */
    public Map<String, Object> toImportMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("label", label);
        map.put("extensions", extensions);
        map.put("representable", representable());
        return map;
    }
}
