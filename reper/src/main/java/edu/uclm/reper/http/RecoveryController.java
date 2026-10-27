package edu.uclm.reper.http;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.reper.services.UserService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("recovery")
public class RecoveryController {

    @Autowired
    private UserService userService;

    private static final String COOKIE_FORMAT = "SESSION_TOKEN=%s; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=5400000";
    private static final String REFRESH_COOKIE_FORMAT = "REFRESH_TOKEN=%s; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=1209600";
    private static final String SET_COOKIE_HEADER = "Set-Cookie";

    @PostMapping("/request")
    public String request(@RequestBody Map<String, String> request) {
        try {
            return this.userService.recoveryRequest(request.get("email"));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody Map<String, String> request, HttpServletResponse response) {
        try {
            String resJson = this.userService.recoveryValidate(
                    request.get("tokenId"),
                    request.get("email"),
                    request.get("code"));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(resJson);
            String token = root.has("token") ? root.get("token").asText() : "";
            String refreshToken = root.has("refreshToken") ? root.get("refreshToken").asText() : "";

            if (!token.isEmpty()) {
                response.addHeader(SET_COOKIE_HEADER, String.format(COOKIE_FORMAT, token));
            }
            if (!refreshToken.isEmpty()) {
                response.addHeader(SET_COOKIE_HEADER, String.format(REFRESH_COOKIE_FORMAT, refreshToken));
            }
            return ResponseEntity.ok(mapper.convertValue(root, Object.class));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid recovery code");
        }
    }

    @PostMapping("/reset")
    public void reset(@RequestBody Map<String, String> request) {
        try {
            this.userService.recoveryReset(request.get("email"), request.get("newPassword"));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error resetting password");
        }
    }
}
