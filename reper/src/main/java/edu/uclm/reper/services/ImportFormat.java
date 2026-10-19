package edu.uclm.reper.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public enum ImportFormat {
    QASM("qasm", "OpenQASM 2.0", List.of(".qasm"), true),
    QUIL("quil", "Quil (Rigetti)", List.of(".quil"), true),
    QOBJ("qobj", "Qobj (IBM, JSON)", List.of(".json"), true),
    IONQ("ionq", "IonQ (JSON)", List.of(".json"), false),
    QUANTUM_CIRCUIT("quantum-circuit", "Quantum Programming Studio (JSON)", List.of(".json"), true),
    QUBIT_TOASTER("qubit-toaster", "Qubit Toaster (JSON)", List.of(".json"), true),
    QUIRK("quirk", "Quirk (JSON)", List.of(".json"), true),
    // Scripts Python: q-convert-api los ejecuta y los pasa a QASM (qasm2.dumps / cirq.qasm)
    QISKIT("qiskit", "Qiskit (Python)", List.of(".py"), true),
    CIRQ("cirq", "Cirq (Python)", List.of(".py"), true);

    private final String id;
    private final String label;
    private final List<String> extensions;
    private final boolean representable;

    ImportFormat(String id, String label, List<String> extensions, boolean representable) {
        this.id = id;
        this.label = label;
        this.extensions = extensions;
        this.representable = representable;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public boolean isRepresentable() {
        return representable;
    }

    public static Optional<ImportFormat> fromId(String id) {
        if (id == null) {
            return Optional.empty();
        }
        String trimmed = id.trim();
        for (ImportFormat format : values()) {
            if (format.id.equalsIgnoreCase(trimmed)) {
                return Optional.of(format);
            }
        }
        return Optional.empty();
    }

    public boolean acceptsExtension(String ext) {
        if (ext == null) {
            return false;
        }
        return extensions.contains(ext.toLowerCase().trim());
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("label", label);
        map.put("extensions", extensions);
        map.put("representable", representable);
        return map;
    }
}
