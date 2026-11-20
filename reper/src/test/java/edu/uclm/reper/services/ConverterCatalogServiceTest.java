package edu.uclm.reper.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uclm.reper.http.QConvertProxy;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConverterCatalogServiceTest {

    private JsonNode formatsJson;
    private QConvertProxy proxy;

    @BeforeEach
    void setUp() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/qconvert-formats.json")) {
            formatsJson = new ObjectMapper().readTree(in);
        }
        proxy = mock(QConvertProxy.class);
    }

    @Test
    void importableHasTenFormatsWithQasm3AndIonqNotRepresentable() {
        when(proxy.getFormats()).thenReturn(formatsJson);
        ConverterCatalogService catalog = new ConverterCatalogService(proxy, 300);

        List<ConverterFormat> formats = catalog.importable();

        assertEquals(10, formats.size());
        assertEquals(List.of("qasm", "quil", "qobj", "ionq", "quantum-circuit", "qubit-toaster", "quirk", "qasm3", "qiskit", "cirq"),
                formats.stream().map(ConverterFormat::id).toList());
        assertFalse(catalog.find("ionq").orElseThrow().representable());
        assertTrue(catalog.find("qasm3").orElseThrow().representable());
        assertEquals(List.of(".qasm"), catalog.find("qasm3").orElseThrow().extensions());
        assertEquals("OpenQASM 3.0", catalog.find(" QASM3 ").orElseThrow().label());
        assertTrue(catalog.find("svg").isPresent());
        assertFalse(catalog.importable().stream().anyMatch(f -> f.id().equals("svg")));
        assertTrue(catalog.find("nope").isEmpty());
    }

    @Test
    void secondCallUsesCache() {
        when(proxy.getFormats()).thenReturn(formatsJson);
        ConverterCatalogService catalog = new ConverterCatalogService(proxy, 300);
        catalog.all();
        catalog.all();
        catalog.raw();
        verify(proxy, times(1)).getFormats();
    }

    @Test
    void failureWithCopyReturnsCopy() throws Exception {
        when(proxy.getFormats()).thenReturn(formatsJson)
                .thenThrow(new ImportException(ImportException.CONVERTER_UNAVAILABLE, "Code converter is not available", 503));
        ConverterCatalogService catalog = new ConverterCatalogService(proxy, 0);
        assertEquals(10, catalog.importable().size());
        assertEquals(10, catalog.importable().size());
    }

    @Test
    void failureWithoutCopyIs503() {
        when(proxy.getFormats())
                .thenThrow(new ImportException(ImportException.CONVERTER_UNAVAILABLE, "Code converter is not available", 503));
        ConverterCatalogService catalog = new ConverterCatalogService(proxy, 300);
        ImportException e = assertThrows(ImportException.class, catalog::all);
        assertEquals(503, e.getHttpStatus());
        assertEquals(ImportException.CONVERTER_UNAVAILABLE, e.getErrorCode());
    }
}
