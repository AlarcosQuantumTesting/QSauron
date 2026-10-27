package edu.uclm.reper.http;

import java.util.Map;
import java.util.Optional;
import java.util.Arrays;

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

import edu.uclm.reper.model.User;
import edu.uclm.reper.repository.UserRepository;
import edu.uclm.reper.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("users")
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	AlarideProxy alarideProxy;

	private static final String COOKIE_FORMAT = "SESSION_TOKEN=%s; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=5400000";
	private static final String REFRESH_COOKIE_FORMAT = "REFRESH_TOKEN=%s; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=1209600";
	private static final String SET_COOKIE_HEADER = "Set-Cookie";

	@PostMapping("/create")
	public void create(@RequestBody Map<String, String> user) {

		Optional<User> existingUser = this.userRepository.findById(user.get("email"));

		if (existingUser.isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, user.get("email") + " already exists");
		}

		try {
			this.userService.create(user);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Error creating user: " + e.getMessage(), e);
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> user, HttpServletResponse response) {
		try {
			String resJson = this.userService.login(user);
			if (resJson != null && resJson.equals("2fa")) {
				return ResponseEntity.ok(resJson);
			}

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

			@SuppressWarnings("unchecked")
			Map<String, Object> responseMap = mapper.convertValue(root, Map.class);
			String email = user.get("email");
			if (email != null && !email.isEmpty()) {
				User u = this.userService.getUserProfile(email);
				responseMap.put("role", u.getRole().name());
				responseMap.put("isAdmin", u.isAdmin());
			}
			return ResponseEntity.ok(responseMap);

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid credentials");
		}

	}

	@PostMapping("/getUser")
	public ResponseEntity<?> getUser(HttpServletRequest request, HttpServletResponse response) {
		try {
			if (request.getCookies() == null) {
				return ResponseEntity.ok(null);
			}
			Optional<Cookie> sCookie = Arrays.stream(request.getCookies())
					.filter(c -> "SESSION_TOKEN".equals(c.getName()))
					.findFirst();
			Optional<Cookie> rCookie = Arrays.stream(request.getCookies())
					.filter(c -> "REFRESH_TOKEN".equals(c.getName()))
					.findFirst();

			if (sCookie.isEmpty() && rCookie.isEmpty()) {
				return ResponseEntity.ok(null);
			}

			String email = null;
			if (sCookie.isPresent()) {
				try {
					String token = sCookie.get().getValue();
					email = this.userService.getUserEmail(Map.of("token", token));
				} catch (Exception e) {
					// Token de sesión inválido o expirado
				}
			}

			if ((email == null || email.isEmpty()) && rCookie.isPresent()) {
				try {
					String refreshTokenStr = rCookie.get().getValue();
					Map<String, String> requestData = Map.of(
							"refreshToken", refreshTokenStr,
							"email", "");
					String resJson = this.userService.refreshToken(requestData);

					ObjectMapper mapper = new ObjectMapper();
					JsonNode root = mapper.readTree(resJson);
					String newToken = root.has("token") ? root.get("token").asText() : "";

					if (!newToken.isEmpty()) {
						response.addHeader(SET_COOKIE_HEADER, String.format(COOKIE_FORMAT, newToken));
						email = this.userService.getUserEmail(Map.of("token", newToken));
					}
				} catch (Exception ex) {
					return ResponseEntity.ok(null);
				}
			}

			if (email == null || email.isEmpty()) {
				return ResponseEntity.ok(null);
			}

			User u = this.userService.getUserProfile(email);
			return ResponseEntity.ok(Map.of(
					"email", u.getId(),
					"role", u.getRole().name(),
					"isAdmin", u.isAdmin()));

		} catch (Exception e) {
			return ResponseEntity.ok(null);
		}
	}

	@PostMapping("/activate2FA")
	public ResponseEntity<?> activar2FA(@RequestBody Map<String, String> request, HttpServletResponse response) {

		// Lógica para activar 2FA (enviar código, verificar, etc.)
		try {
			String resJson = alarideProxy.activar2FA(request);

			if (resJson != null) {
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
			} else {
				return ResponseEntity.badRequest().body(Map.of("mensaje", "Error al activar 2FA"));
			}
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Error al activar 2FA");
		}
	}

	@PostMapping("/verify2FACode")
	public ResponseEntity<?> verify2faCode(@RequestBody Map<String, String> data, HttpServletResponse response) {

		try {
			String tokenValue = alarideProxy.confirm2faCode(data);

			if (tokenValue == null) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}

			try {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode root = mapper.readTree(tokenValue);
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
				response.addHeader(SET_COOKIE_HEADER, String.format(COOKIE_FORMAT, tokenValue));
				return ResponseEntity.ok(tokenValue);
			}
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Error al verificar 2FA");
		}
	}

	@PostMapping("/refreshToken")
	public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			String refreshTokenStr = body.get("refreshToken");

			if (refreshTokenStr == null || refreshTokenStr.isEmpty()) {
				if (request.getCookies() != null) {
					Optional<Cookie> cookie = Arrays.stream(request.getCookies())
							.filter(c -> "REFRESH_TOKEN".equals(c.getName()))
							.findFirst();
					if (cookie.isPresent()) {
						refreshTokenStr = cookie.get().getValue();
					}
				}
			}

			if (refreshTokenStr == null || refreshTokenStr.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found");
			}

			String email = body.get("email");
			Map<String, String> requestData = Map.of(
					"refreshToken", refreshTokenStr,
					"email", email == null ? "" : email);

			String resJson = this.userService.refreshToken(requestData);

			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(resJson);
			String newToken = root.has("token") ? root.get("token").asText() : "";
			String newRefreshToken = root.has("refreshToken") ? root.get("refreshToken").asText() : "";

			if (!newToken.isEmpty()) {
				response.addHeader(SET_COOKIE_HEADER, String.format(COOKIE_FORMAT, newToken));
			}
			if (!newRefreshToken.isEmpty()) {
				response.addHeader(SET_COOKIE_HEADER, String.format(REFRESH_COOKIE_FORMAT, newRefreshToken));
			}
			return ResponseEntity.ok(mapper.convertValue(root, Object.class));

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Error refreshing token");
		}
	}

	@PostMapping("/logout")
	public boolean logout(HttpServletRequest request, HttpServletResponse response) {
		Optional<Cookie> cookie = Arrays.stream(request.getCookies() != null ? request.getCookies() : new Cookie[0])
				.filter(c -> "SESSION_TOKEN".equals(c.getName()))
				.findFirst();

		if (cookie.isPresent()) {
			String token = cookie.get().getValue();
			try {
				this.alarideProxy.logout(token);
			} catch (Exception e) {
				// Ignore error, proceed to logout locally
			}

			response.addHeader(SET_COOKIE_HEADER, "SESSION_TOKEN=; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=0");
			response.addHeader(SET_COOKIE_HEADER, "REFRESH_TOKEN=; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=0");
			return true;
		}
		return false;
	}

}
