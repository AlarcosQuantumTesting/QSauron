package edu.uclm.reper.config;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.web.server.Compression;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.util.unit.DataSize;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompressionConfigTest {

    @Test
    void testCustomizeEnablesGzipCompression() {
        CompressionConfig config = new CompressionConfig();
        ConfigurableServletWebServerFactory factory = mock(ConfigurableServletWebServerFactory.class);

        config.customize(factory);

        ArgumentCaptor<Compression> captor = ArgumentCaptor.forClass(Compression.class);
        verify(factory).setCompression(captor.capture());

        Compression compression = captor.getValue();
        assertNotNull(compression);
        assertTrue(compression.getEnabled());
        assertEquals(DataSize.ofBytes(2048), compression.getMinResponseSize());
        assertNotNull(compression.getMimeTypes());
        assertTrue(Arrays.asList(compression.getMimeTypes()).contains("application/json"));
        assertTrue(Arrays.asList(compression.getMimeTypes()).contains("text/plain"));
        assertTrue(Arrays.asList(compression.getMimeTypes()).contains("text/csv"));
    }
}

