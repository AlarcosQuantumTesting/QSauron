package edu.uclm.reper.http;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import edu.uclm.reper.services.ConverterCatalogService;
import edu.uclm.reper.services.ProjectImportService;
import edu.uclm.reper.services.TestCatalog;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ImportControllerFormatsTest {

    @Test
    @SuppressWarnings("unchecked")
    void formatsComeFromCatalogWithSameShapeAsBefore() throws Exception {
        ConverterCatalogService catalog = TestCatalog.create();
        ImportController controller = new ImportController(mock(ProjectImportService.class), catalog, 20, 2097152L, 20971520L, 16);

        Map<String, Object> body = controller.getFormats().getBody();

        List<Map<String, Object>> formats = (List<Map<String, Object>>) body.get("formats");
        assertEquals(10, formats.size());
        assertEquals(List.of("id", "label", "extensions", "representable"), List.copyOf(formats.get(0).keySet()));
        Map<String, Object> qasm3 = formats.stream().filter(f -> f.get("id").equals("qasm3")).findFirst().orElseThrow();
        assertEquals(true, qasm3.get("representable"));
        Map<String, Object> ionq = formats.stream().filter(f -> f.get("id").equals("ionq")).findFirst().orElseThrow();
        assertEquals(false, ionq.get("representable"));
        assertEquals(16, ((Map<String, Object>) body.get("limits")).get("maxQubits"));
    }
}
