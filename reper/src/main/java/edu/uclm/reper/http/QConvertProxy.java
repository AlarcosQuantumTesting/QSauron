package edu.uclm.reper.http;

import com.fasterxml.jackson.databind.JsonNode;
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

    public record QConvertResult(boolean success, String output, String error) {}

    private record IndexedResult(int index, QConvertResult result) {}

    public QConvertProxy(
            @Value("${app.qconvert.url:http://localhost:3000}") String url,
            @Value("${app.qconvert.timeout-seconds:60}") int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        this.webClient = WebClient.builder()
                .baseUrl(url)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
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

        List<QConvertResult> combined = new ArrayList<>();
        int chunkSize = 100;
        try {
            for (int i = 0; i < allItems.size(); i += chunkSize) {
                List<Map<String, Object>> chunk = allItems.subList(i, Math.min(allItems.size(), i + chunkSize));
                Map<String, Object> requestPayload = Map.of("items", chunk);

                JsonNode response = webClient.post()
                        .uri("/api/convert/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(requestPayload))
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .block(Duration.ofSeconds(timeoutSeconds));

                if (response == null || !response.has("results") || !response.get("results").isArray()) {
                    logger.error("Invalid response from q-convert-api batch: {}", response);
                    throw new ImportException(ImportException.CONVERTER_UNAVAILABLE, "Code converter is not available", 503);
                }

                JsonNode resultsArray = response.get("results");
                List<IndexedResult> chunkResults = new ArrayList<>();
                for (JsonNode resNode : resultsArray) {
                    int idx = resNode.has("index") ? resNode.get("index").asInt() : 0;
                    boolean success = resNode.has("success") && resNode.get("success").asBoolean();
                    String output = resNode.has("output") ? resNode.get("output").asText() : null;
                    String error = resNode.has("error") ? resNode.get("error").asText() : null;
                    chunkResults.add(new IndexedResult(idx, new QConvertResult(success, output, error)));
                }
                chunkResults.sort(Comparator.comparingInt(r -> r.index));
                for (IndexedResult r : chunkResults) {
                    combined.add(r.result);
                }
            }
            return combined;
        } catch (ImportException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error communicating with q-convert-api convertBatch: {}", e.getMessage(), e);
            throw new ImportException(ImportException.CONVERTER_UNAVAILABLE, "Code converter is not available", 503);
        }
    }

    public QConvertResult convertOne(String code, String sourceFormat, String destFormat) {
        Map<String, Object> body = Map.of(
                "code", code,
                "sourceFormat", sourceFormat,
                "destFormat", destFormat
        );

        try {
            return webClient.post()
                    .uri("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(body))
                    .exchangeToMono(clientResponse -> {
                        if (clientResponse.statusCode().is4xxClientError()) {
                            return clientResponse.bodyToMono(JsonNode.class)
                                    .map(json -> {
                                        String err = json.has("error") ? json.get("error").asText() : json.toString();
                                        return new QConvertResult(false, null, err);
                                    })
                                    .defaultIfEmpty(new QConvertResult(false, null, "Conversion failed with client error"));
                        }
                        if (clientResponse.statusCode().is5xxServerError()) {
                            return Mono.error(new ImportException(ImportException.CONVERTER_UNAVAILABLE, "Code converter is not available", 503));
                        }
                        return clientResponse.bodyToMono(JsonNode.class)
                                .map(json -> {
                                    boolean success = json.has("success") && json.get("success").asBoolean();
                                    String output = json.has("output") ? json.get("output").asText() : null;
                                    String error = json.has("error") ? json.get("error").asText() : null;
                                    return new QConvertResult(success, output, error);
                                });
                    })
                    .block(Duration.ofSeconds(timeoutSeconds));
        } catch (ImportException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error communicating with q-convert-api convertOne: {}", e.getMessage(), e);
            throw new ImportException(ImportException.CONVERTER_UNAVAILABLE, "Code converter is not available", 503);
        }
    }
}
