package edu.uclm.reper.config;

import org.springframework.boot.web.server.Compression;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class CompressionConfig implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        Compression compression = new Compression();
        compression.setEnabled(true);
        compression.setMimeTypes(new String[]{"application/json", "text/plain", "text/csv"});
        compression.setMinResponseSize(DataSize.ofBytes(2048));
        factory.setCompression(compression);
    }
}
