package edu.uclm.reper.http;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uclm.reper.model.Role;
import edu.uclm.reper.model.User;
import edu.uclm.reper.services.ConverterCatalogService;
import edu.uclm.reper.services.ImportException;
import edu.uclm.reper.services.TestCatalog;
import edu.uclm.reper.services.UserService;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

class ConverterControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private QConvertProxy proxy;
    private UserService userService;
    private ConverterController controller;
    private MockHttpServletRequest req;

    @BeforeEach
    void setUp() {
        proxy = mock(QConvertProxy.class);
        userService = mock(UserService.class);
        ConverterCatalogService catalog = TestCatalog.create();
        controller = new ConverterController(proxy, catalog, 100);
        controller.userService = userService;
        req = new MockHttpServletRequest();
        req.setCookies(new Cookie("SESSION_TOKEN", "t"));
    }

    private Map<String, Object> body(String code) {
        return Map.of("email", "a@b.c", "code", code, "sourceFormat", "qasm", "destFormat", "quirk");
    }

    @Test
    void nonAdminGets403() {
        when(userService.getUserProfile("a@b.c")).thenReturn(new User("a@b.c", Role.USUARIO));
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> controller.convert(body("x"), req));
        assertEquals(403, e.getStatusCode().value());
        verifyNoInteractions(proxy);
    }

    @Test
    void adminForwardsBodyAndStatusWithoutEmail() throws Exception {
        when(userService.getUserProfile("a@b.c")).thenReturn(new User("a@b.c", Role.ADMINISTRADOR));
        JsonNode ok = mapper.readTree("{\"success\":true,\"output\":\"o\",\"filename\":\"f\"}");
        when(proxy.convertRaw(any())).thenReturn(new QConvertProxy.ProxyResponse(200, ok));

        ResponseEntity<JsonNode> r = controller.convert(body("x"), req);

        assertEquals(200, r.getStatusCode().value());
        assertEquals("f", r.getBody().get("filename").asText());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(proxy).convertRaw(captor.capture());
        assertFalse(captor.getValue().containsKey("email"));
        assertEquals("quirk", captor.getValue().get("destFormat"));
    }

    @Test
    void converterErrorStatusIsPropagated() throws Exception {
        when(userService.getUserProfile("a@b.c")).thenReturn(new User("a@b.c", Role.ADMINISTRADOR));
        when(proxy.convertRaw(any())).thenReturn(
                new QConvertProxy.ProxyResponse(422, mapper.readTree("{\"error\":\"bad\"}")));
        ResponseEntity<JsonNode> r = controller.convert(body("x"), req);
        assertEquals(422, r.getStatusCode().value());
        assertEquals("bad", r.getBody().get("error").asText());
    }

    @Test
    void tooLargeCodeIs413() {
        when(userService.getUserProfile("a@b.c")).thenReturn(new User("a@b.c", Role.ADMINISTRADOR));
        ImportException e = assertThrows(ImportException.class, () -> controller.convert(body("x".repeat(101)), req));
        assertEquals(413, e.getHttpStatus());
        assertEquals(ImportException.FILE_TOO_LARGE, e.getErrorCode());
    }

    @Test
    void converterDownIs503() {
        when(userService.getUserProfile("a@b.c")).thenReturn(new User("a@b.c", Role.ADMINISTRADOR));
        when(proxy.convertRaw(any())).thenThrow(
                new ImportException(ImportException.CONVERTER_UNAVAILABLE, "Code converter is not available", 503));
        ImportException e = assertThrows(ImportException.class, () -> controller.convert(body("x"), req));
        assertEquals(503, e.getHttpStatus());
        when(proxy.health()).thenReturn(false);
        assertEquals(503, assertThrows(ImportException.class, controller::health).getHttpStatus());
    }

    @Test
    void formatsIncludeExtensionsAndNoAuth() {
        JsonNode raw = controller.formats().getBody();
        assertTrue(raw.has("formatExtensions"));
        assertTrue(raw.get("sourceFormats").toString().contains("qasm3"));
        when(proxy.health()).thenReturn(true);
        assertEquals("ok", controller.health().getBody().get("status"));
    }
}
