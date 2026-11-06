package edu.uclm.reper.services;

import com.fasterxml.jackson.databind.JsonNode;
import edu.uclm.reper.http.QConvertProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Catálogo de formatos de q-convert-api con caché. Si q-convert-api falla se
 * sirve la última copia buena; si nunca se obtuvo, 503 CONVERTER_UNAVAILABLE.
 */
@Service
public class ConverterCatalogService {

    private static final Logger logger = LoggerFactory.getLogger(ConverterCatalogService.class);

    private final QConvertProxy proxy;
    private final long cacheMillis;

    private JsonNode cachedRaw;
    private List<ConverterFormat> cachedFormats = List.of();
    private long fetchedAt;

    public ConverterCatalogService(QConvertProxy proxy,
            @Value("${app.qconvert.formats-cache-seconds:300}") long cacheSeconds) {
        this.proxy = proxy;
        this.cacheMillis = cacheSeconds * 1000;
    }

    /** JSON de /api/formats tal cual (para el traductor). */
    public synchronized JsonNode raw() {
        refreshIfNeeded();
        return cachedRaw;
    }

    public synchronized List<ConverterFormat> all() {
        refreshIfNeeded();
        return cachedFormats;
    }

    /** Formatos admitidos como entrada, en el orden de sourceFormats. */
    public List<ConverterFormat> importable() {
        return all().stream().filter(ConverterFormat::source).toList();
    }

    public Optional<ConverterFormat> find(String id) {
        if (id == null) {
            return Optional.empty();
        }
        String trimmed = id.trim();
        return all().stream().filter(f -> f.id().equalsIgnoreCase(trimmed)).findFirst();
    }

    private void refreshIfNeeded() {
        long now = System.currentTimeMillis();
        if (cachedRaw != null && now - fetchedAt < cacheMillis) {
            return;
        }
        try {
            JsonNode raw = proxy.getFormats();
            List<ConverterFormat> formats = parse(raw);
            cachedRaw = raw;
            cachedFormats = formats;
            fetchedAt = now;
        } catch (RuntimeException e) {
            if (cachedRaw == null) {
                if (e instanceof ImportException) {
                    throw e;
                }
                throw new ImportException(ImportException.CONVERTER_UNAVAILABLE, "Code converter is not available", 503);
            }
            logger.warn("No se pudo refrescar el catálogo de q-convert-api; se usa la última copia: {}", e.getMessage());
            fetchedAt = now; // no reintentar en cada llamada
        }
    }

    static List<ConverterFormat> parse(JsonNode raw) {
        List<String> sources = strings(raw.get("sourceFormats"));
        List<String> dests = strings(raw.get("destFormats"));
        List<String> jupyter = strings(raw.get("jupyterEligibleDestFormats"));
        JsonNode labels = raw.path("formatLabels");
        JsonNode extensions = raw.path("formatExtensions");

        List<String> ids = new ArrayList<>(sources);
        dests.stream().filter(d -> !ids.contains(d)).forEach(ids::add);

        List<ConverterFormat> out = new ArrayList<>();
        for (String id : ids) {
            String label = labels.has(id) ? labels.get(id).asText() : id;
            List<String> exts = strings(extensions.get(id));
            out.add(new ConverterFormat(id, label, exts, sources.contains(id), dests.contains(id), jupyter.contains(id)));
        }
        return List.copyOf(out);
    }

    private static List<String> strings(JsonNode array) {
        List<String> out = new ArrayList<>();
        if (array != null && array.isArray()) {
            array.forEach(n -> out.add(n.asText()));
        }
        return out;
    }
}
