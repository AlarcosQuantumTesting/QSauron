package edu.uclm.reper.services;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class QuirkCircuitUtils {

    private static final Pattern INNER_BRACKETS_PATTERN = Pattern.compile("\\{[^{}]*\\}");

    private QuirkCircuitUtils() {}

    public static int countQubits(JsonNode quirk) {
        if (quirk == null || !quirk.has("cols") || !quirk.get("cols").isArray()) {
            return 0;
        }

        JsonNode cols = quirk.get("cols");
        if (cols.isEmpty()) {
            return 0;
        }

        Map<String, JsonNode> customGatesMap = new HashMap<>();
        if (quirk.has("gates") && quirk.get("gates").isArray()) {
            for (JsonNode gate : quirk.get("gates")) {
                if (gate != null && gate.has("id") && gate.get("id").isTextual()) {
                    customGatesMap.put(gate.get("id").asText(), gate);
                }
            }
        }

        Map<String, Integer> calculatedHeights = new HashMap<>();
        Set<String> calculating = new HashSet<>();

        int maxQubits = 0;
        for (JsonNode col : cols) {
            if (col != null && col.isArray()) {
                for (int i = 0; i < col.size(); i++) {
                    JsonNode cell = col.get(i);
                    if (isCellEmpty(cell)) {
                        continue;
                    }
                    int h = getGateHeight(cell, customGatesMap, calculatedHeights, calculating);
                    if (i + h > maxQubits) {
                        maxQubits = i + h;
                    }
                }
            }
        }

        if (quirk.has("qubits") && quirk.get("qubits").isInt()) {
            int explicitQubits = quirk.get("qubits").asInt();
            if (explicitQubits > maxQubits) {
                maxQubits = explicitQubits;
            }
        }

        return maxQubits;
    }

    private static boolean isCellEmpty(JsonNode cell) {
        if (cell == null || cell.isNull()) {
            return true;
        }
        if (cell.isInt() && cell.asInt() == 1) {
            return true;
        }
        return cell.isTextual() && "1".equals(cell.asText());
    }

    private static int getGateHeight(JsonNode cell, Map<String, JsonNode> customGatesMap,
                                     Map<String, Integer> calculatedHeights, Set<String> calculating) {
        if (cell == null || cell.isNull()) {
            return 1;
        }
        String id = null;
        if (cell.isTextual()) {
            id = cell.asText();
        } else if (cell.isObject() && cell.has("id") && cell.get("id").isTextual()) {
            id = cell.get("id").asText();
        }

        if (id != null && id.startsWith("~") && customGatesMap.containsKey(id)) {
            return getCustomGateHeight(customGatesMap.get(id), customGatesMap, calculatedHeights, calculating);
        }
        return 1;
    }

    private static int getCustomGateHeight(JsonNode customGate, Map<String, JsonNode> customGatesMap,
                                           Map<String, Integer> calculatedHeights, Set<String> calculating) {
        String id = customGate.has("id") ? customGate.get("id").asText() : "";
        if (calculatedHeights.containsKey(id)) {
            return calculatedHeights.get(id);
        }
        if (calculating.contains(id)) {
            return 1;
        }
        calculating.add(id);

        int height = 1;
        if (customGate.has("matrix")) {
            JsonNode matrix = customGate.get("matrix");
            int numRows = 0;
            if (matrix.isArray()) {
                numRows = matrix.size();
            } else if (matrix.isTextual()) {
                String mStr = matrix.asText();
                Matcher matcher = INNER_BRACKETS_PATTERN.matcher(mStr);
                while (matcher.find()) {
                    numRows++;
                }
            }
            if (numRows > 0) {
                height = (int) Math.round(Math.log(numRows) / Math.log(2));
            }
        } else if (customGate.has("circuit")) {
            height = getCircuitQubits(customGate.get("circuit"), customGatesMap, calculatedHeights, calculating);
        }

        if (height < 1) {
            height = 1;
        }

        calculating.remove(id);
        calculatedHeights.put(id, height);
        return height;
    }

    private static int getCircuitQubits(JsonNode circuit, Map<String, JsonNode> customGatesMap,
                                        Map<String, Integer> calculatedHeights, Set<String> calculating) {
        if (circuit == null || !circuit.has("cols") || !circuit.get("cols").isArray()) {
            return 1;
        }
        int maxQ = 1;
        JsonNode cols = circuit.get("cols");
        for (JsonNode col : cols) {
            if (col != null && col.isArray()) {
                for (int i = 0; i < col.size(); i++) {
                    JsonNode cell = col.get(i);
                    if (isCellEmpty(cell)) {
                        continue;
                    }
                    int h = getGateHeight(cell, customGatesMap, calculatedHeights, calculating);
                    if (i + h > maxQ) {
                        maxQ = i + h;
                    }
                }
            }
        }
        return maxQ;
    }

    public static int countColumns(JsonNode quirk) {
        if (quirk == null || !quirk.has("cols") || !quirk.get("cols").isArray()) {
            return 0;
        }
        return quirk.get("cols").size();
    }

    public static String rangeCsv(int n) {
        if (n <= 0) {
            return "";
        }
        return IntStream.range(0, n)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
