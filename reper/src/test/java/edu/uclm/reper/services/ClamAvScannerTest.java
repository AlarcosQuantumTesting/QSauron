package edu.uclm.reper.services;

import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ClamAvScannerTest {

    private void runMockServer(ServerSocket serverSocket, String response) {
        new Thread(() -> {
            try (Socket client = serverSocket.accept()) {
                DataInputStream in = new DataInputStream(client.getInputStream());
                // Leer comando zINSTREAM\0
                byte[] cmd = new byte[10];
                in.readFully(cmd);

                // Leer chunks hasta len == 0
                while (true) {
                    int len = in.readInt();
                    if (len == 0) {
                        break;
                    }
                    in.skipBytes(len);
                }

                // Enviar respuesta
                OutputStream out = client.getOutputStream();
                out.write(response.getBytes(StandardCharsets.UTF_8));
                out.flush();
            } catch (Exception ignored) {
            }
        }).start();
    }

    @Test
    void testScanCleanOk() throws Exception {
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            runMockServer(server, "stream: OK\0");

            ClamAvScanner scanner = new ClamAvScanner(true, "localhost", port, 2000);
            var result = scanner.scan("hello world".getBytes(StandardCharsets.UTF_8));
            assertTrue(result.clean());
            assertNull(result.signature());
        }
    }

    @Test
    void testScanMalwareFound() throws Exception {
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            runMockServer(server, "stream: Eicar-Test-Signature FOUND\0");

            ClamAvScanner scanner = new ClamAvScanner(true, "localhost", port, 2000);
            var result = scanner.scan("malicious code".getBytes(StandardCharsets.UTF_8));
            assertFalse(result.clean());
            assertEquals("Eicar-Test-Signature", result.signature());
        }
    }

    @Test
    void testScanServerUnavailable() {
        // Port without server
        ClamAvScanner scanner = new ClamAvScanner(true, "localhost", 65530, 500);
        ImportException ex = assertThrows(ImportException.class, () ->
                scanner.scan("test".getBytes(StandardCharsets.UTF_8)));
        assertEquals(ImportException.ANTIVIRUS_UNAVAILABLE, ex.getErrorCode());
        assertEquals(503, ex.getHttpStatus());
    }

    @Test
    void testScanDisabledNoConnection() {
        ClamAvScanner scanner = new ClamAvScanner(false, "localhost", 65530, 500);
        var result = scanner.scan("test".getBytes(StandardCharsets.UTF_8));
        assertTrue(result.clean());
        assertNull(result.signature());
    }
}
