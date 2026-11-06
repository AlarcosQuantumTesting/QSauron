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
        validator = new ImportContentValidator(2097152, TestCatalog.create());
    }

    @Test
    void testAcceptedQasm() {
        ImportItemDTO item = new ImportItemDTO("bell.qasm", "qasm", "bell",
                "OPENQASM 2.0;\ninclude \"qelib1.inc\";\nqreg q[2];\nh q[0];\ncx q[0],q[1];");
        var res = validator.validate(item);
        assertEquals("bell.qasm", res.fileName());
        assertEquals("qasm", res.format().id());
        assertNotNull(res.code());
    }

    @Test
    void testAcceptedQuil() {
        ImportItemDTO item = new ImportItemDTO("ghz.quil", "quil", "ghz", "H 0\nCNOT 0 1");
        var res = validator.validate(item);
        assertEquals("ghz.quil", res.fileName());
        assertEquals("quil", res.format().id());
    }

    @Test
    void testAcceptedQuirk() {
        ImportItemDTO item = new ImportItemDTO("b.json", "quirk", "b", "{\"cols\":[[\"H\"],[\"•\",\"X\"]]}");
        var res = validator.validate(item);
        assertEquals("quirk", res.format().id());
    }

    @Test
    void testAcceptedQobj() {
        ImportItemDTO item = new ImportItemDTO("q.json", "qobj", "q", "{\"experiments\":[]}");
        var res = validator.validate(item);
        assertEquals("qobj", res.format().id());
    }

    @Test
    void testAcceptedIonq() {
        ImportItemDTO item = new ImportItemDTO("i.json", "ionq", "i", "{\"circuit\":[]}");
        var res = validator.validate(item);
        assertEquals("ionq", res.format().id());
    }

    @Test
    void testAcceptedQuantumCircuit() {
        ImportItemDTO item = new ImportItemDTO("c.json", "quantum-circuit", "c", "{\"gates\":[]}");
        var res = validator.validate(item);
        assertEquals("quantum-circuit", res.format().id());
    }

    @Test
    void testAcceptedQubitToaster() {
        ImportItemDTO item = new ImportItemDTO("t.json", "qubit-toaster", "t", "{}");
        var res = validator.validate(item);
        assertEquals("qubit-toaster", res.format().id());
    }

    @Test
    void testAcceptedPastedCode() {
        ImportItemDTO item = new ImportItemDTO(null, "quirk", "x", "{\"cols\":[]}");
        var res = validator.validate(item);
        assertNull(res.fileName());
        assertEquals("quirk", res.format().id());
    }

    @Test
    void testAcceptedQiskit() {
        ImportItemDTO item = new ImportItemDTO("bell.py", "qiskit", "bell",
                "from qiskit import QuantumCircuit\nqc = QuantumCircuit(2, 2)\nqc.h(0)\nqc.cx(0, 1)\n");
        var res = validator.validate(item);
        assertEquals("qiskit", res.format().id());
        assertEquals("bell.py", res.fileName());
    }

    @Test
    void testAcceptedCirq() {
        ImportItemDTO item = new ImportItemDTO("bell.py", "cirq", "bell",
                "import cirq\nq = cirq.LineQubit.range(2)\ncircuit = cirq.Circuit(cirq.H(q[0]), cirq.CNOT(*q))\n");
        var res = validator.validate(item);
        assertEquals("cirq", res.format().id());
    }

    @Test
    void testAcceptedPastedQiskit() {
        ImportItemDTO item = new ImportItemDTO(null, "qiskit", "pegado",
                "# comentario\nimport qiskit\nqc = qiskit.QuantumCircuit(1)\n");
        assertEquals("qiskit", validator.validate(item).format().id());
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
        var catalog = TestCatalog.create();
        assertEquals("qiskit", catalog.find("qiskit").orElseThrow().id());
        assertEquals("cirq", catalog.find("CIRQ").orElseThrow().id());
        assertTrue(catalog.find("qiskit").orElseThrow().representable());
        assertTrue(catalog.find("cirq").orElseThrow().acceptsExtension(".py"));
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
        assertEquals("qasm", res.format().id());
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
    void testQasmAcceptsOpenQasm3() {
        ImportItemDTO item = new ImportItemDTO("g.qasm", "qasm", "g", "// MQT\nOPENQASM 3.0;\ninclude \"stdgates.inc\";\nqubit[2] q;");
        assertEquals("qasm", validator.validate(item).format().id());
    }

    @Test
    void testQasm3AcceptsAllThreeXHeaders() {
        for (String header : new String[] {"OPENQASM 3.0;", "OPENQASM 3;", "OPENQASM 3.1;"}) {
            ImportItemDTO item = new ImportItemDTO("g.qasm", "qasm3", "g", header + "\nqubit q;");
            assertEquals("qasm3", validator.validate(item).format().id(), header);
        }
    }

    @Test
    void testQasm3RejectsOpenQasm2() {
        ImportItemDTO item = new ImportItemDTO("g.qasm", "qasm3", "g", "OPENQASM 2.0;\nqreg q[1];");
        ImportException ex = assertThrows(ImportException.class, () -> validator.validate(item));
        assertEquals(ImportException.INVALID_CONTENT, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Missing 'OPENQASM 3.0;' header"));
    }

    @Test
    void testQasm3AcceptsQasmExtensionAndRejectsOthers() {
        ImportItemDTO bad = new ImportItemDTO("g.json", "qasm3", "g", "OPENQASM 3.0;");
        ImportException ex = assertThrows(ImportException.class, () -> validator.validate(bad));
        assertEquals(ImportException.INVALID_EXTENSION, ex.getErrorCode());
    }

    @Test
    void testUnknownLanguage() {
        ImportItemDTO item = new ImportItemDTO("g.qasm", "nope", "g", "x");
        ImportException ex = assertThrows(ImportException.class, () -> validator.validate(item));
        assertEquals(ImportException.INVALID_FORMAT, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Unknown language"));
    }

    @Test
    void testRejectedQasmWithoutHeader() {
        ImportItemDTO item = new ImportItemDTO("bell.qasm", "qasm", "bell", "qreg q[2];");
        ImportException ex = assertThrows(ImportException.class, () -> validator.validate(item));
        assertEquals(ImportException.INVALID_CONTENT, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Missing 'OPENQASM 2.0;' or 'OPENQASM 3.0;' header"));
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
