package edu.uclm.reper.http;

import java.io.IOException;
import java.util.Map;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AlarideProxy {
    
    private String urlAlaride = "http://localhost:80/users/";

    public void create(String email, String pwd) throws Exception{
        Map<String, String> user = Map.of("email", email, "pwd", pwd);

        HttpPost httpPost = new HttpPost(urlAlaride + "create");

        ObjectMapper mapper = new ObjectMapper();
        String jsonUser;
        try {
            jsonUser = mapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting user to JSON", e);
        }

        httpPost.setEntity(new StringEntity(jsonUser));
        httpPost.setHeader("Content-Type", "application/json");
        
         try (CloseableHttpClient httpclient = HttpClients.createDefault();
             CloseableHttpResponse response = httpclient.execute(httpPost)) {
   
            int code = response.getCode();
            if (code != 200) {
                throw new RuntimeException("El servicio solicitado requiere pago");
            }
        } catch (IOException e) {
            throw new IOException("Error executing HTTP request", e);
        }
    }
    


}
