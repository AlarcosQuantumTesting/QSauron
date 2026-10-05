package edu.uclm.reper.services;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProjectNameSanitizerTest {

    @Test
    void testFromItemBellQasm() {
        String name = ProjectNameSanitizer.fromItem("bell.qasm", null);
        assertEquals("bell", name);
    }

    @Test
    void testFromItemDangerousCharacters() {
        String name = ProjectNameSanitizer.fromItem("<img src=x>.qasm", null);
        assertFalse(name.contains("<"));
        assertFalse(name.contains(">"));
    }

    @Test
    void testFromItemRequestedName() {
        String name = ProjectNameSanitizer.fromItem(null, "  Mi   proyecto ");
        assertEquals("Mi proyecto", name);
    }

    @Test
    void testFromItemOnlyDots() {
        String name = ProjectNameSanitizer.fromItem("....json", null);
        assertEquals("Imported project", name);
    }

    @Test
    void testMakeUnique() {
        Set<String> taken = new HashSet<>();
        taken.add("bell");

        String unique1 = ProjectNameSanitizer.makeUnique("bell", taken);
        assertEquals("bell (2)", unique1);

        String unique2 = ProjectNameSanitizer.makeUnique("bell", taken);
        assertEquals("bell (3)", unique2);
    }
}
