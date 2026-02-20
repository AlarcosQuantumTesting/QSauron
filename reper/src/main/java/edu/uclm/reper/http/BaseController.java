package edu.uclm.reper.http;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.reper.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Clase base para controladores que requieren validación de sesión mediante
 * cookie.
 * Los controllers que necesiten {@code validateCookie} deben extender esta
 * clase.
 */
public abstract class BaseController {

    @Autowired
    protected UserService userService;

    /**
     * Valida que la petición contenga una cookie SESSION_TOKEN válida para el email
     * dado.
     *
     * @param request la petición HTTP entrante
     * @param email   el email del usuario que debe coincidir con el token
     * @throws ResponseStatusException 401 si no hay cookie, si falta el token o si
     *                                 el token no es válido
     */
    protected void validateCookie(HttpServletRequest request, String email) {
        if (request.getCookies() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No cookies found");
        }

        Optional<Cookie> cookie = Arrays.stream(request.getCookies())
                .filter(c -> "SESSION_TOKEN".equals(c.getName()))
                .findFirst();

        if (cookie.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session token not found");
        }

        try {
            this.userService.validateToken(cookie.get().getValue(), email);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid session token");
        }
    }

    /**
     * Extrae el valor de la cookie SESSION_TOKEN de la petición.
     *
     * @param request la petición HTTP entrante
     * @return el valor del token de sesión
     * @throws ResponseStatusException 401 si no hay cookies o el token no existe
     */
    protected String getSessionToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No cookies found");
        }

        return Arrays.stream(request.getCookies())
                .filter(c -> "SESSION_TOKEN".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session token not found"));
    }
}
