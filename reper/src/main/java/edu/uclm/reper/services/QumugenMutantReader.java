package edu.uclm.reper.services;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Lee la respuesta de qumugen mutante a mutante para no tener todos en memoria (antes se leia como List.class y se
 * copiaba, lo que producia OutOfMemoryError con ciclos grandes). Ademas, la respuesta se descarga entera a un fichero
 * antes de guardar en BD, para no dejar de leer el socket mientras se hacen los INSERT; si no, nginx/qumugen cortan la
 * conexion (Premature EOF).
 */
final class QumugenMutantReader {

    private QumugenMutantReader() {
    }

    static int forEachMutant(InputStream in, ObjectMapper mapper, Consumer<JsonNode> consumer) throws IOException {
        try (JsonParser p = mapper.getFactory().createParser(in)) {
            if (p.nextToken() != JsonToken.START_ARRAY) {
                throw new IOException("Respuesta de qumugen inesperada: se esperaba un array JSON");
            }
            int count = 0;
            while (p.nextToken() == JsonToken.START_OBJECT) {
                JsonNode node = mapper.readTree(p);
                consumer.accept(node);
                count++;
            }
            if (p.currentToken() != JsonToken.END_ARRAY) {
                throw new IOException("Respuesta de qumugen incompleta: el array JSON no termina");
            }
            return count;
        }
    }

    static long download(InputStream in, Path target) throws IOException {
        long start = System.nanoTime();
        long total = 0;
        try (OutputStream out = Files.newOutputStream(target)) {
            byte[] buffer = new byte[64 * 1024];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
                total += n;
            }
        } catch (IOException e) {
            long seconds = (System.nanoTime() - start) / 1_000_000_000L;
            throw new IOException("Descarga de qumugen cortada tras " + (total / (1024 * 1024)) + " MB y " + seconds
                    + " s: " + e.getMessage(), e);
        }
        return total;
    }

    static InputStream open(Path file, boolean gzip) throws IOException {
        InputStream in = new BufferedInputStream(Files.newInputStream(file), 64 * 1024);
        return gzip ? new GZIPInputStream(in, 64 * 1024) : in;
    }

    static JsonNode circuitOf(JsonNode mutant) {
        if (mutant.get("quirk") != null && mutant.get("quirk").isObject()) {
            return mutant.get("quirk");
        }
        if (mutant.get("qCircuit") != null && mutant.get("qCircuit").isObject()) {
            return mutant.get("qCircuit");
        }
        if (mutant.get("circuit") != null && mutant.get("circuit").isObject()) {
            return mutant.get("circuit");
        }
        if (mutant.has("cols")) {
            return mutant;
        }
        return null;
    }
}
