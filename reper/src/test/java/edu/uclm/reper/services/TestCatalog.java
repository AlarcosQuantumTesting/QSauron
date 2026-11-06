package edu.uclm.reper.services;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uclm.reper.http.QConvertProxy;
import java.io.InputStream;

/** Catálogo de formatos de pruebas: los 10 formatos de entrada actuales de q-convert-api (incluye qasm3). */
public final class TestCatalog {

    private TestCatalog() {}

    public static ConverterCatalogService create() {
        try (InputStream in = TestCatalog.class.getResourceAsStream("/qconvert-formats.json")) {
            QConvertProxy proxy = mock(QConvertProxy.class);
            when(proxy.getFormats()).thenReturn(new ObjectMapper().readTree(in));
            return new ConverterCatalogService(proxy, 300);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
