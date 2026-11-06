package edu.uclm.reper.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uclm.reper.services.ImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QConvertProxy {

    private static final Logger logger = LoggerFactory.getLogger(QConvertProxy.class);

    private final WebClient webClient;
    private final int timeoutSeconds;
    private final long maxRequestBytes;
    private final ObjectMapper objectMapper;

    public record QConvertResult(boolean success, String output, String error) {}

    private record IndexedResult(int index, QConvertResult result) {}

    public QConvertProxy(
            @Value("${app.qconvert.url:http://localhost:3000}") String url,
            @Value("${app.qconvert.timeout-seconds:60}") int timeoutSeconds,
            @Value("${app.qconvert.max-request-bytes:4194304}") long maxRequestBytes) {
        this.timeoutSeconds = timeoutSeconds;
        this.maxRequestBytes = maxRequestBytes;
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
                .baseUrl(url)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                .build();
    }

    public static List<List<Integer>> planChunks(List<Map<String, Object>> items, ObjectMapper mapper, long maxBytes, int maxItems) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        List<List<Integer>> chunks = new ArrayList<>();
        List<Integer> currentChunk = new ArrayList<>();
        long currentBytes = 0;

        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            long itemBytes;
            try {
                itemBytes = mapper.writeValueAsBytes(item).length;
            } catch (Exception e) {
                itemBytes = 0;
            }

            if (!currentChunk.isEmpty() && (currentChunk.size() >= maxItems || currentBytes + itemBytes > maxBytes - 64)) {
                chunks.add(currentChunk);
                currentChunk = new ArrayList<>();
                currentBytes = 0;
            }

            currentChunk.add(i);
            currentBytes += itemBytes;

            if (itemBytes >= maxBytes) {
                chunks.add(currentChunk);
                currentChunk = new ArrayList<>();
                currentBytes = 0;
            }
        }

        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk);
        }

        return chunks;
    }

    public List<QConvertResult> convertBatch(List<String> codes, List<String> sourceFormats, String destFormat) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> allItems = new ArrayList<>();
        for (int i = 0; i < codes.size(); i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("code", codes.get(i));
            item.put("sourceFormat", sourceFormats.get(i));
            item.put("destFormat", destFormat);
            allItems.add(item);
        }

        // q-convert-api acepta como máximo 5 MB por petición (express.json limit)
        List<List<Integer>> chunkIndices = planChunks(allItems, objectMapper, maxRequestBytes, 100);
        QConvertResult[] combined = new QConvertResult[allItems.size()];

        try {
            for (List<Integer> chunkIdxList : chunkIndices) {
                List<Map<String, Object>> chunk = new ArrayList<>();
                for (int idx : chunkIdxList) {
                    chunk.add(allItems.get(idx));
                }
                Map<String, Object> requestPayload = Map.of("items", chunk);

                String responseStr = webClient.post()
                        .uri("/api/convert/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(requestPayload))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block(Duration.ofSeconds(timeoutSeconds));

                JsonNode response = responseStr != null ? objectMapper.readTree(responseStr) : null;

                if (response == null || !response.has("results") || !response.get("results").isArray()) {
                    logger.error("Invalid response from q-convert-api batch: {}", response);
                    throw new ImportException(ImportException.CONVERTER_UNAVAILABLE, "Code converter is not available", 503);
                }

                JsonNode resultsArray = response.get("results");
                for (JsonNode resNode : resultsArray) {
                    int chunkRelIdx = resNode.has("index") ? resNode.get("index").asInt() : 0;
                    boolean success = resNode.has("success") && resNode.get("success").asBoolean();
                    String output = resNode.has("output") ? resNode.get("output").asText() : null;
                    String error = resNode.has("error") ? resNode.get("error").asText() : null;

                    if (chunkRelIdx >= 0 && chunkRelIdx < chunkIdxList.size()) {
                        int originalIdx = chunkIdxList.get(chunkRelIdx);
                        combined[originalIdx] = new QConvertResult(success, output, error);
                    }
                }
            }

            List<QConvertResult> resultList = new ArrayList<>(allItems.size());
            for (int i = 0; i < allItems.size(); i++) {
                if (combined[i] != null) {
                    resultList.add(combined[i]);
                } else {
                    resultList.add(new QConvertResult(false, null, "No result returned for item " + i));
                }
            }
            return resultList;
        } catch (ImportException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error communicating with q-convert-api convertBatch: {}", e.getMessage(), e);
            throw new ImportException(ImportException.CONVERTER_UNAVAILABLE, "Code converter is not available", 503);
        }
    }

    /** Respuesta de q-convert-api tal cual: estado HTTP (2xx/4xx) y cuerpo JSON sin recortar. */
    public record ProxyResponse(int status, JsonNode body) {}

    /** Catálogo de formatos de q-convert-api (GET /api/formats), sin tocar. */
    public JsonNode getFormats() {
        try {
            String str = webClient.get()
                    .uri("/api/formats")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(timeoutSeconds));
            if (str == null) {
                throw new ImportException(ImportException.CONVERTER_UNAVAILABLE, "Code converter is not available", 503);
            }
            return objectMapper.readTree(str);
        } catch (ImportException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error communicating with q-convert-api getFormats: {}", e.getMessage(), e);
            throw new ImportException(ImportException.CONVERTER_UNAVAILABLE, "Code converter is not available", 503);
        }
    }

    /** true si q-convert-api responde 2xx en /api/health. */
    public boolean health() {
        try {
            return Boolean.TRUE.equals(webClient.get()
                    .uri("/api/health")
                    .exchangeToMono(r -> Mono.just(r.statusCode().is2xxSuccessful()))
                    .block(Duration.ofSeconds(Math.min(timeoutSeconds, 10))));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * POST /api/convert devolviendo el cuerpo completo y el estado. 5xx o fallo de red
     * → ImportException(CONVERTER_UNAVAILABLE, 503).
     */
    public ProxyResponse convertRaw(Map<String, Object> body) {
        try {
            ProxyResponse response = webClient.post()
                    .uri("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(body))
                    .exchangeToMono(clientResponse -> {
                        int status = clientResponse.statusCode().value();
                        if (clientResponse.statusCode().is5xxServerError()) {
                            return Mono.<ProxyResponse>error(new ImportException(
                                    ImportException.CONVERTER_UNAVAILABLE, "Code converter is not available", 503));
                        }
                        return clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(str -> new ProxyResponse(status, parseBody(str, status)));
                    })
                    .block(Duration.ofSeconds(timeoutSeconds));
            if (response == null) {
                throw new ImportException(ImportException.CONVERTER_UNAVAILABLE, "Code converter is not available", 503);
            }
            return response;
        } catch (ImportException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error communicating with q-convert-api convertRaw: {}", e.getMessage(), e);
            throw new ImportException(ImportException.CONVERTER_UNAVAILABLE, "Code converter is not available", 503);
        }
    }

    private JsonNode parseBody(String str, int status) {
        try {
            return objectMapper.readTree(str);
        } catch (Exception e) {
            String text = !str.isEmpty() ? str : (status >= 400 ? "Conversion failed with client error" : "Failed to parse response");
            return objectMapper.createObjectNode().put("error", text);
        }
    }

    public QConvertResult convertOne(String code, String sourceFormat, String destFormat) {
        ProxyResponse r = convertRaw(Map.of(
                "code", code,
                "sourceFormat", sourceFormat,
                "destFormat", destFormat
        ));
        JsonNode json = r.body();
        if (r.status() >= 400) {
            String err = json != null && json.has("error") ? json.get("error").asText() : String.valueOf(json);
            return new QConvertResult(false, null, err);
        }
        boolean success = json.has("success") && json.get("success").asBoolean();
        String output = json.has("output") ? json.get("output").asText() : null;
        String error = json.has("error") ? json.get("error").asText() : null;
        return new QConvertResult(success, output, error);
    }
}
