package edu.uclm.reper.config;

import io.netty.util.concurrent.FastThreadLocal;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.HttpResources;

@Configuration
public class NettyCleanupConfig {

    @PreDestroy
    public void cleanup() {
        // Clean up FastThreadLocal to prevent memory leaks in Tomcat
        FastThreadLocal.removeAll();

        // Dispose of Reactor Netty resources
        HttpResources.disposeLoopsAndConnectionsLater().block();
    }
}
