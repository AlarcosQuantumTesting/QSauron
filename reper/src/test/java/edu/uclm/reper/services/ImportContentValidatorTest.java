package edu.uclm.reper.services;

import edu.uclm.reper.dto.ImportItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ImportContentValidatorTest {

    private ImportContentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ImportContentValidator(2097152);
    }

    @Test
    void testAcceptedQasm() {
        ImportItemDTO item = new ImportItemDTO("bell.qasm", "qasm", "bell",
                "OPENQASM 2.0;\ninclude \"qelib1.inc\";\nqreg q[2];\nh q[0];\ncx q[0],q[1];");
        var res = validator.validate(item);
        assertEquals("bell.qasm", res.fileName());
        assertEquals(ImportFormat.QASM, res.format());
        assertNotNull(res.code());
    }

    @Test
    void testAcceptedQuil() {
        ImportItemDTO item = new ImportItemDTO("ghz.quil", "quil", "ghz", "H 0\nCNOT 0 1");
        var res = validator.validate(item);
        assertEquals("ghz.quil", res.fileName());
        assertEquals(ImportFormat.QUIL, res.format());
    }

    @Test
    void testAcceptedQuirk() {
        ImportItemDTO item = new ImportItemDTO("b.json", "quirk", "b", "{\"cols\":[[\"H\"],[\"•\",\"X\"]]}");
        var res = validator.validate(item);
        assertEquals(ImportFormat.QUIRK, res.format());
    }

    @Test
    void testAcceptedQobj() {
        ImportItemDTO item = new ImportItemDTO("q.json", "qobj", "q", "{\"experiments\":[]}");
        var res = validator.validate(item);
        assertEquals(ImportFormat.QOBJ, res.format());
    }

    @Test
    void testAcceptedIonq() {
        ImportItemDTO item = new ImportItemDTO("i.json", "ionq", "i", "{\"circuit\":[]}");
        var res = validator.validate(item);
        assertEquals(ImportFormat.IONQ, res.format());
    }

    @Test
    void testAcceptedQuantumCircuit() {
        ImportItemDTO item = new ImportItemDTO("c.json", "quantum-circuit", "c", "{\"gates\":[]}");
        var res = validator.validate(item);
        assertEquals(ImportFormat.QUANTUM_CIRCUIT, res.format());
    }

    @Test
    void testAcceptedQubitToaster() {
        ImportItemDTO item = new ImportItemDTO("t.json", "qubit-toaster", "t", "{}");
        var res = validator.validate(item);
        assertEquals(ImportFormat.QUBIT_TOASTER, res.format());
    }

    @Test
    void testAcceptedPastedCode() {
        ImportItemDTO item = new ImportItemDTO(null, "quirk", "x", "{\"cols\":[]}");
        var res = validator.validate(item);
        assertNull(res.fileName());
        assertEquals(ImportFormat.QUIRK, res.format());
    }

    @Test
    void testAcceptedQiskit() {
        ImportItemDTO item = new ImportItemDTO("bell.py", "qiskit", "bell",
                "from qiskit import QuantumCircuit\nqc = QuantumCircuit(2, 2)\nqc.h(0)\nqc.cx(0, 1)\n");
        var res = validator.validate(item);
        assertEquals(ImportFormat.QISKIT, res.format());
        assertEquals("bell.py", res.fileName());
    }

    @Test
    void testAcceptedCirq() {
        ImportItemDTO item = new ImportItemDTO("bell.py", "cirq", "bell",
                "import cirq\nq = cirq.LineQubit.range(2)\ncircuit = cirq.Circuit(cirq.H(q[0]), cirq.CNOT(*q))\n");
        var res = validator.validate(item);
        assertEquals(ImportFormat.CIRQ, res.format());
    }

    @Test
    void testAcceptedPastedQiskit() {
        ImportItemDTO item = new ImportItemDTO(null, "qiskit", "pegado",
                "# comentario\nimport qiskit\nqc = qiskit.QuantumCircuit(1)\n");
        assertEquals(ImportFormat.QISKIT, validator.validate(item).format());
    }

    @Test
    void testRejectedPythonWithoutLibraryImport() {
        ImportItemDTO qiskit = new ImportItemDTO("x.py", "qiskit", "x", "import cirq\ncircuit = cirq.Circuit()\n");
        ImportException ex = assertThrows(ImportException.class, () -> validator.validate(qiskit));
        assertEquals(ImportException.INVALID_CONTENT, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("qiskit"));

        ImportItemDTO cirq = new ImportItemDTO("x.py", "cirq", "x", "from qiskit import QuantumCircuit\n");
        assertThrows(ImportException.class, () -> validator.validate(cirq));
    }

    @Test
    void testRejectedPythonWrongExtension() {
        ImportItemDTO item = new ImportItemDTO("bell.json", "qiskit", "bell", "from qiskit import QuantumCircuit\n");
        ImportException ex = assertThrows(ImportException.class, () -> validator.validate(item));
        assertEquals(ImportException.INVALID_EXTENSION, ex.getErrorCode());
    }

    @Test
    void testPythonFormatsExposed() {
        assertEquals(ImportFormat.QISKIT, ImportFormat.fromId("qiskit").orElseThrow());
        assertEquals(ImportFormat.CIRQ, ImportFormat.fromId("CIRQ").orElseThrow());
        assertTrue(ImportFormat.QISKIT.isRepresentable());
        assertTrue(ImportFormat.CIRQ.acceptsExtension(".py"));
    }

    @Test
    void testRejectedInvalidFormat() {
        ImportItemDTO item = new ImportItemDTO("bell.qasm", "braket", "bell", "x");
        ImportException ex = assertThrows(ImportException.class, () -> validator.validate(item));
        assertEquals(ImportException.INVALID_FORMAT, ex.getErrorCode());
    }

    @Test
    void testRejectedInvalidExtensionQasm() {
        ImportItemDTO item = new ImportItemDTO("bell.json", "qasm", "bell", "OPENQASM 2.0;");
        ImportException ex = assertThrows(ImportException.class, () -> validator.validate(item));
        assertEquals(ImportException.INVALID_EXTENSION, ex.getErrorCode());
    }

    @Test
    void testRejectedInvalidExtensionQuirk() {
        ImportItemDTO item = new ImportItemDTO("x.py", "quirk", "x", "{\"cols\":[]}");
        ImportException ex = assertThrows(ImportException.class, () -> validator.validate(item));
        assertEquals(ImportException.INVALID_EXTENSION, ex.getErrorCode());
    }

    @Test
    void testRejectedMissingNamePastedCode() {
        ImportItemDTO item = new ImportItemDTO(null, "quirk", "", "{\"cols\":[]}");
        ImportException ex = assertThrows(ImportException.class, () -> validator.validate(item));
        assertEquals(ImportException.MISSING_NAME, ex.getErrorCode());
    }

    @Test
    void testRejectedEmptyCode() {
        ImportItemDTO item = new ImportItemDTO("bell.qasm", "qasm", "bell", "   ");
        ImportException ex = assertThrows(ImportException.class, () -> validator.validate(item));
        assertEquals(ImportException.EMPTY_FILE, ex.getErrorCode());
    }

    @Test
    void testRejectedFileTooLarge() {
        String large = "a".repeat(2097152 + 1);
        ImportItemDTO item = new ImportItemDTO("large.quil", "quil", "large", large);
        ImportException ex = assertThrows(ImportException.class, () -> validator.validate(item));
        assertEquals(ImportException.FILE_TOO_LARGE, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("2 MB"));
    }

    @Test
    void testAcceptedValidLargeQasm() {
        String header = "OPENQASM 2.0;\ninclude \"qelib1.inc\";\nqreg q[2];\n";
        String gate = "h q[0];\n";
        int repeatCount = 240000; // ~1.92 MB
        String code = header + gate.repeat(repeatCount);
        ImportItemDTO item = new ImportItemDTO("large.qasm", "qasm", "large", code);
        var res = validator.validate(item);
        assertEquals("large.qasm", res.fileName());
        assertEquals(ImportFormat.QASM, res.format());
    }

    @Test
    void testRejectedNotTextNullChar() {
        ImportItemDTO item = new ImportItemDTO("bell.qasm", "qasm", "bell", "OPENQASM 2.0;\u0000");
        ImportException ex = assertThrows(ImportException.class, () -> validator.validate(item));
        assertEquals(ImportException.NOT_TEXT, ex.getErrorCode());
    }

    @Test
    void testRejectedNotTextReplacementChar() {
        ImportItemDTO item = new ImportItemDTO("bell.qasm", "qasm", "bell", "OPENQASM 2.0;\uFFFD");
        ImportException ex = assertThrows(ImportException.class, () -> validator.validate(item));
        assertEquals(ImportException.NOT_TEXT, ex.getErrorCode());
    }

    @Test
    void testRejectedOpenQasm3() {
        ImportItemDTO item = new ImportItemDTO("bell.qasm", "qasm", "bell", "OPENQASM 3;");
        ImportException ex = assertThrows(ImportException.class, () -> validator.validate(item));
        assertEquals(ImportException.INVALID_CONTENT, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Only OpenQASM 2.0 is supported"));
    }

    @Test
    void testRejectedQasmWithoutHeader() {
        ImportItemDTO item = new ImportItemDTO("bell.qasm", "qasm", "bell", "qreg q[2];");
        ImportException ex = assertThrows(ImportException.class, () -> validator.validate(item));
        assertEquals(ImportException.INVALID_CONTENT, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Missing 'OPENQASM 2.0;' header"));
    }

    @Test
    void testRejectedJsonExcessiveNesting() {
        StringBuilder sb = new StringBuilder();
        sb.append("[".repeat(100));
        sb.append("]".repeat(100));
        ImportItemDTO item = new ImportItemDTO("deep.json", "quirk", "deep", sb.toString());
        ImportException ex = assertThrows(ImportException.class, () -> validator.validate(item));
        assertEquals(ImportException.INVALID_CONTENT, ex.getErrorCode());
    }

    @Test
    void testRejectedQuirkMissingCols() {
        ImportItemDTO item = new ImportItemDTO("bad.json", "quirk", "bad", "{\"a\":1}");
        ImportException ex = assertThrows(ImportException.class, () -> validator.validate(item));
        assertEquals(ImportException.INVALID_CONTENT, ex.getErrorCode());
    }

    @Test
    void testPathTraversalSanitization() {
        ImportItemDTO item = new ImportItemDTO("../../etc/bell.qasm", "qasm", "bell", "OPENQASM 2.0;");
        var res = validator.validate(item);
        assertEquals("bell.qasm", res.fileName());
    }

    @Test
    void testBomStripping() {
        ImportItemDTO item = new ImportItemDTO("bell.qasm", "qasm", "bell", "\uFEFFOPENQASM 2.0;\nqreg q[1];");
        var res = validator.validate(item);
        assertFalse(res.code().startsWith("\uFEFF"));
        assertTrue(res.code().startsWith("OPENQASM 2.0;"));
    }
}
