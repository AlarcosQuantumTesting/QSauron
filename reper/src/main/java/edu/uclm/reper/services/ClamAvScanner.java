package edu.uclm.reper.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Component
public class ClamAvScanner implements VirusScanner {

    private static final Logger logger = LoggerFactory.getLogger(ClamAvScanner.class);

    private final boolean enabled;
    private final String host;
    private final int port;
    private final int timeoutMs;

    public ClamAvScanner(
            @Value("${app.import.clamav.enabled:true}") boolean enabled,
            @Value("${app.import.clamav.host:localhost}") String host,
            @Value("${app.import.clamav.port:3310}") int port,
            @Value("${app.import.clamav.timeout-ms:10000}") int timeoutMs) {
        this.enabled = enabled;
        this.host = host;
        this.port = port;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public ScanResult scan(byte[] data) {
        if (!enabled) {
            return new ScanResult(true, null);
        }

        // Protocolo clamd INSTREAM: https://docs.clamav.net/manual/Usage/Scanning.html#clamd
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            socket.setSoTimeout(timeoutMs);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.write("zINSTREAM\0".getBytes(StandardCharsets.US_ASCII));

            if (data != null && data.length > 0) {
                int offset = 0;
                while (offset < data.length) {
                    int chunk = Math.min(8192, data.length - offset);
                    out.writeInt(chunk);
                    out.write(data, offset, chunk);
                    offset += chunk;
                }
            }
            out.writeInt(0);
            out.flush();

            InputStream in = socket.getInputStream();
            ByteArrayOutputStream respBytes = new ByteArrayOutputStream();
            int b;
            while ((b = in.read()) != -1) {
                if (b == 0 || b == '\n') {
                    break;
                }
                respBytes.write(b);
            }
            String resp = respBytes.toString(StandardCharsets.UTF_8).trim();

            if (resp.endsWith(" FOUND")) {
                String sig = resp.substring(0, resp.length() - " FOUND".length()).trim();
                if (sig.startsWith("stream:")) {
                    sig = sig.substring("stream:".length()).trim();
                }
                return new ScanResult(false, sig);
            } else if (resp.contains("OK")) {
                return new ScanResult(true, null);
            } else {
                logger.warn("Unexpected clamd response: {}", resp);
                throw new ImportException(ImportException.ANTIVIRUS_UNAVAILABLE, "Antivirus service is not available. Try again later.", 503);
            }
        } catch (IOException e) {
            logger.warn("Error communicating with ClamAV at {}:{}: {}", host, port, e.getMessage());
            throw new ImportException(ImportException.ANTIVIRUS_UNAVAILABLE, "Antivirus service is not available. Try again later.", 503);
        }
    }
}
