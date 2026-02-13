package edu.uclm.reper.http;

import java.util.Map;
import java.util.Optional;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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

	private static final String COOKIE_FORMAT = "SESSION_TOKEN=%s; Path=/; HttpOnly; Secure; SameSite=None";
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
	public String login(@RequestBody Map<String, String> user, HttpServletResponse response) {
		try {
			String token = this.userService.login(user);
			if (!token.isEmpty() && !token.equals("2fa")) {
				response.addHeader(SET_COOKIE_HEADER, String.format(COOKIE_FORMAT, token));
			}
			return token;

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid credentials");
		}

	}

	@PostMapping("/getUser")
	public String getUserEmail(HttpServletRequest request) {
		try {
			if (request.getCookies() == null) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No cookies found");
			}
			Optional<Cookie> cookie = Arrays.stream(request.getCookies())
					.filter(c -> "SESSION_TOKEN".equals(c.getName()))
					.findFirst();

			if (cookie.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session token not found");
			}

			String token = cookie.get().getValue();
			String email = this.userService.getUserEmail(Map.of("token", token));
			return email;

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid token");
		}

	}

	@PostMapping("/activate2FA")
	public ResponseEntity<Map<String, String>> activar2FA(@RequestBody Map<String, String> request) {

		// Lógica para activar 2FA (enviar código, verificar, etc.)
		try {
			Map<String, String> res = alarideProxy.activar2FA(request);

			if (!res.isEmpty()) {
				return ResponseEntity.ok(Map.of("mensaje", res.get("mensaje")));
			} else {
				return ResponseEntity.badRequest().body(Map.of("mensaje", "Error al activar 2FA"));
			}
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Error al activar 2FA");
		}
	}

	@PostMapping("/verify2FACode")
	public String verify2faCode(@RequestBody Map<String, String> data, HttpServletResponse response) {

		try {
			String tokenValue = alarideProxy.confirm2faCode(data);

			if (tokenValue == null) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}

			response.addHeader(SET_COOKIE_HEADER, String.format(COOKIE_FORMAT, tokenValue));
			return tokenValue;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Error al verificar 2FA");
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
			return true;
		}
		return false;
	}

}
