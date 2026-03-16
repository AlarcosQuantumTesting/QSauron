package edu.uclm.reper.http;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class MutantAnalyticsProxy {

    private final WebClient webClient;

    public MutantAnalyticsProxy(@Value("${app.mutantanalytics.url}") String urlMutantAnalytics) {
        this.webClient = WebClient.builder()
                .baseUrl(urlMutantAnalytics)
                .build();
    }

    public Mono<Map<String, Object>> calculateStatistics(MultipartFile file, Double threshold) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource());
        if (threshold != null) {
            builder.part("threshold", threshold.toString());
        }

        return webClient.post()
                .uri("/calculateStatistics")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorResume(e -> Mono.error(new Exception("Error calculation statistics: " + e.getMessage(), e)));
    }

    public Mono<Map<String, Object>> uploadResults(Map<String, Object> stats) {
        return webClient.post()
                .uri("/uploadResults")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(stats))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorResume(e -> Mono.error(new Exception("Error uploading results: " + e.getMessage(), e)));
    }

    public Mono<Map<String, Object>> chatQuestion(Map<String, Object> payload) {
        return webClient.post()
                .uri("/chatQuestion")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(payload))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorResume(e -> Mono.error(new Exception("Error asking chat question: " + e.getMessage(), e)));
    }
}
