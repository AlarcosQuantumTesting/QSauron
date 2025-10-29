package edu.uclm.reper.http;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AlarideProxy {
    
    private final WebClient webClient;
    private String urlAlaride = "http://localhost:80/";

    public AlarideProxy() {
        this.webClient = WebClient.builder()
            .baseUrl(urlAlaride)
            .build();
    }

    public void create(String email, String pwd) throws Exception {
        Map<String, String> user = Map.of("email", email, "pwd", pwd);

        try {
            webClient.post()
                .uri("users/create")
                .header("Content-Type", "application/json")
                .body(BodyInserters.fromValue(user))
                .retrieve()
                .bodyToMono(String.class)
                .block();

            setUser(email);
        } catch (Exception e) {
            throw new Exception("Error creating user: " + e.getMessage(), e);
        }
    }

    private void setUser(String email) throws Exception {
        String systemName = "QPorjectsPrueba";
        String privateKey = "pri_86f314b2-6b48-42b1-aa2e-6f8f05eb6cde";
        Map<String, String> user = Map.of("email", email, "systemName", systemName, "privateKey", privateKey);

        try {
            webClient.post()
                .uri("systems/setUser")
                .header("Content-Type", "application/json")
                .body(BodyInserters.fromValue(user))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (Exception e) {
            throw new Exception("Error setting user: " + e.getMessage(), e);
        }
    }

    public String login(Map<String, String> user) throws Exception {
        try {
            return webClient.post()
                .uri("users/login")
                .header("Content-Type", "application/json")
                .body(BodyInserters.fromValue(user))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (Exception e) {
            throw new Exception("Error during login: " + e.getMessage(), e);
        }
    }

    public String validateToken(Map <String,String> user) throws Exception {
        try {
            return webClient.post()
                .uri("tokens/validate")
                .header("Content-Type", "application/json")
                .body(BodyInserters.fromValue(user))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (Exception e) {
            throw new Exception("Error validating token: " + e.getMessage(), e);
        }
    }

    public String getUserId(Map <String,String> token) throws Exception {
        try {
            return webClient.post()
                .uri("tokens/getUser")
                .header("Content-Type", "application/json")
                .body(BodyInserters.fromValue(token))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (Exception e) {
            throw new Exception("Error getting user ID: " + e.getMessage(), e);
        }
    }

    
}